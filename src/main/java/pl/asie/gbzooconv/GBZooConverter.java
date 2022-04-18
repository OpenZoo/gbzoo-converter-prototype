package pl.asie.gbzooconv;

import pl.asie.libzzt.Board;
import pl.asie.libzzt.Element;
import pl.asie.libzzt.Platform;
import pl.asie.libzzt.Stat;
import pl.asie.libzzt.World;
import pl.asie.libzzt.ZOutputStream;
import pl.asie.libzzt.oop.OopParseException;
import pl.asie.libzzt.oop.OopProgram;
import pl.asie.libzzt.oop.commands.OopCommand;
import pl.asie.libzzt.oop.commands.OopCommandLabel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GBZooConverter {
	private final BankPacker bankPacker;
	private final int worldCount;
	private Map<Integer, List<byte[]>> bankData;
	private byte[] worldPointers;
	private byte[] boardPointers;
	private int worldOffset;

	public GBZooConverter(int startingBank, int maxBanks, int worldCount) {
		this.bankPacker = new BankPacker(startingBank, maxBanks);
		this.worldCount = worldCount;
		this.worldPointers = new byte[getPointerArraySize(worldCount)];
		this.worldOffset = 0;
		this.bankData = new HashMap<>();
		this.bankPacker.addToBank(startingBank, this.worldPointers);
	}

	private int getPointerArraySize(int count) {
		return count * 3;
	}

	public void addWorld(World world) throws IOException {
		GBZooOopWorldState oopWorldState = new GBZooOopWorldState(world, this.bankPacker);

		byte[] worldHeader = writeWorld(oopWorldState, world);
		this.boardPointers = new byte[getPointerArraySize(world.getBoards().size())];

		this.bankPacker.add(List.of(worldHeader, this.boardPointers));
		this.bankPacker.updatePointer(this.worldPointers, this.worldOffset, worldHeader, true);

		this.worldOffset += 3;

		// write boards
		for (int i = 0; i < world.getBoards().size(); i++) {
			Board board = world.getBoards().get(i);
			byte[] data = addBoard(board, oopWorldState);
			this.bankPacker.updatePointer(this.boardPointers, i * 3, data, true);
		}

		System.out.println("Max data offset size: " + oopWorldState.getMaxDataOffsetSize());
		System.out.println("Max lines in program: " + oopWorldState.getMaxLinesInProgram());
	}

	public void write(OutputStream stream) throws IOException {
		this.bankPacker.pack();
		this.bankPacker.write(stream);
	}

	private int fixCentipedeStatId(int v) {
		if (v > Platform.ZZT.getActualMaxStatCount()) {
			return -1;
		} else if (v < -2) {
			return -2;
		} else {
			return v;
		}
	}

	private byte[] writeWorld(GBZooOopWorldState worldState, World world) throws IOException {
		try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream(); ZOutputStream stream = new ZOutputStream(byteStream, Platform.ZZT)) {
			stream.writePShort(world.getAmmo());
			stream.writePShort(world.getGems());
			int keys = 0;
			for (int i = 0; i < 7; i++) {
				if (world.getKeys()[i]) {
					keys |= 1 << (i + 1);
				}
			}
			stream.writePByte(keys);
			stream.writePShort(world.getHealth());
			stream.writePShort(world.getTorches());
			stream.writePShort(world.getScore());
			stream.writePByte(world.getCurrentBoard());
			stream.writePByte(world.getTorchTicks());
			stream.writePByte(world.getEnergizerTicks());
			for (int i = 0; i < 10; i++) {
				if (i >= world.getFlags().size()) {
					stream.writePByte(255);
				} else {
					String flag = world.getFlags().get(i);
					int flagIdx = worldState.getFlags().indexOf(flag);
					if (flagIdx == -1) {
						throw new RuntimeException("Flag " + flag + " not found in " + worldState.getFlags());
					}
					stream.writePByte(flagIdx);
				}
			}
			stream.writePShort(world.getBoardTimeSec());
			stream.writePShort(world.getBoardTimeHsec());
			stream.writePByte(world.getBoards().size() - 1);
			return byteStream.toByteArray();
		}
	}

	private byte[] addBoard(Board board, GBZooOopWorldState oopWorldState) throws IOException {
		System.out.println("Saving board " + board.getName());

		try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream(); ZOutputStream stream = new ZOutputStream(byteStream, Platform.ZZT)) {
			// fancy RLE logic
			int ix = 1;
			int iy = 1;
			int rleCount = 1;
			Element rleElement = board.getElement(ix, iy);
			int rleColor = board.getColor(ix, iy);
			do {
				ix++;
				if (ix > board.getWidth()) {
					ix = 1;
					iy++;
				}

				if (board.getColor(ix, iy) == rleColor && board.getElement(ix, iy) == rleElement && rleCount < 255 && iy <= board.getHeight()) {
					rleCount++;
				} else {
					if (rleCount == 1) {
						stream.writePByte(rleElement.getId() | 0x80);
						stream.writePByte(rleColor);
					} else {
						stream.writePByte(rleElement.getId());
						stream.writePByte(rleColor);
						stream.writePByte(rleCount);
					}
					rleElement = board.getElement(ix, iy);
					rleColor = board.getColor(ix, iy);
					rleCount = 1;
				}
			} while (iy <= board.getHeight());

			// zoo_board_info_t
			stream.writePByte(board.getMaxShots());
			stream.writePByte((board.isDark() ? 1 : 0) | (board.isReenterWhenZapped() ? 2 : 0));
			for (int i = 0; i < 4; i++)
				stream.writePByte(board.getNeighborBoards()[i]);
			stream.writePByte(board.getStartPlayerX());
			stream.writePByte(board.getStartPlayerY());
			stream.writePShort(board.getTimeLimitSec());

			stream.writePByte(board.getStats().size() - 1);

			GBZooOopBoardConverter oopConverter = new GBZooOopBoardConverter(oopWorldState, this.bankPacker, false);

			for (Stat stat : board.getStats()) {
				stat.copyStatToStatId(board);
				try {
					OopProgram program = stat.getCode();
					if (program != null) {
						oopConverter.addProgram(program);
					}
				} catch (OopParseException e) {
					e.printStackTrace();
				}
			}

			Map<OopProgram, byte[]> programMap = new HashMap<>();
			for (OopProgram program : oopConverter.getPrograms()) {
				byte[] data = oopConverter.serializeProgram(program);
				this.bankPacker.add(List.of(data));
				programMap.put(program, data);
			}

			System.out.println(oopConverter.getLabels() + " " + oopConverter.getNames());

			Map<Stat, Integer> statToDataOfs = new IdentityHashMap<>();
			List<Integer> dataOffsets = new ArrayList<>();
			List<BankPacker.PointerUpdateRequest> dataPtrRequests = new ArrayList<>();

			for (Stat stat : board.getStats()) {
				stream.writePByte(stat.getX());
				stream.writePByte(stat.getY());
				stream.writePByte(stat.getStepX());
				stream.writePByte(stat.getStepY());
				stream.writePByte(stat.getCycle());
				stream.writePByte(stat.getP1());
				stream.writePByte(stat.getP2());
				stream.writePByte(stat.getP3());
				stream.writePByte(fixCentipedeStatId(stat.getFollower()));
				stream.writePByte(fixCentipedeStatId(stat.getLeader()));
				stream.writePByte(stat.getUnderElement() != null ? stat.getUnderElement().getId() : 0);
				stream.writePByte(stat.getUnderColor());
				int dataOfs = 0xFFFF;
				boolean emitNewDataOfs = false;
				if (stat.getCode() != null) {
					if (stat.getBoundStat() != null) {
						Stat boundStat = stat.getBoundStat();
						while (boundStat.getBoundStat() != null) {
							boundStat = boundStat.getBoundStat();
						}
						dataOfs = statToDataOfs.get(boundStat);
					} else {
						dataOfs = dataOffsets.size();
						emitNewDataOfs = true;
					}
				}
				stream.writePShort(dataOfs);
				int dataPos;
				if (stat.getDataPos() < 0) {
					dataPos = -1;
				} else if (stat.getDataPos() == 0) {
					dataPos = 0;
				} else if (stat.getCode() != null) {
					Integer newDataPos = oopConverter.convertProgramPositionToSerialized(stat.getCode(), stat.getDataPos());
					if (newDataPos != null) {
						dataPos = newDataPos;
					} else {
						throw new RuntimeException("Unsupported data position: " + stat.getDataPos() + " (stat index " + board.getStatId(stat) + ", positions: " + oopConverter.getProgramPositions(stat.getCode()) + ")");
					}
				} else {
					dataPos = stat.getDataPos(); // ditto
				}
				stream.writePShort(dataPos);

				if (emitNewDataOfs) {
					dataPtrRequests.add(new BankPacker.PointerUpdateRequest(true, programMap.get(stat.getCode()), null, dataOfs));
					int dataOffsetsPos = dataOffsets.size();
					dataOffsets.add(0); // ptr request
					dataOffsets.add(0);
					dataOffsets.add(0);
					dataOffsets.add(0); // obj flags/size
					// zapped flags
					int flagVals = 0;
					int i = 0;
					for (OopCommand command : stat.getCode().getCommands()) {
						if (command instanceof OopCommandLabel lbl) {
							if (lbl.isZapped()) {
								flagVals |= 1 << (i & 7);
							}
							i++;
							if ((i & 7) == 0) {
								dataOffsets.add(flagVals);
								flagVals = 0;
							}
						}
					}
					if ((i & 7) != 0) {
						dataOffsets.add(flagVals);
					}
					int dataOffsetsSize = dataOffsets.size() - dataOffsetsPos;
					if (dataOffsetsSize >= 128) {
						throw new RuntimeException("Too big data offset size: " + dataOffsetsSize);
					}
					dataOffsets.set(dataOffsetsPos + 3, dataOffsetsSize);
					statToDataOfs.put(stat, dataOffsetsPos);
				}
			}

			oopWorldState.setMaxDataOffsetSize(dataOffsets.size());
			stream.writePShort(dataOffsets.size());
			int dataOffsetsPos = byteStream.size();
			for (Integer dataOfsByte : dataOffsets) {
				stream.writePByte(dataOfsByte);
			}

			byte[] arr = byteStream.toByteArray();
			this.bankPacker.add(List.of(arr));
			for (BankPacker.PointerUpdateRequest request : dataPtrRequests) {
				if (request.getPtrArray() == null) {
					request = request.withPtrArray(arr);
				}
				request = request.withPosition(request.getPosition() + dataOffsetsPos);
				this.bankPacker.updatePointer(request);
			}

			return arr;
		}
	}
}

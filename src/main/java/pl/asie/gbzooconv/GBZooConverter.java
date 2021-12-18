package pl.asie.gbzooconv;

import pl.asie.libzzt.Board;
import pl.asie.libzzt.Element;
import pl.asie.libzzt.Platform;
import pl.asie.libzzt.Stat;
import pl.asie.libzzt.World;
import pl.asie.libzzt.ZOutputStream;
import pl.asie.libzzt.oop.OopParseException;
import pl.asie.libzzt.oop.OopProgram;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
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
		GBZooOopWorldState oopWorldState = new GBZooOopWorldState(world);

		byte[] worldHeader = writeWorld(world);
		this.boardPointers = new byte[getPointerArraySize(world.getBoards().size())];

		this.bankPacker.add(List.of(worldHeader, this.boardPointers));
		this.bankPacker.updatePointer(this.worldPointers, this.worldOffset, worldHeader, true);

		this.worldOffset += 3;

		// write boards
		for (int i = 0; i < world.getBoards().size(); i++) {
			Board board = world.getBoards().get(i);
			byte[] data = writeBoard(board, oopWorldState);
			this.bankPacker.add(List.of(data));
			this.bankPacker.updatePointer(this.boardPointers, i * 3, data, true);
		}
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

	private byte[] writeWorld(World world) throws IOException {
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
			// TODO: Flags
			for (int i = 0; i < 10; i++) {
				stream.writePByte(255);
			}
			stream.writePShort(world.getBoardTimeSec());
			stream.writePShort(world.getBoardTimeHsec());
			stream.writePByte(world.getBoards().size() - 1);
			return byteStream.toByteArray();
		}
	}

	private byte[] writeBoard(Board board, GBZooOopWorldState oopWorldState) throws IOException {
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

			GBZooOopBoardConverter oopConverter = new GBZooOopBoardConverter(oopWorldState);

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

			for (Stat stat : board.getStats()) {
				// TODO: handle data
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
				int dataOfs = 0;
				stream.writePShort(dataOfs);
				int dataPos;
				if (stat.getDataPos() < 0) {
					dataPos = -1;
				} else if (stat.getDataPos() == 0) {
					dataPos = 0;
				} else {
					throw new RuntimeException("Unsupported data position: " + stat.getDataPos());
				}
				stream.writePShort(dataPos);
			}

			return byteStream.toByteArray();
		}
	}
}

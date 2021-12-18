package pl.asie.gbzooconv;

import lombok.Getter;
import pl.asie.libzzt.Board;
import pl.asie.libzzt.Stat;
import pl.asie.libzzt.World;
import pl.asie.libzzt.oop.OopUtils;
import pl.asie.libzzt.oop.commands.OopCommandTextLine;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class GBZooOopWorldState {
	private static final int MAX_FLAGS = 255;
	private final List<String> flags = new ArrayList<>();
	private final BankPacker packer;
	private final Map<OopCommandTextLine, byte[]> textLineMap = new HashMap<>();
	private int maxDataOffsetSize;
	private int maxLinesInProgram;

	public GBZooOopWorldState(World world, BankPacker packer) {
		for (Board b : world.getBoards()) {
			for (Stat stat : b.getStats()) {
				if (stat.getData() != null) {
					OopUtils.allChildren(stat.getCode().getCommands().stream())
							.flatMap(c -> c.getFlags().stream())
							.filter(f -> !flags.contains(f))
							.forEach(flags::add);
				}
			}
		}

		System.out.println(flags);
		if (flags.size() > MAX_FLAGS) {
			throw new RuntimeException("Too many flags: " + flags.size() + " > " + MAX_FLAGS);
		}

		this.packer = packer;
	}

	public byte[] addTextLine(OopCommandTextLine line, int labelId) {
		byte[] dataArray = textLineMap.get(line);
		if (dataArray != null) {
			return dataArray;
		}

		List<Integer> data = new ArrayList<>();
		switch (line.getType()) {
			case REGULAR -> data.add(0);
			case CENTERED -> data.add(1);
			case HYPERLINK -> { data.add(2); data.add(labelId); }
			default -> throw new RuntimeException("Unsupported type: " + line.getType());
		}
		byte[] lineText = line.getMessage().getBytes(StandardCharsets.ISO_8859_1);
		data.add(lineText.length);
		for (int i = 0; i < lineText.length; i++) {
			data.add((int) lineText[i] & 0xFF);
		}

		dataArray = GBZooUtils.toByteArray(data);
		if (line.getType() != OopCommandTextLine.Type.HYPERLINK) {
			textLineMap.put(line, dataArray);
		}
		this.packer.add(dataArray);
		return dataArray;
	}

	public void setMaxDataOffsetSize(int maxDataOffsetSize) {
		if (this.maxDataOffsetSize < maxDataOffsetSize) {
			this.maxDataOffsetSize = maxDataOffsetSize;
		}
	}

	public void setMaxLinesInProgram(int maxLinesInProgram) {
		if (this.maxLinesInProgram < maxLinesInProgram) {
			this.maxLinesInProgram = maxLinesInProgram;
		}
	}
}

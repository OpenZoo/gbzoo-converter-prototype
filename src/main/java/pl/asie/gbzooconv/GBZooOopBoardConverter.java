package pl.asie.gbzooconv;

import lombok.AccessLevel;
import lombok.Getter;
import pl.asie.libzzt.Platform;
import pl.asie.libzzt.oop.OopLabelTarget;
import pl.asie.libzzt.oop.OopProgram;
import pl.asie.libzzt.oop.OopSound;
import pl.asie.libzzt.oop.OopTile;
import pl.asie.libzzt.oop.OopUtils;
import pl.asie.libzzt.oop.commands.OopCommand;
import pl.asie.libzzt.oop.commands.OopCommandBecome;
import pl.asie.libzzt.oop.commands.OopCommandBind;
import pl.asie.libzzt.oop.commands.OopCommandChange;
import pl.asie.libzzt.oop.commands.OopCommandChar;
import pl.asie.libzzt.oop.commands.OopCommandClear;
import pl.asie.libzzt.oop.commands.OopCommandComment;
import pl.asie.libzzt.oop.commands.OopCommandCycle;
import pl.asie.libzzt.oop.commands.OopCommandDie;
import pl.asie.libzzt.oop.commands.OopCommandDirection;
import pl.asie.libzzt.oop.commands.OopCommandDirectionTry;
import pl.asie.libzzt.oop.commands.OopCommandEnd;
import pl.asie.libzzt.oop.commands.OopCommandEndgame;
import pl.asie.libzzt.oop.commands.OopCommandGive;
import pl.asie.libzzt.oop.commands.OopCommandGo;
import pl.asie.libzzt.oop.commands.OopCommandIdle;
import pl.asie.libzzt.oop.commands.OopCommandIf;
import pl.asie.libzzt.oop.commands.OopCommandLabel;
import pl.asie.libzzt.oop.commands.OopCommandLock;
import pl.asie.libzzt.oop.commands.OopCommandPlay;
import pl.asie.libzzt.oop.commands.OopCommandPut;
import pl.asie.libzzt.oop.commands.OopCommandRestart;
import pl.asie.libzzt.oop.commands.OopCommandRestore;
import pl.asie.libzzt.oop.commands.OopCommandSend;
import pl.asie.libzzt.oop.commands.OopCommandSet;
import pl.asie.libzzt.oop.commands.OopCommandShoot;
import pl.asie.libzzt.oop.commands.OopCommandTextLine;
import pl.asie.libzzt.oop.commands.OopCommandThrowstar;
import pl.asie.libzzt.oop.commands.OopCommandTry;
import pl.asie.libzzt.oop.commands.OopCommandUnlock;
import pl.asie.libzzt.oop.commands.OopCommandWalk;
import pl.asie.libzzt.oop.commands.OopCommandZap;
import pl.asie.libzzt.oop.conditions.OopCondition;
import pl.asie.libzzt.oop.conditions.OopConditionAlligned;
import pl.asie.libzzt.oop.conditions.OopConditionAny;
import pl.asie.libzzt.oop.conditions.OopConditionBlocked;
import pl.asie.libzzt.oop.conditions.OopConditionContact;
import pl.asie.libzzt.oop.conditions.OopConditionEnergized;
import pl.asie.libzzt.oop.conditions.OopConditionFlag;
import pl.asie.libzzt.oop.conditions.OopConditionNot;
import pl.asie.libzzt.oop.directions.OopDirection;
import pl.asie.libzzt.oop.directions.OopDirectionCcw;
import pl.asie.libzzt.oop.directions.OopDirectionCw;
import pl.asie.libzzt.oop.directions.OopDirectionEast;
import pl.asie.libzzt.oop.directions.OopDirectionFlow;
import pl.asie.libzzt.oop.directions.OopDirectionIdle;
import pl.asie.libzzt.oop.directions.OopDirectionNorth;
import pl.asie.libzzt.oop.directions.OopDirectionOpp;
import pl.asie.libzzt.oop.directions.OopDirectionRnd;
import pl.asie.libzzt.oop.directions.OopDirectionRndne;
import pl.asie.libzzt.oop.directions.OopDirectionRndns;
import pl.asie.libzzt.oop.directions.OopDirectionRndp;
import pl.asie.libzzt.oop.directions.OopDirectionSeek;
import pl.asie.libzzt.oop.directions.OopDirectionSouth;
import pl.asie.libzzt.oop.directions.OopDirectionWest;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class GBZooOopBoardConverter {
	private static final Map<String, Integer> SPECIAL_LABELS = Map.of(
			"RESTART", 255,
			"SHOT", 254,
			"ENERGIZE", 253,
			"THUD", 252,
			"TOUCH", 251,
			"BOMBED", 250
	);
	private static final Map<String, Integer> SPECIAL_NAMES = Map.of(
			"ALL", 254,
			"OTHERS", 253,
			"SELF", 252,
			"", 251
	);
	private static final int MAX_LABELS = 256 - SPECIAL_LABELS.size();
	private static final int MAX_NAMES = 255 - SPECIAL_NAMES.size();
	private static final int CODE_OFFSET = 5;

	private final GBZooOopWorldState worldState;
	private final Set<OopProgram> programs = new HashSet<>();
	private final List<String> labels = new ArrayList<>(); // ordering sensitive
	private final List<String> names = new ArrayList<>(); // ordering sensitive
	@Getter(AccessLevel.PRIVATE)
	private final Map<OopProgram, Map<Integer, Integer>> programPositionMap = new HashMap<>();
	private final BankPacker packer;
	private final boolean failFast;

	public GBZooOopBoardConverter(GBZooOopWorldState worldState, BankPacker packer, boolean failFast) {
		this.worldState = worldState;
		this.packer = packer;
		this.failFast = failFast;
	}

	public void warnOrError(String s) {
		if (failFast) {
			throw new RuntimeException(s);
		} else {
			System.err.println("WARNING: " + s);
		}
	}

	public Integer convertProgramPositionToSerialized(OopProgram program, int position) {
		if (position == -1) {
			return -1;
		} else if (position == 0) {
			return 0;
		} else if (programPositionMap.containsKey(program)) {
			Map<Integer, Integer> positionMap = programPositionMap.get(program);
			return positionMap.get(position);
		} else {
			return null;
		}
	}

	public Map<Integer, Integer> getProgramPositions(OopProgram program) {
		return programPositionMap.getOrDefault(program, Map.of());
	}

	private static <T> int indexOfOrThrow(List<T> list, T item) {
		int idx = list.indexOf(item);
		if (idx < 0) {
			throw new RuntimeException(item + " not found in " + list);
		}
		return idx;
	}

	private static <T> int indexOfOrThrow(List<T> list, Map<T, Integer> specialMap, T item) {
		//noinspection SuspiciousMethodCalls
		if (item == null || ((item instanceof String) && ((String) item).isEmpty() && !specialMap.containsKey(""))) {
			return 255;
		}
		if (specialMap.containsKey(item)) {
			return specialMap.get(item);
		}
		int idx = list.indexOf(item);
		if (idx < 0) {
			throw new RuntimeException(item + " not found in " + list);
		}
		return idx;
	}

	public void addProgram(OopProgram program) {
		if (!programs.add(program)) {
			return;
		}

		if (program.getName() != null && !program.getName().isEmpty() && !names.contains(program.getName())) {
			names.add(program.getName());
		}

		OopUtils.allChildren(program.getCommands().stream()).flatMap(c -> c.getLabels().stream()).map(OopUtils::stripChars)
				.filter(label -> !SPECIAL_LABELS.containsKey(label) && !labels.contains(label)).forEach(labels::add);
	}

	private void serializeTile(OopTile tile, List<Integer> code) {
		code.add(tile.getElement().getId());
		code.add(tile.getColor());
	}

	private String isValidLabelTarget(OopLabelTarget target) {
		List<String> errors = new ArrayList<>();
		try {
			indexOfOrThrow(names, SPECIAL_NAMES, target.getTarget());
		} catch (RuntimeException e) {
			errors.add(e.getMessage());
		}
		try {
			indexOfOrThrow(labels, SPECIAL_LABELS, target.getLabel());
		} catch (RuntimeException e) {
			errors.add(e.getMessage());
		}
		if (errors.isEmpty()) {
			return null;
		} else {
			return String.join(", ", errors);
		}
	}

	private void serializeLabelTarget(OopLabelTarget target, List<Integer> code) {
		code.add(indexOfOrThrow(names, SPECIAL_NAMES, target.getTarget()));
		code.add(indexOfOrThrow(labels, SPECIAL_LABELS, target.getLabel()));
	}

	private void serializeCondition(OopCondition condition, List<Integer> code) {
		if (condition instanceof OopConditionNot cond) {
			code.add(0x00);
			serializeCondition(cond.getCond(), code);
		} else if (condition instanceof OopConditionAlligned) {
			code.add(0x01);
		} else if (condition instanceof OopConditionContact) {
			code.add(0x02);
		} else if (condition instanceof OopConditionBlocked cond) {
			code.add(0x03);
			serializeDirection(cond.getDirection(), code);
		} else if (condition instanceof OopConditionEnergized) {
			code.add(0x04);
		} else if (condition instanceof OopConditionAny cond) {
			code.add(0x05);
			serializeTile(cond.getTile(), code);
		} else if (condition instanceof OopConditionFlag cond) {
			code.add(0x06);
			int flagIdx = worldState.getFlags().indexOf(cond.getFlag());
			if (flagIdx == -1) {
				warnOrError("Flag " + cond.getFlag() + " not found in " + worldState.getFlags());
				flagIdx = 255;
			}
			code.add(flagIdx);
		} else {
			throw new RuntimeException("Unsupported condition: " + condition);
		}
	}

	private void serializeDirection(OopDirection direction, List<Integer> code) {
		if (direction instanceof OopDirectionIdle) {
			code.add(0x00);
		} else if (direction instanceof OopDirectionNorth) {
			code.add(0x01);
		} else if (direction instanceof OopDirectionSouth) {
			code.add(0x02);
		} else if (direction instanceof OopDirectionEast) {
			code.add(0x03);
		} else if (direction instanceof OopDirectionWest) {
			code.add(0x04);
		} else if (direction instanceof OopDirectionSeek) {
			code.add(0x05);
		} else if (direction instanceof OopDirectionFlow) {
			code.add(0x06);
		} else if (direction instanceof OopDirectionRnd) {
			code.add(0x07);
		} else if (direction instanceof OopDirectionRndns) {
			code.add(0x08);
		} else if (direction instanceof OopDirectionRndne) {
			code.add(0x09);
		} else if (direction instanceof OopDirectionCw dir) {
			code.add(0x0A);
			serializeDirection(dir.getChild(), code);
		} else if (direction instanceof OopDirectionCcw dir) {
			code.add(0x0B);
			serializeDirection(dir.getChild(), code);
		} else if (direction instanceof OopDirectionRndp dir) {
			code.add(0x0C);
			serializeDirection(dir.getChild(), code);
		} else if (direction instanceof OopDirectionOpp dir) {
			code.add(0x0D);
			serializeDirection(dir.getChild(), code);
		} else {
			throw new RuntimeException("Unsupported direction: " + direction);
		}
	}

	private void serializeSkippableCommand(OopCommand command, List<Integer> code, List<BankPacker.PointerUpdateRequest> ptrRequests) {
		if (command == null) {
			code.add(0);
			return;
		}

		List<Integer> cmdCode = new ArrayList<>();
		serializeCommand(command, cmdCode, null, ptrRequests);
		code.add(cmdCode.size());
		code.addAll(cmdCode);
	}

	private void serializeCommand(OopCommand command, List<Integer> code, List<Integer> labels, List<BankPacker.PointerUpdateRequest> ptrRequests) {
		boolean isInner = labels == null;
		if (command instanceof OopCommandLabel label) {
			if (isInner) throw new RuntimeException("Not allowed inside a command!");
			labels.add(indexOfOrThrow(this.labels, SPECIAL_LABELS, label.getLabel().toUpperCase(Locale.ROOT)));
			int pos = code.size() | (label.isRestoreFindStringVisible() ? 0x8000 : 0);
			labels.add(pos & 0xFF);
			labels.add(pos >> 8);
		} else if (command instanceof OopCommandEnd) {
			code.add(0x00);
		} else if (command instanceof OopCommandDirection cmd) {
			code.add(0x01);
			serializeDirection(cmd.getDirection(), code);
		} else if (command instanceof OopCommandDirectionTry cmd) {
			code.add(0x02);
			serializeDirection(cmd.getDirection(), code);
		} else if (command instanceof OopCommandGo cmd) {
			code.add(0x03);
			serializeDirection(cmd.getDirection(), code);
		} else if (command instanceof OopCommandTry cmd) {
			code.add(0x04);
			serializeDirection(cmd.getDirection(), code);
			serializeSkippableCommand(cmd.getElseCommand(), code, ptrRequests);
		} else if (command instanceof OopCommandWalk cmd) {
			code.add(0x05);
			serializeDirection(cmd.getDirection(), code);
		} else if (command instanceof OopCommandSet cmd) {
			code.add(0x06);
			code.add(indexOfOrThrow(worldState.getFlags(), cmd.getFlag()));
		} else if (command instanceof OopCommandClear cmd) {
			code.add(0x07);
			code.add(indexOfOrThrow(worldState.getFlags(), cmd.getFlag()));
		} else if (command instanceof OopCommandIf cmd) {
			code.add(0x08);
			serializeCondition(cmd.getCondition(), code);
			serializeSkippableCommand(cmd.getElseCommand(), code, ptrRequests);
		} else if (command instanceof OopCommandShoot cmd) {
			code.add(0x09);
			serializeDirection(cmd.getDirection(), code);
		} else if (command instanceof OopCommandThrowstar cmd) {
			code.add(0x0A);
			serializeDirection(cmd.getDirection(), code);
		} else if (command instanceof OopCommandGive cmd) {
			code.add(0x0B);
			switch (cmd.getCounterType()) {
				case HEALTH -> code.add(0x00);
				case AMMO -> code.add(0x01);
				case GEMS -> code.add(0x02);
				case TORCHES -> code.add(0x03);
				case SCORE -> code.add(0x04);
				case TIME -> code.add(0x05);
				default -> throw new RuntimeException("Unsupported counter type: " + cmd.getCounterType());
			}
			code.add(cmd.getAmount() & 0xFF);
			code.add((cmd.getAmount() >> 8) & 0xFF);
			serializeSkippableCommand(cmd.getElseCommand(), code, ptrRequests);
		} else if (command instanceof OopCommandEndgame) {
			code.add(0x0D);
		} else if (command instanceof OopCommandIdle) {
			code.add(0x0E);
		} else if (command instanceof OopCommandRestart) {
			code.add(0x0F);
		} else if (command instanceof OopCommandZap cmd) {
			String err = isValidLabelTarget(cmd.getTarget());
			if (err != null) {
				warnOrError("#ZAP: " + err);
				code.add(0x0C);
			} else {
				code.add(0x10);
				serializeLabelTarget(cmd.getTarget(), code);
			}
		} else if (command instanceof OopCommandRestore cmd) {
			String err = isValidLabelTarget(cmd.getTarget());
			if (err != null) {
				warnOrError("#RESTORE: " + err);
				code.add(0x0C);
			} else {
				code.add(0x11);
				serializeLabelTarget(cmd.getTarget(), code);
			}
		} else if (command instanceof OopCommandLock) {
			code.add(0x12);
		} else if (command instanceof OopCommandUnlock) {
			code.add(0x13);
		} else if (command instanceof OopCommandSend cmd) {
			String err = isValidLabelTarget(cmd.getTarget());
			if (err != null) {
				warnOrError("#SEND: " + err);
				code.add(0x0C);
			} else {
				code.add(0x14);
				serializeLabelTarget(cmd.getTarget(), code);
			}
		} else if (command instanceof OopCommandBecome cmd) {
			code.add(0x15);
			serializeTile(cmd.getTile(), code);
		} else if (command instanceof OopCommandPut cmd) {
			code.add(0x16);
			serializeDirection(cmd.getDirection(), code);
			serializeTile(cmd.getTile(), code);
		} else if (command instanceof OopCommandChange cmd) {
			code.add(0x17);
			serializeTile(cmd.getTileFrom(), code);
			serializeTile(cmd.getTileTo(), code);
		} else if (command instanceof OopCommandPlay cmd) {
			code.add(0x18);
			List<OopSound.Note> notes = cmd.getSound().getNotes();
			code.add(notes.size() * 2);
			for (OopSound.Note note : notes) {
				code.add(note.getNote());
				code.add(note.getDuration());
			}
		} else if (command instanceof OopCommandCycle cmd) {
			code.add(0x19);
			code.add(cmd.getValue());
		} else if (command instanceof OopCommandChar cmd) {
			code.add(0x1A);
			code.add(cmd.getValue());
		} else if (command instanceof OopCommandDie) {
			code.add(0x1B);
		} else if (command instanceof OopCommandBind cmd) {
			try {
				int bindId = indexOfOrThrow(names, SPECIAL_NAMES, cmd.getTargetName());
				code.add(0x1C);
				code.add(bindId);
			} catch (RuntimeException e) {
				warnOrError("#BIND: " + e.getMessage());
			}
		} else if (command instanceof OopCommandGBZWrappedTextLines cmd) {
			code.add(0x1D);
			code.add(cmd.getLineCount());
			code.add(cmd.getLines().size());

			for (OopCommandTextLine line : cmd.getLines()) {
				int labelId = 255;
				try {
					labelId = indexOfOrThrow(this.labels, SPECIAL_LABELS, line.getDestination() != null ? line.getDestination().toUpperCase(Locale.ROOT) : line.getDestination());
				} catch (Exception e) {
					warnOrError(e.getMessage());
				}
				byte[] textLine = worldState.addTextLine(line, labelId);
				ptrRequests.add(new BankPacker.PointerUpdateRequest(true, textLine, null, CODE_OFFSET + code.size()));
				code.add(0);
				code.add(0);
				code.add(0);
			}
		} else {
			throw new RuntimeException("Unsupported command: " + command);
		}
	}

	public byte[] serializeProgram(OopProgram program) {
		Map<Integer, Integer> positionMap = new HashMap<>();
		List<Integer> code = new ArrayList<>();
		List<Integer> labels = new ArrayList<>();
		List<BankPacker.PointerUpdateRequest> ptrRequests = new ArrayList<>();
		byte[] windowName = program.getWindowName() != null ? program.getWindowName().getBytes(StandardCharsets.ISO_8859_1) : new byte[0];

		List<OopCommand> commands = new ArrayList<>(program.getCommands().size());
		List<OopCommandTextLine> textLines = new ArrayList<>();
		for (OopCommand cmd : program.getCommands()) {
			if (cmd instanceof OopCommandComment) {
				continue;
			}

			if (cmd instanceof OopCommandCycle c) {
				if (c.getValue() <= 0) {
					continue;
				}
			} else if (cmd instanceof OopCommandChar c) {
				if (c.getValue() <= 0 || c.getValue() > 255) {
					continue;
				}
			}

			if (cmd instanceof OopCommandTextLine tl) {
				if (tl.getType() != OopCommandTextLine.Type.EXTERNAL_HYPERLINK) {
					if (tl.getType() == OopCommandTextLine.Type.REGULAR && tl.getMessage().isEmpty()) {
						commands.add(new OopCommandGBZWrappedTextLines(List.of(tl), 20));
					} else {
						textLines.add(tl);
					}
				} else {
					warnOrError("External hyperlinks not supported!");
				}
			} else {
				if (!textLines.isEmpty()) {
					commands.add(new OopCommandGBZWrappedTextLines(textLines, 20));
					textLines.clear();
				}
				commands.add(cmd);
			}
		}
		if (!textLines.isEmpty()) {
			commands.add(new OopCommandGBZWrappedTextLines(textLines, 20));
			textLines.clear();
		}

		// Serialization
		OopCommand lastCmd = null;
		for (OopCommand cmd : commands) {
			if (cmd.getPosition() != null) {
				positionMap.put(cmd.getPosition(), code.size());
			}
			serializeCommand(cmd, code, labels, ptrRequests);
			lastCmd = cmd;
		}
		if (!(lastCmd instanceof OopCommandEnd)) {
			serializeCommand(new OopCommandEnd(), code, labels, ptrRequests);
		}

		// Statistics
		int linesInProgram = 0;
		for (OopCommand cmd : commands) {
			if (cmd instanceof OopCommandGBZWrappedTextLines tl) {
				linesInProgram += tl.getLines().size();
			}
		}
		this.worldState.setMaxLinesInProgram(linesInProgram);

		List<Integer> fullData = new ArrayList<>();
		int idx = names.indexOf(program.getName());
		fullData.add(idx >= 0 ? idx : 255);
		int offsetToWindowName = 0;
		if (windowName.length > 0) {
			offsetToWindowName = code.size() + 5;
		}
		int offsetToLabelList = 0;
		if (!labels.isEmpty()) {
			if (windowName.length > 0) {
				offsetToLabelList = code.size() + 6 + windowName.length;
			} else {
				offsetToLabelList = code.size() + 5;
			}
		}
		fullData.add(offsetToWindowName & 0xFF);
		fullData.add(offsetToWindowName >> 8);
		fullData.add(offsetToLabelList & 0xFF);
		fullData.add(offsetToLabelList >> 8);
		fullData.addAll(code);
		if (windowName.length > 0) {
			fullData.add(windowName.length);
			for (byte c : windowName) {
				fullData.add((int) c & 0xFF);
			}
		}
		if (!labels.isEmpty()) {
			int labelCount = labels.size() / 3;
			if (labelCount >= 256) {
				throw new RuntimeException("Maximum of 255 labels per stat supported!");
			}
			fullData.add(labelCount);
			fullData.addAll(labels);
		}

		byte[] fullDataByte = GBZooUtils.toByteArray(fullData);

		for (BankPacker.PointerUpdateRequest request : ptrRequests) {
			if (request.getPtrArray() == null) {
				request = request.withPtrArray(fullDataByte);
			}
			this.packer.updatePointer(request);
		}

		programPositionMap.put(program, positionMap);
		return fullDataByte;
	}

	public int countZappableLabels(OopProgram program) {
		return (int) OopUtils.allChildren(program.getCommands().stream()).filter(c -> c instanceof OopCommandLabel).count();
	}
}

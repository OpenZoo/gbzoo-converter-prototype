package pl.asie.gbzooconv;

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
import pl.asie.libzzt.oop.commands.OopCommandCycle;
import pl.asie.libzzt.oop.commands.OopCommandDie;
import pl.asie.libzzt.oop.commands.OopCommandDirection;
import pl.asie.libzzt.oop.commands.OopCommandDirectionTry;
import pl.asie.libzzt.oop.commands.OopCommandEnd;
import pl.asie.libzzt.oop.commands.OopCommandEndgame;
import pl.asie.libzzt.oop.commands.OopCommandGo;
import pl.asie.libzzt.oop.commands.OopCommandIdle;
import pl.asie.libzzt.oop.commands.OopCommandLabel;
import pl.asie.libzzt.oop.commands.OopCommandLock;
import pl.asie.libzzt.oop.commands.OopCommandPlay;
import pl.asie.libzzt.oop.commands.OopCommandPut;
import pl.asie.libzzt.oop.commands.OopCommandRestart;
import pl.asie.libzzt.oop.commands.OopCommandRestore;
import pl.asie.libzzt.oop.commands.OopCommandSend;
import pl.asie.libzzt.oop.commands.OopCommandSet;
import pl.asie.libzzt.oop.commands.OopCommandShoot;
import pl.asie.libzzt.oop.commands.OopCommandThrowstar;
import pl.asie.libzzt.oop.commands.OopCommandTry;
import pl.asie.libzzt.oop.commands.OopCommandUnlock;
import pl.asie.libzzt.oop.commands.OopCommandWalk;
import pl.asie.libzzt.oop.commands.OopCommandZap;
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Getter
public class GBZooOopBoardConverter {
	private static final Map<String, Integer> SPECIAL_LABELS = Map.of(
			"SHOT", 254,
			"ENERGIZE", 253,
			"THUD", 252,
			"TOUCH", 251,
			"BOMBED", 250
	);
	private static final Map<String, Integer> SPECIAL_NAMES = Map.of(
			"ALL", 254,
			"OTHERS", 253,
			"SELF", 252
	);
	private static final int MAX_LABELS = 255 - SPECIAL_LABELS.size();
	private static final int MAX_NAMES = 255 - SPECIAL_NAMES.size();

	private final GBZooOopWorldState worldState;
	private final Set<OopProgram> programs = new HashSet<>();
	private final List<String> labels = new ArrayList<>(); // ordering sensitive
	private final List<String> names = new ArrayList<>(); // ordering sensitive

	public GBZooOopBoardConverter(GBZooOopWorldState worldState) {
		this.worldState = worldState;
	}

	private static <T> int indexOfOrThrow(List<T> list, T item) {
		int idx = list.indexOf(item);
		if (idx < 0) {
			throw new RuntimeException(item + " not found in " + list);
		}
		return idx;
	}

	private static <T> int indexOfOrThrow(List<T> list, Map<T, Integer> specialMap, T item) {
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

		System.out.println(program);

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

	private void serializeLabelTarget(OopLabelTarget target, List<Integer> code) {
		code.add(indexOfOrThrow(names, SPECIAL_NAMES, target.getTarget()));
		code.add(indexOfOrThrow(labels, SPECIAL_LABELS, target.getLabel()));
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

	private void serializeCommand(OopCommand command, List<Integer> code, List<Integer> labels) {
		if (command instanceof OopCommandLabel label) {
			labels.add(indexOfOrThrow(this.labels, SPECIAL_LABELS, label.getLabel().toUpperCase(Locale.ROOT)));
			int pos = code.size();
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
		} else if (command instanceof OopCommandWalk cmd) {
			code.add(0x05);
			serializeDirection(cmd.getDirection(), code);
		} else if (command instanceof OopCommandSet cmd) {
			code.add(0x06);
			code.add(indexOfOrThrow(worldState.getFlags(), cmd.getFlag()));
		} else if (command instanceof OopCommandClear cmd) {
			code.add(0x07);
			code.add(indexOfOrThrow(worldState.getFlags(), cmd.getFlag()));
		} else if (command instanceof OopCommandShoot cmd) {
			code.add(0x09);
			serializeDirection(cmd.getDirection(), code);
		} else if (command instanceof OopCommandThrowstar cmd) {
			code.add(0x0A);
			serializeDirection(cmd.getDirection(), code);
		} else if (command instanceof OopCommandEndgame) {
			code.add(0x0D);
		} else if (command instanceof OopCommandIdle) {
			code.add(0x0E);
		} else if (command instanceof OopCommandRestart) {
			code.add(0x0F);
		} else if (command instanceof OopCommandZap cmd) {
			code.add(0x10);
			serializeLabelTarget(cmd.getTarget(), code);
		} else if (command instanceof OopCommandRestore cmd) {
			code.add(0x11);
			serializeLabelTarget(cmd.getTarget(), code);
		} else if (command instanceof OopCommandLock) {
			code.add(0x12);
		} else if (command instanceof OopCommandUnlock) {
			code.add(0x13);
		} else if (command instanceof OopCommandSend cmd) {
			code.add(0x14);
			serializeLabelTarget(cmd.getTarget(), code);
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
			code.add(0x1C);
			code.add(indexOfOrThrow(names, SPECIAL_NAMES, cmd.getTargetName()));
		} else {
			throw new RuntimeException("Unsupported command: " + command);
		}
	}

	public byte[] serializeProgram(OopProgram program) {
		List<Integer> code = new ArrayList<>();
		List<Integer> labels = new ArrayList<>();
		byte[] windowName = program.getWindowName().getBytes(StandardCharsets.ISO_8859_1);

		for (OopCommand cmd : program.getCommands()) {
			serializeCommand(cmd, code, labels);
		}

		List<Integer> fullData = new ArrayList<>();
		int idx = names.indexOf(program.getName());
		fullData.add(idx >= 0 ? idx : 255);
		int offsetToWindowName = 0;
		if (windowName.length > 0) {
			offsetToWindowName = code.size() + 2;
		}
		int offsetToLabelList = 0;
		if (!labels.isEmpty()) {
			if (windowName.length > 0) {
				offsetToLabelList = code.size() + 1 + windowName.length;
			}
		} else {
			offsetToLabelList = code.size();
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
			fullData.addAll(labels);
		}

		byte[] fullDataByte = new byte[fullData.size()];
		for (int i = 0; i < fullData.size(); i++) {
			fullDataByte[i] = (byte) ((int) fullData.get(i));
		}
		return fullDataByte;
	}

	public int countZappableLabels(OopProgram program) {
		return (int) OopUtils.allChildren(program.getCommands().stream()).filter(c -> c instanceof OopCommandLabel).count();
	}
}

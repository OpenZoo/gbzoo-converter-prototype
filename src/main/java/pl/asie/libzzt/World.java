package pl.asie.libzzt;

import lombok.Data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Data
public class World {
	private static final int VERSION = -1;

	private int ammo;
	private int gems;
	private boolean[] keys = new boolean[7];
	private int health = 100;
	private int currentBoard;
	private int torches;
	private int torchTicks;
	private int energizerTicks;
	private int score;
	private String name = "";
	private List<String> flags = new ArrayList<>();
	private int boardTimeSec;
	private int boardTimeHsec;
	private boolean isSave;
	private List<Board> boards = new ArrayList<>();

	private final Platform platform;

	public World(Platform platform) {
		this.platform = platform;
		Board titleBoard = new Board(platform);
		titleBoard.setName("Title screen");
		boards.add(titleBoard);
	}

	public void readZ(ZInputStream stream) throws IOException {
		int version = stream.readPShort();
		if (version != VERSION) {
			throw new RuntimeException("Invalid version: " + version);
		}
		int boardCount = stream.readPShort();

		// world info
		this.ammo = stream.readPShort();
		this.gems = stream.readPShort();
		for (int i = 0; i < 7; i++)
			this.keys[i] = stream.readPBoolean();
		this.health = stream.readPShort();
		this.currentBoard = stream.readPShort();
		this.torches = stream.readPShort();
		this.torchTicks = stream.readPShort();
		this.energizerTicks = stream.readPShort();
		stream.readPShort();
		this.score = stream.readPShort();
		this.name = stream.readPString(20);
		this.flags.clear();
		for (int i = 0; i < 10; i++) {
			String flag = stream.readPString(20);
			if (!flag.isBlank()) {
				this.flags.add(flag);
			}
		}
		this.boardTimeSec = stream.readPShort();
		this.boardTimeHsec = stream.readPShort();
		this.isSave = stream.readPBoolean();
		if (stream.skip(14) != 14) {
			throw new IOException("World info error!");
		}
		if (stream.skip(233) != 233) {
			throw new IOException("World padding error!");
		}

		// boards
		boards.clear();
		for (int i = 0; i <= boardCount; i++) {
			Board board = new Board(platform);
			board.readZ(stream);
			boards.add(board);
		}
	}

	public void writeZ(ZOutputStream stream) throws IOException {
		stream.writePShort(VERSION);
		stream.writePShort(boards.size() - 1);

		// world info
		stream.writePShort(ammo);
		stream.writePShort(gems);
		for (int i = 0; i < 7; i++)
			stream.writePBoolean(keys[i]);
		stream.writePShort(health);
		stream.writePShort(currentBoard);
		stream.writePShort(torches);
		stream.writePShort(torchTicks);
		stream.writePShort(energizerTicks);
		stream.writePShort(0);
		stream.writePShort(score);
		stream.writePString(name, 20);
		for (int i = 0; i < 10; i++)
			stream.writePString(flags.size() > i ? flags.get(i) : "", 20);
		stream.writePShort(boardTimeSec);
		stream.writePShort(boardTimeHsec);
		stream.writePBoolean(isSave);
		stream.pad(14); // world info
		stream.pad(233); // world padding

		// boards
		for (Board board : boards) {
			board.writeZ(stream);
		}
	}
}

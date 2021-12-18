package pl.asie.gbzooconv;

import lombok.Getter;
import pl.asie.libzzt.Board;
import pl.asie.libzzt.Stat;
import pl.asie.libzzt.World;
import pl.asie.libzzt.oop.OopUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
public class GBZooOopWorldState {
	private static final int MAX_FLAGS = 255;
	private final List<String> flags = new ArrayList<>();

	public GBZooOopWorldState(World world) {
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
	}
}

package pl.asie.libzzt.oop;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Data
public final class OopSound {
	@Data
	public static final class Note {
		private final int note;
		private final int duration;
	}

	private final List<Note> notes;

	public OopSound(String noteString) {
		this.notes = new ArrayList<>();
		noteString = noteString.toUpperCase(Locale.ROOT);

		int noteOctave = 3;
		int noteDuration = 1;

		int i = 0;
		while (i < noteString.length()) {
			int noteTone = -1;
			switch (noteString.charAt(i++)) {
				case 'T':
					noteDuration = 1;
					break;
				case 'S':
					noteDuration = 2;
					break;
				case 'I':
					noteDuration = 4;
					break;
				case 'Q':
					noteDuration = 8;
					break;
				case 'H':
					noteDuration = 16;
					break;
				case 'W':
					noteDuration = 32;
					break;
				case '.':
					noteDuration = (noteDuration * 3) / 2;
					break;
				case '3':
					noteDuration = noteDuration / 3;
					break;
				case '+':
					if (noteOctave < 6) noteOctave++;
					break;
				case '-':
					if (noteOctave > 1) noteOctave--;
					break;
				case 'A':
				case 'B':
				case 'C':
				case 'D':
				case 'E':
				case 'F':
				case 'G':
					switch (noteString.charAt(i - 1)) {
						case 'A':
							noteTone = 9;
							break;
						case 'B':
							noteTone = 11;
							break;
						case 'C':
							noteTone = 0;
							break;
						case 'D':
							noteTone = 2;
							break;
						case 'E':
							noteTone = 4;
							break;
						case 'F':
							noteTone = 5;
							break;
						case 'G':
							noteTone = 7;
							break;
					}

					if (i < noteString.length()) switch (noteString.charAt(i)) {
						case '!':
							noteTone--; i++;
							break;
						case '#':
							noteTone++; i++;
							break;
					}

					notes.add(new Note((noteOctave << 4) + noteTone, noteDuration));
					break;
				case 'X':
					notes.add(new Note(0, noteDuration));
					break;
				case '0':
				case '1':
				case '2':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
					notes.add(new Note(noteString.charAt(i - 1) - '0' + 0xF0, noteDuration));
					break;
			}
		}
	}
}

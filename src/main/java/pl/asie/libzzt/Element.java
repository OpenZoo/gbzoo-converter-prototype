package pl.asie.libzzt;

import lombok.Builder;
import lombok.Getter;
import pl.asie.libzzt.oop.OopUtils;

import java.util.Objects;

@SuppressWarnings("ConstantConditions")
@Getter
@Builder
public final class Element {
    @Builder.Default
    private final int character = ' ';
    @Builder.Default
    private final int color = COLOR_CHOICE_ON_BLACK;
    @Builder.Default
    private final boolean destructible = false;
    @Builder.Default
    private final boolean pushable = false;
    @Builder.Default
    private final boolean visibleInDark = false;
    @Builder.Default
    private final boolean placeableOnTop = false;
    @Builder.Default
    private final boolean walkable = false;
    @Builder.Default
    private final boolean hasDrawProc = false;
    @Builder.Default
    private final int textBackgroundColor = -1;
    @Builder.Default
    private final int cycle = -1;
    @Builder.Default
    private final int scoreValue = 0;
    @Builder.Default
    private final String name = "";
    @Builder.Default
    private final int id = -1;
    private String oopName;

    public String getOopName() {
        if (oopName == null) {
            oopName = OopUtils.stripChars(name);
        }
        return oopName;
    }

    public int getColor() {
        return textBackgroundColor >= 0 ? (textBackgroundColor * 16 + 15) : this.color;
    }

    public boolean isText() {
        return textBackgroundColor >= 0;
    }

    public boolean isStat() {
        return cycle >= 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Element element = (Element) o;
        return id == element.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Element{" +
                "name='" + name + '\'' +
                ", id=" + id +
                '}';
    }

    public static final int COLOR_SPECIAL_MIN = 0xF0;
    public static final int COLOR_CHOICE_ON_BLACK = 0xFF;
    public static final int COLOR_WHITE_ON_CHOICE = 0xFE;
    public static final int COLOR_CHOICE_ON_CHOICE = 0xFD;
}

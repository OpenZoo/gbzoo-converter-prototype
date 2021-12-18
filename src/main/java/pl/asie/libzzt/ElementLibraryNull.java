package pl.asie.libzzt;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ElementLibraryNull {
    private static final Element EMPTY = Element.builder().name("Empty").id(0).build();

    public static final ElementLibrary INSTANCE;

    static {
        INSTANCE = new ElementLibrary(List.of("EMPTY"), List.of(EMPTY));
    }
}

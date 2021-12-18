package pl.asie.libzzt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElementLibrary {
    private final Map<Integer, Element> elementsById = new HashMap<>();
    private final Map<Element, String> elementInternalNames = new HashMap<>();
    private final Map<String, Element> elementsByInternalNames = new HashMap<>();
    private final Element empty;

    ElementLibrary(List<String> names, List<Element> elements) {
        for (int i = 0; i < elements.size(); i++) {
            String name = names.get(i);
            Element element = elements.get(i);

            elementsById.put(element.getId(), element);
            elementInternalNames.put(element, name);
            elementsByInternalNames.put(name, element);
        }

        empty = elementsById.get(0);
        if (empty == null) {
            throw new RuntimeException();
        }
    }

    public Element byId(int id) {
        return elementsById.getOrDefault(id, empty);
    }

    public Element byInternalName(String name) {
        return elementsByInternalNames.getOrDefault(name, empty);
    }

    public String getInternalName(Element element) {
        return elementInternalNames.getOrDefault(element, "(unknown)");
    }

    public Element getEmpty() {
        return empty;
    }
}

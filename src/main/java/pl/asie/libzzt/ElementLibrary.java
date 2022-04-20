package pl.asie.libzzt;

import pl.asie.libzzt.oop.OopUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ElementLibrary {
    private final List<Element> elements;
    private final Map<Integer, Element> elementsById = new HashMap<>();
    private final Map<Element, String> elementInternalNames = new HashMap<>();
    private final Map<String, Element> elementsByInternalNames = new HashMap<>();
    private final Element empty;

    ElementLibrary(List<String> names, List<Element> elements) {
        this.elements = List.copyOf(elements);
        for (int i = 0; i < elements.size(); i++) {
            String name = names.get(i);
            Element element = elements.get(i);

            elementsById.put(element.getId(), element);
            elementInternalNames.put(element, name);
            elementsByInternalNames.putIfAbsent(name, element);
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

    public Element byOopTokenName(String name) {
        for (Element element : elements) {
            if (Objects.equals(OopUtils.stripChars(name), element.getOopName())) {
                return element;
            }
        }
        return null;
    }

    public String getInternalName(Element element) {
        return elementInternalNames.getOrDefault(element, "(unknown)");
    }

    public Element getEmpty() {
        return empty;
    }
}

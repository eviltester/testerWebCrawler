package uk.co.compendiumdev.javafortesters.webcrawl;

import org.jsoup.nodes.Element;

/**
 * Created by Alan on 17/01/2017.
 */
public class HtmlElement {
    private final Element element;

    public HtmlElement(Element element) {
        // from a JSoup Element
        this.element = element;
    }

    public String getAttribute(String attributeName) {
        return element.attr(attributeName);
    }
}

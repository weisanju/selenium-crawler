package com.weisanju.crawler.selectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * @author code4crafter@gmail.com
 * @since 0.3.0
 */
public abstract class BaseElementSelector implements Selector, ElementSelector {
    private Document parse(String text) {
        if (text == null) {
            return null;
        }

        // Jsoup could not parse <tr></tr> or <td></td> tag directly
        // https://stackoverflow.com/questions/63607740/jsoup-couldnt-parse-tr-tag
        if ((text.startsWith("<tr>") && text.endsWith("</tr>"))
                || (text.startsWith("<td>") && text.endsWith("</td>"))) {
            text = "<table>" + text + "</table>";
        }
        return Jsoup.parse(text);
    }

    @Override
    public String select(String text) {
        if (text != null) {
            return select(parse(text));
        }
        return null;
    }

    @Override
    public List<String> selectList(String text) {
        if (text != null) {
            return selectList(parse(text));
        } else {
            return new ArrayList<String>();
        }
    }


    public abstract Element selectElement(Element element);

    public abstract List<Element> selectElements(Element element);

    public abstract boolean hasAttribute();

}

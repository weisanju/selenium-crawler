package com.weisanju.crawler.selectors;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * parse json
 *
 * @author code4crafter@gmail.com
 * @since 0.5.0
 */
public class Json extends AbstractSelectable {

    private final JsonNode jsonNode;

    public Json(JsonNode jsonNode) {
        this.jsonNode = jsonNode;
    }


    @Override
    protected List<String> getSourceTexts() {

        List<String> arrays = new ArrayList<>();

        for (JsonNode node : jsonNode) {
            arrays.add(node.asText());
        }

        return arrays;
    }

    @Override
    public Selectable css(String selector) {
        throw new UnsupportedOperationException("css selector can not apply to json");
    }

    @Override
    public Selectable css(String selector, String attrName) {
        throw new UnsupportedOperationException("css selector can not apply to json");
    }

    @Override
    public JsonNode smartContent() {
        return jsonNode;
    }

    @Override
    public Selectable links() {
        throw new UnsupportedOperationException("links selector can not apply to json");
    }

    @Override
    public JsonNode jqPath(String rule) {

        JqPathSelector jqPathSelector = new JqPathSelector(rule);
        return jqPathSelector.select(  );
    }

    @Override
    public List<Selectable> nodes() {
        List<Selectable> arrays = new ArrayList<>();

        for (JsonNode node : jsonNode) {
            arrays.add(new Json(node));
        }
        return arrays;
    }
}

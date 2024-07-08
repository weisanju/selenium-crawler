package com.weisanju.crawler.selectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Selectable plain text.<br>
 * Can not be selected by XPath or CSS Selector.
 *
 * @author code4crafter@gmail.com <br>
 * @since 0.1.0
 */
public class PlainText extends AbstractSelectable {

    protected List<String> sourceTexts;

    public PlainText(List<String> sourceTexts) {
        this.sourceTexts = sourceTexts;
    }

    public PlainText(String text) {
        this.sourceTexts = new ArrayList<String>();
        sourceTexts.add(text);
    }

    public static PlainText create(String text) {
        return new PlainText(text);
    }

    @Override
    public Selectable css(String selector) {
        throw new UnsupportedOperationException("$ can not apply to plain text. Please check whether you use a previous xpath with attribute select (/@href etc).");
    }

    @Override
    public Selectable css(String selector, String attrName) {
        throw new UnsupportedOperationException("$ can not apply to plain text. Please check whether you use a previous xpath with attribute select (/@href etc).");
    }

    @Override
    public JsonNode smartContent() {
        return new TextNode(String.join("", all()));
    }

    @Override
    public Selectable links() {
        throw new UnsupportedOperationException("Links can not apply to plain text. Please check whether you use a previous xpath with attribute select (/@href etc).");
    }

    @Override
    public List<Selectable> nodes() {
        List<Selectable> nodes = new ArrayList<Selectable>(getSourceTexts().size());
        for (String string : getSourceTexts()) {
            nodes.add(PlainText.create(string));
        }
        return nodes;
    }

    @Override
    protected List<String> getSourceTexts() {
        return sourceTexts;
    }
}

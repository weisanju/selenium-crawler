package com.weisanju.crawler.selectors;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Collections;
import java.util.List;

/**
 * Selectable html.<br>
 *
 * @author code4crafter@gmail.com <br>
 * @since 0.1.0
 */
@Slf4j
public class Html extends HtmlNode {

	/**
	 * Disable jsoup html entity escape. It can be set just before any Html instance is created.
     * @deprecated
	 */
	public static boolean DISABLE_HTML_ENTITY_ESCAPE = false;

    /**
     * Store parsed document for better performance when only one text exist.
     */
    private Document document;

    public Html(String text, String url) {
        try {
            this.document = Jsoup.parse(text, url);
        } catch (Exception e) {
            this.document = null;
            log.warn("parse document error ", e);
        }
    }

    public Html(String text) {
        try {
            this.document = Jsoup.parse(text);
        } catch (Exception e) {
            this.document = null;
            log.warn("parse document error ", e);
        }
    }

    public Html(Document document) {
        this.document = document;
    }

    public Document getDocument() {
        return document;
    }

    @Override
    protected List<Element> getElements() {
        return Collections.<Element>singletonList(getDocument());
    }


    public static Html create(String text) {
        return new Html(text);
    }

}

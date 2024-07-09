package com.weisanju.crawler.selectors;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.weisanju.crawler.util.JacksonUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;
/**
 * Convenient methods for selectors.<br>
 *
 * @author code4crafter@gmail.com <br>
 * @since 0.2.1
 */
public abstract class Selectors {

    public static RegexSelector regex(String expr) {
        return new RegexSelector(expr);
    }

    public static RegexSelector regex(String expr, int group) {
        return new RegexSelector(expr, group);
    }

    public static SmartContentSelector smartContent() {
        return new SmartContentSelector();
    }

    public static CssSelector css(String expr) {
        return new CssSelector(expr);
    }

    public static CssSelector css(String expr, String attrName) {
        return new CssSelector(expr, attrName);
    }

    public static AndSelector and(Selector... selectors) {
        return new AndSelector(selectors);
    }

    public static OrSelector or(Selector... selectors) {
        return new OrSelector(selectors);
    }


    public static ArrayNode links(String css, String doc, String baseUrl) {
        Elements elements = Jsoup.parse(doc, baseUrl).select(css).select("a");
        ArrayNode arrayNode = JacksonUtil.createArrayNode();
        for (Element element0 : elements) {
            String baseUri = element0.baseUri();
            if (!baseUri.trim().isEmpty()) {
                arrayNode.add(element0.attr("abs:href"));
            } else {
                arrayNode.add(element0.attr("href"));
            }
        }
        return arrayNode;
    }

    public static String css(String css, String doc, String baseUrl) {
        Elements elements = Jsoup.parse(doc, baseUrl).select(css);
        return cleanHtml(elements.html());
    }

    public static Safelist basicWithImages() {
        return Safelist.none()
                .addTags("p")
                .addTags("video").addAttributes("video", "src", "controls", "autoplay", "loop", "muted", "mediaType")
                .addTags("img")
                .addAttributes("img", "align", "alt", "height", "src", "title", "width")
                .addProtocols("img", "src", "http", "https");
    }

    public static  String cleanHtml(String html) {
        html = Jsoup.clean(html, basicWithImages());
        html = html.replaceAll("<p>(.*?)</p>", "$1\n");
        html = html.replaceAll("<br>.*?</br>", "\n");
        html = html.replaceAll("<br/*>", "\n");
        return html;
    }
}
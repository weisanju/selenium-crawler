package com.weisanju.crawler.selectors;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.util.Collections;
import java.util.List;

/**
 * Borrowed from https://code.google.com/p/cx-extractor/
 *
 * @author code4crafter@gmail.com <br>
 * @since 0.4.1
 */
public class SmartContentSelector implements Selector {

    public SmartContentSelector() {
    }

    @Override
    public String select(String html) {
        //将 <p>标签 替换为换行符

        html = Jsoup.clean(html, basicWithImages());

        html = html.replaceAll("<p>(.*?)</p>", "$1\n");
        html = html.replaceAll("<br>.*?</br>", "\n");
        html = html.replaceAll("<br/*>", "\n");
        return html;
    }

    public static Safelist basicWithImages() {
        return Safelist.none()
                .addTags("p")
                .addTags("video").addAttributes("video", "src", "controls", "autoplay", "loop", "muted", "mediaType")
                .addTags("img")
                .addAttributes("img", "align", "alt", "height", "src", "title", "width")
                .addProtocols("img", "src", "http", "https");
    }

    @Override
    public List<String> selectList(String text) {
        return Collections.singletonList(select(text));
    }
}

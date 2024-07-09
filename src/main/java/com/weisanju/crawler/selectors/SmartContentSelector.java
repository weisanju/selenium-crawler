package com.weisanju.crawler.selectors;

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

        return Selectors.cleanHtml(html);
    }

    @Override
    public List<String> selectList(String text) {
        return Collections.singletonList(select(text));
    }
}

package com.weisanju.crawler.crawlers.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.weisanju.crawler.crawlers.CrawlerContext;
import com.weisanju.crawler.crawlers.PageCrawler;

import java.util.List;

public class RoutedCrawler implements PageCrawler {

    private final List<PageCrawler> matchers;
    private final PageCrawler defaultCrawler;

    public RoutedCrawler(List<PageCrawler> matchers, PageCrawler defaultCrawler) {
        this.matchers = matchers;
        this.defaultCrawler = defaultCrawler;

    }

    @Override
    public JsonNode tryExtract(CrawlerContext context) {
        for (PageCrawler matcher : matchers) {
            JsonNode node = matcher.tryExtract(context);
            if (node != null) {
                return node;
            }
        }

        return defaultCrawler.tryExtract(context);
    }
}

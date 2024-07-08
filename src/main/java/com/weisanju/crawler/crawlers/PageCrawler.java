package com.weisanju.crawler.crawlers;

import com.fasterxml.jackson.databind.JsonNode;

public interface PageCrawler {

    /**
     * 尝试解析。如果无法解析在返回 EmptyNode
     */
    JsonNode tryExtract(CrawlerContext context);

}

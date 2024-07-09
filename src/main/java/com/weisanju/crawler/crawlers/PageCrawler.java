package com.weisanju.crawler.crawlers;

import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Mono;

public interface PageCrawler {

    /**
     * 尝试解析。如果无法解析在返回 EmptyNode
     */
    Mono<JsonNode> tryExtract(CrawlerContext context);

}

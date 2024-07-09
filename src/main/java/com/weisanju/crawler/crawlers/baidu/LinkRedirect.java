package com.weisanju.crawler.crawlers.baidu;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.weisanju.crawler.crawlers.CrawlerContext;
import com.weisanju.crawler.crawlers.PageCrawler;
import com.weisanju.crawler.util.HttpClientUtil;
import com.weisanju.crawler.util.JacksonUtil;
import reactor.core.publisher.Mono;

public class LinkRedirect implements PageCrawler {
    @Override
    public Mono<JsonNode> tryExtract(CrawlerContext context) {
        if (!context.getRequest().getUrl().startsWith("http://www.baidu.com/link?url=")) {
            return null;
        }

        Mono<String> redirectUrl = HttpClientUtil.getRedirectUrl(context.getRequest().getUrl());
        return redirectUrl.map(x -> {
            return JacksonUtil.createObjectNode("urls", JacksonUtil.createArrayNode( x ));
        });
    }
}

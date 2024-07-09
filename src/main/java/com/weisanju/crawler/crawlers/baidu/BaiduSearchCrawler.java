package com.weisanju.crawler.crawlers.baidu;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.weisanju.crawler.crawlers.CrawlerContext;
import com.weisanju.crawler.crawlers.PageCrawler;
import com.weisanju.crawler.selectors.Selectors;
import com.weisanju.crawler.util.JacksonUtil;
import com.weisanju.crawler.util.WebDriverUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import reactor.core.publisher.Mono;
public class BaiduSearchCrawler implements PageCrawler {
    @Override
    public Mono<JsonNode> tryExtract(CrawlerContext context) {
        if (!context.getRequest().getUrl().startsWith("https://www.baidu.com/s?&wd=")) {
            return null;
        }
        Mono<String> body = WebDriverUtil.getPageSourceReactive(context.getRequest().getUrl(), ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
        return body.map(x -> {
            ArrayNode links = Selectors.links("a.tts-title,.tts-title a", x, context.getRequest().getUrl());
            ObjectNode objectNode = JacksonUtil.createObjectNode();
            objectNode.set("urlList", links);
            return objectNode;
        });
    }
}

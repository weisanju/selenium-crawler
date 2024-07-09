package com.weisanju.crawler.crawlers.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.weisanju.crawler.crawlers.CrawlerContext;
import com.weisanju.crawler.util.JacksonUtil;
import com.weisanju.crawler.crawlers.PageCrawler;
import com.weisanju.crawler.selectors.Html;
import com.weisanju.crawler.selectors.Selector;
import com.weisanju.crawler.util.WebDriverUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.function.Function;

public abstract class CssSelectorPagCrawler implements PageCrawler {

    private final Map<String, Selector> selectors;

    public CssSelectorPagCrawler(Map<String, Selector> selectors) {
        this.selectors = selectors;
    }

    protected abstract boolean match(CrawlerContext context);

    @Override
    public Mono<JsonNode> tryExtract(CrawlerContext context) {

        if (!match(context)) {
            return null;
        }
        String url = context.getRequest().getUrl();
        Mono<String> sourceMono = WebDriverUtil.getPageSourceReactive(url, getEc());
        return sourceMono.map((source) -> {
            return postProcess(doSelector(source, url, selectors), context);
        }).flatMap(Function.identity());
    }

    public static ObjectNode doSelector(String source, String url, Map<String, Selector> selectors) {
        Document doc = Jsoup.parse(source, url);
        Html html = new Html(doc);

        ObjectNode objectNode = JacksonUtil.createObjectNode();

        for (Map.Entry<String, Selector> keyAndSelector : selectors.entrySet()) {
            String key = keyAndSelector.getKey();

            Selector selector = keyAndSelector.getValue();

            objectNode.set(key, html.select(selector).smartContent());
        }

        CommonCrawler.extractCommonField(objectNode, doc, url);

        return objectNode;
    }

    protected Mono<JsonNode> postProcess(ObjectNode objectNode, CrawlerContext context) {
        return Mono.just(objectNode);
    }


    protected ExpectedCondition<WebElement> getEc() {
        return ExpectedConditions.visibilityOfElementLocated(By.tagName("body"));
    }
}

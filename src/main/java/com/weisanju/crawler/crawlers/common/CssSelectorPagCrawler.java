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

import java.util.Map;

public abstract class CssSelectorPagCrawler implements PageCrawler {

    private final Map<String, Selector> selectors;

    public CssSelectorPagCrawler(Map<String, Selector> selectors) {
        this.selectors = selectors;
    }

    protected abstract boolean match(CrawlerContext context);

    @Override
    public JsonNode tryExtract(CrawlerContext context) {

        if (!match(context)) {
            return null;
        }

        String url = context.getRequest().getUrl();

        String source = WebDriverUtil.getPageSource(url, getEc());

        Document doc = Jsoup.parse(source, url);

        Html html = new Html(doc);

        ObjectNode objectNode = JacksonUtil.createObjectNode();

        for (Map.Entry<String, Selector> keyAndSelector : selectors.entrySet()) {
            String key = keyAndSelector.getKey();

            Selector selector = keyAndSelector.getValue();
            objectNode.set(key, html.select(selector).smartContent());
        }


        //url
        // comments: JsonArray


        CommonCrawler.extractCommonField(objectNode, doc);
        return objectNode;
    }

    protected ExpectedCondition<WebElement> getEc() {
        return ExpectedConditions.visibilityOfElementLocated(By.tagName("body"));
    }
}

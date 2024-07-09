package com.weisanju.crawler.crawlers.weibo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.weisanju.crawler.crawlers.CrawlerContext;
import com.weisanju.crawler.crawlers.PageCrawler;
import com.weisanju.crawler.crawlers.common.CommonCrawler;
import com.weisanju.crawler.selectors.Selectors;
import com.weisanju.crawler.util.JacksonUtil;
import com.weisanju.crawler.util.WebDriverUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import reactor.core.publisher.Mono;

import java.util.regex.Pattern;

public class WeiboNewsCrawler implements PageCrawler {
    static Pattern pattern = Pattern.compile("https://weibo.com/\\d+/\\w+");

    @Override
    public Mono<JsonNode> tryExtract(CrawlerContext context) {
        String url = context.getRequest().getUrl();
        if (!pattern.matcher(url).find()) {
            return null;
        }

        Mono<String> pageSourceMono = WebDriverUtil.getPageSourceReactive(url, ExpectedConditions.visibilityOfElementLocated(By.className("wbpro-feed-content")));

        return pageSourceMono.map(pageSource -> {

            ObjectNode objectNode = JacksonUtil.createObjectNode();
            objectNode.put("url", url);
            objectNode.put("content", Selectors.css(".wbpro-feed-content", pageSource, url));

            CommonCrawler.extractCommonField(objectNode, pageSource, url);

            return objectNode;
        });
    }
}

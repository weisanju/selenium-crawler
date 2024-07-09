package com.weisanju.crawler.crawlers.toutiao;

import com.weisanju.crawler.crawlers.CrawlerContext;
import com.weisanju.crawler.crawlers.common.CssSelectorPagCrawler;
import com.weisanju.crawler.selectors.Selector;
import com.weisanju.crawler.selectors.Selectors;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.HashMap;
import java.util.Map;

public class ToutiaoVideoPageCrawler extends CssSelectorPagCrawler {
    public ToutiaoVideoPageCrawler() {
        super(createSelectors());
    }


    static Map<String, Selector> createSelectors() {
        Map<String, Selector> selectors = new HashMap<>();

        selectors.put("content", Selectors.css("#root .video-detail-container video"));
        //获取第一个子元素： 2024-07-06 12:21 pattern
        selectors.put("pubTime", ToutiaoArticlePageCrawler.pubTimeSelector);
        selectors.put("author", Selectors.css(".author-info .author-name"));
        return selectors;
    }

    @Override
    protected boolean match(CrawlerContext context) {
        return context.getRequest().getUrl().startsWith("https://www.toutiao.com/video/");
    }

    @Override
    protected ExpectedCondition<WebElement> getEc() {
        return ExpectedConditions.visibilityOfElementLocated(By.id("root"));
    }
}

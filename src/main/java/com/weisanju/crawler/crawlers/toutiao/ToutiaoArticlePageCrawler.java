package com.weisanju.crawler.crawlers.toutiao;

import com.weisanju.crawler.crawlers.CrawlerContext;
import com.weisanju.crawler.crawlers.common.CssSelectorPagCrawler;
import com.weisanju.crawler.selectors.AndSelector;
import com.weisanju.crawler.selectors.RegexSelector;
import com.weisanju.crawler.selectors.Selector;
import com.weisanju.crawler.selectors.Selectors;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.HashMap;
import java.util.Map;

public class ToutiaoArticlePageCrawler extends CssSelectorPagCrawler {

    public static AndSelector pubTimeSelector = Selectors.and(
                        Selectors.css(".meta-info span,.article-meta span"),
                        Selectors.regex("<span.*?>.*?(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}).*?</span>")
            );


    public ToutiaoArticlePageCrawler() {
        super(createSelectors());
    }

    static Map<String, Selector> createSelectors() {
        Map<String, Selector> selectors = new HashMap<>();

        selectors.put("content", Selectors.css("article.tt-article-content"));
        //获取第一个子元素： 2024-07-06 12:21 pattern
        selectors.put("pubTime", pubTimeSelector);

        selectors.put("author", Selectors.css(".article-meta .name a"));

        return selectors;
    }

    @Override
    protected boolean match(CrawlerContext context) {
        return context.getRequest().getUrl().startsWith("https://www.toutiao.com/article/");
    }


    @Override
    protected ExpectedCondition<WebElement> getEc() {
        return ExpectedConditions.visibilityOfElementLocated(By.id("root"));
    }

}

package com.weisanju.crawler.crawlers.toutiao;

import com.fasterxml.jackson.databind.JsonNode;
import com.weisanju.crawler.UrlCrawlerRequest;
import com.weisanju.crawler.crawlers.CrawlerContext;
import com.weisanju.crawler.crawlers.PageCrawler;
import com.weisanju.crawler.util.JacksonUtil;
import com.weisanju.crawler.util.WebDriverUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.locators.RelativeLocator;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ToutiaoTrendingCrawler implements PageCrawler {
    @Override
    public Mono<JsonNode> tryExtract(CrawlerContext context) {

        UrlCrawlerRequest request = context.getRequest();

        if (!request.getUrl().startsWith("https://www.toutiao.com/trending/")) {
            return null;
        }

        Mono<WebDriver> webDriver = WebDriverUtil.getWebDriver();

        return webDriver.flatMap(driver -> {

            ExpectedCondition<WebElement> ec = ExpectedConditions.presenceOfElementLocated(By.cssSelector("#root"));

            WebDriverWait driverWait = new WebDriverWait(driver, Duration.ofSeconds(10));

            driverWait.until(ec);

            By relativeContent = By.xpath("//div[@class='block-title'][text()='相关内容']");

            By loadButton = By.cssSelector(".load-more button");

            RelativeLocator.RelativeBy below = RelativeLocator.with(loadButton).above(relativeContent);

            tryLocation(driver, below);

            // find elements
            List<WebElement> elements;
            try {
                elements = driver.findElements(RelativeLocator.with(By.cssSelector(".block-container a.content,.block-container a.title,.block-container p.content a")).above(relativeContent));
            } catch (NoSuchElementException ignore) {
                elements = Collections.emptyList();
            }

            List<String> urls = elements.stream().map(x -> x.getAttribute("href")).collect(Collectors.toList());

            return Mono.just(JacksonUtil.createObjectNode("urls", JacksonUtil.valueToTree(urls)));
        });
    }

    static void tryLocation(WebDriver driver, By relativeContent) {
        // # 此处替换为你所需点击元素的具体定位方法
        while (true) {
            try {
                WebElement button = driver.findElement(relativeContent);
                button.click();
                Thread.sleep(1000);
            } catch (NoSuchElementException | InterruptedException e) {
                break;
            }
        }
    }


}

package com.weisanju.crawler.crawlers.baidu;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.weisanju.crawler.crawlers.CrawlerContext;
import com.weisanju.crawler.crawlers.PageCrawler;
import com.weisanju.crawler.crawlers.common.CssSelectorPagCrawler;
import com.weisanju.crawler.selectors.Selector;
import com.weisanju.crawler.selectors.Selectors;
import com.weisanju.crawler.util.JacksonUtil;
import com.weisanju.crawler.util.JqUtil;
import com.weisanju.crawler.util.WebDriverUtil;
import lombok.extern.slf4j.Slf4j;
import net.thisptr.jackson.jq.JsonQuery;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.devtools.v126.network.Network;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class BaiduBaijiahaoCrawler implements PageCrawler {

    static Pattern scriptPathExtract = Pattern.compile("_\\w+\\((\\{.*?})\\)");

    static JsonQuery commentsJsonQuery = JqUtil.compile(".ret.list[]|{uname,area,content}");

    public BaiduBaijiahaoCrawler() {
    }

    private static Map<String, Selector> createSelectors() {
        Map<String, Selector> selectors = new HashMap<>();

        //获取第一个子元素： 2024-07-06 12:21 pattern

        selectors.put("author", Selectors.css(".author-info .author-name"));
        // css selector判断 data-testid="article"
        selectors.put("content", Selectors.css("[data-testid='article']"));
        selectors.put("pubTime", Selectors.css("[data-testid='updatetime']"));
        selectors.put("fromSource", Selectors.css("[data-testid='author-name']"));
        return selectors;
    }


    @Override
    public Mono<JsonNode> tryExtract(CrawlerContext context) {

        if (!context.getRequest().getUrl().startsWith("https://baijiahao.baidu.com/s") && !context.getRequest().getUrl().startsWith("https://mbd.baidu.com/newspage/data")) {
            return null;
        }

        Mono<WebDriver> webDriver = WebDriverUtil.getWebDriver();

        return webDriver.flatMap(driver -> {

            Flux<Tuple2<String, JsonNode>> commentsMono = fetchComments(driver, (sink) -> {

                driver.get(context.getRequest().getUrl());

                WebDriverWait driverWait = new WebDriverWait(driver, Duration.ofSeconds(10));

                //wait
                WebElement ignore = driverWait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='article']")));

                //click。点击某个元素。直到这个元素不可见 或者 限制次数，比如最多5次

                WebDriverUtil.tryClickByLocator(driver, By.xpath("//span[text()='查看更多评论']"), 10);

                sink.complete();
            }).timeout(Duration.ofSeconds(10)).onErrorReturn(Tuples.of("", NullNode.getInstance()));

            return commentsMono.onErrorReturn(Tuples.of("", NullNode.getInstance())).distinct(Tuple2::getT1).collectList().map(x -> {

                ArrayNode arrayNode = JacksonUtil.createArrayNode();

                for (Tuple2<String, JsonNode> objects : x) {
                    arrayNode.addAll((ArrayNode) objects.getT2());
                }

                String pageSource = driver.getPageSource();

                ObjectNode objectNode = CssSelectorPagCrawler.doSelector(pageSource, context.getRequest().getUrl(), createSelectors());
                objectNode.set("title", new TextNode(driver.getTitle()));
                objectNode.set("comments", arrayNode);
                return objectNode;
            });
        });
    }

    private Flux<Tuple2<String, JsonNode>> fetchComments(WebDriver webDriver, Consumer<FluxSink<Tuple2<String, JsonNode>>> afterBind) {
        return Flux.create(sink -> {
            DevTools devTools = ((HasDevTools) webDriver).getDevTools();
            devTools.createSessionIfThereIsNotOne();
            devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
            devTools.addListener(Network.responseReceived(), responseReceived -> {
                if (responseReceived.getResponse().getUrl().startsWith("https://ext.baidu.com/api/comment/v2/comment/list")) {
                    Network.GetResponseBodyResponse responseBody = devTools.send(Network.getResponseBody(responseReceived.getRequestId()));
                    try {
                        String body = responseBody.getBody();
                        Matcher matcher = scriptPathExtract.matcher(body);
                        if (matcher.find()) {
                            ArrayNode evaluate = JqUtil.evaluate(commentsJsonQuery, matcher.group(1), getClass().getSimpleName() + ".CommentsParse");
                            sink.next(Tuples.of(responseReceived.getResponse().getUrl(), evaluate));
                        }
                    } catch (Exception e) {
                        log.error("error", e);
                    }
                }
            });
            sink.onDispose(devTools::close);
            afterBind.accept(sink);
        });


    }
}

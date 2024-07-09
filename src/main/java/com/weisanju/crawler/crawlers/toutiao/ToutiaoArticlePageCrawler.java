package com.weisanju.crawler.crawlers.toutiao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.weisanju.crawler.crawlers.CrawlerContext;
import com.weisanju.crawler.crawlers.common.CssSelectorPagCrawler;
import com.weisanju.crawler.selectors.AndSelector;
import com.weisanju.crawler.selectors.Selector;
import com.weisanju.crawler.selectors.Selectors;
import com.weisanju.crawler.util.HttpClientUtil;
import com.weisanju.crawler.util.JqUtil;
import net.thisptr.jackson.jq.JsonQuery;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ToutiaoArticlePageCrawler extends CssSelectorPagCrawler {

    public static AndSelector pubTimeSelector = Selectors.and(
            Selectors.css(".meta-info span,.article-meta span"),
            Selectors.regex("<span.*?>.*?(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}).*?</span>")
    );


    final JsonQuery commentExtract;

    {
        commentExtract = JqUtil.compile(".data[]| {text:.comment.text,createTime:.comment.create_time,userName:.comment.user_name,location:.comment.publish_loc_info} ");
    }

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

    Pattern pattern = Pattern.compile("https://www.toutiao.com/article/(\\d+)/.*");


    @Override
    protected Mono<JsonNode> postProcess(ObjectNode objectNode, Document document, CrawlerContext context) {
        //获取评论
        String url = context.getRequest().getUrl();

        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {

            String group = matcher.group(1);

            String commentsUrl = String.format("https://www.toutiao.com/article/v4/tab_comments/?aid=24&app_name=toutiao_web&offset=${offset}&count=${pageSize}&group_id=%s&item_id=%s", group, group);

            //评论下载
            Mono<ArrayNode> comments = HttpClientUtil.getPage(commentsUrl, 1, 50, 10, x -> JqUtil.evaluate(commentExtract, x, getClass().getSimpleName()));

            return comments.map(x -> {
                objectNode.set("comments", x);
                return objectNode;
            });
        }
        return Mono.just(objectNode);
    }

    @Override
    protected ExpectedCondition<WebElement> getEc() {
        return ExpectedConditions.visibilityOfElementLocated(By.id("root"));
    }

}

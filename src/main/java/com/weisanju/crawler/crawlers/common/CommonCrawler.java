package com.weisanju.crawler.crawlers.common;

import cn.edu.hfut.dmic.contentextractor.ContentExtractor;
import cn.edu.hfut.dmic.contentextractor.News;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.weisanju.crawler.crawlers.CrawlerContext;
import com.weisanju.crawler.util.HttpClientUtil;
import com.weisanju.crawler.util.JacksonUtil;
import com.weisanju.crawler.crawlers.PageCrawler;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

public class CommonCrawler implements PageCrawler {
    @Override
    public Mono<JsonNode> tryExtract(CrawlerContext context) {

        String url = context.getRequest().getUrl();

        ObjectNode objectNode = JacksonUtil.createObjectNode();

        Mono<Document> documentMono = HttpClientUtil.parseHtml(url);

        return documentMono.map(doc -> {

            try {
                News newsByDoc = ContentExtractor.getNewsByDoc(doc);

                if (newsByDoc.getTitle() != null) {
                    objectNode.put("title", newsByDoc.getTitle());
                }

                if (newsByDoc.getContent() != null) {
                    objectNode.put("content", newsByDoc.getContent());
                }

                if (newsByDoc.getTime() != null) {
                    objectNode.put("pubTime", newsByDoc.getTime());
                }
            } catch (Exception ignore) {
            }

            extractCommonField(objectNode, doc);

            return objectNode;
        });
    }

    public static void extractCommonField(ObjectNode extractObj, Document document) {
        if (extractObj.get("keywords") == null) {
            extractObj.set("keywords", JacksonUtil.createArrayNode(document.select("html head meta[name=keywords]").eachAttr("content")));
        }


        if (extractObj.get("source") == null) {
            List<String> sources = document.select("html head meta[name=source]").eachAttr("content");
            //去除来源：前缀
            sources = sources.stream().map(x -> x.replaceAll("来源：\\s*", "")).collect(Collectors.toList());

            if (!sources.isEmpty()) {
                extractObj.put("source", sources.get(0));
            }
        }

        if (extractObj.get("author") == null) {
            List<String> content = document.select("html head meta[name=author]").eachAttr("content");
            if (!content.isEmpty()) {
                extractObj.put("author", content.get(0));
            }
        }

        if (extractObj.get("description") == null) {
            List<String> content = document.select("html head meta[name=description]").eachAttr("content");
            if (!content.isEmpty()) {
                extractObj.put("description", content.get(0));
            }
        }

        if (extractObj.get("title") == null) {
            extractObj.put("title", document.select("html head title").text());
        }

        if (extractObj.get("content") == null) {
            extractObj.put("title", extractionWholeText(document));
        }
    }


    private static String extractionWholeText(Document document) {
        if (document == null) {
            return null;
        }
        Element select = document.body();
        return select.wholeText();
    }
}

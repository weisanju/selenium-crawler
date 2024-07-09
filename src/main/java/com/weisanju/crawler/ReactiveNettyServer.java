package com.weisanju.crawler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.weisanju.crawler.crawlers.CrawlerContext;
import com.weisanju.crawler.crawlers.common.CommonCrawler;
import com.weisanju.crawler.crawlers.common.RoutedCrawler;
import com.weisanju.crawler.crawlers.toutiao.ToutiaoArticlePageCrawler;
import com.weisanju.crawler.crawlers.toutiao.ToutiaoTrendingCrawler;
import com.weisanju.crawler.crawlers.toutiao.ToutiaoVideoPageCrawler;
import com.weisanju.crawler.util.JacksonUtil;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.util.Arrays;
import java.util.function.BiFunction;

public class ReactiveNettyServer implements BiFunction<HttpServerRequest, HttpServerResponse, Publisher<Void>> {
    public static void main(String[] args) {
        HttpServer.create()
                .port(8080)
                .route(routes -> routes.post("/process", new ReactiveNettyServer())).bindNow()
                .onDispose()
                .block();
    }


    static RoutedCrawler matcher = new RoutedCrawler(Arrays.asList(
            new ToutiaoTrendingCrawler(),
            new ToutiaoArticlePageCrawler(),
            new ToutiaoVideoPageCrawler()
    ), new CommonCrawler());


    @Override
    public Publisher<Void> apply(HttpServerRequest httpServerRequest, HttpServerResponse response) {

        Mono<CrawlerContext> contextMono = httpServerRequest.receive().aggregate().asString().handle((x, sink) -> {
            try {
                UrlCrawlerRequest req = JacksonUtil.convert(x, UrlCrawlerRequest.class);
                CrawlerContext context = new CrawlerContext();
                context.setRequest(req);
                sink.next(context);
            } catch (JsonProcessingException e) {
                sink.error(e);
            }
        });


        Mono<String> map = contextMono.flatMap(ReactiveNettyServer::doExtract).map(JacksonUtil::toJsonString).onErrorResume(x -> {
            response.status(HttpResponseStatus.BAD_REQUEST);
            return Mono.just(x.getMessage());
        });
        return response.sendString(map);
    }

    private static Mono<JsonNode> doExtract(CrawlerContext context) {
        Mono<JsonNode> jsonNode = matcher.tryExtract(context);
        if (jsonNode != null) {
            return jsonNode.flatMap(x -> {
                JsonNode urlArray = x.get("urls");
                if (urlArray != null && urlArray.isArray()) {
                    return Flux.fromIterable(urlArray).flatMap(url -> {
                        CrawlerContext contextInner = new CrawlerContext();
                        contextInner.setRequest(new UrlCrawlerRequest(url.asText()));
                        return doExtract(contextInner);
                    }).collectList().map(JacksonUtil::createArrayNodeFromNode).cast(JsonNode.class);
                } else {
                    JsonNode otherUrls = x.get("otherUrls");
                    if (otherUrls != null && otherUrls.isArray()) {

                        return Flux.fromIterable(otherUrls).flatMap(url -> {
                            CrawlerContext contextInner = new CrawlerContext();
                            contextInner.setRequest(new UrlCrawlerRequest(url.asText()));
                            return doExtract(contextInner);
                        }).collectList().map(JacksonUtil::createArrayNodeFromNode).doOnNext(x1 -> x1.add(x)).cast(JsonNode.class);
                    }
                }
                return Mono.just(x);
            });
        }
        return Mono.empty();
    }
}

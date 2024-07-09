package com.weisanju.crawler.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.netty.handler.codec.http.HttpHeaderNames;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.io.*;
import java.time.Duration;
import java.util.function.Function;
import java.util.regex.Pattern;

@Slf4j
public class HttpClientUtil {
    static HttpClient httpClient = HttpClient.create();

    static Pattern pageNumberPattern = Pattern.compile("\\$\\{pageNumber}");

    static Pattern pageSizePattern = Pattern.compile("\\$\\{pageSize}");

    static Pattern offsetPattern = Pattern.compile("\\$\\{offset}");

    public static Mono<JsonNode> getJson(String url) {
        return httpClient.get()
                .uri(url)
                .responseSingle((response, content) -> {
                    if (response.status().code() != 200) {
                        return Mono.error(new RuntimeException("Failed to download file: " + response.status().code()));
                    }
                    return content.asString().map(JacksonUtil::convert);
                });
    }

    public static Mono<ArrayNode> getPage(String url, int pageNumber, int pageSize, int maxPages, Function<JsonNode, ArrayNode> pageExtract) {


        String s = pageNumberPattern.matcher(url).replaceAll(pageNumber + "");

        s = pageSizePattern.matcher(s).replaceAll(pageSize + "");

        //calc offset
        int offset = (pageNumber - 1) * pageSize;

        s = offsetPattern.matcher(s).replaceAll(offset + "");

        log.info("getPage: {}", s);

        Mono<ArrayNode> datas = getJson(s).map(pageExtract);
        return datas.flatMap(x -> {
            if (x.size() >= pageSize && pageNumber < maxPages) {
                return Mono.delay(Duration.ofMillis(500)).flatMap(ignore -> {
                    return getPage(url, pageNumber + 1, pageSize, maxPages, pageExtract).map(x::addAll);
                });
            }
            return Mono.just(x);
        });
    }


    public static Mono<Document> parseHtml(String url) {
        return HttpClient.create()
                .get()
                .uri(url)
                .responseSingle((response, content) -> {
                    if (response.status().code() != 200) {
                        return Mono.error(new RuntimeException("Failed to download file: " + response.status().code()));
                    }
                    return content.asInputStream()
                            .handle((inputStream, sink) -> {
                                //获取charset
                                String s = response.responseHeaders().get(HttpHeaderNames.CONTENT_TYPE);
                                String charset = null;
                                if (s != null) {
                                    int i = s.indexOf("charset=");
                                    if (i > 0) {
                                        charset = s.substring(i + 8);
                                    }
                                }
                                try {
                                    sink.next(Jsoup.parse(inputStream, charset, url));
                                } catch (IOException e) {
                                    sink.error(e);
                                }
                            });
                });
    }

    public static Mono<String> getRedirectUrl(String url) {
        return httpClient.head()
                .uri(url)
                .responseSingle((response, content) -> {
                    if (response.status().code() >= 300 && response.status().code() < 400) {
                        return Mono.just(response.responseHeaders().get(HttpHeaderNames.LOCATION));
                    }
                    return Mono.just(url);
                });
    }
}

package com.weisanju.crawler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.weisanju.crawler.crawlers.CrawlerContext;
import com.weisanju.crawler.crawlers.common.CommonCrawler;
import com.weisanju.crawler.crawlers.common.RoutedCrawler;
import com.weisanju.crawler.crawlers.toutiao.ToutiaoArticlePageCrawler;
import com.weisanju.crawler.crawlers.toutiao.ToutiaoTrendingCrawler;
import com.weisanju.crawler.crawlers.toutiao.ToutiaoVideoPageCrawler;
import com.weisanju.crawler.util.JacksonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Slf4j
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static RoutedCrawler matcher = new RoutedCrawler(Arrays.asList(
            new ToutiaoTrendingCrawler(),
            new ToutiaoArticlePageCrawler(),
            new ToutiaoVideoPageCrawler()
    ), new CommonCrawler());

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        log.error("Error", cause);


        sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if (request.method() == HttpMethod.POST) {
            if ("/process".equals(request.uri())) {
                ByteBuf content = request.content();
                String jsonStr = content.toString(StandardCharsets.UTF_8);

                UrlCrawlerRequest urlCrawlerRequest = objectMapper.readValue(jsonStr, UrlCrawlerRequest.class);


                CrawlerContext context = new CrawlerContext();
                context.setRequest(urlCrawlerRequest);

                log.info("Start Crawler");

                JsonNode jsonNode = doExtract(context);

                log.info("End Crawler");

                // 处理逻辑
                String responseMessage = objectMapper.writeValueAsString(jsonNode);

                ByteBuf bytes = Unpooled.copiedBuffer(responseMessage, StandardCharsets.UTF_8);
                FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                        HttpResponseStatus.OK,
                        bytes);
                response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
                response.headers().set(HttpHeaderNames.CONTENT_LENGTH, bytes.readableBytes());
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            } else {
                sendError(ctx, HttpResponseStatus.NOT_FOUND);
            }
        } else {
            sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED);
        }
    }

    private static JsonNode doExtract(CrawlerContext context) {
        JsonNode jsonNode = matcher.tryExtract(context);
        if (jsonNode != null) {
            JsonNode urlArray = jsonNode.get("urls");
            if (urlArray != null && urlArray.isArray()) {
                ArrayNode arrayNode = JacksonUtil.createArrayNode();

                for (JsonNode url : urlArray) {
                    CrawlerContext contextInner = new CrawlerContext();
                    contextInner.setRequest(new UrlCrawlerRequest(url.asText()));
                    arrayNode.add(doExtract(contextInner));
                }

                return arrayNode;
            } else {
                JsonNode otherUrls = jsonNode.get("otherUrls");
                if (otherUrls != null && otherUrls.isArray()) {

                    ArrayNode arrayNode = JacksonUtil.createArrayNode();

                    arrayNode.add(jsonNode);

                    for (JsonNode url : otherUrls) {

                        CrawlerContext contextInner = new CrawlerContext();

                        context.setRequest(new UrlCrawlerRequest(url.asText()));

                        arrayNode.add(doExtract(contextInner));
                    }

                    return doExtract(context);
                }
            }
        }
        return jsonNode;
    }

    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, status,
                Unpooled.copiedBuffer("Failure: " + status.toString() + "\r\n", StandardCharsets.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}

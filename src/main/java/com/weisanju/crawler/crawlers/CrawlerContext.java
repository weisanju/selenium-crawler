package com.weisanju.crawler.crawlers;

import com.weisanju.crawler.UrlCrawlerRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrawlerContext {
    private UrlCrawlerRequest request;
}

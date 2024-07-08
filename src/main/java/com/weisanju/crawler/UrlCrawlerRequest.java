package com.weisanju.crawler;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UrlCrawlerRequest {
    private String url;
    private String rule;

    public UrlCrawlerRequest(String url) {
        this.url = url;
    }

    public UrlCrawlerRequest() {
    }
}

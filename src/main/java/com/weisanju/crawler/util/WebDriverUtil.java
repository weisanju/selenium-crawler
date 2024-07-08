package com.weisanju.crawler.util;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class WebDriverUtil {


    public static String getPageSource(String url, ExpectedCondition<WebElement> ec) {
        WebDriver webDriver = WebDriverManager.borrowWebDriver();
        try {
            webDriver.get(url);
            //wait for page load
            WebDriverWait driverWait = new WebDriverWait(webDriver, Duration.ofSeconds(10));
            WebElement ignore = driverWait.until(ec);
            return webDriver.getPageSource();
        } finally {
            WebDriverManager.returnWebDriver(webDriver);
        }
    }
}

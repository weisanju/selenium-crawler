package com.weisanju.crawler.util;

import org.apache.commons.pool2.DestroyMode;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class WebDriverUtil {


    static GenericObjectPool<WebDriver> pool;

    static {
        GenericObjectPoolConfig<WebDriver> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(8);  // 最大池对象数量
        config.setMinIdle(2);    // 最小空闲对象数量
        config.setMaxIdle(6);    // 最大空闲对象数量

        WebDriverFactory factory = new WebDriverFactory();
        pool = new GenericObjectPool<>(factory, config);
    }

    public static WebDriver borrowWebDriver(long waitMills) {
        try {
            return pool.borrowObject(waitMills);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static void returnWebDriver(WebDriver driver) {
        pool.returnObject(driver);
    }


    public static Mono<WebDriver> getWebDriver() {
        return getWebDriver(0);
    }


    public static Mono<WebDriver> getWebDriver(int waitCount) {

        WebDriver webDriver = borrowWebDriver(0);

        if (webDriver == null) {

            if (waitCount > 20) {
                return Mono.error(new RuntimeException("FailedToGetWebDriver"));
            }

            return Mono.delay(Duration.ofMillis(500)).flatMap(ignore -> getWebDriver(waitCount + 1));
        }

        if (!WebDriverFactory.isValidateObject(webDriver)) {
            returnWebDriver(webDriver);

            try {
                pool.invalidateObject(webDriver);
            } catch (Exception ignore) {
            }


            return getWebDriver(waitCount);
        }


        return Mono.just(webDriver).doOnTerminate(() -> {
            returnWebDriver(webDriver);
        });
    }

    public static Mono<String> getPageSourceReactive(String url, ExpectedCondition<WebElement> ec) {
        Mono<WebDriver> webDriver = getWebDriver(0);
        return webDriver.map(driver -> {
            driver.get(url);
            //wait for page load
            WebDriverWait driverWait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement ignore = driverWait.until(ec);
            return driver.getPageSource();
        });
    }

}

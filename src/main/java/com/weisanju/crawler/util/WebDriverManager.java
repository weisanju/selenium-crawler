package com.weisanju.crawler.util;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.openqa.selenium.WebDriver;

public class WebDriverManager {
    static GenericObjectPool<WebDriver> pool;

    static {
        GenericObjectPoolConfig<WebDriver> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(10);  // 最大池对象数量
        config.setMinIdle(10);    // 最小空闲对象数量
        config.setMaxIdle(10);    // 最大空闲对象数量

        WebDriverFactory factory = new WebDriverFactory();
        pool = new GenericObjectPool<>(factory, config);
    }

    public static WebDriver borrowWebDriver() {
        try {
            return pool.borrowObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static void returnWebDriver(WebDriver driver) {
        pool.returnObject(driver);
    }
}

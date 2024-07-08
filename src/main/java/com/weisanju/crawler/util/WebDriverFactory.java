package com.weisanju.crawler.util;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;

public class WebDriverFactory implements PooledObjectFactory<WebDriver> {


    static WebDriver createWebDriver() {
        ChromeOptions options = new ChromeOptions();
        //options.addArguments("--headless");
        //options.setExperimentalOption("detach", true);
        ChromeDriverService service = new ChromeDriverService.Builder().usingPort(9225).build();
        return new ChromeDriver(service, options);
    }

    @Override
    public PooledObject<WebDriver> makeObject() throws Exception {
        return new DefaultPooledObject<>(createWebDriver());
    }

    @Override
    public void destroyObject(PooledObject<WebDriver> p) throws Exception {
        // Clean up the object
        p.getObject().close();
    }

    @Override
    public boolean validateObject(PooledObject<WebDriver> p) {
        // Validate the object
        try {
            p.getObject().getTitle();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public void activateObject(PooledObject<WebDriver> p) throws Exception {

    }

    @Override
    public void passivateObject(PooledObject<WebDriver> p) throws Exception {

    }
}

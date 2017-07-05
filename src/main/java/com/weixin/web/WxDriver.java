package com.weixin.web;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import javax.servlet.http.HttpServletResponse;
import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class WxDriver extends PhantomJSDriver {
    private final PhantomJSDriver webDriver;
    private final String id;

    public WxDriver(String phantomjsPath){
        System.setProperty("phantomjs.binary.path", phantomjsPath);
        DesiredCapabilities desiredCapabilities = DesiredCapabilities.phantomjs();
        desiredCapabilities.setCapability("applicationCacheEnabled", true);
        desiredCapabilities.setCapability("takesScreenshot", true);
        desiredCapabilities.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, phantomjsPath);
        desiredCapabilities.setCapability("phantomjs.page.settings.userAgent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
        desiredCapabilities.setCapability("phantomjs.page.customHeaders.User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
        webDriver=new PhantomJSDriver(desiredCapabilities);
        webDriver.manage().window().setSize(new Dimension(1900, 937));
        webDriver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        id= UUID.randomUUID().toString().replace("-","");
    }

    public String getId() {
        return id;
    }

    public void close() {
        webDriver.quit();
    }

    private void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private WebElement findStaleElementByCssSelector(String cssSelector) {
        try {
            return webDriver.findElementByCssSelector(cssSelector);
        } catch (StaleElementReferenceException | NoSuchElementException e) {
            System.out.println("Attempting to recover from " + e.getClass().getSimpleName() + "...");
            return findStaleElementByCssSelector(cssSelector);
        }
    }

    public byte[] getScreenshotAs() throws IOException {
        byte[] img = webDriver.getScreenshotAs(OutputType.BYTES);
        return img;

    }


    public byte[] qr() throws IOException {
        webDriver.get("https://wx.qq.com/");
        System.out.println(webDriver.getTitle());
        sleep(1000);
        byte[] img =webDriver.getScreenshotAs(OutputType.BYTES);
        return img;
    }

    private void contract() {
        int scrollLength = 200;
        webDriver.findElementByCssSelector("i.web_wechat_tab_friends").click();
        Long totalHeight = (Long) webDriver.executeScript("return $('div.J_ContactScrollBody.scrollbar-dynamic.contact_list.ng-isolate-scope.scroll-content.scroll-scrolly_visible>div').height()");
        long scrollCount = 1;
        if (totalHeight != null) scrollCount = totalHeight / scrollLength + 1;
        final Set<String> friends = new HashSet<>();
        for (int i = 0; i < scrollCount; i++) {
            webDriver.executeScript("$('div.J_ContactScrollBody.scrollbar-dynamic.contact_list.ng-isolate-scope.scroll-content.scroll-scrolly_visible').scrollTop(" + scrollLength * i + ")");
            List<WebElement> contacts = webDriver.findElementsByCssSelector("div.contact_item");
            int toIndex;
            if ("0px".equals(webDriver.executeScript("return $('div.J_ContactScrollBody.scrollbar-dynamic.contact_list.ng-isolate-scope.scroll-content.scroll-scrolly_visible>div').find('div.bottom-placeholder.ng-scope').css('height')")))
                toIndex = contacts.size();
            else
                toIndex = contacts.size() > 5 ? 5 : contacts.size();
            contacts = webDriver.findElementsByCssSelector("div.contact_item");
            contacts.subList(0, toIndex).forEach(e -> {
                String username = e.getText();
                if (friends.contains(username)) return;
                friends.add(username);
                e.click();
                findStaleElementByCssSelector("div.action_area>a.button").click();
                findStaleElementByCssSelector("i.web_wechat_tab_friends").click();
                System.out.println(e.getText());
            });
        }
    }



}

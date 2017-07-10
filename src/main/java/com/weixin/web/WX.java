package com.weixin.web;

import com.weixin.domain.MassConfig;
import com.weixin.domain.MassConfigRepository;
import com.weixin.domain.User;
import com.weixin.domain.UserRepository;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/api")
public class WX implements InitializingBean {


    @Autowired
    private ExecutorService executorService;

    @Autowired
    UserRepository userRepository;
    @Autowired
    MassConfigRepository wxConfigRepository;


    private Map<String, PhantomJSDriver> drivers = new ConcurrentHashMap<>();

    @Value("${phantomjs.path}")
    private String phantomjsPath;

    @Value("${test}")
    private boolean test;

    //private PhantomJSDriver webDriver;

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    private PhantomJSDriver getPhantomJs() {

        System.setProperty("phantomjs.binary.path", phantomjsPath);
        DesiredCapabilities desiredCapabilities = DesiredCapabilities.phantomjs();
        desiredCapabilities.setCapability("applicationCacheEnabled", true);
        desiredCapabilities.setCapability("takesScreenshot", true);
        desiredCapabilities.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, phantomjsPath);
        desiredCapabilities.setCapability("phantomjs.page.settings.userAgent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
        desiredCapabilities.setCapability("phantomjs.page.customHeaders.User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");

        return new PhantomJSDriver(desiredCapabilities);
    }


    @RequestMapping("/qr")
    @ResponseBody
    public Map<String, Object> qr(HttpServletResponse response) throws IOException {
        PhantomJSDriver webDriver = getPhantomJs();
        String id = System.currentTimeMillis() + "";
        drivers.put(id, webDriver);
        webDriver.manage().window().setSize(new Dimension(1900, 937));
        webDriver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        //webDriver.manage().timeouts().pageLoadTimeout(5, TimeUnit.SECONDS);
        executorService.execute(() -> {
            webDriver.get("https://wx.qq.com/");
            System.out.println(webDriver.getTitle());
        });

//
//        webDriver.get("https://wx.qq.com/");
//        System.out.println(webDriver.getTitle());

        Map<String, Object> result = new HashMap<>();

        String qrSrc = "/assets/i/face/output_1499406067.jpg";
        long time = 0;
        while (time < 1000 * 20) {
            try {
                WebElement qrImg = webDriver.findElement(By.cssSelector("div.qrcode>img.img"));
                if (qrImg.getAttribute("src").startsWith("https://login.weixin.qq.com/qrcode/")) {
                    qrSrc = qrImg.getAttribute("src");
                    break;
                }
            } catch (Exception e) {

            }
            System.out.println(time);
            time += 200;
            sleep(200);
        }
         //WebElement qrImg = webDriver.findElement(By.cssSelector("div.qrcode>img.img"));
        //byte[] imgqqr = qrImg.getScreenshotAs(OutputType.BYTES);
//        sleep(5000);
//        WebElement qrImg = webDriver.findElement(By.cssSelector("div.qrcode>img.img"));
//        String qrSrc = qrImg.getAttribute("src");
        result.put("id", id);
        result.put("qr", qrSrc);
        return result;
//        byte[] img = qrImg.getScreenshotAs(OutputType.BYTES);
        //response.getOutputStream().write(img);
    }


    @RequestMapping("/state/{id}")
    @ResponseBody
    public String state(@PathVariable String id, HttpServletResponse response) throws IOException {
        PhantomJSDriver webDriver = drivers.get(id);
        WebElement qrImg = webDriver.findElement(By.cssSelector("div.main"));
        return qrImg.getCssValue("display");
//        if (!"none".equalsIgnoreCase( qrImg.getCssValue("display"))) {
//            executorService.execute(()->contract());
//        }
//        return
    }

    private String getCopyright(String username) {
        User user = userRepository.findByUsername(username);
        if (user.getRole().toString().equals(User.ROLE.EMPLOYEE.toString())) {
            return getCopyright(user.getEmployer());
        } else if (user.getRole().toString().equals(User.ROLE.USER.toString())) {
            if (StringUtils.isEmpty(user.getParent())) {
                return user.getCopyright();
            } else {
                return getCopyright(user.getParent());
            }
        }
        return "";
    }

    @RequestMapping("/contact/{id}")
    @ResponseBody
    public String contact2(@PathVariable String id, @RequestBody MassConfig wxConfig, HttpServletRequest request, HttpServletResponse response) throws IOException {

        PhantomJSDriver webDriver = drivers.get(id);
        executorService.execute(() -> contract(webDriver, wxConfig));
        return "accept";
    }

    @RequestMapping("/contact/{id}/{wxConfigId}")
    @ResponseBody
    public String contact(@PathVariable String id, @PathVariable Integer wxConfigId, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getUserPrincipal().getName();
        String copyright = getCopyright(username);
        PhantomJSDriver webDriver = drivers.get(id);
        final MassConfig wxConfig = wxConfigRepository.findOne(wxConfigId);
        wxConfig.setText(wxConfig.getText() + "\r\n--" + copyright);
        executorService.execute(() -> contract(webDriver, wxConfig));
        return "accept";
    }

    private void contract(PhantomJSDriver webDriver, MassConfig wxConfig) {
        try {
            sleep(2000);
            int scrollLength = 200;
            webDriver.findElementByCssSelector("i.web_wechat_tab_friends").click();
            Long totalHeight = (Long) webDriver.executeScript("return $('div.J_ContactScrollBody.scrollbar-dynamic.contact_list.ng-isolate-scope.scroll-content.scroll-scrolly_visible>div').height()");
            long scrollCount = 1;
            if (totalHeight != null) scrollCount = totalHeight / scrollLength + 1;
            final Set<String> friends = new HashSet<>();
            for (int i = 0; i < scrollCount; i++) {
                sleep(100);
                webDriver.executeScript("$('div.J_ContactScrollBody.scrollbar-dynamic.contact_list.ng-isolate-scope.scroll-content.scroll-scrolly_visible').scrollTop(" + scrollLength * i + ")");
                List<WebElement> contacts = webDriver.findElementsByCssSelector("div.contact_item");
                int toIndex;
                if ("0px".equals(webDriver.executeScript("return $('div.J_ContactScrollBody.scrollbar-dynamic.contact_list.ng-isolate-scope.scroll-content.scroll-scrolly_visible>div').find('div.bottom-placeholder.ng-scope').css('height')")))
                    toIndex = contacts.size();
                else
                    toIndex = contacts.size() > 5 ? 5 : contacts.size();
                contacts = webDriver.findElementsByCssSelector("div.contact_item");
                contacts.subList(0, toIndex).forEach(e -> {
                    try {
                        String username = e.getText();
                        if (friends.contains(username)) return;
                        friends.add(username);
                        e.click();
                        findStaleElementByCssSelector(webDriver, "div.action_area>a.button").click();
                        findStaleElementByCssSelector(webDriver, "i.web_wechat_tab_friends").click();
                        System.out.println(e.getText());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            }
            chat(webDriver, wxConfig);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            logout(webDriver);
            webDriver.quit();
        }
    }

    private WebElement findStaleElementByCssSelector(PhantomJSDriver webDriver, String cssSelector) {
        try {
            return webDriver.findElementByCssSelector(cssSelector);
        } catch (StaleElementReferenceException | NoSuchElementException e) {
            System.out.println("Attempting to recover from " + e.getClass().getSimpleName() + "...");
            return findStaleElementByCssSelector(webDriver, cssSelector);
        }
    }

    private void chat(PhantomJSDriver webDriver, final MassConfig wxConfig) {

        List<File> imgFiles = new ArrayList<>();
        try {
            if(!StringUtils.isEmpty(wxConfig.getImage())) {
                String[] images = wxConfig.getImage().split("\t");
                for (String img : images) {
                    if (StringUtils.isEmpty(img.trim())) continue;
                    File imgFile = new File(UUID.randomUUID().toString().replaceAll("-", "") + ".jpg");
                    FileUtils.writeByteArrayToFile(imgFile, Base64.getDecoder().decode(images[0]));
                    imgFiles.add(imgFile);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        int scrollLength = 200;
        webDriver.findElementByCssSelector("i.web_wechat_tab_chat").click();
        Long totalHeight = (Long) webDriver.executeScript("return $('div.chat_list.scrollbar-dynamic.scroll-content.scroll-scrolly_visible>div').height()");
        long scrollCount = 1;
        if (totalHeight != null) scrollCount = totalHeight / scrollLength + 1;

        final Set<String> chatnames = new HashSet<>();
        for (int i = 0; i < scrollCount; i++) {
            webDriver.executeScript("$('div.chat_list.scrollbar-dynamic.scroll-content.scroll-scrolly_visible').scrollTop(" + scrollLength * i + ")");
            List<WebElement> chats = webDriver.findElementsByCssSelector("div.chat_item");
            int toIndex;
            if ("0px".equals(webDriver.executeScript("return $('div.chat_list.scrollbar-dynamic.scroll-content.scroll-scrolly_visible>div').find('div.bottom-placeholder.ng-scope').css('height')")))
                toIndex = chats.size();
            else
                toIndex = chats.size() > 5 ? 5 : chats.size();
            chats.subList(0, toIndex).forEach(e -> {
                try {
                String username = e.getAttribute("data-username");
                if (chatnames.contains(username)) return;
                chatnames.add(username);
                    if (username.startsWith("@@")) return;

                    System.out.println(e.getText());
                    e.click();
                    webDriver.executeScript("$('input[type=file]').removeClass('webuploader-element-invisible')");
                    webDriver.executeScript("$('input[type=file]').parent().css('width','300px')");

                    if (test) {
                        if (e.getText().equals("文件传输助手") || e.getText().equals("File Transfer")) {
                            for (File imgFile : imgFiles) {
                                webDriver.executePhantomJS("var page = this;page.uploadFile('input[type=file]','" + imgFile.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\\\\\\\\\") + "');");
                                findStaleElementByCssSelector(webDriver, "a.btn.btn_send").click();
                            }
                            findStaleElementByCssSelector(webDriver, "#editArea").click();
                            findStaleElementByCssSelector(webDriver, "#editArea").sendKeys(wxConfig.getText());
                            findStaleElementByCssSelector(webDriver, "a.btn.btn_send").click();
                        }
                    } else {
                        for (File imgFile : imgFiles) {
                            webDriver.executePhantomJS("var page = this;page.uploadFile('input[type=file]','" + imgFile.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\\\\\\\\\") + "');");
                            findStaleElementByCssSelector(webDriver, "a.btn.btn_send").click();
                        }
                        findStaleElementByCssSelector(webDriver, "#editArea").click();
                        findStaleElementByCssSelector(webDriver, "#editArea").sendKeys(wxConfig.getText());
                        findStaleElementByCssSelector(webDriver, "a.btn.btn_send").click();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            });
        }
    }

    private void logout(PhantomJSDriver webDriver) {
        try {
            webDriver.executeScript("$('.web_wechat_add').click()");
            sleep(500);
            webDriver.executeScript("$('#mmpop_system_menu li:last>a').click()");
        } catch (Exception e) {
        }

    }

    @Scheduled(fixedRate = 5000) //通过@Scheduled声明该方法是计划任务，使用fixedRate属性每隔固定时间执行
    public void reportCurrentTime() {
        for (String key : drivers.keySet()) {
            long timestamp = Long.valueOf(key);
            if (System.currentTimeMillis() - timestamp > 1000 * 60 * 5) {
                drivers.get(key).quit();
                drivers.remove(key);
            }
        }
    }

//    @RequestMapping("/4")
//    public void run4(HttpServletResponse response) throws IOException {
//        webDriver.get("http://blog.sina.com.cn/s/blog_539a70d30101ajsg.html");
//        //findStaleElementByCssSelector("input[type=file]").sendKeys("C:\\Users\\XQL\\Pictures\\-353371.png");
//        webDriver.executePhantomJS("var page = this;page.uploadFile('input[type=file]','C:\\\\Users\\\\XQL\\\\Pictures\\\\-353371.png');");
//
//        byte[] img = webDriver.getScreenshotAs(OutputType.BYTES);
//        response.getOutputStream().write(img);
//    }


    private void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

package com.weixin.web;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.weixin.domain.MassConfig;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URI;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WebWeixin {

    final CloseableHttpClient httpClient;
    final ObjectMapper objectMapper = new ObjectMapper();

    private String deviceID;
    private String qrLoginUUID;
    private boolean scan = false;
    private String skey;
    private String wxsid;
    private String wxuin;
    private String pass_ticket;
    private String host;
    private Map<String, Object> user;
    private Map<String, Object> contracts;
    private List<Map<String, Object>> userContactList;

    public boolean isScan() {
        return scan;
    }

    public Map<String, Object> getUser() {
        return user;
    }

    public WebWeixin() {
        httpClient = HttpClientBuilder.create().build();
        deviceID = "e" + RandomStringUtils.randomAlphanumeric(15);
    }

    public void close() {
        if (httpClient != null)
            HttpClientUtils.closeQuietly(httpClient);
    }

    private String baseRequest() {
        return String.format("{\"Uin\":%s,\"Sid\":\"%s\",\"Skey\":\"%s\",\"DeviceID\":\"%s\"}", wxuin, wxsid, skey, deviceID);
    }


    public String qrGen() throws IOException {
        //https://login.wx.qq.com/jslogin?appid=wx782c26e4c19acffb&redirect_uri=https%3A%2F%2Fwx.qq.com%2Fcgi-bin%2Fmmwebwx-bin%2Fwebwxnewloginpage&fun=new&lang=zh_CN&_=1500443229176
        String url = "https://login.wx.qq.com/jslogin?appid=wx782c26e4c19acffb&redirect_uri=https%3A%2F%2Fwx.qq.com%2Fcgi-bin%2Fmmwebwx-bin%2Fwebwxnewloginpage&fun=new&lang=zh_CN&_=" + tm13();
        String responseText = EntityUtils.toString(httpClient.execute(new HttpGet(url)).getEntity(), "UTF-8");
        String qrLoginCode = getQRLoginCode(responseText);
        qrLoginUUID = getQRLoginUUID(responseText);
        System.out.println(qrLoginCode);
        System.out.println(qrLoginUUID);
        System.out.println("https://login.weixin.qq.com/qrcode/" + qrLoginUUID);
        return "https://login.weixin.qq.com/qrcode/" + qrLoginUUID;
    }


    public void waitForScan() {
        try {
            String code = "unkonwn";
            int tip = 1;
            String redirectUri = "unkonwn";
            do {
                String url2 = String.format("https://login.weixin.qq.com/cgi-bin/mmwebwx-bin/login?tip=%s&uuid=%s&_=%s", tip, qrLoginUUID, new Date().getTime());
                //String responseText2 = HttpsSSLClient.doGet(url2, "UTF-8");
                String responseText2 = EntityUtils.toString(httpClient.execute(new HttpGet(url2)).getEntity(), "UTF-8");
                System.out.println(responseText2);
                code = getWindowCode(responseText2);
                if ("201".equals(code)) {
                    tip = 0;
                } else {
                    tip = 1;
                }
                if ("200".equals(code)) {
                    redirectUri = getRedirectUri(responseText2);
                    URI uri = new URI(redirectUri);
                    host = uri.getHost();
                }
            } while (!code.equals("200"));
            System.out.println(redirectUri);

            String responseText3 = EntityUtils.toString(httpClient.execute(new HttpGet(redirectUri + "&fun=new")).getEntity(), "UTF-8");
            //String responseText3 = HttpsSSLClient.doGet(redirectUri + "&fun=new", "UTF-8");
            Document document = Jsoup.parse(responseText3);
            skey = document.select("skey").text();
            wxsid = document.select("wxsid").text();
            wxuin = document.select("wxuin").text();
            pass_ticket = document.select("pass_ticket").text();
            scan = true;
            System.out.println(responseText3);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void initUser() throws IOException {
        //https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxinit?r=-1499852191&lang=zh_CN&pass_ticket=rwiYtaf62C786zv7yx%252F41yX2c%252FMyDoR1J%252FcuL1S3PeNaXgCSkmB3%252BKLR9f7dXwe2
        String url3 = String.format("https://" + host + "/cgi-bin/mmwebwx-bin/webwxinit?r=-%s&lang=zh_CN&pass_ticket=%s", tm10(), pass_ticket);
        System.out.println(url3);
        HttpPost post = new HttpPost(url3);
        post.addHeader("Origin", "https://wx2.qq.com");
        post.addHeader("Referer", "https://wx2.qq.com/");
        post.addHeader("Content-Type", "application/json;charset=UTF-8");
        post.addHeader("Accept", "application/json, text/plain, */*");
        post.addHeader("Accept-Encoding", "gzip, deflate, br");
        post.addHeader("Accept-Language", "zh-CN,zh;q=0.8,de;q=0.6");
        post.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36");
        post.setEntity(new StringEntity(String.format("{\"BaseRequest\":%s}", baseRequest())));

        String s = EntityUtils.toString(httpClient.execute(post).getEntity(), "utf-8");
        Map<String, Object> wxInits = objectMapper.readValue(s, Map.class);
        user = (Map<String, Object>) wxInits.get("User");
        userContactList = (List<Map<String, Object>>) wxInits.get("ContactList");
        //System.out.println(s);
    }

    public List<Map<String, Object>> getContracts() throws IOException {

        String url4 = String.format("https://" + host + "/cgi-bin/mmwebwx-bin/webwxgetcontact?lang=zh_CN&seq=%s&pass_ticket=%s&skey=%s&r=%s", 0, pass_ticket, skey, tm10());
        System.out.println(url4);
        HttpPost post = new HttpPost(url4);
        post.addHeader("Origin", "https://wx2.qq.com");
        post.addHeader("Referer", "https://wx2.qq.com/");
        post.addHeader("Content-Type", "application/json;charset=UTF-8");
        post.addHeader("Accept", "application/json, text/plain, */*");
        post.addHeader("Accept-Encoding", "gzip, deflate, br");
        post.addHeader("Accept-Language", "zh-CN,zh;q=0.8,de;q=0.6");
        post.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36");
        post.setEntity(new StringEntity("{}"));
        String s = EntityUtils.toString(httpClient.execute(post).getEntity(), "utf-8");
        //System.out.println(s);
        contracts = objectMapper.readValue(s, Map.class);
        List<Map<String, Object>> memberList = (List<Map<String, Object>>) contracts.get("MemberList");
        memberList = memberList.stream().filter(m -> userContactList.stream().anyMatch(u -> u.get("UserName").equals(m.get("UserName")))).collect(Collectors.toList());
        memberList.addAll(userContactList);
        List<Map<String, Object>> friends = memberList.stream().filter(m -> ((int) m.get("VerifyFlag")) == 0 && m.get("UserName").toString().startsWith("@")).collect(Collectors.toList());
        //List<String> friendNames = friends.stream().map(m -> (String) m.get("UserName")).collect(Collectors.toList());
        return friends;
        // sendImage(friendNames);
        //System.out.println("contracts");
        //sendText(friendNames);
    }

    public void logout() throws IOException {
        String url = String.format("https://" + host + "/cgi-bin/mmwebwx-bin/webwxlogout?redirect=1&type=0&skey=%s", skey);
        HttpPost post = new HttpPost(url);
        post.addHeader("Origin", "https://wx2.qq.com");
        post.addHeader("Referer", "https://wx2.qq.com/");
        post.addHeader("Content-Type", "application/x-www-form-urlencoded");
        post.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        post.addHeader("Accept-Encoding", "gzip, deflate, br");
        post.addHeader("Accept-Language", "zh-CN,zh;q=0.8,de;q=0.6");
        post.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36");
        post.setEntity(new StringEntity(String.format("sid=%s&uin=%s", wxsid, wxuin)));
        EntityUtils.consume(httpClient.execute(post).getEntity());
    }


    public void sendText(List<Map<String, Object>> friends, MassConfig wxConfig, Consumer<Map<String, Object>> toDB) throws IOException {

        friends.forEach(friend -> {
            if (!wxConfig.isQun() && friend.get("UserName").toString().startsWith("@@")) return;
            sendText(wxConfig, friend);
            toDB.accept(friend);
            sleep(500);
        });
    }


    public void sendImage(List<Map<String, Object>> friends, MassConfig wxConfig, Consumer<Map<String, Object>> toDB) throws IOException {
        Map<String, Object> upload = uploadImage(wxConfig);
        friends.forEach(friend -> {
            if (!wxConfig.isQun() && friend.get("UserName").toString().startsWith("@@")) return;
            sendImage(upload, friend);
            toDB.accept(friend);
            sleep(8000);

        });
    }

    public void sendTextImage(List<Map<String, Object>> friends, MassConfig wxConfig, Consumer<Map<String, Object>> toDB) throws IOException {
        Map<String, Object> upload = uploadImage(wxConfig);
        friends.forEach(friend -> {
            if (!wxConfig.isQun() && friend.get("UserName").toString().startsWith("@@")) return;
            sendImage(upload, friend);
            sendText(wxConfig, friend);
            toDB.accept(friend);
            sleep(8000);

        });
    }


    private void sendText(MassConfig wxConfig, Map<String, Object> friend) {
        String url = String.format("https://" + host + "/cgi-bin/mmwebwx-bin/webwxsendmsg?lang=zh_CN&pass_ticket=%s", pass_ticket);
        String msgId = tm17();
        String body = String.format("{\"BaseRequest\":%s,\"Msg\":{\"Type\":1,\"Content\":\"%s\",\"FromUserName\":\"%s\",\"ToUserName\":\"%s\",\"LocalID\":\"%s\",\"ClientMsgId\":\"%s\"},\"Scene\":0}",
                baseRequest(), friend.get("NickName") + "," + wxConfig.getText(), user.get("UserName"), friend.get("UserName"), msgId, msgId);
        HttpPost post = new HttpPost(url);
        post.addHeader("Origin", "https://wx2.qq.com");
        post.addHeader("Referer", "https://wx2.qq.com/");
        post.addHeader("Content-Type", "application/json;charset=UTF-8");
        post.addHeader("Accept", "application/json, text/plain, */*");
        post.addHeader("Accept-Encoding", "gzip, deflate, br");
        post.addHeader("Accept-Language", "zh-CN,zh;q=0.8,de;q=0.6");
        post.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36");
        post.setEntity(new StringEntity(body, "utf-8"));
        try {
            String s = EntityUtils.toString(httpClient.execute(post).getEntity(), "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void sendImage(Map<String, Object> upload, Map<String, Object> friend) {
        String url = String.format("https://" + host + "/cgi-bin/mmwebwx-bin/webwxsendmsgimg?fun=async&f=json&lang=zh_CN&pass_ticket=%s", pass_ticket);
        String msgId = tm17();
        //{"BaseRequest":{"Uin":257904755,"Sid":"3R6kAV7dVW+SsZwY","Skey":"@crypt_6e9c4eaa_24e69c4bee5cd3d37a1c7c393b6975da","DeviceID":"e175917857574295"},"Msg":{"Type":3,"MediaId":"@crypt_4aae3693_503217ba1f10d451d85cfff07b2ea3f8c69181d07e42a8afa4def13ae5bf142036ba4124b1e14cde83fed4c1227bb903ca12bfec38eac3fe0a23204ae709a87493e11be84d8a044af2022a0a47fb8ec2e0a37b494aeb8407498cca0ff27534ef1d2d9c9e8c0436b5aff91dff260b7e645aaed304be9fbd0ef44c9cd1f8d8253b49b95f2397aeb86ed108c0fd06bb0fe234f6441e0045d795de5e1ca0fde2567467f7f062d6f84960c8f20dec25e80955151a7eeb79ba3a4d8a77c7c767c30a28d935737025d6c48b7adca08d1d083b5eabac9708db042325f00d13874973f7db9d9862395d7cbb8ee2d601d527cbc9e6d7203f901680348580c11dd229fecf74e29887c57c87b6a50fb4ea5dcd4ad861c3ff92aee136f1ff1ab8b6b2c337ccca4fda0f606b461772f8584a7d210b5859","Content":"","FromUserName":"@7e8d672b913fd9e0cc562a68d081e0d6","ToUserName":"filehelper","LocalID":"15004345902150675","ClientMsgId":"15004345902150675"},"Scene":0}
        String body = String.format("{\"BaseRequest\":%s,\"Msg\":{\"Type\":3,\"MediaId\":\"%s\",\"Content\":\"\",\"FromUserName\":\"%s\",\"ToUserName\":\"%s\",\"LocalID\":\"%s\",\"ClientMsgId\":\"%s\"},\"Scene\":2}"
                , baseRequest(), upload.get("MediaId"), user.get("UserName"), friend.get("UserName"), msgId, msgId);
        HttpPost post = new HttpPost(url);
        post.addHeader("Origin", "https://wx2.qq.com");
        post.addHeader("Referer", "https://wx2.qq.com/");
        post.addHeader("Content-Type", "application/json;charset=UTF-8");
        post.addHeader("Accept", "application/json, text/plain, */*");
        post.addHeader("Accept-Encoding", "gzip, deflate, br");
        post.addHeader("Accept-Language", "zh-CN,zh;q=0.8,de;q=0.6");
        post.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36");
        post.setEntity(new StringEntity(body, "utf-8"));
        try {
            String s = EntityUtils.toString(httpClient.execute(post).getEntity(), "utf-8");
            Map<String, Object> response = objectMapper.readValue(s, Map.class);
            Map<String, Object> baseResponse = (Map<String, Object>) response.get("BaseResponse");
            if (((int) baseResponse.get("Ret")) == 1205) {
                System.out.println(new Date() + ":图片发送失败1205");
            } else {
                System.out.println(new Date() + ":图片发送成功");
            }
            System.out.println(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Object> uploadImage(MassConfig wxConfig) throws IOException {
        String url = "https://file." + host + "/cgi-bin/mmwebwx-bin/webwxuploadmedia?f=json";
        HttpPost post = new HttpPost(url);
        byte[] img = Base64.getUrlDecoder().decode(wxConfig.getImage());
        //File img = new File("C:\\Users\\XQL\\Pictures\\weixin\\logo.png");
        //{"UploadType":2,"BaseRequest":{"Uin":257904755,"Sid":"3R6kAV7dVW+SsZwY","Skey":"@crypt_6e9c4eaa_24e69c4bee5cd3d37a1c7c393b6975da","DeviceID":"e392294948459375"},"ClientMediaId":1500434590213,"TotalLen":2991,"StartPos":0,"DataLen":2991,"MediaType":4,"FromUserName":"@7e8d672b913fd9e0cc562a68d081e0d6","ToUserName":"filehelper","FileMd5":"a6ee1f3a3468cb46c61a5ebc9b4f05e2"}
        String md5 = md5(img);
        String fileName = md5 + ".png";
        String uploadmediarequest = String.format("{\"UploadType\":2,\"BaseRequest\":%s,\"ClientMediaId\":%s,\"TotalLen\":%s,\"StartPos\":0,\"DataLen\":%s,\"MediaType\":4,\"FromUserName\":\"%s\",\"ToUserName\":\"%s\",\"FileMd5\":\"%s\"}"
                , baseRequest(), tm13(), img.length, img.length, user.get("UserName"), "filehelper", md5);
        HttpEntity entity = MultipartEntityBuilder.create()
                .addPart("id", new StringBody("WU_FILE_0", ContentType.DEFAULT_TEXT))
                .addPart("name", new StringBody(fileName, ContentType.DEFAULT_TEXT))
                .addPart("type", new StringBody("image/png", ContentType.DEFAULT_TEXT))
                .addPart("lastModifiedDate", new StringBody(jsNow(), ContentType.DEFAULT_TEXT))
                .addPart("size", new StringBody(img.length + "", ContentType.DEFAULT_TEXT))
                .addPart("mediatype", new StringBody("pic", ContentType.DEFAULT_TEXT))
                .addPart("uploadmediarequest", new StringBody(uploadmediarequest, ContentType.DEFAULT_TEXT))
                .addPart("webwx_data_ticket", new StringBody(pass_ticket, ContentType.DEFAULT_TEXT))
                .addPart("pass_ticket", new StringBody(pass_ticket, ContentType.DEFAULT_TEXT))
                .addBinaryBody("filename", img, ContentType.APPLICATION_OCTET_STREAM, fileName).build();
        post.setEntity(entity);
        String s = EntityUtils.toString(httpClient.execute(post).getEntity(), "utf-8");

        //System.out.println(s);
        //map.get("MediaId")
        return objectMapper.readValue(s, Map.class);
    }


    private String getQRLoginCode(String s) {
        Pattern p = Pattern.compile("window.QRLogin.code = (\\d+)?;");
        Matcher matcher = p.matcher(s);
        matcher.find();
        return matcher.group(1);

    }

    private String getQRLoginUUID(String s) {
        Pattern p = Pattern.compile("window.QRLogin.uuid = \"([\\w|\\W]+)?\";");
        Matcher matcher = p.matcher(s);
        matcher.find();
        return matcher.group(1);

    }

    private String getWindowCode(String s) {
        Pattern p = Pattern.compile("window.code=([\\d]+)?;");
        Matcher matcher = p.matcher(s);
        matcher.find();
        return matcher.group(1);

    }

    private String getRedirectUri(String s) {
        Pattern p = Pattern.compile("window.redirect_uri=\"([\\w|\\W]+)?\";");
        Matcher matcher = p.matcher(s);
        matcher.find();
        return matcher.group(1);

    }


    private String tm17() {
        return (new Date().getTime() + RandomStringUtils.randomNumeric(4));
    }

    private String tm13() {
        return (new Date().getTime() + "");
    }

    private String tm10() {
        return (new Date().getTime() + "").substring(0, 10);
    }

    private String jsNow() {
        String[] d = new Date().toString().split(" ");
        return String.format("%s %s %s %s %s GMT+0800 (中国标准时间)", d[0], d[1], d[2], d[5], d[3]);
    }

    private String md5(byte[] file) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            return bytesToHexString(md5.digest(file));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    private void sleep(long s) {
        try {
            Thread.sleep(s);
        } catch (InterruptedException e) {

        }
    }


}

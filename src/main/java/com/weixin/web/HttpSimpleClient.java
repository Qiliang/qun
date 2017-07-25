package com.weixin.web;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.net.URI;

public class HttpSimpleClient {
    private final CookieStore cookieStore = new BasicCookieStore();
    private final HttpClientConnectionManager connMrg = new BasicHttpClientConnectionManager();
    private final CloseableHttpClient httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).setConnectionManager(connMrg).build();
    //private final HttpGet httpGet = new HttpGet();
    //private final HttpPost httpPost = new HttpPost();


    public HttpSimpleClient() {
        //connMrg.closeIdleConnections();
    }

    public void close() {
        HttpClientUtils.closeQuietly(httpClient);
    }

    public String get(String url) {
        final HttpGet httpGet = new HttpGet();
        CloseableHttpResponse response = null;
        try {
            httpGet.setURI(new URI(url));
            response = httpClient.execute(httpGet);
            String s = EntityUtils.toString(response.getEntity(), "utf-8");
            response.close();
            return s;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            HttpClientUtils.closeQuietly(response);
            httpGet.reset();
        }

    }

    public Cookie webwx_data_ticket() {
        return cookieStore.getCookies().stream().filter(c -> c.getName().equals("webwx_data_ticket")).findAny().get();
    }

    public String postMultipart(String url, HttpEntity httpEntity) {
        final HttpPost httpPost = new HttpPost();
        CloseableHttpResponse response = null;
        try {
            httpPost.setURI(new URI(url));
            httpPost.setEntity(httpEntity);
            httpPost.addHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36");
            httpPost.addHeader("Origin", "https://wx2.qq.com");
            httpPost.addHeader("Referer", "https://wx2.qq.com/");
            httpPost.addHeader("Accept", "*/*");
            httpPost.addHeader("Accept-Encoding", "gzip, deflate, br");
            httpPost.addHeader("Accept-Language", "zh-CN,zh;q=0.8,de;q=0.6");
            response = httpClient.execute(httpPost);
            String s = EntityUtils.toString(response.getEntity(), "utf-8");
            response.close();
            return s;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            HttpClientUtils.closeQuietly(response);
            httpPost.reset();
        }
    }

    public String postJson(String url, HttpEntity httpEntity) {
        final HttpPost httpPost = new HttpPost();
        httpPost.completed();
        CloseableHttpResponse response = null;
        try {
            httpPost.setURI(new URI(url));
            httpPost.addHeader("Origin", "https://wx2.qq.com");
            httpPost.addHeader("Referer", "https://wx2.qq.com/");
            httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
            httpPost.addHeader("Accept", "application/json, text/plain, */*");
            httpPost.addHeader("Accept-Encoding", "gzip, deflate, br");
            httpPost.addHeader("Accept-Language", "zh-CN,zh;q=0.8,de;q=0.6");
            httpPost.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36");
            httpPost.setEntity(httpEntity);
            response = httpClient.execute(httpPost);
            String s = EntityUtils.toString(response.getEntity(), "utf-8");
            response.close();
            return s;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            HttpClientUtils.closeQuietly(response);
            httpPost.reset();
        }
    }

    public String postFormUrlEncoded(String url, HttpEntity httpEntity) {
        HttpPost httpPost = new HttpPost();
        CloseableHttpResponse response = null;
        try {
            httpPost.setURI(new URI(url));
            httpPost.addHeader("Origin", "https://wx2.qq.com");
            httpPost.addHeader("Referer", "https://wx2.qq.com/");
            httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
            httpPost.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
            httpPost.addHeader("Accept-Encoding", "gzip, deflate, br");
            httpPost.addHeader("Accept-Language", "zh-CN,zh;q=0.8,de;q=0.6");
            httpPost.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36");
            httpPost.setEntity(httpEntity);
            response = httpClient.execute(httpPost);
            String s = EntityUtils.toString(response.getEntity(), "utf-8");
            response.close();
            return s;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            HttpClientUtils.closeQuietly(response);
            httpPost.reset();
        }
    }
}

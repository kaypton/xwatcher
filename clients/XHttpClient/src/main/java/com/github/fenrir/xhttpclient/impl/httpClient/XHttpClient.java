package com.github.fenrir.xhttpclient.impl.httpClient;

import com.github.fenrir.xhttpclient.Client;
import com.github.fenrir.xhttpclient.XHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

public class XHttpClient implements Client {
    private static final Logger LOGGER = LoggerFactory.getLogger(XHttpClient.class);
    private final String host;
    private HttpClient client;

    private XHttpClient(String host){
        this.host = host;
    }

    private HttpClient getClient() {
        return client;
    }

    private void setClient(HttpClient client) {
        this.client = client;
    }

    private void setHeaders(Map<String, String> headers, HttpRequest.Builder builder){
        if(headers != null){
            for(String key : headers.keySet()){
                builder.setHeader(key, headers.get(key));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> HttpResponse<T> send(HttpRequest request, Class<T> clazz){
        try {
            HttpResponse.BodyHandler<T> bodyHandler;
            if (String.class.equals(clazz)) {
                bodyHandler = (HttpResponse.BodyHandler<T>) HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
            }else if(InputStream.class.equals(clazz)){
                bodyHandler = (HttpResponse.BodyHandler<T>) HttpResponse.BodyHandlers.ofInputStream();
            } else{
                LOGGER.error("return type wrong");
                return null;
            }
            HttpResponse<T> resp = this.client.send(request, bodyHandler);
            LOGGER.info("{} {} {}", request.method(), request.uri(), resp.statusCode());
            return resp;
        } catch (IOException | InterruptedException e) {
            LOGGER.error("{} failed", request.method());
            e.printStackTrace();
            return null;
        }
    }

    public <T> XHttpResponse<T> get(String url,
                                    Map<String, String> headers,
                                    Map<String, String> params,
                                    String body,
                                    Class<T> clazz){
        HttpResponse<T> response = this._get(url, headers, params, body, clazz);
        return XHttpResponse.Builder.newBuilder(clazz)
                .statusCode(response.statusCode())
                .body(response.body())
                .build();
    }

    private <T> HttpResponse<T> _get(String url,
                                     Map<String, String> headers,
                                     Map<String, String> params,
                                     String body,
                                     Class<T> clazz){
        boolean first = true;
        if(params != null){
            StringBuilder urlBuilder = new StringBuilder(url);
            for(String key : params.keySet()){
                String pair = key + "=" + URLEncoder.encode(params.get(key), StandardCharsets.UTF_8);
                urlBuilder.append(first ? ("?" + pair) : ("&" + pair));
                if(first){
                    first = false;
                }
            }
            url = urlBuilder.toString();
        }
        url = this.host + url;
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(1));
        this.setHeaders(headers, builder);
        HttpRequest request;
        if(body != null)
            request = builder.method("GET", HttpRequest.BodyPublishers.ofString(body)).build();
        else request = builder.GET().build();
        return this.send(request, clazz);
    }

    public static XHttpClient create(String host){
        XHttpClient client = new XHttpClient(host);
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
        client.setClient(httpClient);
        return client;
    }

    public static void main(String[] args){
        XHttpClient client = XHttpClient.create("http://www.baidu.com");

        XHttpResponse<String> response = client.get("/", null, null, "hello", String.class);
        System.out.println(response.body());
    }
}

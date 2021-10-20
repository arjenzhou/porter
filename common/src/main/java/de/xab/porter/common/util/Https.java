package de.xab.porter.common.util;

import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * http utils
 */
public final class Https {
    private static final int CONNECTION_TIMEOUT = 3;
    private static final int SOCKET_TIMEOUT = 5;
    private static final int MAX_RECURSIVE_DEPTH = 5;
    private static final MediaType JSON = MediaType.Companion.parse("application/json; charset=utf-8");
    private static final Interceptor INTERCEPTOR = new PorterNetworkInterceptor();
    private static final OkHttpClient CLIENT = new OkHttpClient.Builder().
            connectTimeout(CONNECTION_TIMEOUT, TimeUnit.MINUTES).
            readTimeout(SOCKET_TIMEOUT, TimeUnit.MINUTES).
            writeTimeout(SOCKET_TIMEOUT, TimeUnit.MINUTES).
            addNetworkInterceptor(INTERCEPTOR).
            followRedirects(false).
            build();

    private Https() {
    }

    public static <T> T get(String url, Map<String, String> header, Class<T> clazz) {
        return Jsons.fromJson(get(url, header), clazz);
    }

    public static <T> T get(String url, Map<String, String> header, TypeReference<T> type) {
        return Jsons.fromJson(get(url, header), type);
    }

    private static String get(String url, Map<String, String> header) {
        Request request = new Request.Builder().
                url(url).
                headers(Headers.Companion.of(header)).
                build();
        return request(request);
    }

    public static <T> T post(String url, Map<String, String> header, Object body, Class<T> clazz) {
        return Jsons.fromJson(post(url, header, body), clazz);
    }

    public static <T> T post(String url, Map<String, String> header, Object body, TypeReference<T> type) {
        return Jsons.fromJson(post(url, header, body), type);
    }

    private static String post(String url, Map<String, String> header, Object body) {
        RequestBody requestBody = RequestBody.Companion.create(Jsons.toJson(body), JSON);
        Request request = new Request.Builder().
                url(url).
                headers(Headers.Companion.of(header)).
                post(requestBody).
                build();
        return request(request);
    }

    public static <T> T put(String url, Map<String, String> header, Object body, Class<T> clazz) {
        return Jsons.fromJson(put(url, header, body), clazz);
    }

    public static <T> T put(String url, Map<String, String> header, Object body, TypeReference<T> type) {
        return Jsons.fromJson(put(url, header, body), type);
    }

    private static String put(String url, Map<String, String> header, Object body) {
        RequestBody requestBody = RequestBody.Companion.create(Jsons.toJson(body), JSON);
        Request request = new Request.Builder().
                url(url).
                headers(Headers.Companion.of(header)).
                put(requestBody).
                build();
        return request(request);
    }

    private static String request(Request request) {
        return doRequest(request, 0);
    }

    /**
     * follow request redirection
     */
    private static String doRequest(Request request, int depth) {
        if (depth > MAX_RECURSIVE_DEPTH) {
            throw new IllegalStateException("redirect exceed more than max depth");
        }
        try (Response response = CLIENT.newCall(request).execute();
             ResponseBody responseBody = response.body()) {
            String result = null;
            if (response.isRedirect()) {
                String location = response.header("Location");
                String authorization = request.header("Authorization");
                if (location != null) {
                    Request.Builder builder = request.newBuilder().url(location);
                    if (authorization != null) {
                        builder.header("Authorization", authorization);
                    }
                    request = builder.build();
                    return doRequest(request, ++depth);
                }
            }
            if (responseBody != null) {
                result = responseBody.string();
            }
            if (response.isSuccessful()) {
                return result;
            }
            throw new IllegalStateException(String.format("request %s failed: %s %s",
                    request.url(),
                    response.message(),
                    result));
        } catch (IOException e) {
            throw new IllegalStateException(String.format("request %s failed", request.url()), e);
        }
    }
}
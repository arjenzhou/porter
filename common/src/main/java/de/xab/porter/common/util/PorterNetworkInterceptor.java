package de.xab.porter.common.util;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.logging.Logger;

public class PorterNetworkInterceptor implements Interceptor {
    private static final Logger LOGGER = Loggers.getLogger("INTERCEPTOR");

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        if (!response.isSuccessful() && !response.isRedirect()) {
            logHeader(request, response);
        }
        return response;
    }

    /**
     * log request and response header
     */
    private void logHeader(Request request, Response response) {
        if (response == null) {
            return;
        }
        String method = request.method();
        LOGGER.severe(String.format("-> %s %s", method, request.url()));
        request.headers().forEach(header ->
                LOGGER.severe(String.format("%s: %s", header.getFirst(), header.getSecond())));
        LOGGER.severe(String.format("-> END %s", method));

        long responseTime = response.receivedResponseAtMillis() - response.sentRequestAtMillis();
        LOGGER.severe(String.format("<- %s %s %sms", response.code(), response.message(), responseTime));
        response.headers().forEach(header ->
                LOGGER.severe(String.format("%s: %s", header.getFirst(), header.getSecond())));
        LOGGER.severe(String.format("<- END %s", response.protocol()));
    }
}

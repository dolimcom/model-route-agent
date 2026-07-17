package com.modelroute.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        if (!request.getRequestURI().startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString().substring(0, 12);
        }
        long startedAt = System.nanoTime();
        String method = request.getMethod();
        String path = request.getRequestURI();
        AtomicBoolean logged = new AtomicBoolean();
        MDC.put("requestId", requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            if (request.isAsyncStarted()) {
                request.getAsyncContext().addListener(new AsyncRequestLogger(
                        requestId, method, path, response, startedAt, logged));
            } else {
                logCompletion(requestId, method, path, response, startedAt, logged, null);
            }
            MDC.remove("requestId");
        }
    }

    private void logCompletion(
            String requestId,
            String method,
            String path,
            HttpServletResponse response,
            long startedAt,
            AtomicBoolean logged,
            Throwable failure) {
        if (!logged.compareAndSet(false, true)) {
            return;
        }
        MDC.put("requestId", requestId);
        try {
            long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000L;
            if (failure == null) {
                log.info("HTTP {} {} -> {} ({} ms)", method, path, response.getStatus(), elapsedMs);
            } else {
                log.warn("HTTP {} {} -> {} ({} ms), asyncError={}",
                        method, path, response.getStatus(), elapsedMs, failure.getMessage());
            }
        } finally {
            MDC.remove("requestId");
        }
    }

    private final class AsyncRequestLogger implements AsyncListener {

        private final String requestId;
        private final String method;
        private final String path;
        private final HttpServletResponse response;
        private final long startedAt;
        private final AtomicBoolean logged;

        private AsyncRequestLogger(
                String requestId,
                String method,
                String path,
                HttpServletResponse response,
                long startedAt,
                AtomicBoolean logged) {
            this.requestId = requestId;
            this.method = method;
            this.path = path;
            this.response = response;
            this.startedAt = startedAt;
            this.logged = logged;
        }

        @Override
        public void onComplete(AsyncEvent event) {
            logCompletion(requestId, method, path, response, startedAt, logged, null);
        }

        @Override
        public void onTimeout(AsyncEvent event) {
            logCompletion(requestId, method, path, response, startedAt, logged,
                    new IllegalStateException("async request timed out"));
        }

        @Override
        public void onError(AsyncEvent event) {
            logCompletion(requestId, method, path, response, startedAt, logged, event.getThrowable());
        }

        @Override
        public void onStartAsync(AsyncEvent event) {
            event.getAsyncContext().addListener(this);
        }
    }
}

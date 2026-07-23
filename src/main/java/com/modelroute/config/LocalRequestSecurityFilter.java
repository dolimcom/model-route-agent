package com.modelroute.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Set;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Enforces the local-only browser trust boundary without requiring remote-user authentication.
 */
@Component
public class LocalRequestSecurityFilter extends OncePerRequestFilter {

    private static final Set<String> LOOPBACK_HOSTS = Set.of("localhost", "127.0.0.1", "::1");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        addSecurityHeaders(response);
        if (isProtectedMutation(request) && isCrossSiteBrowserRequest(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Cross-site API requests are not allowed");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean isProtectedMutation(HttpServletRequest request) {
        String method = request.getMethod();
        if (HttpMethod.GET.matches(method)
                || HttpMethod.HEAD.matches(method)
                || HttpMethod.OPTIONS.matches(method)) {
            return false;
        }
        String path = request.getRequestURI();
        return path.startsWith("/api/") || path.equals("/actuator/semanticrouter");
    }

    private boolean isCrossSiteBrowserRequest(HttpServletRequest request) {
        String fetchSite = request.getHeader("Sec-Fetch-Site");
        if ("cross-site".equalsIgnoreCase(fetchSite)) {
            return true;
        }
        String origin = request.getHeader("Origin");
        if (!StringUtils.hasText(origin)) {
            return false;
        }
        try {
            String host = URI.create(origin).getHost();
            return host == null || !LOOPBACK_HOSTS.contains(host.toLowerCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return true;
        }
    }

    private void addSecurityHeaders(HttpServletResponse response) {
        response.setHeader("Content-Security-Policy",
                "default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self' data:; "
                        + "connect-src 'self'; object-src 'none'; base-uri 'none'; frame-ancestors 'none'; form-action 'self'");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Referrer-Policy", "no-referrer");
        response.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
    }
}

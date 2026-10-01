package com.budgetbot.api.v1;

import com.budgetbot.config.AppProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
@RequiredArgsConstructor
public class ApiKeyFilter extends OncePerRequestFilter {
  private final AppProperties props;

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    if (path == null || !path.startsWith("/api/v1/")) return true;
    return "/api/v1/health".equals(path) || path.startsWith("/api/v1/webhooks/");
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    if (!matches(props.apiKey(), request.getHeader("X-API-Key"))) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json");
      response.getWriter().write("{\"error\":\"unauthorized\"}");
      return;
    }
    chain.doFilter(request, response);
  }

  static boolean matches(String expected, String given) {
    if (expected == null || expected.isBlank() || given == null) return false;
    byte[] a = expected.getBytes(StandardCharsets.UTF_8);
    byte[] b = given.getBytes(StandardCharsets.UTF_8);
    return a.length == b.length && MessageDigest.isEqual(a, b);
  }
}

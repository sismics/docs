package com.sismics.util.filter;

import com.sismics.util.EnvironmentUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter used to handle CORS requests.
 *
 * @author bgamard
 */
public class CorsFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) {
        // NOP
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        if (EnvironmentUtil.isDevMode() && request.getHeader("origin") != null) {
            // Add CORS in dev mode
            response.addHeader("Access-Control-Allow-Origin", request.getHeader("origin"));
            response.addHeader("Access-Control-Allow-Credentials", "true");
            response.addHeader("Access-Control-Max-Age", "3600");
            response.addHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization");
            response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
        }

        if ("OPTIONS".equals(request.getMethod())) {
            // Handle preflight request
            response.getWriter().print("{ \"status\": \"ok\" }");
        } else {
            filterChain.doFilter(req, res);
        }
    }

    @Override
    public void destroy() {
        // NOP
    }
}

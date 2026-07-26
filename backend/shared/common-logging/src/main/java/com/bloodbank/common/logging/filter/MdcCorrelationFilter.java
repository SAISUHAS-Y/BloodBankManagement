package com.bloodbank.common.logging.filter;

import com.bloodbank.common.core.context.RequestContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * MDC Correlation Filter (Delegates tracing lifecycle to RequestContextFilter).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class MdcCorrelationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        // Ensure RequestContext traceId is populated
        RequestContext.getTraceId();
        filterChain.doFilter(request, response);
    }
}

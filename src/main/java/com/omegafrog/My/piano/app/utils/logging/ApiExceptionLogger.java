package com.omegafrog.My.piano.app.utils.logging;

import jakarta.servlet.http.HttpServletRequest;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class ApiExceptionLogger {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionLogger.class);

    public void logHandledException(Throwable ex, HttpStatus status) {
        HttpServletRequest request = currentRequest();
        if (request != null) {
            request.setAttribute(ApiRequestLoggingFilter.HANDLED_EXCEPTION_LOGGED_ATTRIBUTE, Boolean.TRUE);
            request.setAttribute(ApiRequestLoggingFilter.HANDLED_EXCEPTION_STATUS_ATTRIBUTE, status.value());
        }

        if (status.is5xxServerError()) {
            log.error("http request handled with exception {}", StructuredArguments.entries(
                ApiRequestLoggingFilter.commonLogFields(request, status.value(), ex)), ex);
        } else {
            log.warn("http request handled with exception {}", StructuredArguments.entries(
                ApiRequestLoggingFilter.commonLogFields(request, status.value(), ex)), ex);
        }
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }
}

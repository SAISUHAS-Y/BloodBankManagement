package com.bloodbank.common.logging.converter;

import ch.qos.logback.core.pattern.CompositeConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SensitiveDataMaskingConverter extends CompositeConverter<ILoggingEvent> {

    private static final Pattern SENSITIVE_PATTERN = Pattern.compile(
            "(?i)\"(password|token|accessToken|refreshToken|authorization)\"\\s*:\\s*\"[^\"]*\"|" +
            "(?i)(password|token|accessToken|refreshToken|authorization)\\s*=\\s*[^,\\)\\s]+"
    );

    @Override
    protected String transform(ILoggingEvent event, String in) {
        if (in == null || in.isEmpty()) {
            return in;
        }

        Matcher matcher = SENSITIVE_PATTERN.matcher(in);
        if (!matcher.find()) {
            return in;
        }

        matcher.reset();
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String match = matcher.group();
            String redactedMatch;
            if (match.contains(":")) {
                String fieldName = match.substring(0, match.indexOf(':'));
                redactedMatch = fieldName + ": \"[REDACTED]\"";
            } else if (match.contains("=")) {
                String fieldName = match.substring(0, match.indexOf('='));
                redactedMatch = fieldName + "=[REDACTED]";
            } else {
                redactedMatch = "[REDACTED]";
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(redactedMatch));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}

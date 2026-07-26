# 🪵 Common Logging Library

## Overview
`common-logging` configures structured JSON logging with Logstash encoders, MDC request context propagation, and AOP performance tracing.

## Features
* **Structured Logstash JSON Output:** Formats console and file logs as JSON objects for ELK / Grafana Loki aggregation.
* **MDC Tracing:** Populates `traceId`, `userId`, and `clientIp` in Mapped Diagnostic Context (MDC) for correlation across Zipkin and log files.
* **`@LogExecutionTime`:** Aspect annotation to measure and log execution duration of critical service methods.

## Configuration
Imports `logback-spring.xml` from classpath into application logging context.

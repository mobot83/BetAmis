package com.betamis.match.infrastructure.rest;

import io.opentelemetry.api.trace.Span;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

@Provider
public class TraceResponseFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        Span span = Span.current();
        if (span.getSpanContext().isValid()) {
            responseContext.getHeaders().putSingle("X-Trace-Id", span.getSpanContext().getTraceId());
        }
    }
}

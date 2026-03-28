package com.betamis.notification.infrastructure.rest;

import com.betamis.notification.domain.exception.PreferenceNotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class NotificationExceptionMapper implements ExceptionMapper<PreferenceNotFoundException> {

    @Override
    public Response toResponse(PreferenceNotFoundException e) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorBody(e.getMessage()))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    public record ErrorBody(String message) {}
}

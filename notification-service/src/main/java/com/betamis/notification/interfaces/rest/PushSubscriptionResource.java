package com.betamis.notification.interfaces.rest;

import com.betamis.notification.domain.port.in.UpdatePreferences;
import com.betamis.notification.interfaces.rest.dto.PushSubscriptionRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@Path("/notifications/push-subscription")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PushSubscriptionResource {

    private static final String USER_ROLE = "betamis-user";

    private final UpdatePreferences updatePreferences;
    private final ObjectMapper objectMapper;

    public PushSubscriptionResource(UpdatePreferences updatePreferences, ObjectMapper objectMapper) {
        this.updatePreferences = updatePreferences;
        this.objectMapper = objectMapper;
    }

    @POST
    @RolesAllowed(USER_ROLE)
    public Response subscribe(@Context SecurityContext security, PushSubscriptionRequest request) {
        String userId = security.getUserPrincipal().getName();
        try {
            String subscriptionJson = objectMapper.writeValueAsString(request);
            updatePreferences.savePushSubscription(userId, subscriptionJson);
        } catch (JsonProcessingException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return Response.noContent().build();
    }
}

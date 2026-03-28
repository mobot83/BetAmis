package com.betamis.notification.interfaces.rest;

import com.betamis.notification.domain.model.NotificationPreference;
import com.betamis.notification.domain.port.in.UpdatePreferences;
import com.betamis.notification.interfaces.rest.dto.PreferenceRequest;
import com.betamis.notification.interfaces.rest.dto.PreferenceResponse;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/notifications/preferences")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NotificationPreferenceResource {

    private static final String USER_ROLE = "betamis-user";

    private final UpdatePreferences updatePreferences;
    private final JsonWebToken jwt;

    public NotificationPreferenceResource(UpdatePreferences updatePreferences, JsonWebToken jwt) {
        this.updatePreferences = updatePreferences;
        this.jwt = jwt;
    }

    @GET
    @RolesAllowed(USER_ROLE)
    public PreferenceResponse get(@Context SecurityContext security) {
        String userId = security.getUserPrincipal().getName();
        String email = jwt.getClaim("email");
        NotificationPreference pref = updatePreferences.getOrCreate(userId, email);
        return PreferenceResponse.from(pref);
    }

    @PUT
    @RolesAllowed(USER_ROLE)
    public PreferenceResponse update(@Context SecurityContext security, PreferenceRequest request) {
        String userId = security.getUserPrincipal().getName();
        NotificationPreference pref = updatePreferences.update(userId, request.emailEnabled(), request.webPushEnabled());
        return PreferenceResponse.from(pref);
    }


}

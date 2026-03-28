package com.betamis.notification.interfaces.rest;

import com.betamis.notification.domain.exception.PreferenceNotFoundException;
import com.betamis.notification.domain.port.in.UpdatePreferences;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


/**
 * One-click unsubscribe endpoint embedded in notification emails.
 * No authentication required — the token acts as a capability credential.
 */
@Path("/notifications/unsubscribe")
@Produces(MediaType.TEXT_HTML)
public class UnsubscribeResource {

    private final UpdatePreferences updatePreferences;

    public UnsubscribeResource(UpdatePreferences updatePreferences) {
        this.updatePreferences = updatePreferences;
    }

    @GET
    public Response unsubscribe(@QueryParam("token") String token) {
        if (token == null || token.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("<html><body><p>Invalid unsubscribe link.</p></body></html>")
                    .build();
        }
        try {
            updatePreferences.unsubscribeByToken(token);
            return Response.noContent().build();
        } catch (PreferenceNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("<html><body><p>Unsubscribe link not found or already used.</p></body></html>")
                    .build();
        }
    }
}

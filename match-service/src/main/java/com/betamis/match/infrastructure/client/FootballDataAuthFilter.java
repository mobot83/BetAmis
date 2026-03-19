package com.betamis.match.infrastructure.client;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;

@ApplicationScoped
public class FootballDataAuthFilter implements ClientRequestFilter {

    @ConfigProperty(name = "football-data.api-token")
    String apiToken;

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        requestContext.getHeaders().putSingle("X-Auth-Token", apiToken);
    }
}

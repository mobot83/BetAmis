package com.betamis.match.infrastructure.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.util.Map;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class WireMockFootballDataResource implements QuarkusTestResourceLifecycleManager {

    static WireMockServer SERVER;

    @Override
    public Map<String, String> start() {
        SERVER = new WireMockServer(wireMockConfig().dynamicPort());
        SERVER.start();
        return Map.of("quarkus.rest-client.football-data.url", SERVER.baseUrl());
    }

    @Override
    public void stop() {
        if (SERVER != null) {
            SERVER.stop();
        }
    }

    @Override
    public void inject(TestInjector testInjector) {
        testInjector.injectIntoFields(SERVER, new TestInjector.MatchesType(WireMockServer.class));
    }
}

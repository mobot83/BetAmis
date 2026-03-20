package com.betamis.match.infrastructure.rest;

import com.betamis.match.infrastructure.client.WireMockFootballDataResource;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for POST /admin/sync.
 * WireMock stubs the external football-data API so no real HTTP calls are made.
 */
@QuarkusTest
@QuarkusTestResource(WireMockFootballDataResource.class)
class AdminResourceIT {

    WireMockServer wireMock;

    @BeforeEach
    void setUp() {
        wireMock.resetAll();
    }

    // ── POST /admin/sync ───────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /admin/sync without auth should return 401")
    void triggerSync_withoutAuth_shouldReturn401() {
        given()
        .when()
                .post("/admin/sync")
        .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("POST /admin/sync with betamis-user role should return 403")
    @TestSecurity(user = "regular-user", roles = "betamis-user")
    void triggerSync_withUserRole_shouldReturn403() {
        given()
        .when()
                .post("/admin/sync")
        .then()
                .statusCode(403);
    }

    @Test
    @DisplayName("POST /admin/sync with betamis-admin role should return 200 and sync results")
    @TestSecurity(user = "admin-user", roles = "betamis-admin")
    void triggerSync_withAdminRole_shouldReturn200() {
        stubEmptyMatchList("PL");
        stubEmptyMatchList("WC");

        given()
        .when()
                .post("/admin/sync")
        .then()
                .statusCode(200)
                .body("synced", hasItems("PL", "WC"))
                .body("failed", empty());
    }

    @Test
    @DisplayName("POST /admin/sync returns 207 when a competition sync throws")
    @TestSecurity(user = "admin-user", roles = "betamis-admin")
    void triggerSync_withPartialFailure_shouldReturn207() {
        stubEmptyMatchList("PL");
        // WC not stubbed → football-data client gets a 404 → goes into failed list

        given()
        .when()
                .post("/admin/sync")
        .then()
                .statusCode(207)
                .body("synced", hasItem("PL"))
                .body("failed", hasItem("WC"));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void stubEmptyMatchList(String competitionCode) {
        wireMock.stubFor(get(urlPathEqualTo("/v4/competitions/" + competitionCode + "/matches"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                { "count": 0, "matches": [] }
                                """)));
    }
}

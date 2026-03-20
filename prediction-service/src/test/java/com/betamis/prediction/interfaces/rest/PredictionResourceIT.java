package com.betamis.prediction.interfaces.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;

/**
 * REST integration tests for POST /predictions.
 * Uses H2 in-memory database (configured in test application.properties)
 * and @TestSecurity to bypass JWT validation.
 */
@QuarkusTest
class PredictionResourceIT {

    private static final String FUTURE_KICK_OFF = "2099-06-15T15:00:00Z";

    @Test
    @TestSecurity(user = "user-submit", roles = "betamis-user")
    void submitPrediction_returns201WithLocation() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"matchId":"match-res-1","homeScore":2,"awayScore":1,"kickOffTime":"%s"}
                        """.formatted(FUTURE_KICK_OFF))
        .when()
                .post("/predictions")
        .then()
                .statusCode(201)
                .header("Location", containsString("/predictions/"));
    }

    @Test
    void submitPrediction_withoutAuth_returns401() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"matchId":"match-res-2","homeScore":1,"awayScore":0,"kickOffTime":"%s"}
                        """.formatted(FUTURE_KICK_OFF))
        .when()
                .post("/predictions")
        .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "user-submit-bad", roles = "betamis-user")
    void submitPrediction_nullMatchId_returns400() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"homeScore":1,"awayScore":0,"kickOffTime":"%s"}
                        """.formatted(FUTURE_KICK_OFF))
        .when()
                .post("/predictions")
        .then()
                .statusCode(400);
    }

    @Test
    @TestSecurity(user = "user-submit-nko", roles = "betamis-user")
    void submitPrediction_nullKickOffTime_returns400() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"matchId":"match-res-3","homeScore":1,"awayScore":0}
                        """)
        .when()
                .post("/predictions")
        .then()
                .statusCode(400);
    }

    @Test
    @TestSecurity(user = "user-submit-late", roles = "betamis-user")
    void submitPrediction_kickOffInPast_returns422() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"matchId":"match-res-4","homeScore":1,"awayScore":0,"kickOffTime":"2000-01-01T12:00:00Z"}
                        """)
        .when()
                .post("/predictions")
        .then()
                .statusCode(422);
    }

    @Test
    @TestSecurity(user = "user-dup", roles = "betamis-user")
    void submitPrediction_duplicate_returns409() {
        String body = """
                {"matchId":"match-res-dup","homeScore":1,"awayScore":0,"kickOffTime":"%s"}
                """.formatted(FUTURE_KICK_OFF);

        given().contentType(ContentType.JSON).body(body).post("/predictions").then().statusCode(201);

        given()
                .contentType(ContentType.JSON)
                .body(body)
        .when()
                .post("/predictions")
        .then()
                .statusCode(409);
    }
}

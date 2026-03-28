package com.betamis.match.infrastructure.rest;

import com.betamis.match.domain.model.match.MatchStatus;
import com.betamis.match.infrastructure.persistence.entity.MatchEntity;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class MatchResourceIT {

    @BeforeEach
    @Transactional
    void setUp() {
        MatchEntity.deleteAll();

        MatchEntity planned1 = new MatchEntity();
        planned1.matchId = "planned-1";
        planned1.homeTeamId = "FRA";
        planned1.awayTeamId = "BRA";
        planned1.status = MatchStatus.PLANNED;
        planned1.kickoffAt = Instant.now().plus(1, ChronoUnit.DAYS);
        planned1.persist();

        MatchEntity planned2 = new MatchEntity();
        planned2.matchId = "planned-2";
        planned2.homeTeamId = "GER";
        planned2.awayTeamId = "ARG";
        planned2.status = MatchStatus.PLANNED;
        planned2.kickoffAt = Instant.now().plus(2, ChronoUnit.DAYS);
        planned2.persist();

        MatchEntity finished = new MatchEntity();
        finished.matchId = "finished-1";
        finished.homeTeamId = "ESP";
        finished.awayTeamId = "ENG";
        finished.homeTeamScore = 3;
        finished.awayTeamScore = 2;
        finished.status = MatchStatus.FINISHED;
        finished.kickoffAt = Instant.now().minus(1, ChronoUnit.DAYS);
        finished.persist();
    }

    // ── Auth ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /matches without auth should return 401")
    void getMatches_withoutAuth_shouldReturn401() {
        given()
        .when()
                .get("/matches")
        .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("GET /matches with betamis-admin role should return 403")
    @TestSecurity(user = "admin", roles = "betamis-admin")
    void getMatches_withAdminRole_shouldReturn403() {
        given()
        .when()
                .get("/matches")
        .then()
                .statusCode(403);
    }

    // ── Filtering ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /matches returns all matches sorted by kickoffAt ASC")
    @TestSecurity(user = "user", roles = "betamis-user")
    void getMatches_noFilter_returnsAllMatchesSortedByKickoff() {
        given()
        .when()
                .get("/matches")
        .then()
                .statusCode(200)
                .body("", hasSize(3))
                .body("[0].id", is("finished-1"))
                .body("[1].id", is("planned-1"))
                .body("[2].id", is("planned-2"));
    }

    @Test
    @DisplayName("GET /matches?status=PLANNED returns only planned matches with null scores")
    @TestSecurity(user = "user", roles = "betamis-user")
    void getMatches_filterByPlanned_returnsPlannedMatchesWithNullScores() {
        given()
        .when()
                .get("/matches?status=PLANNED")
        .then()
                .statusCode(200)
                .body("", hasSize(2))
                .body("status", everyItem(is("PLANNED")))
                .body("[0].homeTeamScore", nullValue())
                .body("[0].awayTeamScore", nullValue());
    }

    @Test
    @DisplayName("GET /matches?status=FINISHED returns finished matches with scores")
    @TestSecurity(user = "user", roles = "betamis-user")
    void getMatches_filterByFinished_returnsFinishedMatchesWithScores() {
        given()
        .when()
                .get("/matches?status=FINISHED")
        .then()
                .statusCode(200)
                .body("", hasSize(1))
                .body("[0].id", is("finished-1"))
                .body("[0].homeTeamScore", is(3))
                .body("[0].awayTeamScore", is(2));
    }

    @Test
    @DisplayName("GET /matches?status=STARTED returns empty list when no match in progress")
    @TestSecurity(user = "user", roles = "betamis-user")
    void getMatches_filterByStarted_returnsEmptyList() {
        given()
        .when()
                .get("/matches?status=STARTED")
        .then()
                .statusCode(200)
                .body("", empty());
    }

    @Test
    @DisplayName("GET /matches?status=INVALID returns 400")
    @TestSecurity(user = "user", roles = "betamis-user")
    void getMatches_withInvalidStatus_shouldReturn400() {
        given()
        .when()
                .get("/matches?status=INVALID")
        .then()
                .statusCode(400);
    }

    // ── Response shape ────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /matches response contains expected fields")
    @TestSecurity(user = "user", roles = "betamis-user")
    void getMatches_responseShape_containsExpectedFields() {
        given()
        .when()
                .get("/matches?status=PLANNED")
        .then()
                .statusCode(200)
                .body("[0].id", notNullValue())
                .body("[0].homeTeamId", notNullValue())
                .body("[0].awayTeamId", notNullValue())
                .body("[0].status", notNullValue())
                .body("[0].kickoffAt", notNullValue());
    }
}

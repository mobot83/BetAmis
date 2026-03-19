package com.betamis.league.infrastructure.rest;

import com.betamis.league.domain.model.League;
import com.betamis.league.domain.port.out.LeagueRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.UserTransaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for REST endpoints using a real PostgreSQL database
 * started automatically by Quarkus DevServices (Testcontainers).
 * JWT authentication is handled by @TestSecurity.
 */
@QuarkusTest
class LeagueResourceIT {

    @Inject
    LeagueRepository leagueRepository;

    @Inject
    UserTransaction tx;

    // ── POST /leagues ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /leagues should create a league and return 201 with body")
    @TestSecurity(user = "user-1", roles = "betamis-user")
    void createLeague_shouldReturn201() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        { "name": "Champions Integration" }
                        """)
        .when()
                .post("/leagues")
        .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo("Champions Integration"))
                .body("invitationCode", hasLength(6))
                .body("invitationExpiresAt", notNullValue())
                .header("Location", containsString("/leagues/"));
    }

    @Test
    @DisplayName("POST /leagues without auth should return 401")
    void createLeague_withoutAuth_shouldReturn401() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        { "name": "Unauthorized League" }
                        """)
        .when()
                .post("/leagues")
        .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("POST /leagues with blank name should return 400")
    @TestSecurity(user = "user-1", roles = "betamis-user")
    void createLeague_withBlankName_shouldReturn400() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        { "name": "" }
                        """)
        .when()
                .post("/leagues")
        .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("POST /leagues with name exceeding 255 characters should return 400")
    @TestSecurity(user = "user-1", roles = "betamis-user")
    void createLeague_withOversizedName_shouldReturn400() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        { "name": "%s" }
                        """.formatted("x".repeat(256)))
        .when()
                .post("/leagues")
        .then()
                .statusCode(400);
    }

    // ── GET /leagues/{id} ─────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /leagues/{id} should return league details with invitation code")
    @TestSecurity(user = "user-get", roles = "betamis-user")
    void getLeague_shouldReturnLeague() throws Exception {
        League league = League.create("Get Test League", "owner-get");
        league.pollDomainEvents();
        tx.begin();
        leagueRepository.save(league);
        tx.commit();

        given()
        .when()
                .get("/leagues/" + league.getId())
        .then()
                .statusCode(200)
                .body("id", equalTo(league.getId()))
                .body("name", equalTo("Get Test League"))
                .body("ownerId", equalTo("owner-get"))
                .body("invitationCode", hasLength(6))
                .body("invitationExpiresAt", notNullValue());
    }

    @Test
    @DisplayName("GET /leagues/{id} on unknown league should return 404")
    @TestSecurity(user = "user-get-404", roles = "betamis-user")
    void getLeague_unknownLeague_shouldReturn404() {
        given()
        .when()
                .get("/leagues/non-existing-id")
        .then()
                .statusCode(404);
    }

    // ── POST /leagues/{id}/join ───────────────────────────────────────────────

    @Test
    @DisplayName("POST /leagues/{id}/join should add member and return 204")
    @TestSecurity(user = "user-join", roles = "betamis-user")
    void joinLeague_shouldReturn204() throws Exception {
        League league = League.create("Join Test League", "user-owner");
        league.pollDomainEvents();
        String code = league.getInvitations().get(0).code();
        tx.begin();
        leagueRepository.save(league);
        tx.commit();

        given()
                .contentType(ContentType.JSON)
                .body(String.format("""
                        { "code": "%s" }
                        """, code))
        .when()
                .post("/leagues/" + league.getId() + "/join")
        .then()
                .statusCode(204);
    }

    @Test
    @DisplayName("POST /leagues/{id}/join with invalid code should return 422")
    @TestSecurity(user = "user-bad-code", roles = "betamis-user")
    void joinLeague_withInvalidCode_shouldReturn422() throws Exception {
        League league = League.create("Invalid Code League", "user-owner2");
        league.pollDomainEvents();
        tx.begin();
        leagueRepository.save(league);
        tx.commit();

        given()
                .contentType(ContentType.JSON)
                .body("""
                        { "code": "BADCOD" }
                        """)
        .when()
                .post("/leagues/" + league.getId() + "/join")
        .then()
                .statusCode(422);
    }

    @Test
    @DisplayName("POST /leagues/{id}/join on unknown league should return 404")
    @TestSecurity(user = "user-404", roles = "betamis-user")
    void joinLeague_unknownLeague_shouldReturn404() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        { "code": "ABC123" }
                        """)
        .when()
                .post("/leagues/non-existing-id/join")
        .then()
                .statusCode(404);
    }

    // ── GET /leagues/{id}/members ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /leagues/{id}/members should return member list")
    @TestSecurity(user = "user-list", roles = "betamis-user")
    void listMembers_shouldReturnMembers() throws Exception {
        League league = League.create("Member List League", "owner-list");
        league.pollDomainEvents();
        tx.begin();
        leagueRepository.save(league);
        tx.commit();

        given()
        .when()
                .get("/leagues/" + league.getId() + "/members")
        .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].userId", equalTo("owner-list"));
    }

    @Test
    @DisplayName("GET /leagues/{id}/members on unknown league should return 404")
    @TestSecurity(user = "user-404-list", roles = "betamis-user")
    void listMembers_unknownLeague_shouldReturn404() {
        given()
        .when()
                .get("/leagues/non-existing-id/members")
        .then()
                .statusCode(404);
    }
}

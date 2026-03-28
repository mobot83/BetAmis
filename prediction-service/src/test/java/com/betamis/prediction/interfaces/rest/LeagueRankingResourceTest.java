package com.betamis.prediction.interfaces.rest;

import com.betamis.prediction.domain.model.ranking.RankingEntry;
import com.betamis.prediction.domain.port.out.RankingReader;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@QuarkusTest
class LeagueRankingResourceTest {

    @InjectMock
    RankingReader rankingReader;

    @Test
    @TestSecurity(user = "test-user", roles = "betamis-user")
    @DisplayName("GET /leagues/{id}/ranking returns entries sorted by rank")
    void getLeagueRanking_returnsEntries() {
        when(rankingReader.findLeagueRanking("league-1")).thenReturn(List.of(
                new RankingEntry(1, "user-a", 10L),
                new RankingEntry(2, "user-b", 5L)
        ));

        given()
                .when().get("/leagues/league-1/ranking")
                .then()
                .statusCode(200)
                .body("$.size()", is(2))
                .body("[0].rank", is(1))
                .body("[0].userId", is("user-a"))
                .body("[0].totalPoints", is(10))
                .body("[1].rank", is(2))
                .body("[1].userId", is("user-b"));
    }

    @Test
    @DisplayName("GET /leagues/{id}/ranking without auth returns 401")
    void getLeagueRanking_withoutAuth_returns401() {
        given()
                .when().get("/leagues/league-1/ranking")
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "test-user", roles = "betamis-user")
    @DisplayName("GET /leagues/{id}/ranking with oversized leagueId returns 400")
    void getLeagueRanking_oversizedLeagueId_returns400() {
        given()
                .when().get("/leagues/" + "x".repeat(101) + "/ranking")
                .then()
                .statusCode(400);
    }

    @Test
    @TestSecurity(user = "test-user", roles = "betamis-user")
    @DisplayName("GET /leagues/{id}/ranking for empty league returns empty list")
    void getLeagueRanking_emptyLeague_returns200WithEmptyList() {
        when(rankingReader.findLeagueRanking("empty")).thenReturn(List.of());

        given()
                .when().get("/leagues/empty/ranking")
                .then()
                .statusCode(200)
                .body("$.size()", is(0));
    }
}

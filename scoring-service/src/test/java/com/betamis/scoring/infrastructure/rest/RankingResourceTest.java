package com.betamis.scoring.infrastructure.rest;

import com.betamis.scoring.domain.model.UserRanking;
import com.betamis.scoring.domain.port.out.RankingRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

@QuarkusTest
class RankingResourceTest {

    @InjectMock
    RankingRepository rankingRepository;

    @Test
    @DisplayName("GET /rankings/{leagueId} should return ranked entries in order")
    void shouldReturnLeagueRanking() {
        when(rankingRepository.findLeagueRanking("global")).thenReturn(List.of(
                new UserRanking("user-1", "global", 9L, 1),
                new UserRanking("user-2", "global", 6L, 2),
                new UserRanking("user-3", "global", 3L, 3)
        ));

        given()
                .when().get("/rankings/global")
                .then()
                .statusCode(200)
                .body("$.size()", is(3))
                .body("[0].rank", is(1))
                .body("[0].userId", is("user-1"))
                .body("[0].totalPoints", is(9))
                .body("[1].rank", is(2))
                .body("[1].userId", is("user-2"))
                .body("[2].rank", is(3))
                .body("[2].userId", is("user-3"));
    }

    @Test
    @DisplayName("GET /rankings/{leagueId} should return empty list when league has no players")
    void shouldReturnEmptyListForUnknownLeague() {
        when(rankingRepository.findLeagueRanking("empty-league")).thenReturn(List.of());

        given()
                .when().get("/rankings/empty-league")
                .then()
                .statusCode(200)
                .body("$.size()", is(0));
    }

    @Test
    @DisplayName("GET /rankings/{leagueId} should return 400 when leagueId exceeds 100 characters")
    void shouldReturn400ForOversizedLeagueId() {
        String oversized = "x".repeat(101);

        given()
                .when().get("/rankings/" + oversized)
                .then()
                .statusCode(400);
    }
}

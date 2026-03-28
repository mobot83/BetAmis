package com.betamis.notification.interfaces.rest;

import com.betamis.notification.domain.model.NotificationPreference;
import com.betamis.notification.domain.port.in.UpdatePreferences;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@QuarkusTest
class NotificationPreferenceResourceIT {

    @InjectMock
    UpdatePreferences updatePreferences;

    @Test
    @TestSecurity(user = "user-1", roles = "betamis-user")
    @DisplayName("GET /notifications/preferences returns current preferences")
    void get_returns_preferences() {
        when(updatePreferences.getOrCreate(eq("user-1"), any()))
                .thenReturn(new NotificationPreference("user-1", "user@test.com",
                        true, false, "token-abc", null));

        given()
                .when().get("/notifications/preferences")
                .then()
                .statusCode(200)
                .body("emailEnabled", is(true))
                .body("webPushEnabled", is(false));
    }

    @Test
    @DisplayName("GET /notifications/preferences without auth returns 401")
    void get_without_auth_returns_401() {
        given()
                .when().get("/notifications/preferences")
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "user-1", roles = "betamis-user")
    @DisplayName("PUT /notifications/preferences updates preferences")
    void put_updates_preferences() {
        when(updatePreferences.update("user-1", false, true))
                .thenReturn(new NotificationPreference("user-1", "user@test.com",
                        false, true, "token-abc", null));

        given()
                .contentType("application/json")
                .body("{\"emailEnabled\": false, \"webPushEnabled\": true}")
                .when().put("/notifications/preferences")
                .then()
                .statusCode(200)
                .body("emailEnabled", is(false))
                .body("webPushEnabled", is(true));
    }

    @Test
    @DisplayName("GET /notifications/unsubscribe with valid token returns 204")
    void unsubscribe_with_valid_token() {
        given()
                .queryParam("token", "valid-token")
                .when().get("/notifications/unsubscribe")
                .then()
                .statusCode(204);
    }

    @Test
    @DisplayName("GET /notifications/unsubscribe without token returns 400")
    void unsubscribe_without_token_returns_400() {
        given()
                .when().get("/notifications/unsubscribe")
                .then()
                .statusCode(400);
    }
}

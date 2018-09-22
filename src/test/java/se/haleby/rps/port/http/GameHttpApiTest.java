package se.haleby.rps.port.http;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import se.haleby.rps.GameServer;
import se.haleby.rps.domain.model.Move;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static se.haleby.rps.domain.model.Move.*;

@DisplayName("Game HTTP API")
class GameHttpApiTest {

    private GameServer gameServer;

    @Nested
    @DisplayName("new game is started")
    class NoGame {

        @Test
        @DisplayName("when PUT to /api/games/:gameId with a valid gameId")
        void when_put_to_api_games() {
            startGame(UUID.randomUUID()).then().statusCode(200).and().body(equalTo("Game started"));
        }
    }

    @Nested
    @DisplayName("game is playable")
    class PlayableGame {

        @Test
        @DisplayName("when PUT to /api/games/:gameId with a gameId of an existing game and a move")
        void when_put_to_api_games() {
            UUID gameId = UUID.randomUUID();

            startGame(gameId);
            makeMove(gameId, "player1", ROCK).then().statusCode(200).body(equalTo("Move made"));
        }

        @Test
        @DisplayName("until game is ended")
        void example_of_full_game() {
            UUID gameId = UUID.randomUUID();
            startGame(gameId);

            makeMove(gameId, "player1", ROCK);
            makeMove(gameId, "player2", SCISSORS);

            makeMove(gameId, "player1", SCISSORS);
            makeMove(gameId, "player2", ROCK);
            
            makeMove(gameId, "player1", PAPER);
            makeMove(gameId, "player2", SCISSORS);
        }
    }

    // Test configuration and helpers

    @BeforeAll
    static void configureRestAssuredBeforeAllTests() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.basePath = "/api/games";
    }

    @AfterAll
    static void resetRestAssuredAfterAllTests() {
        RestAssured.reset();
    }

    @BeforeEach
    void startServer() {
        gameServer = new GameServer(8080).start();
    }

    @AfterEach
    void stopServer() {
        gameServer.stop();
    }

    private static Response startGame(UUID gameId) {
        return given().header("user", "playerId1").when().put("/{gameId}", gameId);
    }

    private static Response makeMove(UUID gameId, String playerId, Move move) {
        return given().header("user", playerId).formParam("move", move).when().put("{gameId}", gameId);
    }
}

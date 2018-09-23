package se.haleby.rps.port.http;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import se.haleby.rps.GameServer;
import se.haleby.rps.domain.model.Move;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
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

    @Nested
    @DisplayName("games are listable")
    class A {

        @Test
        void when_no_query_parameters_are_specified_then_both_ongoing_and_ended_games_are_listed() {
            // Game 1
            startGame("game1");
            makeMove("game1", "player1", ROCK);
            makeMove("game1", "player2", SCISSORS);
            makeMove("game1", "player1", ROCK);
            makeMove("game1", "player2", SCISSORS);

            // Game 2
            startGame("game2");

            // Game 3
            startGame("game3");
            makeMove("game3", "player1", ROCK);

            // Game 4
            startGame("game4");
            makeMove("game4", "player1", ROCK);
            makeMove("game4", "player2", ROCK);

            // Check
            when().
                    get("/").
            then().
                    statusCode(200).
                    body("size()", is(4)).
                    root("find { game -> game.gameId == '%s' }").
                    body(
                            withArgs("game1"), Matchers.<Map<String, Object>>allOf(hasEntry("state", "WON"), hasEntry("joinable", false), hasEntry("playerId1", "player1"), hasEntry("playerId2", "player2"), hasEntry("winnerId", "player1")),
                            withArgs("game2"), Matchers.<Map<String, Object>>allOf(hasEntry("state", "ONGOING"), hasEntry("joinable", true), not(hasKey("playerId1")), not(hasKey("playerId2")), not(hasKey("winnerId"))),
                            withArgs("game3"), Matchers.<Map<String, Object>>allOf(hasEntry("state", "ONGOING"), hasEntry("joinable", true), hasEntry("playerId1", "player1"), not(hasKey("playerId2")), not(hasKey("winnerId"))),
                            withArgs("game4"), Matchers.<Map<String, Object>>allOf(hasEntry("state", "ONGOING"), hasEntry("joinable", false), hasEntry("playerId1", "player1"), hasEntry("playerId2", "player2"), not(hasKey("winnerId")))
                    );
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
        return startGame(gameId.toString());
    }

    private static Response startGame(String gameId) {
        return given().header("player", "playerId1").when().put("/{gameId}", gameId);
    }

    private static Response makeMove(UUID gameId, String playerId, Move move) {
        return makeMove(gameId.toString(), playerId, move);
    }

    private static Response makeMove(String gameId, String playerId, Move move) {
        return given().header("player", playerId).formParam("move", move).when().put("{gameId}", gameId);
    }

}

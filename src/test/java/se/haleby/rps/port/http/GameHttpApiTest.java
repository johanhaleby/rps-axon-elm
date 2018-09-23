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
import static se.haleby.rps.domain.model.State.ONGOING;
import static se.haleby.rps.domain.model.State.WON;

@DisplayName("Game HTTP API")
class GameHttpApiTest {

    private GameServer gameServer;

    @Nested
    @DisplayName("new game is started")
    class NoGame {

        @Test
        @DisplayName("when PUT to /api/games/:gameId with a valid gameId")
        void when_put_to_api_games() {
            startGame("game").then().
                    statusCode(200).
                    body(
                            "gameId", equalTo("game"),
                            "state", equalTo(ONGOING.name()),
                            "joinable", is(true)
                    );
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
            makeMove(gameId, "player1", ROCK).then().
                    statusCode(200).
                    body(
                            "gameId", equalTo(gameId.toString()),
                            "playerId1", equalTo("player1"),
                            "state", equalTo(ONGOING.name()),
                            "joinable", is(true)
                    );
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
        @DisplayName("when GET to /api/games return both ongoing and ended games")
        void when_no_query_parameters_are_specified_then_both_ongoing_and_ended_games_are_returned() {
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

        @Test
        @DisplayName("when GET to /api/games?ended=false returns only ongoing games")
        void when_query_parameter_ended_is_false_then_only_ongoing_games_are_returned() {
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
                    body("collect { it.gameId}", hasItems("game2", "game3", "game4"));
        }

        @Test
        @DisplayName("when GET to /api/games?ongoing=false returns only ended games")
        void when_query_parameter_ongoing_is_false_then_only_ended_games_are_returned() {
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
                    body("collect { it.gameId}", hasItems("game1"));
        }
    }

    @Nested
    @DisplayName("game is retrievable")
    class GetGame {

        @Test
        @DisplayName("when issuing GET to /api/games/:gameId and game is ended")
        void game_is_retrievable_when_ended() {
            String gameId = "game1";
            startGame(gameId);
            makeMove(gameId, "player1", ROCK);
            makeMove(gameId, "player2", SCISSORS);
            makeMove(gameId, "player1", ROCK);
            makeMove(gameId, "player2", SCISSORS);

            when().
                    get("/{gameId}", gameId).
            then().
                    statusCode(200).
                    body(
                            "gameId", equalTo(gameId),
                            "playerId1", equalTo("player1"),
                            "playerId2", equalTo("player2"),
                            "winnerId", equalTo("player1"),
                            "state", equalTo(WON.name()),
                            "joinable", is(false)
                    );
        }
        
        @Test
        @DisplayName("when issuing GET to /api/games/:gameId and game is ongoing")
        void game_is_retrievable_when_ongoing() {
            String gameId = "game1";
            startGame(gameId);
            makeMove(gameId, "player1", ROCK);
            makeMove(gameId, "player2", SCISSORS);

            when().
                    get("/{gameId}", gameId).
            then().
                    statusCode(200).
                    body(
                            "gameId", equalTo(gameId),
                            "playerId1", equalTo("player1"),
                            "playerId2", equalTo("player2"),
                            "state", equalTo(ONGOING.name()),
                            "joinable", is(false)
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
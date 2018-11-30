package se.haleby.rps.port.http;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import se.haleby.rps.GameServer;
import se.haleby.rps.GameServer.CmdArgs;
import se.haleby.rps.domain.model.Move;
import se.haleby.rps.projection.gameinfo.GameInfoState;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.*;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.exparity.hamcrest.date.DateMatchers.within;
import static org.hamcrest.Matchers.*;
import static se.haleby.rps.domain.model.Move.*;
import static se.haleby.rps.projection.gameinfo.GameInfoState.*;

@DisplayName("Game HTTP API")
class GameHttpApiTest {

    private GameServer gameServer;

    @Nested
    @DisplayName("game is startable")
    class NoGame {

        @Test
        @DisplayName("when issuing PUT to /api/games/:gameId with a valid gameId")
        void when_put_to_api_games() {
            startGame("game").then().
                    statusCode(200).
                    body(
                            "gameId", equalTo("game"),
                            "state", equalTo(stateOf(JOINABLE))
                    );
        }
    }

    @Nested
    @DisplayName("game is playable")
    class PlayableGame {

        @Test
        @DisplayName("when issuing PUT to /api/games/:gameId with a gameId of an started game and a move")
        void when_put_to_api_games() {
            UUID gameId = UUID.randomUUID();

            startGame(gameId);
            makeMove(gameId, "player1", ROCK).then().
                    statusCode(200).
                    body(
                            "gameId", equalTo(gameId.toString()),
                            "player1", equalTo("player1"),
                            "state", equalTo(stateOf(JOINABLE))
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
    @DisplayName("multiple games are retrievable")
    class MultipleGames {

        @Test
        @DisplayName("when issuing GET to /api/games returns all games")
        void when_no_query_parameters_are_specified_then_both_started_and_ended_games_are_returned() {
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
                    log().all().
                    statusCode(200).
                    body("size()", is(4)).
                    body("createdAt", everyItem(isCloseTo(new Date()))).
                    root("find { game -> game.gameId == '%s' }").
                    body(
                            withArgs("game1"), Matchers.<Map<String, Object>>allOf(hasEntry("state", stateOf(ENDED)), hasEntry("player1", "player1"), hasEntry("player2", "player2"), hasEntry("winner", "player1")),
                            withArgs("game2"), Matchers.<Map<String, Object>>allOf(hasEntry("state", stateOf(JOINABLE)), not(hasKey("player1")), not(hasKey("player2")), not(hasKey("winner"))),
                            withArgs("game3"), Matchers.<Map<String, Object>>allOf(hasEntry("state", stateOf(JOINABLE)), hasEntry("player1", "player1"), not(hasKey("player2")), not(hasKey("winner"))),
                            withArgs("game4"), Matchers.<Map<String, Object>>allOf(hasEntry("state", stateOf(STARTED)), hasEntry("player1", "player1"), hasEntry("player2", "player2"), not(hasKey("winner")))
                    );
        }

        @Test
        @DisplayName("when issuing GET to /api/games?state=started returns only started games")
        void when_query_parameter_state_is_started_then_only_started_games_are_returned() {
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
            given().
                    queryParam("state", stateOf(STARTED)).
                    when().
                    get("/").
                    then().
                    statusCode(200).
                    body("collect { it.gameId}", containsInAnyOrder("game4"));
        }

        @Test
        @DisplayName("when issuing GET to /api/games?state=ended returns only ended games")
        void when_query_parameter_state_is_ended_then_only_ended_games_are_returned() {
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
            given().
                    queryParam("state", stateOf(ENDED)).
                    when().
                    get("/").
                    then().
                    statusCode(200).
                    body("collect { it.gameId}", hasItems("game1"));
        }

        @Test
        @DisplayName("when issuing GET to /api/games?state=joinable returns only joinable games")
        void when_query_parameter_state_is_joinable_then_only_joinable_games_are_returned() {
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
            given().
                    queryParam("state", stateOf(JOINABLE)).
                    when().
                    get("/").
                    then().
                    statusCode(200).
                    body("collect { it.gameId}", containsInAnyOrder("game2", "game3"));
        }

        @Test
        @DisplayName("when issuing GET to /api/games with multiple state parameters returns the games matching any of the supplied states")
        void when_multiple_state_query_parameters_then_matching_games_are_returned() {
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
            given().
                    queryParam("state", stateOf(JOINABLE), stateOf(STARTED)).
            when().
                    get("/").
            then().
                    statusCode(200).
                    body("collect { it.gameId}", containsInAnyOrder("game2", "game3", "game4"));
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
                            "player1", equalTo("player1"),
                            "player2", equalTo("player2"),
                            "winner", equalTo("player1"),
                            "state", equalTo(stateOf(ENDED))
                    );
        }

        @Test
        @DisplayName("when issuing GET to /api/games/:gameId and game is started")
        void game_is_retrievable_when_started() {
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
                            "player1", equalTo("player1"),
                            "player2", equalTo("player2"),
                            "state", equalTo(stateOf(STARTED))
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
        gameServer = new GameServer(CmdArgs.with().port(8080)).start();
    }

    @AfterEach
    void stopServer() {
        gameServer.stop();
    }

    @SuppressWarnings("UnusedReturnValue")
    private static Response startGame(UUID gameId) {
        return startGame(gameId.toString());
    }

    private static Response startGame(String gameId) {
        return given().header("player", "player1").when().put("/{gameId}", gameId);
    }

    private static Response makeMove(UUID gameId, String player, Move move) {
        return makeMove(gameId.toString(), player, move);
    }

    private static Response makeMove(String gameId, String player, Move move) {
        return given().header("player", player).formParam("move", move).when().put("{gameId}", gameId);
    }

    private String stateOf(GameInfoState state) {
        return state.name().toLowerCase();
    }

    private static Matcher<Long> isCloseTo(Date date) {
        return new BaseMatcher<Long>() {

            @Override
            public boolean matches(Object o) {
                return within(10, SECONDS, date).matches(new Date((Long) o));
            }


            @Override
            public void describeTo(Description description) {
                within(10, SECONDS, date).describeTo(description);
            }
        };
    }

}
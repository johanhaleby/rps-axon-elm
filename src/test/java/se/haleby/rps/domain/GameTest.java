package se.haleby.rps.domain;

import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import se.haleby.rps.domain.command.MakeMove;
import se.haleby.rps.domain.command.StartGame;
import se.haleby.rps.domain.event.*;
import se.haleby.rps.domain.model.Game;

import java.util.UUID;

import static se.haleby.rps.domain.model.Move.*;

@DisplayName("Game")
class GameTest {

    private FixtureConfiguration<Game> fixture;

    @BeforeEach
    void setUp() {
        fixture = new AggregateTestFixture<>(Game.class);
    }

    @Nested
    @DisplayName("is started")
    class IsStarted {

        @Test
        void when_start_game_command_is_issued() {
            String gameId = UUID.randomUUID().toString();
            String playerId = "123";

            fixture.givenNoPriorActivity()
                    .when(StartGame.builder().gameId(gameId).startedBy(playerId).rounds(3).build())
                    .expectSuccessfulHandlerExecution()
                    .expectEvents(GameStarted.builder().gameId(gameId).rounds(3).startedBy(playerId).build());
        }
    }

    @Nested
    @DisplayName("starts first round")
    class ReceivesFirstMove {

        @Test
        void when_game_started_and_player_one_makes_a_move() {
            String gameId = UUID.randomUUID().toString();
            String playerId = "123";

            fixture.given(GameStarted.builder().gameId(gameId).rounds(3).startedBy(playerId).build())
                    .when(MakeMove.builder().gameId(gameId).move(ROCK).playerId(playerId).build())
                    .expectSuccessfulHandlerExecution()
                    .expectEvents(
                            RoundStarted.builder().gameId(gameId).roundNumber(1).build(),
                            FirstPlayerJoinedGame.builder().gameId(gameId).playerId(playerId).build(),
                            MoveMade.builder().gameId(gameId).playerId(playerId).round(1).move(ROCK).build()
                    );
        }
    }

    @Nested
    @DisplayName("ends first round")
    class EndsFirstRound {

        @Test
        void when_game_started_and_a_player_one_has_made_a_move_and_player_two_makes_a_move() {
            String gameId = UUID.randomUUID().toString();
            String playerId1 = "123";
            String playerId2 = "124";

            fixture
                    .given(
                            GameStarted.builder().gameId(gameId).rounds(3).startedBy(playerId1).build(),
                            RoundStarted.builder().gameId(gameId).roundNumber(1).build(),
                            FirstPlayerJoinedGame.builder().gameId(gameId).playerId(playerId1).build(),
                            MoveMade.builder().gameId(gameId).playerId(playerId1).round(1).move(ROCK).build()
                    )
                    .when(
                            MakeMove.builder().gameId(gameId).move(SCISSORS).playerId(playerId2).build()
                    )
                    .expectSuccessfulHandlerExecution()
                    .expectEvents(
                            SecondPlayerJoinedGame.builder().gameId(gameId).playerId(playerId2).build(),
                            MoveMade.builder().gameId(gameId).playerId(playerId2).round(1).move(SCISSORS).build(),
                            RoundWon.builder().gameId(gameId).roundNumber(1).winnerId(playerId1).build(),
                            RoundEnded.builder().gameId(gameId).roundNumber(1).build()
                    );
        }
    }

    @Nested
    @DisplayName("starts next round")
    class StartsNextRound {

        @ParameterizedTest
        @ValueSource(strings = {"123", "124"})
        void when_game_started_and_both_player_one_and_two_has_made_a_move_each_and_any_of_the_players_makes_a_new_move(String playerId) {
            String gameId = UUID.randomUUID().toString();
            String playerId1 = "123";
            String playerId2 = "124";

            fixture
                    .given(
                            GameStarted.builder().gameId(gameId).rounds(3).startedBy(playerId1).build(),
                            RoundStarted.builder().gameId(gameId).roundNumber(1).build(),
                            FirstPlayerJoinedGame.builder().gameId(gameId).playerId(playerId1).build(),
                            MoveMade.builder().gameId(gameId).playerId(playerId1).round(1).move(ROCK).build(),
                            SecondPlayerJoinedGame.builder().gameId(gameId).playerId(playerId2).build(),
                            MoveMade.builder().gameId(gameId).playerId(playerId2).round(1).move(SCISSORS).build(),
                            RoundWon.builder().gameId(gameId).roundNumber(1).winnerId(playerId1).build(),
                            RoundEnded.builder().gameId(gameId).roundNumber(1).build()
                    )
                    .when(
                            MakeMove.builder().gameId(gameId).move(PAPER).playerId(playerId).build()
                    )
                    .expectSuccessfulHandlerExecution()
                    .expectEvents(
                            RoundStarted.builder().gameId(gameId).roundNumber(2).build(),
                            MoveMade.builder().gameId(gameId).playerId(playerId).round(2).move(PAPER).build()
                    );
        }
    }

    @Nested
    @DisplayName("is won")
    class IsWon {

        @Test
        void when_last_move_is_made_in_the_last_round_results_in_a_win(){
            String gameId = UUID.randomUUID().toString();
            String playerId1 = "123";
            String playerId2 = "124";

            fixture
                    .given(
                            GameStarted.builder().gameId(gameId).rounds(2).startedBy(playerId1).build(),
                            RoundStarted.builder().gameId(gameId).roundNumber(1).build(),
                            FirstPlayerJoinedGame.builder().gameId(gameId).playerId(playerId1).build(),
                            MoveMade.builder().gameId(gameId).playerId(playerId1).round(1).move(ROCK).build(),
                            SecondPlayerJoinedGame.builder().gameId(gameId).playerId(playerId2).build(),
                            MoveMade.builder().gameId(gameId).playerId(playerId2).round(1).move(SCISSORS).build(),
                            RoundWon.builder().gameId(gameId).roundNumber(1).winnerId(playerId1).build(),
                            RoundEnded.builder().gameId(gameId).roundNumber(1).build(),
                            RoundStarted.builder().gameId(gameId).roundNumber(2).build(),
                            MoveMade.builder().gameId(gameId).playerId(playerId2).round(2).move(PAPER).build()
                    )
                    .when(
                            MakeMove.builder().gameId(gameId).move(SCISSORS).playerId(playerId1).build()
                    )
                    .expectSuccessfulHandlerExecution()
                    .expectEvents(
                            MoveMade.builder().gameId(gameId).playerId(playerId1).round(2).move(SCISSORS).build(),
                            RoundWon.builder().gameId(gameId).roundNumber(2).winnerId(playerId1).build(),
                            RoundEnded.builder().gameId(gameId).roundNumber(2).build(),
                            GameWon.builder().gameId(gameId).winnerId(playerId1).build(),
                            GameEnded.withGameId(gameId)
                    );
        }
    }

    @Nested
    @DisplayName("is tie")
    class IsTie {

        @Test
        void when_last_move_is_made_in_the_last_round_results_in_a_game_tie(){
            String gameId = UUID.randomUUID().toString();
            String playerId1 = "123";
            String playerId2 = "124";

            fixture
                    .given(
                            GameStarted.builder().gameId(gameId).rounds(2).startedBy(playerId1).build(),
                            RoundStarted.builder().gameId(gameId).roundNumber(1).build(),
                            FirstPlayerJoinedGame.builder().gameId(gameId).playerId(playerId1).build(),
                            MoveMade.builder().gameId(gameId).playerId(playerId1).round(1).move(ROCK).build(),
                            SecondPlayerJoinedGame.builder().gameId(gameId).playerId(playerId2).build(),
                            MoveMade.builder().gameId(gameId).playerId(playerId2).round(1).move(SCISSORS).build(),
                            RoundWon.builder().gameId(gameId).roundNumber(1).winnerId(playerId1).build(),
                            RoundEnded.builder().gameId(gameId).roundNumber(1).build(),
                            RoundStarted.builder().gameId(gameId).roundNumber(2).build(),
                            MoveMade.builder().gameId(gameId).playerId(playerId1).round(2).move(PAPER).build()
                    )
                    .when(
                            MakeMove.builder().gameId(gameId).move(SCISSORS).playerId(playerId2).build()
                    )
                    .expectSuccessfulHandlerExecution()
                    .expectEvents(
                            MoveMade.builder().gameId(gameId).playerId(playerId2).round(2).move(SCISSORS).build(),
                            RoundWon.builder().gameId(gameId).roundNumber(2).winnerId(playerId2).build(),
                            RoundEnded.builder().gameId(gameId).roundNumber(2).build(),
                            GameTied.withGameId(gameId),
                            GameEnded.withGameId(gameId)
                    );
        }
    }
}
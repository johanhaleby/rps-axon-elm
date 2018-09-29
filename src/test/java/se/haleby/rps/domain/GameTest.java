package se.haleby.rps.domain;

import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import se.haleby.rps.domain.command.InitializeNewGame;
import se.haleby.rps.domain.command.MakeMove;
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
            String player = "123";

            fixture.givenNoPriorActivity()
                    .when(InitializeNewGame.builder().gameId(gameId).initializer(player).rounds(3).build())
                    .expectSuccessfulHandlerExecution()
                    .expectEvents(NewGameInitialized.builder().gameId(gameId).rounds(3).initializedBy(player).build());
        }
    }

    @Nested
    @DisplayName("starts first round")
    class ReceivesFirstMove {

        @Test
        void when_game_started_and_player_one_makes_a_move() {
            String gameId = UUID.randomUUID().toString();
            String player = "123";

            fixture.given(NewGameInitialized.builder().gameId(gameId).rounds(3).initializedBy(player).build())
                    .when(MakeMove.builder().gameId(gameId).move(ROCK).player(player).build())
                    .expectSuccessfulHandlerExecution()
                    .expectEvents(
                            RoundStarted.builder().gameId(gameId).roundNumber(1).build(),
                            FirstPlayerJoinedGame.builder().gameId(gameId).player(player).build(),
                            MoveMade.builder().gameId(gameId).player(player).round(1).move(ROCK).build()
                    );
        }
    }

    @Nested
    @DisplayName("ends first round")
    class EndsFirstRound {

        @Test
        void when_game_started_and_a_player_one_has_made_a_move_and_player_two_makes_a_move() {
            String gameId = UUID.randomUUID().toString();
            String player1 = "123";
            String player2 = "124";

            fixture
                    .given(
                            NewGameInitialized.builder().gameId(gameId).rounds(3).initializedBy(player1).build(),
                            RoundStarted.builder().gameId(gameId).roundNumber(1).build(),
                            FirstPlayerJoinedGame.builder().gameId(gameId).player(player1).build(),
                            MoveMade.builder().gameId(gameId).player(player1).round(1).move(ROCK).build()
                    )
                    .when(
                            MakeMove.builder().gameId(gameId).move(SCISSORS).player(player2).build()
                    )
                    .expectSuccessfulHandlerExecution()
                    .expectEvents(
                            SecondPlayerJoinedGame.builder().gameId(gameId).player(player2).build(),
                            MoveMade.builder().gameId(gameId).player(player2).round(1).move(SCISSORS).build(),
                            RoundWon.builder().gameId(gameId).roundNumber(1).winner(player1).build(),
                            RoundEnded.builder().gameId(gameId).roundNumber(1).build()
                    );
        }
    }

    @Nested
    @DisplayName("starts next round")
    class StartsNextRound {

        @ParameterizedTest
        @ValueSource(strings = {"123", "124"})
        void when_game_started_and_both_player_one_and_two_has_made_a_move_each_and_any_of_the_players_makes_a_new_move(String player) {
            String gameId = UUID.randomUUID().toString();
            String player1 = "123";
            String player2 = "124";

            fixture
                    .given(
                            NewGameInitialized.builder().gameId(gameId).rounds(3).initializedBy(player1).build(),
                            RoundStarted.builder().gameId(gameId).roundNumber(1).build(),
                            FirstPlayerJoinedGame.builder().gameId(gameId).player(player1).build(),
                            MoveMade.builder().gameId(gameId).player(player1).round(1).move(ROCK).build(),
                            SecondPlayerJoinedGame.builder().gameId(gameId).player(player2).build(),
                            MoveMade.builder().gameId(gameId).player(player2).round(1).move(SCISSORS).build(),
                            RoundWon.builder().gameId(gameId).roundNumber(1).winner(player1).build(),
                            RoundEnded.builder().gameId(gameId).roundNumber(1).build()
                    )
                    .when(
                            MakeMove.builder().gameId(gameId).move(PAPER).player(player).build()
                    )
                    .expectSuccessfulHandlerExecution()
                    .expectEvents(
                            RoundStarted.builder().gameId(gameId).roundNumber(2).build(),
                            MoveMade.builder().gameId(gameId).player(player).round(2).move(PAPER).build()
                    );
        }
    }

    @Nested
    @DisplayName("is won")
    class IsWon {

        @Test
        void when_last_move_is_made_in_the_last_round_results_in_a_win() {
            String gameId = UUID.randomUUID().toString();
            String player1 = "123";
            String player2 = "124";

            fixture
                    .given(
                            NewGameInitialized.builder().gameId(gameId).rounds(2).initializedBy(player1).build(),
                            RoundStarted.builder().gameId(gameId).roundNumber(1).build(),
                            FirstPlayerJoinedGame.builder().gameId(gameId).player(player1).build(),
                            MoveMade.builder().gameId(gameId).player(player1).round(1).move(ROCK).build(),
                            SecondPlayerJoinedGame.builder().gameId(gameId).player(player2).build(),
                            MoveMade.builder().gameId(gameId).player(player2).round(1).move(SCISSORS).build(),
                            RoundWon.builder().gameId(gameId).roundNumber(1).winner(player1).build(),
                            RoundEnded.builder().gameId(gameId).roundNumber(1).build(),
                            RoundStarted.builder().gameId(gameId).roundNumber(2).build(),
                            MoveMade.builder().gameId(gameId).player(player2).round(2).move(PAPER).build()
                    )
                    .when(
                            MakeMove.builder().gameId(gameId).move(SCISSORS).player(player1).build()
                    )
                    .expectSuccessfulHandlerExecution()
                    .expectEvents(
                            MoveMade.builder().gameId(gameId).player(player1).round(2).move(SCISSORS).build(),
                            RoundWon.builder().gameId(gameId).roundNumber(2).winner(player1).build(),
                            RoundEnded.builder().gameId(gameId).roundNumber(2).build(),
                            GameWon.builder().gameId(gameId).winner(player1).build(),
                            GameEnded.withGameId(gameId)
                    );
        }

        @Test
        void when_move_results_in_a_win_due_to_majority_won_rounds() {
            String gameId = UUID.randomUUID().toString();
            String player1 = "123";
            String player2 = "124";

            fixture
                    .given(
                            NewGameInitialized.builder().gameId(gameId).rounds(5).initializedBy(player1).build(),
                            RoundStarted.builder().gameId(gameId).roundNumber(1).build(),
                            FirstPlayerJoinedGame.builder().gameId(gameId).player(player1).build(),
                            MoveMade.builder().gameId(gameId).player(player1).round(1).move(ROCK).build(),
                            SecondPlayerJoinedGame.builder().gameId(gameId).player(player2).build(),
                            MoveMade.builder().gameId(gameId).player(player2).round(1).move(SCISSORS).build(),
                            RoundWon.builder().gameId(gameId).roundNumber(1).winner(player1).build(),
                            RoundEnded.builder().gameId(gameId).roundNumber(1).build(),
                            RoundStarted.builder().gameId(gameId).roundNumber(2).build(),
                            MoveMade.builder().gameId(gameId).player(player1).round(2).move(SCISSORS).build(),
                            MoveMade.builder().gameId(gameId).player(player2).round(2).move(PAPER).build(),
                            RoundWon.builder().gameId(gameId).roundNumber(2).winner(player1).build(),
                            RoundEnded.builder().gameId(gameId).roundNumber(2).build(),
                            RoundStarted.builder().gameId(gameId).roundNumber(3).build(),
                            MoveMade.builder().gameId(gameId).player(player2).round(3).move(SCISSORS).build()
                    )
                    .when(
                            MakeMove.builder().gameId(gameId).move(ROCK).player(player1).build()
                    )
                    .expectSuccessfulHandlerExecution()
                    .expectEvents(
                            MoveMade.builder().gameId(gameId).player(player1).round(3).move(ROCK).build(),
                            RoundWon.builder().gameId(gameId).roundNumber(3).winner(player1).build(),
                            RoundEnded.builder().gameId(gameId).roundNumber(3).build(),
                            GameWon.builder().gameId(gameId).winner(player1).build(),
                            GameEnded.withGameId(gameId)
                    );
        }
    }

    @Nested
    @DisplayName("is tie")
    class IsTie {

        @Test
        void when_last_move_is_made_in_the_last_round_results_in_a_game_tie() {
            String gameId = UUID.randomUUID().toString();
            String player1 = "123";
            String player2 = "124";

            fixture
                    .given(
                            NewGameInitialized.builder().gameId(gameId).rounds(2).initializedBy(player1).build(),
                            RoundStarted.builder().gameId(gameId).roundNumber(1).build(),
                            FirstPlayerJoinedGame.builder().gameId(gameId).player(player1).build(),
                            MoveMade.builder().gameId(gameId).player(player1).round(1).move(ROCK).build(),
                            SecondPlayerJoinedGame.builder().gameId(gameId).player(player2).build(),
                            MoveMade.builder().gameId(gameId).player(player2).round(1).move(SCISSORS).build(),
                            RoundWon.builder().gameId(gameId).roundNumber(1).winner(player1).build(),
                            RoundEnded.builder().gameId(gameId).roundNumber(1).build(),
                            RoundStarted.builder().gameId(gameId).roundNumber(2).build(),
                            MoveMade.builder().gameId(gameId).player(player1).round(2).move(PAPER).build()
                    )
                    .when(
                            MakeMove.builder().gameId(gameId).move(SCISSORS).player(player2).build()
                    )
                    .expectSuccessfulHandlerExecution()
                    .expectEvents(
                            MoveMade.builder().gameId(gameId).player(player2).round(2).move(SCISSORS).build(),
                            RoundWon.builder().gameId(gameId).roundNumber(2).winner(player2).build(),
                            RoundEnded.builder().gameId(gameId).roundNumber(2).build(),
                            GameTied.withGameId(gameId),
                            GameEnded.withGameId(gameId)
                    );
        }
    }
}
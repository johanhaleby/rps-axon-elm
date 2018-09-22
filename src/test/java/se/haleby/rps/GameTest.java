package se.haleby.rps;

import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.Before;
import org.junit.Test;
import se.haleby.rps.command.MakeMove;
import se.haleby.rps.command.StartGame;
import se.haleby.rps.domain.Game;
import se.haleby.rps.event.*;

import java.util.UUID;

import static se.haleby.rps.domain.Move.ROCK;
import static se.haleby.rps.domain.Move.SCISSORS;

public class GameTest {

    private FixtureConfiguration<Game> fixture;

    @Before
    public void setUp() {
        fixture = new AggregateTestFixture<>(Game.class);
    }

    @Test public void
    start_game() {
        String gameId = UUID.randomUUID().toString();
        String playerId = "123";

        fixture.givenNoPriorActivity()
                .when(StartGame.builder().gameId(gameId).startedBy(playerId).rounds(3).build())
                .expectSuccessfulHandlerExecution()
                .expectEvents(GameStarted.builder().gameId(gameId).rounds(3).startedBy(playerId).build());
    }

    @Test public void
    first_move() {
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

    @Test public void
    second_move() {
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

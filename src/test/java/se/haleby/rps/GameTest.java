package se.haleby.rps;

import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.Before;
import org.junit.Test;
import se.haleby.rps.command.MakeMove;
import se.haleby.rps.domain.Game;
import se.haleby.rps.event.FirstPlayerJoinedGame;
import se.haleby.rps.event.GameStarted;
import se.haleby.rps.event.MoveMade;
import se.haleby.rps.event.RoundStarted;

import java.util.UUID;

import static se.haleby.rps.domain.Move.ROCK;
import static se.haleby.rps.domain.Move.SCISSORS;

public class GameTest {

    private FixtureConfiguration<Game> fixture;

    @Before
    public void setUp() {
        fixture = new AggregateTestFixture<>(Game.class);
    }

    @Test
    public void
    first_move() {
        String gameId = UUID.randomUUID().toString();
        String playerId = "123";

        fixture.given(GameStarted.builder().gameId(gameId).rounds(3).startedBy(playerId).build())
                .when(MakeMove.builder().gameId(gameId).move(ROCK).playerId(playerId).build())
                .expectSuccessfulHandlerExecution()
                .expectEvents(
                        RoundStarted.builder().gameId(gameId).roundNumber(1).build(),
                        FirstPlayerJoinedGame.builder().gameId(gameId).playerId(playerId).build(),
                        MoveMade.builder().gameId(gameId).playerId(playerId).playerId(playerId).round(1).move(ROCK).build()
                );
    }

    @Test
    public void
    second_move() {
        String gameId = UUID.randomUUID().toString();
        String playerId1 = "123";
        String playerId2 = "124";

        fixture
                .given(
                        GameStarted.builder().gameId(gameId).rounds(3).startedBy(playerId1).build(),
                        RoundStarted.builder().gameId(gameId).roundNumber(1).build(),
                        FirstPlayerJoinedGame.builder().gameId(gameId).playerId(playerId1).build(),
                        MoveMade.builder().gameId(gameId).playerId(playerId1).playerId(playerId1).round(1).move(ROCK).build()
                )
                .when(
                        MakeMove.builder().gameId(gameId).move(SCISSORS).playerId(playerId2).build()
                )
                .expectSuccessfulHandlerExecution()
                .expectEvents(

                );
    }
}

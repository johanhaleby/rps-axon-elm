package se.haleby.rps.application;

import org.axonframework.commandhandling.gateway.CommandGateway;
import se.haleby.rps.domain.command.MakeMove;
import se.haleby.rps.domain.command.StartGame;
import se.haleby.rps.domain.model.Move;

import java.util.concurrent.CompletableFuture;

public class GameApplicationService {

    private final CommandGateway commandGateway;
    private final int rounds;

    public GameApplicationService(CommandGateway commandGateway, int rounds) {
        this.commandGateway = commandGateway;
        this.rounds = rounds;
    }

    public CompletableFuture<?> startGame(String gameId, String startedBy) {
        return commandGateway.send(StartGame.builder().gameId(gameId).startedBy(startedBy).rounds(rounds).build());
    }

    public CompletableFuture<?> makeMove(String gameId, String playerId, Move move) {
        return commandGateway.send(MakeMove.builder().gameId(gameId).playerId(playerId).move(move).build());
    }
}

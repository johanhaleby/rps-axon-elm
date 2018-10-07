package se.haleby.rps.application;

import org.axonframework.commandhandling.gateway.CommandGateway;
import se.haleby.rps.domain.command.CreateGame;
import se.haleby.rps.domain.command.MakeMove;
import se.haleby.rps.domain.model.Move;

import java.util.Date;
import java.util.concurrent.CompletableFuture;

public class GameApplicationService {

    private final CommandGateway commandGateway;
    private final int rounds;

    public GameApplicationService(CommandGateway commandGateway, int rounds) {
        this.commandGateway = commandGateway;
        this.rounds = rounds;
    }

    public CompletableFuture<?> startGame(String gameId, String creator) {
        return commandGateway.send(CreateGame.builder().gameId(gameId).creator(creator).rounds(rounds).creationDate(new Date()).build());
    }

    public CompletableFuture<?> makeMove(String gameId, String player, Move move) {
        return commandGateway.send(MakeMove.builder().gameId(gameId).player(player).move(move).build());
    }
}

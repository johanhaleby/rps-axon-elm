package se.haleby.rps.domain.model;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.commandhandling.model.AggregateRoot;
import org.axonframework.eventsourcing.EventSourcingHandler;
import se.haleby.rps.domain.command.MakeMove;
import se.haleby.rps.domain.command.StartGame;
import se.haleby.rps.domain.event.*;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;
import static se.haleby.rps.domain.model.State.*;

@AggregateRoot
public class Game {

    private static final Supplier<TreeSet<Round>> TREE_SET_FACTORY = () -> new TreeSet<>(comparing(Round::roundNumber));

    @AggregateIdentifier
    private String id;
    private State state = State.NOT_STARTED;
    private String playerId1;
    private String playerId2;
    private TreeSet<Round> rounds = TREE_SET_FACTORY.get();
    private int numberOfRoundsInGame;

    @SuppressWarnings("unused")
    Game() {
    }

    // Command processing
    @CommandHandler
    public Game(StartGame cmd) {
        if (state != State.NOT_STARTED) {
            throw new IllegalArgumentException("Game is already started");
        }
        apply(GameStarted.builder().gameId(cmd.getGameId()).startedBy(cmd.getStartedBy()).rounds(cmd.getRounds()).build());
    }

    @CommandHandler
    public void handle(MakeMove cmd) {
        if (state != ONGOING) {
            throw new IllegalStateException("Game is " + state);
        }

        Round round = getOngoingOrInitiateNextRound(rounds);
        joinGameIfNotAlreadyPlaying(cmd.getPlayerId());

        apply(MoveMade.builder().gameId(cmd.getGameId()).playerId(cmd.getPlayerId()).move(cmd.getMove()).round(round.roundNumber()).build());

        Round playedRound = rounds.last();

        if (playedRound.isEnded()) {
            int currentRoundNumber = rounds.size();
            if (playedRound.hasWinner()) {
                apply(RoundWon.builder().gameId(cmd.getGameId()).roundNumber(currentRoundNumber).winnerId(playerIdOf(playedRound.winner())).build());
            } else {
                apply(RoundTied.builder().gameId(cmd.getGameId()).roundNumber(currentRoundNumber).build());
            }
            apply(RoundEnded.builder().gameId(cmd.getGameId()).roundNumber(currentRoundNumber).build());

            if (currentRoundNumber == numberOfRoundsInGame) {
                // We've completed the last round => game has ended!
                Map<Player, List<Round>> wonRoundsPerPlayer = rounds.stream().filter(Round::hasWinner).collect(Collectors.groupingBy(Round::winner));
                int wonRoundsPlayer1 = wonRoundsPerPlayer.getOrDefault(Player.ONE, emptyList()).size();
                int wonRoundsPlayer2 = wonRoundsPerPlayer.getOrDefault(Player.TWO, emptyList()).size();
                if (wonRoundsPlayer1 == wonRoundsPlayer2) {
                    apply(GameTied.withGameId(cmd.getGameId()));
                } else if (wonRoundsPlayer1 > wonRoundsPlayer2) {
                    apply(GameWon.builder().gameId(cmd.getGameId()).winnerId(playerId1).build());
                } else {
                    apply(GameWon.builder().gameId(cmd.getGameId()).winnerId(playerId2).build());
                }

                apply(GameEnded.withGameId(cmd.getGameId()));
            }
        }
    }


    // Event handling
    @EventSourcingHandler
    public void when(GameStarted evt) {
        id = evt.getGameId();
        numberOfRoundsInGame = evt.getRounds();
        state = ONGOING;
    }


    @EventSourcingHandler
    public void when(FirstPlayerJoinedGame evt) {
        playerId1 = evt.getPlayerId();
    }

    @EventSourcingHandler
    public void when(SecondPlayerJoinedGame evt) {
        playerId2 = evt.getPlayerId();
    }

    @EventSourcingHandler
    public void when(RoundStarted evt) {
        rounds.add(new Round(evt.getRoundNumber()));
    }

    @EventSourcingHandler
    public void when(MoveMade evt) {
        rounds.stream().filter(round -> round.roundNumber() == evt.getRound()).findFirst().ifPresent(round -> round.play(playerOf(evt.getPlayerId()), evt.getMove()));
    }

    public void when(GameWon evt) {
        state = WON;
    }

    public void when(GameTied evt) {
        state = TIED;
    }

    // Helpers

    private String playerIdOf(Player player) {
        switch (player) {
            case ONE:
                return playerId1;
            default:
                return playerId2;

        }
    }

    private Player playerOf(String playerId) {
        if (playerId.equals(playerId1)) {
            return Player.ONE;
        } else if (playerId.equals(playerId2)) {
            return Player.TWO;
        }
        throw new IllegalStateException("Unknown player: " + playerId);
    }

    private Round getOngoingOrInitiateNextRound(TreeSet<Round> rounds) {
        final Round round;
        if (rounds.isEmpty() || rounds.last().isEnded()) {
            int roundNumber = rounds.size() + 1;
            round = new Round(roundNumber);
            apply(RoundStarted.builder().gameId(id).roundNumber(roundNumber).build());
        } else {
            round = rounds.last();
        }
        return round;
    }

    private void joinGameIfNotAlreadyPlaying(String playerId) {
        if (playerId.equals(playerId1) || playerId.equals(playerId2)) {
            return;
        }

        if (playerId1 == null) {
            apply(FirstPlayerJoinedGame.builder().gameId(id).playerId(playerId).build());
        } else if (playerId2 == null) {
            apply(SecondPlayerJoinedGame.builder().gameId(id).playerId(playerId).build());
        } else {
            throw new IllegalStateException("Cannot have more than 2 players");
        }
    }
}
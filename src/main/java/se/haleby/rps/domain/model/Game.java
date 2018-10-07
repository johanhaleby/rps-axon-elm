package se.haleby.rps.domain.model;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.commandhandling.model.AggregateRoot;
import org.axonframework.eventsourcing.EventSourcingHandler;
import se.haleby.rps.domain.command.CreateGame;
import se.haleby.rps.domain.command.MakeMove;
import se.haleby.rps.domain.event.*;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;
import static se.haleby.rps.domain.model.State.*;

@AggregateRoot
public class Game {

    @AggregateIdentifier
    private String id;
    private State state;
    private String player1;
    private String player2;
    private TreeSet<Round> rounds = new TreeSet<>(comparing(Round::roundNumber));
    private int numberOfRoundsInGame;

    @SuppressWarnings("unused")
    Game() {
    }

    // Command processing
    @CommandHandler
    public Game(CreateGame cmd) {
        apply(GameCreated.builder().gameId(cmd.getGameId()).createdBy(cmd.getCreator()).rounds(cmd.getRounds()).createdAt(cmd.getCreationDate()).build());
    }

    @CommandHandler
    public void handle(MakeMove cmd) {
        if (state != INITIALIZED && state != STARTED) {
            throw new IllegalStateException("Game is " + state);
        }

        Round round = getOngoingRoundOrStartNextRound(rounds);
        joinGameIfNotAlreadyPlaying(cmd.getPlayer());

        apply(MoveMade.builder().gameId(cmd.getGameId()).player(cmd.getPlayer()).move(cmd.getMove()).round(round.roundNumber()).build());

        Round playedRound = rounds.last();

        if (playedRound.isEnded()) {
            int currentRoundNumber = rounds.size();
            if (playedRound.hasWinner()) {
                apply(RoundWon.builder().gameId(cmd.getGameId()).roundNumber(currentRoundNumber).winner(playerOf(playedRound.winner())).build());
            } else {
                apply(RoundTied.builder().gameId(cmd.getGameId()).roundNumber(currentRoundNumber).build());
            }
            apply(RoundEnded.builder().gameId(cmd.getGameId()).roundNumber(currentRoundNumber).build());


            Map<Player, List<Round>> wonRoundsPerPlayer = rounds.stream().filter(Round::hasWinner).collect(Collectors.groupingBy(Round::winner));
            int wonRoundsPlayer1 = wonRoundsPerPlayer.getOrDefault(Player.ONE, emptyList()).size();
            int wonRoundsPlayer2 = wonRoundsPerPlayer.getOrDefault(Player.TWO, emptyList()).size();
            int majorityRounds = numberOfRoundsInGame / 2;
            if (wonRoundsPlayer1 > majorityRounds) {
                apply(GameWon.builder().gameId(cmd.getGameId()).winner(player1).build());
                apply(GameEnded.withGameId(cmd.getGameId()));
            } else if (wonRoundsPlayer2 > majorityRounds) {
                apply(GameWon.builder().gameId(cmd.getGameId()).winner(player2).build());
                apply(GameEnded.withGameId(cmd.getGameId()));
            } else if (currentRoundNumber == numberOfRoundsInGame) {
                // We've completed the last round => game has ended!
                if (wonRoundsPlayer1 == wonRoundsPlayer2) {
                    apply(GameTied.withGameId(cmd.getGameId()));
                } else if (wonRoundsPlayer1 > wonRoundsPlayer2) {
                    apply(GameWon.builder().gameId(cmd.getGameId()).winner(player1).build());
                } else {
                    apply(GameWon.builder().gameId(cmd.getGameId()).winner(player2).build());
                }
                apply(GameEnded.withGameId(cmd.getGameId()));
            }
        }
    }


    // Event handling
    @EventSourcingHandler
    public void when(GameCreated evt) {
        id = evt.getGameId();
        numberOfRoundsInGame = evt.getRounds();
        state = STARTED;
    }


    @EventSourcingHandler
    public void when(FirstPlayerJoinedGame evt) {
        player1 = evt.getPlayer();
    }

    @EventSourcingHandler
    public void when(SecondPlayerJoinedGame evt) {
        player2 = evt.getPlayer();
    }

    @EventSourcingHandler
    public void when(RoundStarted evt) {
        rounds.add(new Round(evt.getRoundNumber()));
    }

    @EventSourcingHandler
    public void when(MoveMade evt) {
        rounds.stream().filter(round -> round.roundNumber() == evt.getRound()).findFirst().ifPresent(round -> round.play(playerOf(evt.getPlayer()), evt.getMove()));
    }

    public void when(GameWon evt) {
        state = WON;
    }

    public void when(GameTied evt) {
        state = TIED;
    }

    // Helpers

    private String playerOf(Player player) {
        switch (player) {
            case ONE:
                return player1;
            default:
                return player2;

        }
    }

    private Player playerOf(String player) {
        if (player.equals(player1)) {
            return Player.ONE;
        } else if (player.equals(player2)) {
            return Player.TWO;
        }
        throw new IllegalStateException("Unknown player: " + player);
    }

    private Round getOngoingRoundOrStartNextRound(TreeSet<Round> rounds) {
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

    private void joinGameIfNotAlreadyPlaying(String player) {
        if (player.equals(player1) || player.equals(player2)) {
            return;
        }

        if (player1 == null) {
            apply(FirstPlayerJoinedGame.builder().gameId(id).player(player).build());
        } else if (player2 == null) {
            apply(SecondPlayerJoinedGame.builder().gameId(id).player(player).build());
        } else {
            throw new IllegalStateException("Cannot have more than 2 players");
        }
    }
}
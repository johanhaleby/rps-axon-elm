package se.haleby.rps.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.groupingBy;

@Data
@Accessors(fluent = true)
class Round {
    private static final int NUMBER_OF_MOVES_PER_ROUND = 2;
    private final int roundNumber;
    private Set<PlayerMove> moves = new HashSet<>(NUMBER_OF_MOVES_PER_ROUND);

    private boolean hasPlayed(Player player) {
        return moves.stream().anyMatch(playerMove -> playerMove.isMadeBy(player));
    }

    boolean isEnded() {
        State state = state();
        return state == State.WON || state == State.TIED;
    }

    Player winner() {
        return determineWinnerFromMoves();
    }

    boolean hasWinner() {
        return determineWinnerFromMoves() != null;
    }

    void play(Player player, Move move) {
        if (hasPlayed(player) || isEnded()) {
            return;
        }

        PlayerMove playerMove = PlayerMove.make(player, move);
        moves.add(playerMove);
    }

    private State state() {
        final State state;
        boolean hasPlayer1Played = hasPlayed(Player.ONE);
        boolean hasPlayer2Played = hasPlayed(Player.TWO);
        if (hasPlayer1Played && hasPlayer2Played) {
            if (hasWinner()) {
                state = State.WON;
            } else {
                state = State.TIED;
            }
        } else if (hasPlayer1Played || hasPlayer2Played) {
            state = State.ONGOING;
        } else {
            state = State.NOT_STARTED;
        }
        return state;
    }

    private Player determineWinnerFromMoves() {
        if (moves.size() != NUMBER_OF_MOVES_PER_ROUND) {
            return null;
        }

        Map<Player, List<PlayerMove>> playerMoves = moves.stream().collect(groupingBy(PlayerMove::player));
        PlayerMove playerMove1 = playerMoves.get(Player.ONE).get(0);
        PlayerMove playerMove2 = playerMoves.get(Player.TWO).get(0);

        if (playerMove1.move() == playerMove2.move()) {
            return null;
        } else if (playerMove1.beats(playerMove2)) {
            return Player.ONE;
        } else {
            return Player.TWO;
        }
    }

}
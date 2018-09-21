package se.haleby.rps.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import se.haleby.rps.domain.Result.ResultingState;

import java.util.LinkedHashSet;
import java.util.Set;

@Data
@Accessors(fluent = true)
public class Round {
    private static final int NUMBER_OF_MOVES_PER_ROUND = 2;
    private final int roundNumber;
    private Set<PlayerMove> moves = new LinkedHashSet<>(NUMBER_OF_MOVES_PER_ROUND);

    public boolean hasPlayed(Player player) {
        return moves.stream().anyMatch(playerMove -> playerMove.isMadeBy(player));
    }

    public boolean hasWinner() {
        return state() == State.WON;
    }

    public boolean isTied() {
        return state() == State.TIED;
    }

    public boolean isEnded() {
        State state = state();
        return state == State.WON || state == State.TIED;
    }

    public Player winner() {
        return hasWinner() ? deriveWinnerFromMoves(moves) : null;
    }

    public Round play(Player player, Move move) {
        if (hasPlayed(player) || isEnded()) {
            return this;
        }

        moves.add(PlayerMove.make(player, move));
        return this;
    }

    public Result result() {
        State state = state();
        if (isEnded()) {
            return new Result(ResultingState.valueOf(state.name()), state == State.WON ? winner() : null);
        }
        return null;
    }

    public State state() {
        final State state;
        if (hasPlayed(Player.ONE) && hasPlayed(Player.TWO)) {
            if (deriveWinnerFromMoves(moves) == null) {
                state = State.TIED;
            } else {
                state = State.WON;
            }
        } else if (hasPlayed(Player.ONE) || hasPlayed(Player.TWO)) {
            state = State.ONGOING;
        } else {
            state = State.NOT_STARTED;
        }
        return state;
    }

    private static Player deriveWinnerFromMoves(Set<PlayerMove> playerMoves) {
        if (playerMoves.size() != NUMBER_OF_MOVES_PER_ROUND) {
            return null;
        }

        PlayerMove[] playerMovesArray = playerMoves.toArray(new PlayerMove[NUMBER_OF_MOVES_PER_ROUND]);
        PlayerMove playerMove1 = playerMovesArray[0];
        PlayerMove playerMove2 = playerMovesArray[1];
        if (playerMove1.move() == playerMove2.move()) {
            return null;
        } else if (playerMove1.beats(playerMove2)) {
            return Player.ONE;
        } else {
            return Player.TWO;
        }
    }

}
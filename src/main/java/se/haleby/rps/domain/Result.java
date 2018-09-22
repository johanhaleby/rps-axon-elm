package se.haleby.rps.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
class Result {
    private final State state;
    private Player winner;

    public boolean isTied() {
        return state == State.TIED;
    }

    public boolean hasWinner() {
        return state == State.WON;
    }

    public boolean hasRoundEnded() {
        return isTied() || hasWinner();
    }

    public Player winner() {
        return winner;
    }
}

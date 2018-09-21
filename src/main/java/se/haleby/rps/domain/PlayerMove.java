package se.haleby.rps.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import static se.haleby.rps.domain.Move.*;

@Accessors(fluent = true)
@Data
class PlayerMove {
    private Player player;
    private Move move;

    static PlayerMove make(Player player, Move move) {
        return new PlayerMove().move(move).player(player);
    }

    boolean isMadeBy(Player player) {
        return this.player == player;
    }

    boolean beats(PlayerMove other) {
        Move otherMove = other.move;
        switch (this.move) {
            case ROCK:
                return otherMove == SCISSORS;
            case PAPER:
                return otherMove == ROCK;
            case SCISSORS:
                return otherMove == PAPER;
            default:
                return false;
        }
    }
}

package se.haleby.rps.projection.gameinfo;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.Wither;

import static lombok.AccessLevel.PACKAGE;

@Data
@Accessors(fluent = true)
@Wither(PACKAGE)
public class GameInfo {
    private final String gameId;
    private final String winner;
    private final String player1;
    private final String player2;
    private final GameInfoState state;
    private final boolean joinable;

    public boolean hasState(GameInfoState state) {
        return this.state == state;
    }
}

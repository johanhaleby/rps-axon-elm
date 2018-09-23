package se.haleby.rps.projection.endedgames;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.Wither;
import se.haleby.rps.domain.model.State;

import static lombok.AccessLevel.PACKAGE;

@Data
@Accessors(fluent = true)
@Wither(PACKAGE)
public class EndedGame {
    private final String gameId;
    private final String winnerId;
    private final String playerId1;
    private final String playerId2;
    private final State state;

    public boolean hasState(State state) {
        return this.state == state;
    }
}

package se.haleby.rps.projection.ongoinggames;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.Wither;

import static lombok.AccessLevel.PACKAGE;

@Data
@Accessors(fluent = true)
@Wither(PACKAGE)
public class OngoingGame {
    private final String gameId;
    private final String playerId1;
    private final String playerId2;
    private final boolean joinable;
}

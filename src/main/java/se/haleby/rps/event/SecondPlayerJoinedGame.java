package se.haleby.rps.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SecondPlayerJoinedGame {
    private final String gameId;
    private final String playerId;
}

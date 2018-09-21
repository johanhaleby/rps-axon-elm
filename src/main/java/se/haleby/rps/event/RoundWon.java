package se.haleby.rps.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoundWon {
    private final String gameId;
    private final int roundNumber;
    private final String winnerId;
}

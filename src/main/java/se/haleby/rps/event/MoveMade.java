package se.haleby.rps.event;

import lombok.Builder;
import lombok.Data;
import se.haleby.rps.domain.Move;

@Data
@Builder
public class MoveMade {
    private final String gameId;
    private final int round;
    private final String playerId;
    private final Move move;
}

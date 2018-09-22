package se.haleby.rps.domain.event;

import lombok.Builder;
import lombok.Data;
import se.haleby.rps.domain.model.Move;

@Data
@Builder
public class MoveMade {
    private final String gameId;
    private final int round;
    private final String playerId;
    private final Move move;
}

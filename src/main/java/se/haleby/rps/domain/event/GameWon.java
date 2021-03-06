package se.haleby.rps.domain.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameWon {
    private final String gameId;
    private final String winner;
}

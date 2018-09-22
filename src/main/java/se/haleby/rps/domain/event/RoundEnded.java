package se.haleby.rps.domain.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoundEnded {
    private final String gameId;
    private final int roundNumber;
}

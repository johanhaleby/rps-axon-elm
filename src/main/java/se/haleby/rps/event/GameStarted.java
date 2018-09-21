package se.haleby.rps.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameStarted {
    private final String gameId;
    private final String startedBy;
    private final int rounds;
}

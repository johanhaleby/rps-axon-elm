package se.haleby.rps.domain.event;

import lombok.Data;

@Data(staticConstructor = "withGameId")
public class GameEnded {
    private final String gameId;
}

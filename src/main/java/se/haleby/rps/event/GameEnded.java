package se.haleby.rps.event;

import lombok.Data;

@Data(staticConstructor = "withGameId")
public class GameEnded {
    private final String gameId;
}

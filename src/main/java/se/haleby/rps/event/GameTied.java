package se.haleby.rps.event;

import lombok.Data;

@Data(staticConstructor = "withGameId")
public class GameTied {
    private final String gameId;
}

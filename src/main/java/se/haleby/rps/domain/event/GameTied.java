package se.haleby.rps.domain.event;

import lombok.Data;

@Data(staticConstructor = "withGameId")
public class GameTied {
    private final String gameId;
}

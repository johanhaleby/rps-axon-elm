package se.haleby.rps.event;

import lombok.Data;

@Data
public class GameEnded {
    private final String gameId;
}

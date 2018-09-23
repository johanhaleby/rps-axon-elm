package se.haleby.rps.port.http;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import se.haleby.rps.domain.model.State;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@Data
@JsonInclude(NON_EMPTY)
class GameDTO {
    private final String gameId;
    private final String playerId1;
    private final String playerId2;
    private final String winnerId;
    private final State state;
    private final boolean joinable;
}

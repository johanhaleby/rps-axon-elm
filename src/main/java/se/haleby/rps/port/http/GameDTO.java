package se.haleby.rps.port.http;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@Data
@JsonInclude(NON_EMPTY)
class GameDTO {
    private final Date createdAt;
    private final String gameId;
    private final String player1;
    private final String player2;
    private final String winner;
    private final String state;
}

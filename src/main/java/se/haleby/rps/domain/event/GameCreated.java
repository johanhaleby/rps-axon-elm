package se.haleby.rps.domain.event;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class GameCreated {
    private final String gameId;
    private final String createdBy;
    private final int rounds;
    private final Date createdAt;

}

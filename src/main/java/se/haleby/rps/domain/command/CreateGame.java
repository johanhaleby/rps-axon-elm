package se.haleby.rps.domain.command;

import lombok.Builder;
import lombok.Data;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

import java.util.Date;

@Data
@Builder
public class CreateGame {
    @TargetAggregateIdentifier
    private final String gameId;
    private final String creator;
    private final int rounds;
    private final Date creationDate;
}

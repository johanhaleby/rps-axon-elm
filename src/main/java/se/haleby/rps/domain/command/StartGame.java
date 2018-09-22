package se.haleby.rps.domain.command;

import lombok.Builder;
import lombok.Data;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Data
@Builder
public class StartGame {
    @TargetAggregateIdentifier
    private final String gameId;
    private final String startedBy;
    private final int rounds;
}

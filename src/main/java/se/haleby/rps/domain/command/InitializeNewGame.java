package se.haleby.rps.domain.command;

import lombok.Builder;
import lombok.Data;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Data
@Builder
public class InitializeNewGame {
    @TargetAggregateIdentifier
    private final String gameId;
    private final String initializer;
    private final int rounds;
}

package se.haleby.rps.command;

import lombok.Builder;
import lombok.Data;
import org.axonframework.commandhandling.TargetAggregateIdentifier;
import se.haleby.rps.domain.Move;

@Data
@Builder
public class MakeMove {
    @TargetAggregateIdentifier
    private final String gameId;
    private final String playerId;
    private final Move move;
}

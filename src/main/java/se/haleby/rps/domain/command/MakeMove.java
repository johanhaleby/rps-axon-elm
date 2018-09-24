package se.haleby.rps.domain.command;

import lombok.Builder;
import lombok.Data;
import org.axonframework.commandhandling.TargetAggregateIdentifier;
import se.haleby.rps.domain.model.Move;

@Data
@Builder
public class MakeMove {
    @TargetAggregateIdentifier
    private final String gameId;
    private final String player;
    private final Move move;
}

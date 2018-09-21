package se.haleby.rps.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Result {
    enum ResultingState {
        TIED, WON
    }

    private final ResultingState state;
    private final Player winner;
}

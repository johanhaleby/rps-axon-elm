package se.haleby.rps.projection.endedgames;

import org.axonframework.eventhandling.EventHandler;
import se.haleby.rps.domain.event.FirstPlayerJoinedGame;
import se.haleby.rps.domain.event.GameTied;
import se.haleby.rps.domain.event.GameWon;
import se.haleby.rps.domain.event.SecondPlayerJoinedGame;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static se.haleby.rps.domain.model.State.*;

public class EndedGamesProjection {

    private final Map<String, EndedGame> endedGames = new ConcurrentHashMap<>();

    @EventHandler
    public void when(FirstPlayerJoinedGame evt) {
        endedGames.put(evt.getGameId(), new EndedGame(evt.getGameId(), null, evt.getPlayerId(), null, ONGOING));
    }

    @EventHandler
    public void when(SecondPlayerJoinedGame evt) {
        endedGames.computeIfPresent(evt.getGameId(), (__, endedGame) -> endedGame.withPlayerId2(evt.getPlayerId()));
    }

    @EventHandler
    public void when(GameWon evt) {
        endedGames.computeIfPresent(evt.getGameId(), (__, endedGame) -> endedGame.withState(WON).withWinnerId(evt.getWinnerId()));
    }

    @EventHandler
    public void when(GameTied evt) {
        endedGames.computeIfPresent(evt.getGameId(), (__, endedGame) -> endedGame.withState(TIED));
    }

    public List<EndedGame> findAll() {
        return endedGames.values().stream().filter(game -> !game.hasState(ONGOING)).collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    public Optional<EndedGame> findById(String gameId) {
        return Optional.ofNullable(endedGames.get(gameId));
    }
}
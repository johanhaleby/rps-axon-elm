package se.haleby.rps.projection.ongoinggames;

import org.axonframework.eventhandling.EventHandler;
import se.haleby.rps.domain.event.FirstPlayerJoinedGame;
import se.haleby.rps.domain.event.GameEnded;
import se.haleby.rps.domain.event.GameStarted;
import se.haleby.rps.domain.event.SecondPlayerJoinedGame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OngoingGamesProjection {

    private final Map<String, OngoingGame> ongoingGames = new ConcurrentHashMap<>();

    @EventHandler
    public void when(GameStarted evt) {
        ongoingGames.put(evt.getGameId(), new OngoingGame(evt.getGameId(), null, null, true));
    }

    @EventHandler
    public void when(FirstPlayerJoinedGame evt) {
        ongoingGames.computeIfPresent(evt.getGameId(), (__, ongoingGame) -> ongoingGame.withPlayerId1(evt.getPlayerId()));
    }

    @EventHandler
    public void when(SecondPlayerJoinedGame evt) {
        ongoingGames.computeIfPresent(evt.getGameId(), (__, ongoingGame) -> ongoingGame.withPlayerId2(evt.getPlayerId()).withJoinable(false));
    }

    @EventHandler
    public void when(GameEnded evt) {
        ongoingGames.remove(evt.getGameId());
    }

    public List<OngoingGame> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(ongoingGames.values()));
    }
}
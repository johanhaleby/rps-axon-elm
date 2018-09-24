package se.haleby.rps.projection.gameinfo;

import org.axonframework.eventhandling.EventHandler;
import se.haleby.rps.domain.event.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static se.haleby.rps.projection.gameinfo.GameInfoState.*;


public class GameInfoProjection {

    private static final Predicate<GameInfo> ALL_PREDICATE = __ -> true;
    private final Map<String, GameInfo> games = new ConcurrentHashMap<>();

    @EventHandler
    public void when(NewGameInitialized evt) {
        games.put(evt.getGameId(), new GameInfo(evt.getGameId(), null, null, null, JOINABLE, true));
    }

    @EventHandler
    public void when(FirstPlayerJoinedGame evt) {
        games.computeIfPresent(evt.getGameId(), (__, startedGame) -> startedGame.withPlayer1(evt.getPlayer()));
    }

    @EventHandler
    public void when(SecondPlayerJoinedGame evt) {
        games.computeIfPresent(evt.getGameId(), (__, gameInfo) -> gameInfo.withPlayer2(evt.getPlayer()).withState(STARTED));
    }

    @EventHandler
    public void when(GameWon evt) {
        games.computeIfPresent(evt.getGameId(), (__, gameInfo) -> gameInfo.withState(ENDED).withWinnerId(evt.getWinnerId()));
    }

    @EventHandler
    public void when(GameTied evt) {
        games.computeIfPresent(evt.getGameId(), (__, gameInfo) -> gameInfo.withState(ENDED));
    }

    public List<GameInfo> find(Predicate<GameInfo> predicate) {
        return games.values().stream().filter(predicate).collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    public Optional<GameInfo> findById(String gameId) {
        return Optional.ofNullable(games.get(gameId));
    }

    public static class Predicates {
        public static Predicate<GameInfo> all() {
            return ALL_PREDICATE;
        }

        public static Predicate<GameInfo> hasState(GameInfoState state) {
            return gameInfo -> gameInfo.hasState(state);
        }
    }


}
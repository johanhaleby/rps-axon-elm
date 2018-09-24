package se.haleby.rps.port.http;

import io.javalin.Javalin;
import se.haleby.rps.application.GameApplicationService;
import se.haleby.rps.domain.model.Move;
import se.haleby.rps.projection.gameinfo.GameInfo;
import se.haleby.rps.projection.gameinfo.GameInfoProjection;
import se.haleby.rps.projection.gameinfo.GameInfoProjection.Predicates;
import se.haleby.rps.projection.gameinfo.GameInfoState;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static se.haleby.rps.projection.gameinfo.GameInfoProjection.Predicates.all;

public class GameApi {

    private final Javalin app;
    private final int port;

    public GameApi(int port, GameApplicationService gameApplicationService, GameInfoProjection gameInfoProjection) {
        this.port = port;
        app = Javalin.create();
        app.contextPath("/api/games")
                .defaultContentType("text/plain")
                .enableAutogeneratedEtags()
                .disableStartupBanner()
                .enableCaseSensitiveUrls();

        // API
        app.put("/:gameId", ctx -> {
            String gameId = requireNonNull(ctx.pathParam("gameId"));
            String player = requireNonNull(ctx.header("player"));
            String move = ctx.formParam("move");

            CompletableFuture<?> result = move == null ? gameApplicationService.startGame(gameId, player) : gameApplicationService.makeMove(gameId, player, Move.valueOf(move.toUpperCase()));
            ctx.json(result.thenApply(__ -> findGame(gameId, gameInfoProjection)));
        });

        app.get("", ctx -> {
            Predicate<GameInfo> predicate = ctx.queryParams("state").stream()
                    .map(stateString -> GameInfoState.valueOf(stateString.trim().toUpperCase()))
                    .map(Predicates::hasState)
                    .reduce(Predicate::or)
                    .orElse(all());

            List<GameDTO> games = gameInfoProjection.find(predicate).stream().map(GAME_INFO_TO_DTO).collect(Collectors.toList());
            ctx.json(games);
        });

        app.get("/:gameId", ctx -> {
            String gameId = requireNonNull(ctx.pathParam("gameId"));
            ctx.json(findGame(gameId, gameInfoProjection));
        });
    }

    public void start() {
        app.start(port);
    }

    public void stop() {
        app.stop();
    }

    private static final Function<GameInfo, GameDTO> GAME_INFO_TO_DTO = gameInfo -> new GameDTO(gameInfo.gameId(), gameInfo.player1(), gameInfo.player2(), gameInfo.winnerId(), gameInfo.state().name().toLowerCase());

    private static GameDTO findGame(String gameId, GameInfoProjection projection) {
        return projection.findById(gameId).map(GAME_INFO_TO_DTO).orElseThrow(() -> new IllegalStateException("Internal error: Couldn't find game in started games or ended games"));
    }
}
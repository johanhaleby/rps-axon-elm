package se.haleby.rps;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import io.javalin.staticfiles.Location;
import lombok.Data;
import lombok.experimental.Accessors;
import org.axonframework.config.Configuration;
import org.axonframework.config.DefaultConfigurer;
import org.axonframework.config.EventHandlingConfiguration;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.haleby.rps.application.GameApplicationService;
import se.haleby.rps.domain.model.Game;
import se.haleby.rps.port.http.GameApi;
import se.haleby.rps.projection.gameinfo.GameInfoProjection;

import java.util.Arrays;
import java.util.List;

public class GameServer {
    private static final Logger log = LoggerFactory.getLogger(GameServer.class);

    private final Configuration axon;
    private final GameApi gameApi;

    public static void main(String[] args) {
        CmdArgs cmdArgs = new CmdArgs();
        JCommander jCommander = new JCommander(cmdArgs);
        jCommander.setProgramName("rock-paper-scissors");
        jCommander.parse(args);
        if (cmdArgs.help) {
            jCommander.usage();
            return;
        }
        GameServer gameServer = new GameServer(cmdArgs);
        Runtime.getRuntime().addShutdownHook(new Thread(gameServer::stop));
        gameServer.start();
    }

    public GameServer(CmdArgs args) {
        log.info("Starting {} with args: {}", GameServer.class.getSimpleName(), args);

        GameInfoProjection gameInfoProjection = new GameInfoProjection();

        axon = configureAxon(gameInfoProjection);
        GameApplicationService gameApplicationService = new GameApplicationService(axon.commandGateway(), args.rounds);
        gameApi = new GameApi(args.port, args.directoriesLoadLocation, args.staticDirectories, gameApplicationService, gameInfoProjection);
    }

    public GameServer start() {
        axon.start();
        gameApi.start();
        return this;
    }

    public void stop() {
        gameApi.stop();
        axon.shutdown();
    }

    private static Configuration configureAxon(Object... projections) {
        EventHandlingConfiguration eventHandlingConfiguration = new EventHandlingConfiguration();
        Arrays.stream(projections).forEach(projection -> eventHandlingConfiguration.registerEventHandler(__ -> projection));
        return DefaultConfigurer.defaultConfiguration()
                .configureAggregate(Game.class)
                .configureEventStore(c -> new EmbeddedEventStore(new InMemoryEventStorageEngine())) //(2)
                .registerModule(eventHandlingConfiguration) // (3)
                // .registerQueryHandler(c -> projection) // (4)
                .buildConfiguration();
    }

    @Data(staticConstructor = "with")
    @Accessors(fluent = true)
    public static class CmdArgs {
        @Parameter(names = {"-d", "--static-directories"}, description = "Directories from where static files are served")
        private List<String> staticDirectories = Arrays.asList("/static", "/compiled-static");

        @Parameter(names = {"-l", "--directories-load-location"}, description = "Load location of the static directories")
        private Location directoriesLoadLocation = Location.CLASSPATH;

        @Parameter(names = {"-p", "--port"}, description = "Port")
        private int port = 8080;

        @Parameter(names = {"-r", "--rounds"}, description = "Number of rounds in game")
        private int rounds = 3;

        @SuppressWarnings("unused")
        @Parameter(names = {"-h", "--help"}, help = true, hidden = true)
        private boolean help;
    }
}

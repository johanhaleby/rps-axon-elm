package se.haleby.rps;

import org.axonframework.config.Configuration;
import org.axonframework.config.DefaultConfigurer;
import org.axonframework.config.EventHandlingConfiguration;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine;
import se.haleby.rps.application.GameApplicationService;
import se.haleby.rps.domain.model.Game;
import se.haleby.rps.port.http.GameApi;

public class GameServer {

    private static final int ROUNDS_IN_GAME = 3;

    private final Configuration axon;
    private final GameApi gameApi;

    public static void main(String[] args) {
        GameServer gameServer = new GameServer(8080);
        Runtime.getRuntime().addShutdownHook(new Thread(gameServer::stop));
        gameServer.start();
    }

    public GameServer(int port) {
        axon = configureAxon();
        gameApi = new GameApi(port, new GameApplicationService(axon.commandGateway(), ROUNDS_IN_GAME));
    }

    public GameServer start() {
        axon.start();
        gameApi.start();
        return this;
    }

    public GameServer stop() {
        gameApi.stop();
        axon.shutdown();
        return this;
    }


    private static Configuration configureAxon() {
        EventHandlingConfiguration eventHandlingConfiguration = new EventHandlingConfiguration();
        // eventHandlingConfiguration.registerEventHandler(c -> projection);
        return DefaultConfigurer.defaultConfiguration()
                .configureAggregate(Game.class)
                .configureEventStore(c -> new EmbeddedEventStore(new InMemoryEventStorageEngine())) //(2)
                .registerModule(eventHandlingConfiguration) // (3)
                // .registerQueryHandler(c -> projection) // (4)
                .buildConfiguration();
    }
}

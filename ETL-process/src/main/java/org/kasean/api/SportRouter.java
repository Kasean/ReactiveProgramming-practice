package org.kasean.api;

import org.kasean.exceptions.SportAlreadyExistsException;
import org.kasean.services.SportService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;


@Configuration
public class SportRouter {

    @Bean
    public RouterFunction<ServerResponse> sportRoutes(SportService handler) {
        return route()
                .POST("/api/v1/sport/{sportName}/{sportPrice}", handler::createSport)
                .GET("/api/v1/sport", handler::searchSports)
                .build();
    }
}

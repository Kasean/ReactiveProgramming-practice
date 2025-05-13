package org.kasean.services;

import org.kasean.exceptions.SportAlreadyExistsException;
import org.kasean.models.Sport;
import org.kasean.repositories.SportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.ErrorResponse;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Service
public class SportService implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger("SportService");

    private final RakutenService rakutenService;
    private final SportRepository sportRepository;

    public SportService(RakutenService rakutenService, SportRepository sportRepository) {
        this.rakutenService = rakutenService;
        this.sportRepository = sportRepository;
    }

    @Override
    public void run(String... args) {
        rakutenService.fetchAllItemsWithBackpressure()
                .map(item -> new Sport(item.getName(), item.getPrice()))
                .flatMap(sportRepository::save)
                .subscribe(sport -> LOGGER.info("Saved: {}", sport),
                        error -> LOGGER.error("Error: {}", error.getMessage()),
                        () -> LOGGER.info("Initialization completed"));
    }

    public Mono<ServerResponse> createSport(ServerRequest request) {
        String sportName = request.pathVariable("sportName");
        Integer sportPrice = Integer.parseInt(request.pathVariable("sportPrice"));

        return sportRepository.existsByName(sportName)
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return ServerResponse
                                .status(HttpStatus.CONFLICT)
                                .bodyValue(ErrorResponse.create(new SportAlreadyExistsException(sportName),
                                        HttpStatus.CONFLICT, ""));
                    }

                    return sportRepository.save(new Sport(sportName, sportPrice))
                            .flatMap(saved -> ServerResponse
                                    .ok()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(saved)
                                    .onErrorResume(e -> ServerResponse
                                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                            .bodyValue(ErrorResponse.create(e,
                                                    HttpStatus.INTERNAL_SERVER_ERROR,
                                                    "Internal server error"))));
                });
    }

    public Mono<ServerResponse> searchSports(ServerRequest request) {
        String query = request.queryParam("q").orElse("");

        return sportRepository.findByNameContainingIgnoreCase(query)
                .collectList()
                .flatMap(items -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(items))
                .onErrorResume(e -> ServerResponse
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .bodyValue(ErrorResponse.create(e,
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "Failed to search sports")));
    }
}

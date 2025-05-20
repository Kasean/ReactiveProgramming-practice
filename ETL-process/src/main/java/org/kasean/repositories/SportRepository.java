package org.kasean.repositories;

import org.kasean.models.Sport;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface SportRepository extends R2dbcRepository<Sport, Long> {

    Mono<Boolean> existsByName(String name);

    @Query("SELECT * FROM sports WHERE LOWER(name) LIKE LOWER(CONCAT('%', :query, '%'))")
    Flux<Sport> findByNameContainingIgnoreCase(String query);

    Mono<Sport> findByName(String name);
}

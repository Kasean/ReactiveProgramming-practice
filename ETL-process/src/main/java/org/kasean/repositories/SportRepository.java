package org.kasean.repositories;

import org.kasean.models.Sport;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface SportRepository extends R2dbcRepository<Sport, Integer> {
    Mono<Sport> findByName(String name);
}

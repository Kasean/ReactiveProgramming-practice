package org.kasean.services;

import org.kasean.models.Sport;
import org.kasean.repositories.SportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

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
}

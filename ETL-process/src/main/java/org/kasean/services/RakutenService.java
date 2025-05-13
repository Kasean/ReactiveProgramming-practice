package org.kasean.services;

import org.kasean.models.RakutenApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RakutenService {

    private static final Logger LOGGER = LoggerFactory.getLogger("RakutenService");

    private final WebClient webClient;

    private static final String BASE_URL = "https://app.rakuten.co.jp/services/api/IchibaItem/Search/20220601";
    private static final String APP_ID = "1071139417162331226";

    public RakutenService() {
        this.webClient = WebClient.builder()
                .baseUrl(BASE_URL)
                .build();
    }

    public Flux<RakutenApiResponse.Item> fetchAllItemsWithBackpressure() {
        return fetchFirstPage()
                .flatMapMany(firstPageResponse -> {
                    int totalPages = firstPageResponse.getPageCount();

                    AtomicInteger processedPages = new AtomicInteger(0);

                    return Flux.range(1, totalPages)
                            .delayElements(Duration.ofMillis(500))
                            .onBackpressureBuffer(20,
                                    page -> LOGGER.error("Buffer overflow for page: {}", page))
                            .flatMap(page -> fetchPage(page)
                                    .subscribeOn(Schedulers.parallel()), Runtime.getRuntime().availableProcessors())
                            .doOnNext(items -> {
                                int current = processedPages.incrementAndGet();
                                if (current % 10 == 0) {
                                    LOGGER.info(String.format("Processed %d/%d pages (%.1f%%)%n",
                                            current, totalPages, (current * 100.0 / totalPages)));
                                }
                            })
                            .onErrorContinue((error, page) ->
                                    LOGGER.error("Failed to process page {}: {}", page, error.getMessage()))
                            .flatMap(Flux::fromIterable);
                });
    }

    private Mono<RakutenApiResponse> fetchFirstPage() {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("format", "json")
                        .queryParam("genreId", "566095")
                        .queryParam("formatVersion", "1")
                        .queryParam("page", 1)
                        .queryParam("applicationId", APP_ID)
                        .build())
                .retrieve()
                .bodyToMono(RakutenApiResponse.class)
                .timeout(Duration.ofSeconds(10))
                .retry(3)
                .doOnSubscribe(sub -> LOGGER.info("Fetching first page..."))
                .doOnSuccess(resp -> LOGGER.info("First page loaded, total pages: {}", resp.getPageCount()));
    }

    private Mono<List<RakutenApiResponse.Item>> fetchPage(int page) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("format", "json")
                        .queryParam("genreId", "566095")
                        .queryParam("formatVersion", "1")
                        .queryParam("page", page)
                        .queryParam("applicationId", APP_ID)
                        .build())
                .retrieve()
                .bodyToMono(RakutenApiResponse.class)
                .map(RakutenApiResponse::getItems)
                .map(this::getItems)
                .timeout(Duration.ofSeconds(5))
                .retry(2)
                .doOnSubscribe(sub -> LOGGER.info("Starting fetch for page: {}", page))
                .doOnSuccess(items -> LOGGER.info("Completed page: {}, items: {}", page, items.size()))
                .doOnError(e -> LOGGER.error("Error fetching page {}: {}", page, e.getMessage()));
    }

    private List<RakutenApiResponse.Item> getItems(List<RakutenApiResponse.ItemWrapper> list) {
        return list.parallelStream().map(RakutenApiResponse.ItemWrapper::getItem).toList();
    }
}
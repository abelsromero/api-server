package org.bcnjug.jbcn.api.papers;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
public class PapersController {

    private final PapersRepository papersRepository;

    PapersController(PapersRepository papersRepository) {
        this.papersRepository = papersRepository;
    }

    @GetMapping(value = "/papers/{id}", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    Mono<Paper> getPaper(@PathVariable String id) {
        return papersRepository.findById(id);
    }

    @PostMapping(value = "/papers", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    Mono<Map<String, String>> createPaper(@RequestBody Paper paper, Authentication principal) {

        LocalDateTime now = LocalDateTime.now();
        paper.setCreatedOn(now);
        paper.setUpdatedOn(now);
        if (principal != null) {
            paper.setCreatedBy(principal.getName());
            paper.setUpdatedBy(principal.getName());
        }

        return papersRepository.save(paper)
                .map(createdPaper -> Map.of("id", createdPaper.getId()));
    }
}

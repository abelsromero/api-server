package org.bcnjug.jbcn.api.papers;

import org.bcnjug.jbcn.api.papers.Paper;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PapersRepository extends ReactiveMongoRepository<Paper, String> {
}

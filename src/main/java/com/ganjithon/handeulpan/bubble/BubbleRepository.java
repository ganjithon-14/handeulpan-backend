package com.ganjithon.handeulpan.bubble;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BubbleRepository extends JpaRepository<Bubble, Long> {

    Optional<Bubble> findBySheepId(Long sheepId);

    /** 아직 살아 있는(createdAt이 since보다 뒤인) 말풍선만 */
    List<Bubble> findBySheepIdInAndCreatedAtAfter(Collection<Long> sheepIds, Instant since);
}

package com.ganjithon.handeulpan.scarf;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ScarfRepository extends JpaRepository<Scarf, Long> {

    boolean existsByFromSheepIdAndToSheepIdAndSentOn(Long fromSheepId, Long toSheepId, LocalDate sentOn);

    /** 내 양이 받은 목도리, 최근 것부터 */
    List<Scarf> findByToSheepIdOrderByIdDesc(Long toSheepId);

    /** 내 양이 그날 목도리를 보낸 양들의 id */
    @Query("select s.toSheep.id from Scarf s where s.fromSheep.id = :fromSheepId and s.sentOn = :sentOn")
    List<Long> findToSheepIdsSentBy(Long fromSheepId, LocalDate sentOn);
}

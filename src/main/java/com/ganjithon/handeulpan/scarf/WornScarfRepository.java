package com.ganjithon.handeulpan.scarf;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface WornScarfRepository extends JpaRepository<WornScarf, Long> {

    /** 들판 조회에서 양마다 쿼리가 나가지 않도록 목도리를 한 번에 가져옵니다. */
    @Query("select w from WornScarf w join fetch w.scarf where w.sheepId in :sheepIds")
    List<WornScarf> findAllWithScarf(Collection<Long> sheepIds);
}

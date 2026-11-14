package com.ganjithon.handeulpan.sheep;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SheepRepository extends JpaRepository<Sheep, Long> {

    /** 들판에 사는 양 (나간 양 제외) */
    @Query("select s from Sheep s join fetch s.user where s.pasture.id = :pastureId and s.leftAt is null "
            + "order by s.joinedAt, s.id")
    List<Sheep> findAllInPasture(Long pastureId);

    long countByPastureIdAndLeftAtIsNull(Long pastureId);

    /** 나간 양까지 포함해서 찾음: 다시 합류할 때 같은 행을 되살리기 위해 */
    Optional<Sheep> findByPastureIdAndUserId(Long pastureId, Long userId);

    Optional<Sheep> findByPastureIdAndUserIdAndLeftAtIsNull(Long pastureId, Long userId);

    @Query("select s from Sheep s join fetch s.pasture where s.user.id = :userId and s.leftAt is null "
            + "order by s.joinedAt desc, s.id desc")
    List<Sheep> findAllOfUser(Long userId);
}

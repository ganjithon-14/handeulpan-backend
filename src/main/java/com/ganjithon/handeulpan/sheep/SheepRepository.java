package com.ganjithon.handeulpan.sheep;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SheepRepository extends JpaRepository<Sheep, Long> {

    @Query("select s from Sheep s join fetch s.user where s.pasture.id = :pastureId order by s.joinedAt, s.id")
    List<Sheep> findAllInPasture(Long pastureId);

    long countByPastureId(Long pastureId);

    boolean existsByPastureIdAndUserId(Long pastureId, Long userId);

    Optional<Sheep> findByPastureIdAndUserId(Long pastureId, Long userId);

    @Query("select s from Sheep s join fetch s.pasture where s.user.id = :userId order by s.joinedAt desc, s.id desc")
    List<Sheep> findAllOfUser(Long userId);
}

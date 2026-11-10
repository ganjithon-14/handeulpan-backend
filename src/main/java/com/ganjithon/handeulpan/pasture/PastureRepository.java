package com.ganjithon.handeulpan.pasture;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface PastureRepository extends JpaRepository<Pasture, Long> {

    Optional<Pasture> findByPublicId(String publicId);

    boolean existsByPublicId(String publicId);

    /** 합류할 때 6마리 제한이 동시 요청에도 지켜지도록 들판 행을 잠급니다. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pasture p where p.publicId = :publicId")
    Optional<Pasture> findByPublicIdForUpdate(String publicId);
}

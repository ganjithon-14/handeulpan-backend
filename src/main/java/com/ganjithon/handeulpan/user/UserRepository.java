package com.ganjithon.handeulpan.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByDeviceToken(String deviceToken);
}

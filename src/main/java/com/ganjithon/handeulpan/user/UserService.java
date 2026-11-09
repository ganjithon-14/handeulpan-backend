package com.ganjithon.handeulpan.user;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public String issueDeviceToken() {
        String token = UUID.randomUUID().toString();
        userRepository.save(new User(token));
        return token;
    }
}

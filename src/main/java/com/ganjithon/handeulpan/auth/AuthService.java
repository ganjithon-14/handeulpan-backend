package com.ganjithon.handeulpan.auth;

import com.ganjithon.handeulpan.auth.dto.LoginRequest;
import com.ganjithon.handeulpan.auth.dto.SignupRequest;
import com.ganjithon.handeulpan.config.ApiError;
import com.ganjithon.handeulpan.user.User;
import com.ganjithon.handeulpan.user.UserRepository;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /** 가입. 사용자와 계정을 함께 만들고, 바로 쓸 수 있는 토큰을 돌려줍니다. */
    @Transactional
    public String signup(SignupRequest request) {
        String email = normalize(request.email());
        String nickname = request.nickname();
        if (accountRepository.existsByEmail(email)) {
            throw ApiError.DUPLICATE_EMAIL.exception();
        }
        if (accountRepository.existsByNickname(nickname)) {
            throw ApiError.DUPLICATE_NICKNAME.exception();
        }

        User user = userRepository.save(new User(newToken()));
        try {
            accountRepository.saveAndFlush(
                    new Account(user, nickname, email, passwordEncoder.encode(request.password())));
        } catch (DataIntegrityViolationException e) {
            // 위 확인과 저장 사이에 같은 이메일·닉네임이 먼저 들어온 경우
            throw (accountRepository.existsByEmail(email) ? ApiError.DUPLICATE_EMAIL : ApiError.DUPLICATE_NICKNAME)
                    .exception();
        }
        return user.getDeviceToken();
    }

    /** 로그인. 맞으면 새 토큰을 발급합니다. (다른 기기에 있던 이전 토큰은 더 이상 통하지 않습니다) */
    @Transactional
    public String login(LoginRequest request) {
        Account account = accountRepository.findByEmail(normalize(request.email())).orElse(null);
        // 이메일이 없든 비밀번호가 틀리든 같은 에러로 답해서 어떤 이메일이 가입했는지 알려 주지 않습니다.
        if (account == null || !passwordEncoder.matches(request.password(), account.getPasswordHash())) {
            throw ApiError.INVALID_CREDENTIALS.exception();
        }
        User user = account.getUser();
        user.changeDeviceToken(newToken());
        return user.getDeviceToken();
    }

    /** 로그아웃. 토큰을 새 값으로 바꿔서 지금 쓰던 토큰이 더 이상 조회되지 않게 합니다. */
    @Transactional
    public void logout(User me) {
        me.changeDeviceToken(newToken());
        userRepository.save(me);
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static String newToken() {
        return UUID.randomUUID().toString();
    }
}

package com.ganjithon.handeulpan.auth;

import com.ganjithon.handeulpan.auth.dto.AuthRequest;
import com.ganjithon.handeulpan.auth.dto.LoginRequest;
import com.ganjithon.handeulpan.config.ApiError;
import com.ganjithon.handeulpan.user.User;
import com.ganjithon.handeulpan.user.UserRepository;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 가입. 이미 기기 토큰을 가진 사용자가 가입하면 그 사용자(양·들판)에 계정을 붙이고 토큰은 그대로 둡니다.
     * 토큰이 없거나 모르는 토큰이면 새 사용자를 만듭니다. (알 수 없는 토큰에는 지킬 데이터가 없습니다)
     */
    @Transactional
    public String signup(String currentToken, AuthRequest request) {
        String username = normalize(request.username());
        if (accountRepository.existsByUsername(username)) {
            throw ApiError.DUPLICATE_USERNAME.exception();
        }

        User user = StringUtils.hasText(currentToken)
                ? userRepository.findByDeviceToken(currentToken.trim()).orElse(null)
                : null;
        if (user != null && accountRepository.existsByUserId(user.getId())) {
            throw ApiError.ALREADY_HAS_ACCOUNT.exception();
        }
        if (user == null) {
            user = userRepository.save(new User(newToken()));
        }

        accountRepository.save(new Account(user, username, passwordEncoder.encode(request.password())));
        return user.getDeviceToken();
    }

    /** 로그인. 맞으면 새 토큰을 발급합니다. (다른 기기에 있던 이전 토큰은 더 이상 통하지 않습니다) */
    @Transactional
    public String login(LoginRequest request) {
        Account account = accountRepository.findByUsername(normalize(request.username())).orElse(null);
        // 아이디가 없든 비밀번호가 틀리든 같은 에러로 답해서 어떤 아이디가 있는지 알려 주지 않습니다.
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

    private static String normalize(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }

    private static String newToken() {
        return UUID.randomUUID().toString();
    }
}

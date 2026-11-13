package com.ganjithon.handeulpan.auth;

import com.ganjithon.handeulpan.auth.dto.LoginRequest;
import com.ganjithon.handeulpan.auth.dto.SignupRequest;
import com.ganjithon.handeulpan.auth.dto.TokenResponse;
import com.ganjithon.handeulpan.user.User;
import com.ganjithon.handeulpan.user.auth.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 가입. 토큰 없이 호출하고, 가입하면 바로 로그인된 상태로 토큰을 돌려줍니다. */
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenResponse signup(@Valid @RequestBody SignupRequest request) {
        return new TokenResponse(authService.signup(request));
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return new TokenResponse(authService.login(request));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@CurrentUser User me) {
        authService.logout(me);
    }
}

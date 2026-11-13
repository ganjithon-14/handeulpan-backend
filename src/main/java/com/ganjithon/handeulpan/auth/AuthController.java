package com.ganjithon.handeulpan.auth;

import com.ganjithon.handeulpan.auth.dto.AuthRequest;
import com.ganjithon.handeulpan.auth.dto.LoginRequest;
import com.ganjithon.handeulpan.auth.dto.TokenResponse;
import com.ganjithon.handeulpan.user.User;
import com.ganjithon.handeulpan.user.auth.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 가입. 토큰이 있으면(X-Device-Token) 그 사용자에 계정을 붙입니다. 토큰은 없어도 됩니다. */
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenResponse signup(@RequestHeader(value = "X-Device-Token", required = false) String deviceToken,
                                @Valid @RequestBody AuthRequest request) {
        return new TokenResponse(authService.signup(deviceToken, request));
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

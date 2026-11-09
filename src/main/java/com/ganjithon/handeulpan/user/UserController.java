package com.ganjithon.handeulpan.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** 기기 토큰 발급. 앱은 받은 토큰을 브라우저에 저장하고 이후 모든 요청의 X-Device-Token 헤더로 보냅니다. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeviceTokenResponse issue() {
        return new DeviceTokenResponse(userService.issueDeviceToken());
    }

    public record DeviceTokenResponse(String deviceToken) {
    }
}

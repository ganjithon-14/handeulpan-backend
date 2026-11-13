package com.ganjithon.handeulpan.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** 사용자 한 명. 가입(닉네임·이메일·비밀번호)할 때 만들어지고, 요청은 이 사용자의 기기 토큰으로 구분합니다. */
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String deviceToken;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public User(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    /** 로그인·로그아웃 때 토큰을 바꿉니다. 이전 토큰은 조회되지 않으므로 자동으로 쓸 수 없게 됩니다. */
    public void changeDeviceToken(String newToken) {
        this.deviceToken = newToken;
    }
}

package com.ganjithon.handeulpan.auth;

import com.ganjithon.handeulpan.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 이메일·비밀번호 계정. 모든 {@link User}에 계정이 하나 있고, 기기를 바꿔도 로그인하면
 * 같은 사용자(양·들판)로 들어올 수 있습니다. users 테이블은 건드리지 않습니다.
 */
@Entity
@Table(name = "accounts")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /** 소문자로 정규화해서 저장합니다. (A@x.com과 a@x.com이 다른 계정이 되지 않게) */
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /** BCrypt 해시. 응답과 로그에 절대 내보내지 않습니다. */
    @Column(nullable = false, length = 60)
    private String passwordHash;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public Account(User user, String email, String passwordHash) {
        this.user = user;
        this.email = email;
        this.passwordHash = passwordHash;
    }
}

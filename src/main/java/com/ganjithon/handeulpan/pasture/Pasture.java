package com.ganjithon.handeulpan.pasture;

import com.ganjithon.handeulpan.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 들판. 링크 /p/{publicId}가 곧 초대장이라 publicId는 추측할 수 없는 랜덤 값입니다. (기획서 F1)
 * 내부 숫자 id는 API에 내보내지 않습니다.
 */
@Entity
@Table(name = "pastures")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Pasture {

    public static final int MAX_SHEEP = 6;
    public static final int NAME_MAX_LENGTH = 20;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String publicId;

    @Column(length = NAME_MAX_LENGTH)
    private String name;

    /** 들판장 (만든 사람). 내보내기(P1)에 씀 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public Pasture(String publicId, String name, User owner, Instant now) {
        this.publicId = publicId;
        this.name = name;
        this.owner = owner;
        this.createdAt = now;
    }
}

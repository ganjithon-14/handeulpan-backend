package com.ganjithon.handeulpan.bubble;

import com.ganjithon.handeulpan.sheep.Sheep;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 양이 머리 위에 띄우는 말풍선. 양당 최신 1개만 있고, 새로 올리면 같은 행을 교체합니다.
 * 24시간이 지나면 조회 응답에서 빠집니다. (행은 남아 있다가 다음에 올릴 때 교체됩니다.)
 */
@Entity
@Table(name = "bubbles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bubble {

    public static final int TEXT_MAX_LENGTH = 20;
    public static final Duration LIFETIME = Duration.ofHours(24);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sheep_id", nullable = false, unique = true)
    private Sheep sheep;

    /** 글자 수는 코드포인트로 세기 때문에 이모지가 섞이면 char 길이보다 짧을 수 있어 여유를 둡니다. */
    @Column(nullable = false, length = TEXT_MAX_LENGTH * 4)
    private String text;

    /** 위험 키워드가 있었지만 본인이 확인하고 올린 글. 친구 화면에서 말 걸기 가이드를 띄우는 데 씁니다. */
    @Column(nullable = false)
    private boolean flagged;

    @Column(nullable = false)
    private Instant createdAt;

    public Bubble(Sheep sheep, String text, boolean flagged, Instant now) {
        this.sheep = sheep;
        replace(text, flagged, now);
    }

    public void replace(String text, boolean flagged, Instant now) {
        this.text = text;
        this.flagged = flagged;
        this.createdAt = now;
    }

    public boolean isExpiredAt(Instant now) {
        return !createdAt.plus(LIFETIME).isAfter(now);
    }
}

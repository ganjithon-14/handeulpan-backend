package com.ganjithon.handeulpan.sheep;

import com.ganjithon.handeulpan.pasture.Pasture;
import com.ganjithon.handeulpan.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Duration;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 들판 하나에 사는 한 사람의 양. 한 사람이 여러 들판에 들어가면 들판마다 양이 따로 있습니다.
 *
 * <p>lastPattedAt은 거리 계산에만 쓰고 응답에는 절대 내보내지 않습니다. (기획서 9장 감시 방지)
 */
@Entity
@Table(name = "sheep", uniqueConstraints = @UniqueConstraint(columnNames = {"pasture_id", "user_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Sheep {

    public static final int NAME_MAX_LENGTH = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pasture_id")
    private Pasture pasture;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = NAME_MAX_LENGTH)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private WoolColor woolColor;

    @Column(nullable = false)
    private Instant lastPattedAt;

    /** "멀어지면 들판 친구들이 알 수 있어요" 동의 시각 (다시 합류하면 그때 다시 동의) */
    @Column(nullable = false)
    private Instant consentedAt;

    @Column(nullable = false)
    private Instant joinedAt;

    /** 낮잠이 끝나는 시각. 이 시각 전까지는 멀어지지 않고, 끝난 시점부터 거리 계산을 다시 시작합니다. 낮잠이 아니면 null */
    private Instant napUntil;

    /** 들판을 나간 시각. 나간 양은 목록·계산에서 빠집니다. (행은 지우지 않음: 말풍선·목도리 기록이 이 양을 참조) */
    private Instant leftAt;

    /** 합류 = 들판에 양을 풀어놓음 = 첫 쓰다듬기. 들어오자마자 가운데에 섭니다. */
    public Sheep(Pasture pasture, User user, String name, WoolColor woolColor, Instant now) {
        this.pasture = pasture;
        this.user = user;
        this.name = name;
        this.woolColor = woolColor;
        this.lastPattedAt = now;
        this.consentedAt = now;
        this.joinedAt = now;
    }

    public boolean isOwnedBy(User other) {
        return user.getId().equals(other.getId());
    }

    public void pat(Instant now) {
        this.lastPattedAt = now;
    }

    public boolean isActive() {
        return leftAt == null;
    }

    /** 들판 나가기. 언제든 한 번에. */
    public void leave(Instant now) {
        this.leftAt = now;
        this.napUntil = null;
    }

    /** 나갔던 들판에 다시 들어옴: 같은 양 행을 되살려 가운데에서 새로 시작합니다. */
    public void rejoin(String name, WoolColor woolColor, Instant now) {
        this.name = name;
        this.woolColor = woolColor;
        this.lastPattedAt = now;
        this.consentedAt = now;
        this.joinedAt = now;
        this.napUntil = null;
        this.leftAt = null;
    }

    public boolean isNapping(Instant now) {
        return napUntil != null && now.isBefore(napUntil);
    }

    public void startNap(Instant until) {
        this.napUntil = until;
    }

    /** 낮잠 깨우기. 이때부터 거리 계산을 다시 시작합니다. 낮잠 중이 아니면 아무것도 바꾸지 않습니다. */
    public void wakeUp(Instant now) {
        if (isNapping(now)) {
            this.napUntil = now;
        }
    }

    /** 거리 계산의 기준 시각: 마지막 쓰다듬기, 또는 그 뒤에 끝난 낮잠의 끝 시각 */
    public Instant distanceClockStart() {
        return napUntil != null && napUntil.isAfter(lastPattedAt) ? napUntil : lastPattedAt;
    }

    /** 데모 모드 전용: 마지막 쓰다듬은 시각을 과거로 되감습니다. */
    public void rewind(Duration duration) {
        this.lastPattedAt = lastPattedAt.minus(duration);
    }
}

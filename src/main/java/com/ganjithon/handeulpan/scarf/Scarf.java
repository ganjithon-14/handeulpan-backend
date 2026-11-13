package com.ganjithon.handeulpan.scarf;

import com.ganjithon.handeulpan.sheep.Sheep;
import com.ganjithon.handeulpan.sheep.WoolColor;
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
import java.time.Instant;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 한 양이 다른 양에게 보낸 목도리. 색은 보낼 때 보낸 양의 털 색으로 정해지고 이후 바뀌지 않습니다.
 *
 * <p>같은 친구에게는 하루(Asia/Seoul 날짜) 1회만 보낼 수 있어서 (보낸 양, 받은 양, 날짜)가 유일합니다.
 * 받은 목도리는 만료되지 않고 모두 보관합니다. 보낸 사람·시각·개수는 응답에 내보내지 않습니다.
 */
@Entity
@Table(name = "scarves", uniqueConstraints = @UniqueConstraint(columnNames = {"from_sheep_id", "to_sheep_id", "sent_on"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Scarf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_sheep_id")
    private Sheep fromSheep;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_sheep_id")
    private Sheep toSheep;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private WoolColor color;

    /** 보낸 날(Asia/Seoul). 하루 1회 규칙의 기준입니다. */
    @Column(name = "sent_on", nullable = false)
    private LocalDate sentOn;

    @Column(nullable = false)
    private Instant createdAt;

    public Scarf(Sheep fromSheep, Sheep toSheep, LocalDate sentOn, Instant now) {
        this.fromSheep = fromSheep;
        this.toSheep = toSheep;
        this.color = fromSheep.getWoolColor();
        this.sentOn = sentOn;
        this.createdAt = now;
    }
}

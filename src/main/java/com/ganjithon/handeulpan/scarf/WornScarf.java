package com.ganjithon.handeulpan.scarf;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 양이 지금 두르고 있는 목도리. 양당 최대 1개이고 행이 없으면 벗은 상태입니다.
 * sheep 테이블에 컬럼을 추가하지 않으려고 따로 둡니다.
 */
@Entity
@Table(name = "worn_scarves")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WornScarf {

    @Id
    @Column(name = "sheep_id")
    private Long sheepId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scarf_id", nullable = false)
    private Scarf scarf;

    public WornScarf(Long sheepId, Scarf scarf) {
        this.sheepId = sheepId;
        this.scarf = scarf;
    }

    public void change(Scarf scarf) {
        this.scarf = scarf;
    }
}

package com.ganjithon.handeulpan.pasture;

import com.ganjithon.handeulpan.sheep.Sheep;
import com.ganjithon.handeulpan.user.User;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * 들판 조회(GET /api/pastures/{id}) 응답에 다른 기능의 데이터를 덧붙이는 확장 지점.
 *
 * <p>BE-B는 이 인터페이스를 자기 패키지(bubble, scarf …)에서 {@code @Component}로 구현하면
 * PastureService를 고치지 않고 응답에 필드를 붙일 수 있습니다.
 *
 * <pre>{@code
 * @Component
 * class BubbleViewExtension implements PastureViewExtension {
 *     public Map<Long, Map<String, Object>> sheepFields(Context ctx) {
 *         // 양 id → {"bubble": {...}}
 *     }
 * }
 * }</pre>
 *
 * 붙인 필드는 각 양 객체(sheep[]) 또는 들판 객체 최상위에 그대로 펼쳐져 나갑니다.
 * BE-A가 이미 쓰는 필드 이름(id, name, woolColor, distance, mine, pattedToday, pastureId, mySheepId, sheep)과는 겹치지 않게 해 주세요.
 */
public interface PastureViewExtension {

    /** 양 id → 그 양 객체에 덧붙일 필드 */
    default Map<Long, Map<String, Object>> sheepFields(Context context) {
        return Map.of();
    }

    /** 들판 응답 최상위에 덧붙일 필드 (예: myScarves) */
    default Map<String, Object> pastureFields(Context context) {
        return Map.of();
    }

    /**
     * @param pasture 조회 중인 들판
     * @param viewer  조회하는 사람
     * @param mySheep 조회하는 사람의 양
     * @param sheep   들판의 모든 양 (내 양 포함)
     * @param now     서버 기준 현재 시각
     */
    record Context(Pasture pasture, User viewer, Sheep mySheep, List<Sheep> sheep, Instant now) {
    }
}

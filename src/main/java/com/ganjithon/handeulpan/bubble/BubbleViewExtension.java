package com.ganjithon.handeulpan.bubble;

import com.ganjithon.handeulpan.pasture.PastureViewExtension;
import com.ganjithon.handeulpan.sheep.Sheep;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 들판 조회 응답의 양마다 {@code bubble}을 붙입니다. 24시간이 지났거나 없으면 {@code null}입니다.
 *
 * <pre>{@code
 * "bubble": { "text": "오늘 입사 100일", "flagged": false, "createdAt": "2026-11-10T09:00:00Z" }
 * }</pre>
 */
@Component
@RequiredArgsConstructor
public class BubbleViewExtension implements PastureViewExtension {

    private final BubbleRepository bubbleRepository;

    @Override
    public Map<Long, Map<String, Object>> sheepFields(Context context) {
        List<Long> sheepIds = context.sheep().stream().map(Sheep::getId).toList();
        Instant aliveSince = context.now().minus(Bubble.LIFETIME);

        Map<Long, Map<String, Object>> fields = new HashMap<>();
        sheepIds.forEach(id -> fields.put(id, bubbleField(null)));
        bubbleRepository.findBySheepIdInAndCreatedAtAfter(sheepIds, aliveSince)
                .forEach(b -> fields.put(b.getSheep().getId(), bubbleField(b)));
        return fields;
    }

    private static Map<String, Object> bubbleField(Bubble bubble) {
        Map<String, Object> field = new HashMap<>();
        field.put("bubble", bubble == null ? null : new BubbleView(bubble.getText(), bubble.isFlagged(), bubble.getCreatedAt()));
        return field;
    }

    public record BubbleView(String text, boolean flagged, Instant createdAt) {
    }
}

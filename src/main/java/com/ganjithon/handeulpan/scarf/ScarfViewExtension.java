package com.ganjithon.handeulpan.scarf;

import com.ganjithon.handeulpan.common.config.TimeConfig;
import com.ganjithon.handeulpan.pasture.PastureViewExtension;
import com.ganjithon.handeulpan.sheep.Sheep;
import com.ganjithon.handeulpan.sheep.WoolColor;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 들판 조회 응답에 목도리 데이터를 붙입니다.
 *
 * <ul>
 *   <li>양마다 {@code wornScarf}: 지금 두른 목도리 색, 없으면 null</li>
 *   <li>내가 아닌 양마다 {@code sentScarfToday}: 오늘 내가 이 친구에게 이미 보냈는지 (내 양에는 붙이지 않음)</li>
 *   <li>들판 최상위 {@code myScarves}: 내가 받은 목도리 {@code [{id, color}]}, 최근 것이 먼저.
 *       개수가 드러나지 않도록 같은 색은 가장 최근 것 하나만 내려갑니다. (보낸 사람·시각은 내보내지 않음)</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class ScarfViewExtension implements PastureViewExtension {

    private final ScarfRepository scarfRepository;
    private final WornScarfRepository wornScarfRepository;

    @Override
    public Map<Long, Map<String, Object>> sheepFields(Context context) {
        List<Long> sheepIds = context.sheep().stream().map(Sheep::getId).toList();

        Map<Long, WoolColor> worn = new HashMap<>();
        wornScarfRepository.findAllWithScarf(sheepIds)
                .forEach(w -> worn.put(w.getSheepId(), w.getScarf().getColor()));

        LocalDate today = LocalDate.ofInstant(context.now(), TimeConfig.SEOUL);
        Set<Long> sentToday = new HashSet<>(scarfRepository.findToSheepIdsSentBy(context.mySheep().getId(), today));

        Map<Long, Map<String, Object>> fields = new HashMap<>();
        for (Sheep sheep : context.sheep()) {
            Map<String, Object> field = new HashMap<>();
            field.put("wornScarf", worn.get(sheep.getId()));
            if (!sheep.isOwnedBy(context.viewer())) {
                field.put("sentScarfToday", sentToday.contains(sheep.getId()));
            }
            fields.put(sheep.getId(), field);
        }
        return fields;
    }

    @Override
    public Map<String, Object> pastureFields(Context context) {
        Set<WoolColor> seen = new HashSet<>();
        List<MyScarf> myScarves = new ArrayList<>();
        for (Scarf scarf : scarfRepository.findByToSheepIdOrderByIdDesc(context.mySheep().getId())) {
            if (seen.add(scarf.getColor())) {
                myScarves.add(new MyScarf(scarf.getId(), scarf.getColor()));
            }
        }
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("myScarves", myScarves);
        return fields;
    }

    public record MyScarf(Long id, WoolColor color) {
    }
}

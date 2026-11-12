package com.ganjithon.handeulpan.bubble;

import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;

/**
 * 말풍선에 위험 키워드가 들어 있는지만 봅니다. 글 내용은 저장하거나 판단하지 않고 포함 여부만 돌려줍니다.
 *
 * <p>공백을 모두 없앤 글에서 찾기 때문에 "죽고 싶다"와 "죽고싶다"를 같게 봅니다.
 *
 * <p>TODO: 팀이 정한 키워드 목록(11/10 전달 예정)으로 교체합니다. 지금 목록은 임시입니다.
 */
@Component
public class RiskKeywordChecker {

    private static final List<String> KEYWORDS = List.of(
            "죽고싶", "죽을래", "죽어버", "자살", "자해", "목숨",
            "사라지고싶", "사라질래", "없어지고싶", "끝내고싶", "끝내버",
            "살기싫", "살고싶지않", "살아갈자신", "극단적", "우울");

    public boolean containsRisk(String text) {
        String normalized = normalize(text);
        return KEYWORDS.stream().anyMatch(normalized::contains);
    }

    private static String normalize(String text) {
        return text.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
    }
}

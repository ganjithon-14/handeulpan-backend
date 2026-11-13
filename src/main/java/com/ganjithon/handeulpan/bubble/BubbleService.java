package com.ganjithon.handeulpan.bubble;

import com.ganjithon.handeulpan.bubble.dto.BubbleRequest;
import com.ganjithon.handeulpan.bubble.dto.BubbleResponse;
import com.ganjithon.handeulpan.config.ApiError;
import com.ganjithon.handeulpan.sheep.Sheep;
import com.ganjithon.handeulpan.sheep.SheepFinder;
import com.ganjithon.handeulpan.user.User;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BubbleService {

    private final BubbleRepository bubbleRepository;
    private final SheepFinder sheepFinder;
    private final RiskKeywordChecker riskKeywordChecker;
    private final Clock clock;

    /** F4 말풍선 올리기: 내 양에만, 양당 1개(교체), 위험 키워드는 확인 없이는 올리지 않음 */
    @Transactional
    public BubbleResponse post(User me, Long sheepId, BubbleRequest request) {
        Sheep sheep = sheepFinder.requireMine(me, sheepId);
        String text = validated(request.text());

        boolean flagged = riskKeywordChecker.containsRisk(text);
        if (flagged && !request.confirmed()) {
            return new BubbleResponse(false, true);
        }

        Instant now = clock.instant();
        bubbleRepository.findBySheepId(sheep.getId()).ifPresentOrElse(
                bubble -> bubble.replace(text, flagged, now),
                () -> bubbleRepository.save(new Bubble(sheep, text, flagged, now)));
        return new BubbleResponse(true, flagged);
    }

    /** 말풍선 지우기. 이미 지웠거나 24시간이 지났으면 BUBBLE_NOT_FOUND */
    @Transactional
    public void delete(User me, Long sheepId) {
        Sheep sheep = sheepFinder.requireMine(me, sheepId);
        Bubble bubble = bubbleRepository.findBySheepId(sheep.getId())
                .filter(b -> !b.isExpiredAt(clock.instant()))
                .orElseThrow(ApiError.BUBBLE_NOT_FOUND::exception);
        bubbleRepository.delete(bubble);
    }

    /** 앞뒤 공백을 떼고 1~20자(이모지는 1자)인지 확인합니다. */
    private static String validated(String text) {
        String trimmed = text == null ? "" : text.strip();
        int length = trimmed.codePointCount(0, trimmed.length());
        if (length < 1 || length > Bubble.TEXT_MAX_LENGTH) {
            throw ApiError.BUBBLE_TEXT_INVALID.exception();
        }
        return trimmed;
    }
}

package com.ganjithon.handeulpan.scarf;

import com.ganjithon.handeulpan.common.config.TimeConfig;
import com.ganjithon.handeulpan.config.ApiError;
import com.ganjithon.handeulpan.scarf.dto.SendScarfResponse;
import com.ganjithon.handeulpan.scarf.dto.WearScarfRequest;
import com.ganjithon.handeulpan.scarf.dto.WearScarfResponse;
import com.ganjithon.handeulpan.sheep.Sheep;
import com.ganjithon.handeulpan.sheep.SheepFinder;
import com.ganjithon.handeulpan.user.User;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScarfService {

    private final ScarfRepository scarfRepository;
    private final WornScarfRepository wornScarfRepository;
    private final SheepFinder sheepFinder;
    private final Clock clock;

    /**
     * F7 목도리 보내기. 같은 들판 멤버에게만, 자기 자신은 안 되고, 같은 친구에게 하루 1회.
     * 받은 양은 이 목도리를 바로 두릅니다.
     */
    @Transactional
    public SendScarfResponse send(User me, Long toSheepId) {
        Sheep to = sheepFinder.requireVisibleTo(me, toSheepId);
        if (to.isOwnedBy(me)) {
            throw ApiError.SELF.exception();
        }
        Sheep from = sheepFinder.requireMineInSamePasture(me, to);

        Instant now = clock.instant();
        LocalDate today = today(now);
        if (scarfRepository.existsByFromSheepIdAndToSheepIdAndSentOn(from.getId(), to.getId(), today)) {
            throw ApiError.ALREADY_SENT_TODAY.exception();
        }

        Scarf scarf;
        try {
            scarf = scarfRepository.saveAndFlush(new Scarf(from, to, today, now));
        } catch (DataIntegrityViolationException e) {
            // 동시에 두 번 보낸 경우: 유일 제약이 막아 줍니다.
            throw ApiError.ALREADY_SENT_TODAY.exception();
        }
        wear(to.getId(), scarf);
        return new SendScarfResponse(scarf.getColor());
    }

    /** 내 양의 목도리 두르기·벗기. scarfId가 null이면 벗습니다. */
    @Transactional
    public WearScarfResponse wear(User me, Long sheepId, WearScarfRequest request) {
        Sheep sheep = sheepFinder.requireMine(me, sheepId);

        if (request.scarfId() == null) {
            wornScarfRepository.deleteById(sheep.getId());
            return new WearScarfResponse(null);
        }
        Scarf scarf = scarfRepository.findById(request.scarfId())
                .filter(s -> s.getToSheep().getId().equals(sheep.getId()))
                .orElseThrow(ApiError.SCARF_NOT_FOUND::exception);
        wear(sheep.getId(), scarf);
        return new WearScarfResponse(scarf.getColor());
    }

    private void wear(Long sheepId, Scarf scarf) {
        wornScarfRepository.findById(sheepId).ifPresentOrElse(
                worn -> worn.change(scarf),
                () -> wornScarfRepository.save(new WornScarf(sheepId, scarf)));
    }

    /** "하루"는 Asia/Seoul 날짜 기준입니다. */
    private static LocalDate today(Instant now) {
        return LocalDate.ofInstant(now, TimeConfig.SEOUL);
    }
}

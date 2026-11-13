package com.ganjithon.handeulpan.pasture;

import com.ganjithon.handeulpan.pasture.dto.CreatePastureRequest;
import com.ganjithon.handeulpan.pasture.dto.JoinPastureRequest;
import com.ganjithon.handeulpan.pasture.dto.JoinedResponse;
import com.ganjithon.handeulpan.pasture.dto.MyPastureResponse;
import com.ganjithon.handeulpan.pasture.dto.PastureCreatedResponse;
import com.ganjithon.handeulpan.pasture.dto.PasturePreviewResponse;
import com.ganjithon.handeulpan.pasture.dto.PasturePreviewResponse.PreviewSheep;
import com.ganjithon.handeulpan.pasture.dto.PastureResponse;
import com.ganjithon.handeulpan.pasture.dto.PastureResponse.SheepView;
import com.ganjithon.handeulpan.sheep.Distance;
import com.ganjithon.handeulpan.sheep.DistancePolicy;
import com.ganjithon.handeulpan.sheep.Sheep;
import com.ganjithon.handeulpan.sheep.SheepRepository;
import com.ganjithon.handeulpan.user.User;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class PastureService {

    private final PastureRepository pastureRepository;
    private final SheepRepository sheepRepository;
    private final PublicIdGenerator publicIdGenerator;
    private final DistancePolicy distancePolicy;
    private final List<PastureViewExtension> viewExtensions;
    private final Clock clock;

    /** F1 들판 만들기: 들판 + 만든 사람의 양 */
    @Transactional
    public PastureCreatedResponse create(User me, CreatePastureRequest request) {
        Instant now = clock.instant();
        String name = StringUtils.hasText(request.name()) ? request.name().trim() : null;
        Pasture pasture = pastureRepository.save(new Pasture(newPublicId(), name, me, now));
        Sheep sheep = sheepRepository.save(
                new Sheep(pasture, me, request.sheepName().trim(), request.woolColor(), now));
        return new PastureCreatedResponse(pasture.getPublicId(), sheep.getId());
    }

    /** F1 링크로 들어와 구경하기 (토큰 불필요) */
    @Transactional(readOnly = true)
    public PasturePreviewResponse preview(String publicId) {
        Pasture pasture = find(publicId);
        List<Sheep> sheep = sheepRepository.findAllInPasture(pasture.getId());
        List<PreviewSheep> views = sheep.stream()
                .map(s -> new PreviewSheep(s.getName(), s.getWoolColor(), distancePolicy.distanceOf(s)))
                .toList();
        return new PasturePreviewResponse(pasture.getPublicId(), pasture.getName(), Pasture.MAX_SHEEP,
                sheep.size() >= Pasture.MAX_SHEEP, views);
    }

    /** F1 합류: 동의 필수, 최대 6마리, 같은 들판에 두 번 못 들어옴 */
    @Transactional
    public JoinedResponse join(User me, String publicId, JoinPastureRequest request) {
        Pasture pasture = pastureRepository.findByPublicIdForUpdate(publicId)
                .orElseThrow(CoreError.PASTURE_NOT_FOUND::exception);
        if (sheepRepository.existsByPastureIdAndUserId(pasture.getId(), me.getId())) {
            throw CoreError.ALREADY_JOINED.exception();
        }
        if (sheepRepository.countByPastureId(pasture.getId()) >= Pasture.MAX_SHEEP) {
            throw CoreError.PASTURE_FULL.exception();
        }
        Sheep sheep = sheepRepository.save(
                new Sheep(pasture, me, request.sheepName().trim(), request.woolColor(), clock.instant()));
        return new JoinedResponse(pasture.getPublicId(), sheep.getId());
    }

    /** 들판 조회: 들판 화면 전체. 멤버만 볼 수 있음 */
    @Transactional(readOnly = true)
    public PastureResponse get(User me, String publicId) {
        Pasture pasture = find(publicId);
        List<Sheep> sheep = sheepRepository.findAllInPasture(pasture.getId());
        Sheep mine = sheep.stream().filter(s -> s.isOwnedBy(me)).findFirst()
                .orElseThrow(CoreError.NOT_PASTURE_MEMBER::exception);

        List<SheepView> views = sheep.stream()
                .map(s -> {
                    boolean isMine = s == mine;
                    return new SheepView(s.getId(), s.getName(), s.getWoolColor(), distancePolicy.distanceOf(s),
                            isMine, isMine ? distancePolicy.pattedToday(s) : null);
                })
                .toList();
        PastureResponse response = new PastureResponse(
                pasture.getPublicId(), pasture.getName(), Pasture.MAX_SHEEP, mine.getId(), views);

        PastureViewExtension.Context context =
                new PastureViewExtension.Context(pasture, me, mine, sheep, clock.instant());
        for (PastureViewExtension extension : viewExtensions) {
            response.getExtra().putAll(extension.pastureFields(context));
            Map<Long, Map<String, Object>> perSheep = extension.sheepFields(context);
            views.forEach(v -> v.getExtra().putAll(perSheep.getOrDefault(v.getId(), Map.of())));
        }
        return response;
    }

    /** 홈: 내가 들어간 들판 목록 (최근 합류 순) */
    @Transactional(readOnly = true)
    public List<MyPastureResponse> myPastures(User me) {
        return sheepRepository.findAllOfUser(me.getId()).stream()
                .map(mine -> {
                    Pasture pasture = mine.getPasture();
                    List<String> edgeFriends = sheepRepository.findAllInPasture(pasture.getId()).stream()
                            .filter(s -> !s.isOwnedBy(me))
                            .filter(s -> distancePolicy.distanceOf(s) == Distance.EDGE)
                            .map(Sheep::getName)
                            .toList();
                    return new MyPastureResponse(pasture.getPublicId(), pasture.getName(), mine.getId(),
                            distancePolicy.distanceOf(mine), distancePolicy.pattedToday(mine), edgeFriends);
                })
                .toList();
    }

    private Pasture find(String publicId) {
        return pastureRepository.findByPublicId(publicId).orElseThrow(CoreError.PASTURE_NOT_FOUND::exception);
    }

    private String newPublicId() {
        String id;
        do {
            id = publicIdGenerator.next();
        } while (pastureRepository.existsByPublicId(id));
        return id;
    }
}

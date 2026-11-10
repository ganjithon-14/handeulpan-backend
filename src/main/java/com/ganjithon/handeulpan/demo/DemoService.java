package com.ganjithon.handeulpan.demo;

import com.ganjithon.handeulpan.pasture.CoreError;
import com.ganjithon.handeulpan.sheep.DistancePolicy;
import com.ganjithon.handeulpan.sheep.Sheep;
import com.ganjithon.handeulpan.sheep.SheepFinder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class DemoService {

    private final DemoProperties properties;
    private final SheepFinder sheepFinder;
    private final DistancePolicy distancePolicy;

    /** D 데모 모드: 특정 양의 시간을 며칠 되감아 "며칠 지나면 멀어진다"를 바로 보여줌 */
    @Transactional
    public RewindResponse rewind(String demoKey, Long sheepId, int days) {
        checkKey(demoKey);
        Sheep sheep = sheepFinder.require(sheepId);
        sheep.rewind(Duration.ofDays(days));
        return new RewindResponse(sheep.getId(), distancePolicy.distanceOf(sheep));
    }

    private void checkKey(String demoKey) {
        String expected = properties.key();
        if (!StringUtils.hasText(expected) || demoKey == null
                || !MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                demoKey.getBytes(StandardCharsets.UTF_8))) {
            throw CoreError.INVALID_DEMO_KEY.exception();
        }
    }

    public record RewindResponse(Long sheepId, com.ganjithon.handeulpan.sheep.Distance distance) {
    }
}

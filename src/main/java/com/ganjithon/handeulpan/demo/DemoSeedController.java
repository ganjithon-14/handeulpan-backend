package com.ganjithon.handeulpan.demo;

import static com.ganjithon.handeulpan.demo.DemoController.DEMO_KEY_HEADER;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** 시연 데이터 초기화 (D 데모 모드). 데모 키가 있어야 하고 기기 토큰은 필요 없습니다. */
@RestController
@RequestMapping("/api/demo/seed")
@RequiredArgsConstructor
public class DemoSeedController {

    private final DemoSeedService demoSeedService;

    /** 데모 데이터를 지우고 시연 상태로 다시 만듭니다. 몇 번을 눌러도 같은 상태가 됩니다. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DemoSeedService.SeedResponse seed(
            @RequestHeader(value = DEMO_KEY_HEADER, required = false) String demoKey) {
        return demoSeedService.seed(demoKey);
    }

    /** 시연이 끝난 뒤 데모 데이터를 모두 지웁니다. */
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear(@RequestHeader(value = DEMO_KEY_HEADER, required = false) String demoKey) {
        demoSeedService.clear(demoKey);
    }
}

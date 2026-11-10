package com.ganjithon.handeulpan.demo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
public class DemoController {

    public static final String DEMO_KEY_HEADER = "X-Demo-Key";

    private final DemoService demoService;

    @PostMapping("/sheep/{id}/rewind")
    public DemoService.RewindResponse rewind(@RequestHeader(value = DEMO_KEY_HEADER, required = false) String demoKey,
                                             @PathVariable Long id,
                                             @Valid @RequestBody RewindRequest request) {
        return demoService.rewind(demoKey, id, request.days());
    }

    /** days: 되감을 일수 (1~30) */
    public record RewindRequest(@Min(1) @Max(30) int days) {
    }
}

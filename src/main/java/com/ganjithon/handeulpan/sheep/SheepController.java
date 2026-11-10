package com.ganjithon.handeulpan.sheep;

import com.ganjithon.handeulpan.sheep.dto.PatResponse;
import com.ganjithon.handeulpan.user.User;
import com.ganjithon.handeulpan.user.auth.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sheep")
@RequiredArgsConstructor
public class SheepController {

    private final SheepService sheepService;

    /** F2 쓰다듬기 */
    @PostMapping("/{id}/pat")
    public PatResponse pat(@CurrentUser User me, @PathVariable Long id) {
        return sheepService.pat(me, id);
    }
}

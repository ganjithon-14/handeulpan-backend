package com.ganjithon.handeulpan.sheep;

import com.ganjithon.handeulpan.sheep.dto.NapRequest;
import com.ganjithon.handeulpan.sheep.dto.NapResponse;
import com.ganjithon.handeulpan.sheep.dto.PatResponse;
import com.ganjithon.handeulpan.user.User;
import com.ganjithon.handeulpan.user.auth.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
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

    /** P1 낮잠 모드: 최대 7일 동안 멀어지지 않음 */
    @PutMapping("/{id}/nap")
    public NapResponse nap(@CurrentUser User me, @PathVariable Long id, @Valid @RequestBody NapRequest request) {
        return sheepService.nap(me, id, request.days());
    }

    /** 낮잠 깨우기 */
    @DeleteMapping("/{id}/nap")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void wakeUp(@CurrentUser User me, @PathVariable Long id) {
        sheepService.wakeUp(me, id);
    }

    /** P1 들판 나가기: 언제든 한 번에 */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leave(@CurrentUser User me, @PathVariable Long id) {
        sheepService.leave(me, id);
    }
}

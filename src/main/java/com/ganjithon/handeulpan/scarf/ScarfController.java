package com.ganjithon.handeulpan.scarf;

import com.ganjithon.handeulpan.scarf.dto.SendScarfResponse;
import com.ganjithon.handeulpan.scarf.dto.WearScarfRequest;
import com.ganjithon.handeulpan.scarf.dto.WearScarfResponse;
import com.ganjithon.handeulpan.user.User;
import com.ganjithon.handeulpan.user.auth.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
public class ScarfController {

    private final ScarfService scarfService;

    /** F7 목도리 보내기. {id}는 받는 양 */
    @PostMapping("/{id}/scarves")
    @ResponseStatus(HttpStatus.CREATED)
    public SendScarfResponse send(@CurrentUser User me, @PathVariable Long id) {
        return scarfService.send(me, id);
    }

    /** 목도리 두르기·벗기. {id}는 내 양 */
    @PutMapping("/{id}/scarf")
    public WearScarfResponse wear(@CurrentUser User me, @PathVariable Long id,
                                  @RequestBody WearScarfRequest request) {
        return scarfService.wear(me, id, request);
    }
}

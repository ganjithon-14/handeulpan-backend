package com.ganjithon.handeulpan.bubble;

import com.ganjithon.handeulpan.bubble.dto.BubbleRequest;
import com.ganjithon.handeulpan.bubble.dto.BubbleResponse;
import com.ganjithon.handeulpan.user.User;
import com.ganjithon.handeulpan.user.auth.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sheep")
@RequiredArgsConstructor
public class BubbleController {

    private final BubbleService bubbleService;

    /** F4 말풍선 올리기 */
    @PutMapping("/{id}/bubble")
    public BubbleResponse post(@CurrentUser User me, @PathVariable Long id, @RequestBody BubbleRequest request) {
        return bubbleService.post(me, id, request);
    }

    /** 말풍선 지우기 */
    @DeleteMapping("/{id}/bubble")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@CurrentUser User me, @PathVariable Long id) {
        bubbleService.delete(me, id);
    }
}

package com.ganjithon.handeulpan.pasture;

import com.ganjithon.handeulpan.pasture.dto.CreatePastureRequest;
import com.ganjithon.handeulpan.pasture.dto.JoinPastureRequest;
import com.ganjithon.handeulpan.pasture.dto.JoinedResponse;
import com.ganjithon.handeulpan.pasture.dto.MyPastureResponse;
import com.ganjithon.handeulpan.pasture.dto.PastureCreatedResponse;
import com.ganjithon.handeulpan.pasture.dto.PasturePreviewResponse;
import com.ganjithon.handeulpan.pasture.dto.PastureResponse;
import com.ganjithon.handeulpan.user.User;
import com.ganjithon.handeulpan.user.auth.CurrentUser;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pastures")
@RequiredArgsConstructor
public class PastureController {

    private final PastureService pastureService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PastureCreatedResponse create(@CurrentUser User me, @Valid @RequestBody CreatePastureRequest request) {
        return pastureService.create(me, request);
    }

    @GetMapping
    public List<MyPastureResponse> myPastures(@CurrentUser User me) {
        return pastureService.myPastures(me);
    }

    @GetMapping("/{id}/preview")
    public PasturePreviewResponse preview(@PathVariable String id) {
        return pastureService.preview(id);
    }

    @PostMapping("/{id}/sheep")
    @ResponseStatus(HttpStatus.CREATED)
    public JoinedResponse join(@CurrentUser User me, @PathVariable String id,
                               @Valid @RequestBody JoinPastureRequest request) {
        return pastureService.join(me, id, request);
    }

    @GetMapping("/{id}")
    public PastureResponse get(@CurrentUser User me, @PathVariable String id) {
        return pastureService.get(me, id);
    }
}

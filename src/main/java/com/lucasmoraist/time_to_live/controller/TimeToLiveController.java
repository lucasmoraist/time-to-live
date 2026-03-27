package com.lucasmoraist.time_to_live.controller;

import com.lucasmoraist.time_to_live.domain.MyData;
import com.lucasmoraist.time_to_live.service.ControlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/time-to-live")
public class TimeToLiveController {

    private final ControlService controlService;

    @PostMapping
    public ResponseEntity<Void> receiveUserRequest(@RequestBody MyData data) {
        this.controlService.saveData(data);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/callback/{id}")
    public ResponseEntity<Void> callback(@PathVariable UUID id) {
        // Se o serviço externo chamou este endpoint, cancelamos o fallback
        controlService.interruptMonitoring(id);
        return ResponseEntity.noContent().build();
    }

}

package com.example.loanApp.service;

import com.example.loanApp.dtos.DashboardSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class DashboardSseService {
    private final Map<Integer, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final DashboardService dashboardService;

    public SseEmitter connect(Integer userId) {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.put(userId, emitter);

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError(e -> emitters.remove(userId));

        send(userId); // initial push
        return emitter;
    }

    public void send(Integer userId) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter == null) return;

        try {
            DashboardSummaryDto dto = dashboardService.getLoansSummary(userId);
            emitter.send(SseEmitter.event()
                    .name("DASHBOARD-DATA")
                    .data(dto));
        } catch (Exception e) {
            emitters.remove(userId);
        }
    }
}

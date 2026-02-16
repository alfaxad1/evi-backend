package com.example.loanApp.controller;

import com.example.loanApp.dtos.DashboardSummaryDto;
import com.example.loanApp.service.DashboardService;
import com.example.loanApp.service.DashboardSseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequiredArgsConstructor
public class DashboardController {
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final DashboardSseService dashboardSseService;

//    @GetMapping("/dashboard-data")
//    public SseEmitter getDashboardData(@RequestParam Integer userId) {
//        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
//        emitters.add(emitter);
//
//        emitter.onCompletion(() -> emitters.remove(emitter));
//        emitter.onTimeout(() -> emitters.remove(emitter));
//        emitter.onError((e) -> emitters.remove(emitter));
//
//        DashboardSummaryDto summaryDto = dashboardService.getLoansSummary(userId);
//        try {
//            emitter.send(SseEmitter.event().name("DASHBOARD-DATA").data(summaryDto));
//        }catch (Exception e){
//            emitter.completeWithError(e);
//        }
//        return emitter;
//    }
//
//    public void dispatchDashboardData(DashboardSummaryDto  dashboardSummaryDto) {
//        List<SseEmitter> deadEmitters = new ArrayList<>();
//        emitters.forEach(emitter -> {
//            try {
//                emitter.send(SseEmitter.event().name("DASHBOARD-DATA").data(dashboardSummaryDto));
//            }catch (Exception e) {
//                deadEmitters.add(emitter);
//            }
//        });
//        emitters.removeAll(deadEmitters);
//    }

    @GetMapping("/dashboard-data")
    public SseEmitter dashboardStream(@RequestParam Integer userId) {
        return dashboardSseService.connect(userId);
    }

}

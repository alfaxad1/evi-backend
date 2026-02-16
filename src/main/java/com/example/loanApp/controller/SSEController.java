package com.example.loanApp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Controller
public class SSEController {
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @GetMapping("/stream-sse")
    public SseEmitter streamSseEvents(){
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        executor.execute(() -> {
            try {
                for(int i = 0; i < 10; i++){
                    Thread.sleep(1000);
                    emitter.send("SSE MVC - " + System.currentTimeMillis());
                }
                emitter.complete();
            }catch (IOException | InterruptedException e){
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }
}

package com.vara.org.vara_copilotai;

import com.vara.org.vara_copilotai.orchestration.ReasoningEngine;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ReasoningEngine reasoningEngine;

    public ChatController(ReasoningEngine reasoningEngine) {
        this.reasoningEngine = reasoningEngine;
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestBody ChatRequest request) {
        String response = reasoningEngine.processQuery(request.getMessage());
        return ResponseEntity.ok(response);
    }

    public static class ChatRequest {
        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}

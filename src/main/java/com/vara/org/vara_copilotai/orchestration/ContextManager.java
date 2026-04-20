package com.vara.org.vara_copilotai.orchestration;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class ContextManager {

    private final Map<String, String> contextStore = new ConcurrentHashMap<>();

    public void saveContext(String sessionId, String context) {
        contextStore.put(sessionId, context);
    }

    public String getContext(String sessionId) {
        return contextStore.get(sessionId);
    }
}

package com.vara.org.vara_copilotai.orchestration;

import com.vara.org.vara_copilotai.ai.GenerationService;
import com.vara.org.vara_copilotai.ai.PromptEngineeringService;
import com.vara.org.vara_copilotai.ai.RagService;
import org.springframework.stereotype.Service;

@Service
public class ReasoningEngine {

    private final RagService ragService;
    private final GenerationService generationService;
    private final PromptEngineeringService promptService;

    public ReasoningEngine(RagService ragService, GenerationService generationService, PromptEngineeringService promptService) {
        this.ragService = ragService;
        this.generationService = generationService;
        this.promptService = promptService;
    }

    public String processQuery(String userQuery) {
        // Step 1: Augment prompt with RAG
        String augmentedPrompt = ragService.augmentPrompt(userQuery);

        // Step 2: Generate response
        String response = generationService.generateResponse(augmentedPrompt);

        // Step 3: Evaluate
        promptService.evaluateResponse(response);

        return response;
    }
}

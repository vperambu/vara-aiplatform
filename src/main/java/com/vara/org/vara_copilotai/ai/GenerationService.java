package com.vara.org.vara_copilotai.ai;

import dev.langchain4j.model.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;

@Service
public class GenerationService {

    private final OllamaChatModel chatModel;

    public GenerationService() {
        // Assuming Ollama is running locally with a model like "llama2"
        this.chatModel = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("llama2")
                .build();
    }

    public String generateResponse(String augmentedPrompt) {
        // Feed the full augmented prompt to Ollama for knowledge-based answer
        return chatModel.generate(augmentedPrompt);
    }
}

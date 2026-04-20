package com.vara.org.vara_copilotai.ai;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.huggingface.HuggingFaceEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagService {

    private final ChromaEmbeddingStore embeddingStore;
    private final EmbeddingModel embeddingModel;

    public RagService() {
        this.embeddingStore = ChromaEmbeddingStore.builder()
                .baseUrl("http://localhost:8000")
                .collectionName("knowledge-base")
                .build();
        this.embeddingModel = HuggingFaceEmbeddingModel.builder()
                .accessToken(System.getenv("HF_API_KEY")) // Set HuggingFace API key
                .modelId("sentence-transformers/all-MiniLM-L6-v2")
                .build();
        // Index initial documents (simulate ~600 topics)
        indexKnowledgeBase();
    }

    private void indexKnowledgeBase() {
        // Load documents from a directory (e.g., docs folder with topic files)
        List<Document> documents = FileSystemDocumentLoader.loadDocuments(Paths.get("docs"));
        List<TextSegment> segments = DocumentSplitters.recursive(300, 0).splitAll(documents);
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        embeddingStore.addAll(embeddings, segments);
    }

    public String augmentPrompt(String userQuery) {
        Embedding queryEmbedding = embeddingModel.embed(userQuery).content();
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(5) // Top 5 similar docs
                .build();
        EmbeddingSearchResult<TextSegment> result = embeddingStore.search(request);
        List<String> relevantTexts = result.matches().stream()
                .map(EmbeddingMatch::embedded)
                .map(TextSegment::text)
                .collect(Collectors.toList());
        return String.join("\n", relevantTexts) + "\n\nUser Query: " + userQuery;
    }
}

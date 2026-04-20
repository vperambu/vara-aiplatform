# Vara AIPlatform - Open-Source Setup

## Overview
Vara AIPlatform is an enterprise-grade open-source AI assistant framework using Ollama for local LLM, ChromaDB for vector search, PostgreSQL for data, and JWT for authentication.

## Prerequisites
1. **Java 17+**
2. **Maven**
3. **PostgreSQL**: Install and create database `vara_aiplatform_db` with user `user` and password `password`.
4. **Ollama**: Install from https://ollama.ai/, pull model: `ollama pull llama2`, run on port 11434.
5. **ChromaDB**: Run via Docker: `docker run -p 8000:8000 chromadb/chroma`
6. **HuggingFace API Key**: Set environment variable `HF_API_KEY` for embeddings.

## Setup
1. Clone or navigate to the project.
2. Add knowledge base documents to `docs/` folder (e.g., topic1.txt, topic2.txt).
3. Update `application.yaml` with correct DB credentials if needed.
4. Run `mvn spring-boot:run`.

## API Endpoints
- **POST /vara-ai/auth/login**: Login with `{"username": "user", "password": "password"}` to get JWT token.
- **POST /vara-ai/api/ai/send**: Send message with Authorization header `Bearer <token>`.

## Features
- Full RAG with embeddings for knowledge-based answers.
- JWT authentication.
- Local LLM with Ollama for privacy and cost-efficiency.

## Notes
- For production, replace in-memory context with Redis.
- Add more documents to `docs/` for better RAG.
- Monitor with Prometheus endpoint at `/vara-ai/actuator/prometheus`.

# Vara CopilotAI - Visual Diagrams & Architecture

## 1. System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            USER / CLIENT LAYER                              │
│                                                                              │
│  Web App     Mobile App     API Client     Desktop App                       │
│    │              │             │              │                            │
└────┼──────────────┼─────────────┼──────────────┼────────────────────────────┘
     │              │             │              │
     └──────────────┴─────────────┴──────────────┘
                    │
┌───────────────────▼──────────────────────────────────────────────────────────┐
│                         PRESENTATION LAYER (REST API)                        │
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │ API Gateway / Load Balancer (Spring Cloud Gateway)                     │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
│                              │                │                            │
│                    ┌─────────▼────┐  ┌────────▼────────┐                 │
│                    │ POST /auth/   │  │ POST /api/chat/ │                 │
│                    │   login       │  │   send          │                 │
│                    │ (JWT)         │  │ (RAG+LLM)       │                 │
│                    └───────────────┘  └─────────────────┘                 │
└───────────────────┬────────────────────────────────────────────────────────┘
                    │
┌───────────────────▼──────────────────────────────────────────────────────────┐
│               ORCHESTRATION LAYER (Spring Boot Services)                     │
│                                                                              │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │ ChatController (Routes requests)                                     │  │
│  └──────────────────┬───────────────────────────────────────────────────┘  │
│                    │                                                       │
│  ┌─────────────────▼────────────────────────────────────────────────────┐  │
│  │ ReasoningEngine (Orchestrates multi-step process)                   │  │
│  │ 1. Retrieve context via RAG                                        │  │
│  │ 2. Generate response via LLM                                       │  │
│  │ 3. Evaluate & log results                                          │  │
│  └─────────┬──────────────────────────┬────────────────────────┬───────┘  │
│            │                          │                        │          │
└────────────┼──────────────────────────┼────────────────────────┼──────────┘
             │                          │                        │
┌────────────▼─────────────────────────▼────────────────────────▼──────────────┐
│                    AI SERVICES LAYER (LLM & RAG)                             │
│                                                                              │
│  ┌────────────────────┐    ┌──────────────────┐    ┌────────────────────┐ │
│  │ RAG Service        │    │ Generation       │    │ Search Service     │ │
│  │                    │    │ Service          │    │                    │ │
│  │ 1. Retrieve docs   │    │ 1. Augment       │    │ 1. Vector search   │ │
│  │    from ChromaDB   │    │    prompt        │    │    in ChromaDB     │ │
│  │ 2. Embed query     │    │ 2. Send to       │    │ 2. Index docs      │ │
│  │    (HuggingFace)   │    │    Ollama        │    │ 3. Semantic rank   │ │
│  │ 3. Semantic search │    │ 3. Return answer │    │                    │ │
│  └────────────────────┘    └──────────────────┘    └────────────────────┘ │
│                                                                              │
│  ┌────────────────────┐    ┌──────────────────────────────────────────────┐│
│  │ Prompt Engineering │    │ Context Manager                              ││
│  │ Service            │    │ - In-memory session storage                  ││
│  │ - Load templates   │    │ - User context persistence                   ││
│  │ - Manage versions  │    │ - Conversation history                       ││
│  │ - Evaluate quality │    │                                              ││
│  └────────────────────┘    └──────────────────────────────────────────────┘│
└──────────────────────────────────────────────────────────────────────────────┘
        │                   │                    │
┌───────▼─────────┬─────────▼────────┬──────────▼──────────┬──────────────────┐
│                 │                  │                     │                  │
│ EXTERNAL        │ EXTERNAL         │ EXTERNAL            │ SECURITY         │
│ LLM             │ EMBEDDINGS       │ VECTOR DB           │                  │
│                 │                  │                     │                  │
│ Ollama          │ HuggingFace      │ ChromaDB            │ JWT Auth         │
│ (localhost:     │ Inference API    │ (localhost:8000)    │ (JwtUtil)        │
│  11434)         │                  │                     │                  │
│                 │                  │                     │ Spring Security  │
└─────────────────┴──────────────────┴─────────────────────┴──────────────────┘
        │                   │                    │
┌───────▼──────────────────▼────────────────────▼──────────────────────────────┐
│                            DATA LAYER                                        │
│                                                                              │
│  ┌──────────────────────┐  ┌─────────────────┐  ┌───────────────────────┐ │
│  │ PostgreSQL DB        │  │ ChromaDB Vector │  │ In-Memory Cache       │ │
│  │ - Topics/Docs        │  │ Store           │  │ (Context Sessions)    │ │
│  │ - Users              │  │ - Embeddings    │  │                       │ │
│  │ - Prompt Templates   │  │ - Collections   │  │ Alternative: Redis    │ │
│  │ - Analytics          │  │                 │  │                       │ │
│  └──────────────────────┘  └─────────────────┘  └───────────────────────┘ │
└───────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. RAG Flow Diagram

```
User Query: "What's the company sick leave policy?"
    │
    ▼
┌─────────────────────────────────────────────────────┐
│ 1. EMBEDDING GENERATION                             │
│ - Query: "sick leave policy"                        │
│ - Model: HuggingFace (all-MiniLM-L6-v2)            │
│ - Output: Vector [0.234, -0.891, 0.456, ...]       │
└─────────────────┬───────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────────┐
│ 2. VECTOR SEARCH (ChromaDB)                         │
│ - Compare query embedding to doc embeddings        │
│ - Return top-5 similar documents                   │
│ - Similarity scores: [0.94, 0.87, 0.82, ...]      │
└─────────────────┬───────────────────────────────────┘
                  │
    ┌─────────────┴─────────────┬──────────────┬──────────┐
    │                           │              │          │
    ▼                           ▼              ▼          ▼
┌──────────────┐  ┌──────────────┐  ┌──────────┐  ┌──────────┐
│ HR Policy    │  │ Benefits FAQ │  │ Handbook │  │ Training │
│ Score: 0.94  │  │ Score: 0.87  │  │ 0.82     │  │ 0.75     │
│              │  │              │  │          │  │          │
│ "Employees   │  │ "How long    │  │ "Sick    │  │ "Policy  │
│ get 5 days   │  │ is sick      │  │ leave is │  │ updated  │
│ paid sick    │  │ leave?"      │  │ in the   │  │ 2023"    │
│ leave per    │  │              │  │ handbook"│  │          │
│ year..."     │  │              │  │          │  │          │
└──────────────┘  └──────────────┘  └──────────┘  └──────────┘
    │
    └─────────────────────────────────────────────────────┐
                                                          │
                                                          ▼
                        ┌─────────────────────────────────────────┐
                        │ 3. AUGMENT PROMPT                       │
                        │                                         │
                        │ Augmented Prompt:                       │
                        │ ┌──────────────────────────────────────┐│
                        │ │ Context from HR Policy doc:          ││
                        │ │ "Employees get 5 days paid sick     ││
                        │ │ leave per year, must notify manager ││
                        │ │ within 2 hours..."                  ││
                        │ │                                      ││
                        │ │ Additional context from Benefits FAQ:││
                        │ │ "Sick leave does not roll over..."   ││
                        │ │                                      ││
                        │ │ User Query:                          ││
                        │ │ "What's the company sick leave      ││
                        │ │ policy?"                             ││
                        │ └──────────────────────────────────────┘│
                        └────────────┬─────────────────────────────┘
                                     │
                                     ▼
                        ┌─────────────────────────────┐
                        │ 4. GENERATE RESPONSE        │
                        │ - Model: Ollama (llama2)    │
                        │ - Prompt: Augmented prompt │
                        │ - Method: LLM inference     │
                        └────────────┬────────────────┘
                                     │
                                     ▼
                        Response:
                        "Our company provides 5 days
                         of paid sick leave per year.
                         You must notify your manager
                         within 2 hours of absence.
                         Sick leave does not roll over
                         to the next year."
```

---

## 3. JWT Authentication Flow

```
┌──────────────┐                                    ┌──────────────┐
│   Client     │                                    │   Server     │
│  (Browser/   │                                    │  (Vara App)  │
│   Mobile)    │                                    │              │
└──────────────┘                                    └──────────────┘
       │                                                    │
       │ 1. POST /auth/login                               │
       │    {"username": "user", "password": "pass"}      │
       ├───────────────────────────────────────────────►  │
       │                                                    │ Verify credentials
       │                                                    │ against BCrypt hash
       │                                                    │
       │◄───────────────────────────────────────────────  │
       │ 2. 200 OK + JWT Token                            │
       │    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."   │
       │                                                    │
       │ 3. Store JWT in localStorage/session             │
       │                                                    │
       │ 4. POST /api/chat/send                           │
       │    Headers: Authorization: Bearer <JWT>          │
       │    Body: {"message": "Hello"}                    │
       ├───────────────────────────────────────────────►  │
       │                                                    │
       │                                                    │ JwtAuthenticationFilter
       │                                                    │ - Extract token from header
       │                                                    │ - Validate signature
       │                                                    │ - Check expiration
       │                                                    │ - Load user details
       │                                                    │ - Set SecurityContext
       │                                                    │
       │◄───────────────────────────────────────────────  │
       │ 5. 200 OK + Response                             │
       │    {"response": "AI answer..."}                  │
       │                                                    │
```

---

## 4. Data Flow: From Knowledge Base to Answer

```
Knowledge Base Setup:
┌────────────────────────────────────────────────────────────────┐
│                                                                │
│ Raw Documents                   Processed & Stored            │
│ ┌──────────────┐               ┌──────────────┐              │
│ │ docs/        │────Index──────► ChromaDB     │              │
│ │ topic1.txt   │               │ Collection   │              │
│ │ topic2.txt   │               │              │              │
│ │ topic3.txt   │               │ Embeddings + │              │
│ │ ... (600+)   │               │ Metadata     │              │
│ └──────────────┘               └──────────────┘              │
│                                                                │
│ Processing Steps:                                              │
│ 1. Load documents from file system                            │
│ 2. Split into chunks (300 chars each)                         │
│ 3. Generate embeddings (HuggingFace model)                    │
│ 4. Store in ChromaDB with metadata                            │
│                                                                │
└────────────────────────────────────────────────────────────────┘

At Runtime:
┌────────────────────────────────────────────────────────────────┐
│                                                                │
│ User Query                                                     │
│   │                                                            │
│   ▼                                                            │
│ Embed Query (HuggingFace)                                     │
│   │ Query → Vector                                            │
│   │                                                            │
│   ▼                                                            │
│ Search ChromaDB (Similarity Match)                            │
│   │ Query Vector vs Doc Vectors                              │
│   │                                                            │
│   ▼                                                            │
│ Retrieve Top-5 Chunks                                        │
│   │ [Score 0.94, Score 0.87, Score 0.82, ...]               │
│   │                                                            │
│   ▼                                                            │
│ Augment Prompt (RAG Service)                                  │
│   │ Context + Query → Final Prompt                           │
│   │                                                            │
│   ▼                                                            │
│ Generate Answer (Ollama LLM)                                  │
│   │ Prompt → LLM Inference                                   │
│   │                                                            │
│   ▼                                                            │
│ Return Response to User                                       │
│                                                                │
└────────────────────────────────────────────────────────────────┘
```

---

## 5. Deployment Architecture (Kubernetes)

```
┌──────────────────────────────────────────────────────────────────┐
│                     KUBERNETES CLUSTER                           │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ INGRESS (Load Balancer)                                    │ │
│  │ Route traffic to services                                  │ │
│  └─────────────┬──────────────────────────────────────────────┘ │
│                │                                                 │
│  ┌─────────────▼──────────────────────────────────────────────┐ │
│  │ SERVICE: vara-app (Spring Boot)                            │ │
│  │ - ChatController, ReasoningEngine, etc.                    │ │
│  │ Replicas: 3 (Auto-scale 1-10 based on CPU)               │ │
│  └─────────────┬──────────────────────────────────────────────┘ │
│                │                                                 │
│  ┌─────────────▼──────────┬──────────────────┬─────────────────┐│
│  │ POD 1                   │ POD 2           │ POD 3            ││
│  │ Vara Container          │ Vara Container  │ Vara Container   ││
│  │ (Java Spring Boot)      │ (Java Spring    │ (Java Spring    ││
│  │ Memory: 2Gi             │ Boot)           │ Boot)            ││
│  │ CPU: 1 core             │ Memory: 2Gi     │ Memory: 2Gi      ││
│  │                         │ CPU: 1 core     │ CPU: 1 core      ││
│  └────────────────────────┴──────────────────┴─────────────────┘│
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ STATELESS SERVICES (Sidecars/DaemonSets)                │  │
│  │                                                          │  │
│  │ ┌────────────────┐  ┌────────────────┐  ┌────────────┐ │  │
│  │ │ Prometheus     │  │ Fluent-bit     │  │ Jaeger     │ │  │
│  │ │ (Monitoring)   │  │ (Logging)      │  │ (Tracing)  │ │  │
│  │ │                │  │                │  │            │ │  │
│  │ │ Scrapes        │  │ Sends logs to  │  │ Traces     │ │  │
│  │ │ metrics from   │  │ Elasticsearch  │  │ requests   │ │  │
│  │ │ /actuator/     │  │                │  │            │ │  │
│  │ │ prometheus     │  │                │  │            │ │  │
│  │ └────────────────┘  └────────────────┘  └────────────┘ │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ PERSISTENT STORAGE                                       │  │
│  │                                                          │  │
│  │ ┌────────────┐  ┌────────────┐  ┌──────────────┐        │  │
│  │ │ PostgreSQL │  │ ChromaDB   │  │ MinIO (S3)   │        │  │
│  │ │ StatefulSet│  │ StatefulSet│  │ StatefulSet  │        │  │
│  │ │            │  │            │  │              │        │  │
│  │ │ Data: 100G │  │ Data: 50G  │  │ Data: 200G   │        │  │
│  │ │            │  │            │  │ (Docs)       │        │  │
│  │ │ Replicas:  │  │ Replicas:  │  │ Replicas: 3  │        │  │
│  │ │ 3 (HA)     │  │ 3 (HA)     │  │              │        │  │
│  │ └────────────┘  └────────────┘  └──────────────┘        │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘

External Services:
┌────────────────┐  ┌────────────────┐  ┌────────────────┐
│ Ollama         │  │ HuggingFace    │  │ External APIs  │
│ (Local LLM)    │  │ Inference API  │  │ (If needed)    │
│ localhost:11434│  │                │  │                │
└────────────────┘  └────────────────┘  └────────────────┘
```

---

## 6. Use Case Flow: Customer Support

```
Customer submits question via chat widget:
"How do I reset my password?"

         │
         ▼
┌─────────────────────────────────────────┐
│ 1. REQUEST RECEIVED                     │
│ URL: POST /api/chat/send                │
│ Auth: JWT token validated               │
│ Payload: {"message": "..."}             │
└─────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│ 2. RAG SERVICE (Retrieval)              │
│ Query: "reset password"                 │
│ Searched docs:                          │
│ - FAQ: "Password reset"                 │
│ - Help: "Account recovery"              │
│ - KB: "Security procedures"             │
└─────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│ 3. AUGMENT PROMPT                       │
│ Context + Query → Final Prompt          │
│                                         │
│ "Based on our help docs:                │
│ [FAQ content, Help content, ...]        │
│ User asks: 'How do I reset password?'   │
│ Provide a clear answer."                │
└─────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│ 4. GENERATE RESPONSE (Ollama)           │
│ Model: llama2                           │
│ Response: "To reset your password:      │
│ 1. Click 'Forgot Password' link         │
│ 2. Enter your email                     │
│ 3. Check email for reset link           │
│ 4. Create new password                  │
│ 5. Login with new password"             │
└─────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│ 5. EVALUATE & LOG                       │
│ - Confidence score: 0.95                │
│ - Sources used: FAQ, Help docs          │
│ - Response time: 0.8 seconds            │
│ - Logged to PostgreSQL                  │
└─────────────────────────────────────────┘
         │
         ▼
Customer receives instant, accurate answer
No human support needed!
```

---

## 7. Migration Path: Static Topics → Generative

```
Timeline: 12 Months

Month 1-2: ASSESSMENT
┌──────────────────────────────┐
│ ~600 Static Topics           │
│ (from Copilot Studio)        │
│                              │
│ Import to new system:        │
│ ✓ Topic metadata             │
│ ✓ Q&A pairs                  │
│ ✓ Categorization             │
└──────────────────────────────┘
                │
                ▼
Month 3-4: PILOT (High-Confidence Topics)
┌──────────────────────────────┐
│ Select ~50 topics to convert │
│ to generative responses      │
│                              │
│ - FAQs (high accuracy)       │
│ - Account management         │
│ - Basic troubleshooting      │
│                              │
│ Run A/B test:                │
│ Group A: Static responses    │
│ Group B: Generative (RAG)    │
│                              │
│ Measure: Accuracy, latency   │
└──────────────────────────────┘
                │
                ▼
Month 5-6: EXPAND
┌──────────────────────────────┐
│ Convert 200+ topics          │
│ based on pilot learnings     │
│                              │
│ Approach:                    │
│ - High confidence first      │
│ - Low complexity             │
│ - Easy to validate           │
│                              │
│ Monitor: User satisfaction   │
└──────────────────────────────┘
                │
                ▼
Month 7-9: SCALE
┌──────────────────────────────┐
│ Convert remaining ~350 topics│
│                              │
│ Add fine-tuning for domain   │
│ specific responses           │
│                              │
│ Parallel: Still support      │
│ legacy static topics         │
└──────────────────────────────┘
                │
                ▼
Month 10-12: OPTIMIZE
┌──────────────────────────────┐
│ Full migration complete      │
│                              │
│ All 600 topics now use:      │
│ - RAG for retrieval          │
│ - Generative for response    │
│ - Consistent quality         │
│                              │
│ Sunset: Legacy system        │
│ Savings: Realized ($$$)      │
└──────────────────────────────┘
```

This visual architecture shows how your static topics gradually transform into intelligent, generative responses grounded in knowledge bases.

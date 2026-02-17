**LawSphere** is a production-grade, AI-powered Android application designed to democratize legal access in India. It serves as an intelligent guide to the **Bharatiya Nyaya Sanhita (BNS)**, replacing the colonial IPC.
Built with a strict **RAG (Retrieval-Augmented Generation)** architecture, LawSphere ensures zero hallucinations by deriving answers solely from the official BNS legal text, providing exact section citations and punishment details.

🚀 **Key Features**
🧠 **Core AI Capabilities**
Context-Aware AI Chat: Ask legal queries in natural language (Voice/Text) and get precise answers citing specific BNS sections.
Strict Anti-Hallucination: Unlike generic chatbots, LawSphere refuses to answer if the information is not found in the legal statute.
Multi-Lingual Support: Switch between English and Hindi (Devanagari) for AI responses.
Compare Laws Tool: AI-powered comparison tables (e.g., "Theft vs. Extortion") highlighting differences in punishment, cognizance, and bail.
📸 **Novelty Features**

📷 **Legal Lens (OCR)**: Scan physical legal documents (FIRs, Court Notices) using the camera. The AI analyzes the scanned text and summarizes it instantly.
🎙️ Voice-First Interface: Full speech-to-text integration for accessibility.

🛠️ **Legal Utilities**
Drafting Tools: Generate professional PDFs for FIRs, Bail Applications, and Legal Notices instantly.
Smart Section Explorer: Browse BNS chapters with expandable cards showing ingredients, punishments, and Landmark Supreme Court Judgments.
Citizen Awareness: "What to do if..." guides for arrest, domestic violence, and cybercrime.
Geo-Location: Integrated Google Maps to find the nearest Police Stations and District Courts.

🏗️ **System Architecture**
LawSphere does not use a simple API wrapper. It implements a custom RAG Pipeline to ensure accuracy.
Ingestion: The BNS PDF is chunked into semantic segments (using Recursive Character Splitting) and embedded into vectors using Xenova/Transformers.
Storage: Vectors are stored in Pinecone (Vector Database).
Retrieval: When a user asks a question, the backend performs a semantic similarity search to find the Top-K relevant legal sections.
Generation: The retrieved context + user query is sent to Groq (Llama-3-70b) with strict system prompts to generate the final answer.

🛠️ **Tech Stack**

📱 **Android (Frontend)**
Language: Kotlin
UI Toolkit: Jetpack Compose (Material 3, Glassmorphism Design)
Architecture: MVVM + Clean Architecture
Dependency Injection: Hilt
Networking: Retrofit + OkHttp
Database: Firestore (User Data), DataStore (Preferences)
ML: Google ML Kit (Text Recognition/OCR)

🖥️ **Backend (The Brain)**
Runtime: Node.js + Express
Vector Database: Pinecone
LLM Inference: Groq SDK (Llama-3.3-70b-Versatile)
Embeddings: Xenova Transformers (Local On-Device Embeddings)
PDF Processing: PDF-Parse + LangChain Text Splitters

☁️ **Cloud & DevOps**
Auth: Firebase Authentication
Storage: Firebase Firestore
Maps: Google Maps SDK

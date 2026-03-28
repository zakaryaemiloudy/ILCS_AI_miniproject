# ILCS-AGENT-AI Project

Spring Boot application with RAG (Retrieval-Augmented Generation) functionality for document-based question answering.

## 🚀 Quick Start

```bash
# Clone and setup
cd ILCS-AGENT-AI
./mvnw clean install
./mvnw spring-boot:run
```

## 📋 Features

- ✅ Conversational AI Agent
- ✅ PDF Document Analysis  
- ✅ RAG Implementation
- ✅ Text-to-Speech
- ✅ REST API

## 🛠️ Tech Stack

- Spring Boot 3.5.11
- Spring AI 1.1.2
- Groq API (LLaMA 3.3)
- Vector Store (SimpleVectorStore)
- PDFBox
- H2 Database

## 📁 Project Structure

```
ILCS-AGENT-AI/          # Main Spring Boot application
front-ai/               # Angular frontend  
GUIDE_CLAUDE_DOC_PROF.md # Documentation guide
```

## 📖 Documentation

See `ILCS-AGENT-AI/RAPPORT.md` for detailed technical documentation.

## 🔧 Configuration

Required environment variables:
- `SPRING_AI_OPENAI_API_KEY` - Groq API key
- `SPRING_AI_OPENAI_BASE_URL` - https://api.groq.com/openai
- `SPRING_AI_OPENAI_CHAT_OPTIONS_MODEL` - llama-3.3-70b-versatile

## 📞 Support

For RAG implementation details, see the comprehensive documentation in `ILCS-AGENT-AI/RAPPORT.md`.

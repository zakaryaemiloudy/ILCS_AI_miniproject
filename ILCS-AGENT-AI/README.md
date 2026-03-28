# ILCS-AGENT-AI

Application d'intelligence artificielle avec agent conversationnel et RAG (Retrieval-Augmented Generation).

## 🚀 Installation Rapide

### 1. Prérequis
- Java 21
- Maven
- Clé API Groq (gratuite sur https://console.groq.com)

### 2. Configuration

Le fichier `.env` est déjà créé avec vos identifiants. **NE PAS COMMITER CE FICHIER!**

```bash
# Vérifiez que le fichier .env existe
cat .env
```

### 3. Lancer l'application

```bash
# Compiler et lancer
./mvnw clean install
./mvnw spring-boot:run
```

L'application démarre sur http://localhost:8080

## 📡 API Endpoints

### 1. Agent Simple
```bash
curl "http://localhost:8080/askAgent?question=Bonjour"
```

### 2. Analyse PDF Directe
```bash
curl -X POST http://localhost:8080/askWithPdf \
  -F "files=@document.pdf" \
  -F "question=Résume ce document"
```

### 3. RAG - Upload Documents
```bash
curl -X POST http://localhost:8080/rag/upload \
  -F "files=@document1.pdf" \
  -F "files=@document2.pdf"
```

### 4. RAG - Poser une Question
```bash
curl "http://localhost:8080/rag/ask?question=Quelle+est+la+conclusion"
```

### 5. Text-to-Speech
```bash
curl -X POST http://localhost:8080/tts \
  -H "Content-Type: text/plain" \
  -d "Bonjour, ceci est un test" \
  --output audio.wav
```

## 🗂️ Structure

```
ILCS-AGENT-AI/
├── .env                    # Configuration API (ne pas commiter!)
├── pom.xml                 # Dépendances Maven
├── src/
│   └── main/
│       ├── java/
│       │   └── com/estn/ilcsagentai/
│       │       ├── config/          # Configuration
│       │       ├── services/        # Services RAG
│       │       ├── controller/      # API REST
│       │       ├── agents/          # Agent IA
│       │       └── entities/        # Entités
│       └── resources/
│           └── application.properties
├── data/
│   └── vectorstore.json    # Base vectorielle (créé automatiquement)
└── RAPPORT.md              # Documentation complète
```

## 📚 Documentation

Pour plus de détails, consultez `RAPPORT.md` qui contient:
- Explication complète du RAG
- Architecture détaillée
- Exemples d'utilisation
- Concepts clés

## 🔑 Variables d'Environnement (.env)

```env
SPRING_AI_OPENAI_BASE_URL=https://api.groq.com/openai
SPRING_AI_OPENAI_API_KEY=votre_clé_api
SPRING_AI_OPENAI_CHAT_OPTIONS_MODEL=openai/gpt-oss-120b
SPRING_AI_OPENAI_EMBEDDING_MODEL=text-embedding-3-small
```

## 🛠️ Technologies

- **Spring Boot 3.5.11** - Framework
- **Spring AI 1.1.2** - Intégration IA
- **Groq API** - LLM et embeddings
- **SimpleVectorStore** - Base vectorielle
- **H2 Database** - Base de données

## ⚠️ Important

- Ne jamais commiter le fichier `.env`
- Le fichier `.gitignore` est configuré pour l'exclure
- Garder votre clé API secrète

## 📞 Support

Consultez `RAPPORT.md` pour plus d'informations.

# Guide pour Claude - Génération de Document pour Professeur

## 📋 Objectif
Générer un document professionnel à remettre au professeur présentant le projet ILCS-AGENT-AI avec RAG.

## 🎯 Contenu Requis

### 1. Page de Garde
- Titre: "Projet ILCS-AGENT-AI: Implémentation RAG"
- Sous-titre: "Application d'Intelligence Artificielle avec Retrieval-Augmented Generation"
- Date: Mars 2026
- Auteur: [Votre Nom]
- Matière: [Nom de la matière]

### 2. Introduction (1 page)
- Contexte du projet
- Objectifs visés
- Technologies choisies et pourquoi
- Structure du document

### 3. Architecture Technique (2-3 pages)
#### 3.1 Vue d'ensemble
- Spring Boot 3.5.11 comme backend
- Angular comme frontend
- Base de données H2
- API Groq pour LLM

#### 3.2 Architecture RAG
```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Document  │ -> │ Vector Store│ -> │   LLM       │
│   (PDF/TXT) │    │ (Embeddings)│    │ (Groq)      │
└─────────────┘    └─────────────┘    └─────────────┘
```

#### 3.3 Composants Principaux
- VectorStoreConfig.java: Configuration base vectorielle
- DocumentService.java: Traitement documents
- RagService.java: Logique RAG
- AiAgentController.java: API REST

### 4. Implémentation RAG (2-3 pages)
#### 4.1 Upload de Documents
- Endpoint: POST /rag/upload
- Support PDF et fichiers texte
- Chunking personnalisé (1000 caractères)
- Génération d'embeddings

#### 4.2 Question-Réponse RAG
- Endpoint: GET /rag/ask
- Recherche de similarité
- Construction du contexte
- Génération de réponse

#### 4.3 Défis Techniques
- Configuration modèle d'embedding
- Compatibilité Spring AI 1.1.2
- Solutions implémentées

### 5. API REST (1-2 pages)
#### Endpoints Implémentés:
- `/askAgent` - Agent conversationnel simple
- `/askWithPdf` - Analyse PDF directe
- `/rag/upload` - Upload documents RAG
- `/rag/ask` - Question avec RAG
- `/rag/status` - Status système RAG
- `/tts` - Text-to-Speech

#### Exemples d'utilisation:
```bash
# Upload document
curl -X POST http://localhost:8080/rag/upload \
  -F "files=@document.pdf"

# Question RAG
curl "http://localhost:8080/rag/ask?question=Qu'est-ce que le RAG"
```

### 6. Résultats et Tests (1-2 pages)
#### Tests Réalisés:
- ✅ Compilation réussie
- ✅ Démarrage application
- ✅ Upload documents (PDF/TXT)
- ✅ Status système RAG
- ⚠️ Tests RAG (besoin embedding config)

#### Performances:
- Temps de traitement document: [mesurer]
- Taille chunks: 1000 caractères
- Support multi-fichiers

### 7. Défis et Solutions (1 page)
#### Problèmes rencontrés:
1. Dépendances Spring AI incompatibles
2. Modèle embedding non disponible sur Groq
3. API Spring AI en évolution

#### Solutions apportées:
1. Implémentation personnalisée avec PDFBox
2. Configuration flexible pour différents providers
3. Code modulaire et maintenable

### 8. Conclusion (1 page)
- Réalisations principales
- Fonctionnalités opérationnelles
- Limitations actuelles
- Améliorations futures possibles

## 📝 Instructions pour Claude

### Format du Document:
- Format: PDF ou DOCX
- Police: Arial 12
- Marges: Standards
- Numérotation: Pages numérotées
- Sommaire: Automatique

### Style:
- Professionnel et académique
- Screenshots inclus où pertinent
- Code formaté correctement
- Diagrammes d'architecture clairs

### Éléments à Inclure:
1. Diagrammes d'architecture
2. Extraits de code commentés
3. Captures d'écran de l'application
4. Tableaux récapitulatifs
5. Graphiques de performance si disponibles

### Ton et Langage:
- Français professionnel
- Terminologie technique appropriée
- Explications claires et concises
- Structure logique et progressive

## 🎯 Points Clés à Mettre en Valeur

### Innovation:
- Intégration RAG dans application Spring Boot
- Solutions personnalisées aux défis techniques
- Architecture modulaire et évolutive

### Compétences Démontrées:
- Spring Boot et Spring AI
- Traitement de documents
- Vector databases et embeddings
- API REST et microservices
- Résolution de problèmes techniques

### Impact:
- Base fonctionnelle pour applications RAG
- Code réutilisable et maintenable
- Documentation complète

---

**Note:** Ce guide sert de structure de base. Adaptez-le selon les exigences spécifiques de votre professeur et les particularités de votre implémentation.

package com.estn.ilcsagentai.services;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RagService {

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Autowired
    private VectorStore vectorStore;

    /**
     * Ask a question using RAG (Retrieval-Augmented Generation)
     * The system will automatically retrieve relevant document chunks and include them in the context
     */
    public String askWithRag(String question) {
        // First, retrieve relevant documents
        List<Document> relevantDocs = vectorStore.similaritySearch(question);
        
        if (relevantDocs.isEmpty()) {
            return "I couldn't find relevant information in the uploaded documents to answer your question. Please try uploading relevant documents first.";
        }
        
        // Build context from retrieved documents (limit to top 5)
        StringBuilder context = new StringBuilder();
        int maxDocs = Math.min(5, relevantDocs.size());
        for (int i = 0; i < maxDocs; i++) {
            Document doc = relevantDocs.get(i);
            context.append("Document: ").append(doc.getMetadata().getOrDefault("filename", "Unknown"))
                   .append("\nContent: ").append(doc.getText())
                   .append("\n\n");
        }
        
        // Create enhanced prompt with context
        String enhancedPrompt = String.format(
            "Based on the following document excerpts, please answer the question: %s\n\nDocument excerpts:\n%s",
            question, context.toString()
        );
        
        ChatClient chatClient = chatClientBuilder.build();
        return chatClient.prompt()
                .user(enhancedPrompt)
                .call()
                .content();
    }

    /**
     * Check if vector store has any documents
     */
    public boolean hasDocuments() {
        try {
            List<Document> searchResults = vectorStore.similaritySearch("test");
            return !searchResults.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get information about the vector store status
     */
    public String getVectorStoreStatus() {
        if (hasDocuments()) {
            return "Vector store contains documents and is ready for RAG queries.";
        } else {
            return "Vector store is empty. Please upload documents first using /rag/upload endpoint.";
        }
    }
}

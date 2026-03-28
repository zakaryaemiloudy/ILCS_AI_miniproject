package com.estn.ilcsagentai.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.File;

@Configuration
public class VectorStoreConfig {

    @Value("${vector.store.file.path:./data/vectorstore.json}")
    private String vectorStoreFilePath;

    @Bean
    public SimpleVectorStore vectorStore(EmbeddingModel embeddingModel) {
        // Ensure data directory exists
        File vectorStoreFile = new File(vectorStoreFilePath);
        File parentDir = vectorStoreFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel)
                .build();
        
        // Load existing data if file exists
        if (vectorStoreFile.exists()) {
            try {
                vectorStore.load(vectorStoreFile);
                System.out.println("Loaded existing vector store from: " + vectorStoreFilePath);
            } catch (Exception e) {
                System.err.println("Failed to load vector store: " + e.getMessage());
            }
        }
        
        return vectorStore;
    }
}

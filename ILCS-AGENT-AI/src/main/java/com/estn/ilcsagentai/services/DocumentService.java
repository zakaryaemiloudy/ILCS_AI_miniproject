package com.estn.ilcsagentai.services;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentService {

    @Autowired
    private VectorStore vectorStore;

    @Value("${vector.store.file.path:./data/vectorstore.json}")
    private String vectorStoreFilePath;

    /**
     * Load and store a single PDF file into the vector database
     */
    public String loadAndStorePdf(MultipartFile file) throws IOException {
        // Create temporary file
        Path tempFile = Files.createTempFile("upload-", ".tmp");
        try {
            Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
            
            String fullText;
            String filename = file.getOriginalFilename();
            
            // Handle both PDF and text files
            if (filename != null && filename.toLowerCase().endsWith(".pdf")) {
                // Read PDF using PDFBox
                try (PDDocument document = Loader.loadPDF(tempFile.toFile())) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    fullText = stripper.getText(document);
                }
            } else {
                // Read as text file
                fullText = new String(Files.readAllBytes(tempFile));
            }
            
            // Split text into chunks (simple approach)
            List<Document> documents = splitTextIntoChunks(fullText, filename);
            
            // Store in vector database
            vectorStore.add(documents);
            
            // Persist vector store if it's a SimpleVectorStore
            if (vectorStore instanceof org.springframework.ai.vectorstore.SimpleVectorStore) {
                ((org.springframework.ai.vectorstore.SimpleVectorStore) vectorStore).save(new java.io.File(vectorStoreFilePath));
            }
            
            return String.format("Successfully processed and stored %d chunks from %s", 
                    documents.size(), filename);
            
        } finally {
            // Clean up temporary file
            Files.deleteIfExists(tempFile);
        }
    }

    /**
     * Split text into chunks for better vector storage
     */
    private List<Document> splitTextIntoChunks(String text, String filename) {
        List<Document> documents = new ArrayList<>();
        
        // Simple chunking: split by paragraphs and then by character limit
        String[] paragraphs = text.split("\n\n");
        StringBuilder currentChunk = new StringBuilder();
        int chunkSize = 0;
        final int maxChunkSize = 1000; // characters per chunk
        
        for (String paragraph : paragraphs) {
            if (chunkSize + paragraph.length() > maxChunkSize && currentChunk.length() > 0) {
                // Create document from current chunk
                Document doc = new Document(currentChunk.toString());
                doc.getMetadata().put("filename", filename);
                doc.getMetadata().put("upload_time", System.currentTimeMillis());
                documents.add(doc);
                
                // Reset for next chunk
                currentChunk = new StringBuilder();
                chunkSize = 0;
            }
            
            currentChunk.append(paragraph).append("\n\n");
            chunkSize += paragraph.length() + 2;
        }
        
        // Add the last chunk if it has content
        if (currentChunk.length() > 0) {
            Document doc = new Document(currentChunk.toString());
            doc.getMetadata().put("filename", filename);
            doc.getMetadata().put("upload_time", System.currentTimeMillis());
            documents.add(doc);
        }
        
        return documents;
    }

    /**
     * Load and store multiple PDF files
     */
    public String loadAndStorePdfs(List<MultipartFile> files) throws IOException {
        int totalChunks = 0;
        StringBuilder result = new StringBuilder();
        
        for (MultipartFile file : files) {
            try {
                String fileResult = loadAndStorePdf(file);
                result.append(fileResult).append("\n");
                totalChunks += extractChunkCount(fileResult);
            } catch (Exception e) {
                result.append("Error processing ").append(file.getOriginalFilename())
                      .append(": ").append(e.getMessage()).append("\n");
            }
        }
        
        result.insert(0, String.format("Processed %d files. Total chunks stored: %d\n\n", 
                files.size(), totalChunks));
        
        return result.toString();
    }

    private int extractChunkCount(String result) {
        try {
            String[] parts = result.split(" ");
            for (int i = 0; i < parts.length - 1; i++) {
                if (parts[i].equals("chunks") && i > 0) {
                    return Integer.parseInt(parts[i-1]);
                }
            }
        } catch (Exception e) {
            // Ignore parsing errors
        }
        return 0;
    }

    /**
     * Get current vector store statistics
     */
    public String getVectorStoreInfo() {
        try {
            java.io.File vectorFile = new java.io.File(vectorStoreFilePath);
            if (vectorFile.exists()) {
                long size = vectorFile.length();
                return String.format("Vector store file exists at %s (Size: %d bytes)", 
                        vectorStoreFilePath, size);
            } else {
                return "Vector store file does not exist yet. No documents have been uploaded.";
            }
        } catch (Exception e) {
            return "Error accessing vector store: " + e.getMessage();
        }
    }
}

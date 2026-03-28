package com.estn.ilcsagentai.controller;

import com.estn.ilcsagentai.agents.AiAgent;
import com.estn.ilcsagentai.services.DocumentService;
import com.estn.ilcsagentai.services.RagService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class AiAgentController {

    private static final Logger log = LoggerFactory.getLogger(AiAgentController.class);

    @Autowired
    private AiAgent aiAgent;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private RagService ragService;

    @Autowired
    @Qualifier("ttsWebClient")
    private WebClient ttsClient;

    @Value("${spring.ai.openai.api-key:}")
    private String ttsApiKey;

    @Value("${groq.tts.voice:tara}")
    private String ttsVoice;

    @GetMapping(value = "/askAgent", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> askAgent(@RequestParam(defaultValue = "Bonjour") String question) {
        return aiAgent.onQuestion(question);
    }

    @PostMapping(value = "/askWithPdf", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> askWithPdf(MultipartHttpServletRequest request) throws IOException {
        List<MultipartFile> files = request.getFiles("files");
        String question = request.getParameter("question");
        if (question == null || question.isBlank()) {
            question = "Analyse ces documents et résume leur contenu";
        }

        PDFTextStripper stripper = new PDFTextStripper();
        StringBuilder allText = new StringBuilder();
        final int MAX_CHARS_PER_FILE = 8000;

        for (MultipartFile file : files) {
            try (PDDocument document = Loader.loadPDF(file.getBytes())) {
                String pdfText = stripper.getText(document);
                String truncated = pdfText.length() > MAX_CHARS_PER_FILE
                        ? pdfText.substring(0, MAX_CHARS_PER_FILE) + "\n[... tronqué ...]"
                        : pdfText;
                allText.append("--- ").append(file.getOriginalFilename()).append(" ---\n")
                       .append(truncated).append("\n\n");
            } catch (IOException e) {
                allText.append("--- ").append(file.getOriginalFilename())
                       .append(" --- [Erreur de lecture: ").append(e.getMessage()).append("]\n\n");
            }
        }

        String combinedQuestion = "Voici le contenu de " + files.size() + " document(s) PDF:\n\n"
                + allText + "\nQuestion: " + question;
        return aiAgent.onQuestion(combinedQuestion);
    }

    @PostMapping(value = "/tts", produces = "audio/wav")
    public Mono<byte[]> textToSpeech(@RequestBody String text) {
        if (ttsApiKey == null || ttsApiKey.isBlank()) {
            return Mono.error(new RuntimeException("Groq API key not configured"));
        }
        Map<String, Object> body = Map.of(
                "model", "canopylabs/orpheus-v1-english",
                "input", text,
                "voice", ttsVoice,
                "response_format", "wav"
        );
        log.info("TTS request: voice={}, textLength={}", ttsVoice, text.length());
        return ttsClient.post()
                .uri("/v1/audio/speech")
                .header("Authorization", "Bearer " + ttsApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(byte[].class)
                .doOnSuccess(bytes -> log.info("TTS success: {} bytes", bytes != null ? bytes.length : 0))
                .doOnError(e -> {
                    if (e instanceof WebClientResponseException ex) {
                        log.error("TTS error {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
                    } else {
                        log.error("TTS error: {}", e.getMessage());
                    }
                });
    }

    // RAG Endpoints
    
    @PostMapping(value = "/rag/upload", produces = MediaType.TEXT_PLAIN_VALUE)
    public String uploadDocuments(@RequestParam("files") List<MultipartFile> files) {
        try {
            if (files.isEmpty()) {
                return "No files provided. Please upload at least one PDF file.";
            }
            
            String result = documentService.loadAndStorePdfs(files);
            log.info("RAG upload completed: {}", result);
            return result;
            
        } catch (Exception e) {
            log.error("Error uploading documents to RAG: {}", e.getMessage(), e);
            return "Error processing documents: " + e.getMessage();
        }
    }

    @GetMapping(value = "/rag/ask", produces = MediaType.TEXT_PLAIN_VALUE)
    public String askWithRag(@RequestParam String question) {
        try {
            if (question == null || question.trim().isEmpty()) {
                return "Please provide a question.";
            }
            
            log.info("RAG question received: {}", question);
            String response = ragService.askWithRag(question);
            log.info("RAG response generated successfully");
            return response;
            
        } catch (Exception e) {
            log.error("Error processing RAG question: {}", e.getMessage(), e);
            return "Error processing question: " + e.getMessage();
        }
    }

    @GetMapping(value = "/rag/status", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getRagStatus() {
        try {
            String vectorStoreInfo = documentService.getVectorStoreInfo();
            String ragStatus = ragService.getVectorStoreStatus();
            return "RAG System Status:\n" + ragStatus + "\n\nVector Store Info:\n" + vectorStoreInfo;
            
        } catch (Exception e) {
            log.error("Error getting RAG status: {}", e.getMessage(), e);
            return "Error getting RAG status: " + e.getMessage();
        }
    }
}

package com.lightcatch.ai.rag;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Pure Java vector store - no PGVector needed.
 * Stores embeddings as JSON file, does cosine similarity in-memory.
 */
@Slf4j
@Component
public class VectorStore {

    private static final File STORE_FILE = new File("data/vectors.json");
    private final ObjectMapper mapper = new ObjectMapper();
    private final List<VectorEntry> entries = new CopyOnWriteArrayList<>();

    @Data
    public static class VectorEntry {
        private String id;
        private String docId;
        private String knowledgeId;
        private String content;
        private int chunkIndex;
        private String username;
        private long createTime;
        private float[] vector; // embedding vector
    }

    @PostConstruct
    public void init() {
        try {
            if (STORE_FILE.exists()) {
                List<VectorEntry> loaded = mapper.readValue(STORE_FILE, new TypeReference<List<VectorEntry>>() {});
                entries.addAll(loaded);
                log.info("VectorStore loaded {} entries from {}", loaded.size(), STORE_FILE);
            }
        } catch (Exception e) {
            log.warn("Failed to load vector store, starting fresh: {}", e.getMessage());
        }
    }

    public synchronized void insert(String id, String docId, String knowledgeId, String content,
                                     int chunkIndex, String username, float[] vector) {
        VectorEntry entry = new VectorEntry();
        entry.setId(id);
        entry.setDocId(docId);
        entry.setKnowledgeId(knowledgeId);
        entry.setContent(content);
        entry.setChunkIndex(chunkIndex);
        entry.setUsername(username);
        entry.setCreateTime(System.currentTimeMillis());
        entry.setVector(vector);
        entries.add(entry);
        persist();
    }

    public synchronized void deleteByDocId(String docId) {
        entries.removeIf(e -> e.getDocId().equals(docId));
        persist();
    }

    public synchronized void deleteByKnowledgeId(String knowledgeId) {
        entries.removeIf(e -> knowledgeId.equals(e.getKnowledgeId()));
        persist();
    }

    public List<SearchResult> search(String knowledgeId, float[] queryVector, int topK, double minScore) {
        List<SearchResult> results = new ArrayList<>();
        for (VectorEntry e : entries) {
            if (!knowledgeId.equals(e.getKnowledgeId())) continue;
            double sim = cosineSimilarity(queryVector, e.getVector());
            if (sim >= minScore) {
                results.add(new SearchResult(e.getId(), e.getDocId(), e.getContent(),
                        e.getChunkIndex(), e.getCreateTime(), sim));
            }
        }
        results.sort((a, b) -> Double.compare(b.similarity, a.similarity));
        return results.stream().limit(topK).collect(Collectors.toList());
    }

    @Data
    public static class SearchResult {
        private final String id;
        private final String docId;
        private final String content;
        private final int chunkIndex;
        private final long createTime;
        private final double similarity;
    }

    public int countByKnowledgeId(String knowledgeId) {
        return (int) entries.stream().filter(e -> knowledgeId.equals(e.getKnowledgeId())).count();
    }

    private double cosineSimilarity(float[] a, float[] b) {
        if (a == null || b == null || a.length != b.length) return 0;
        double dot = 0, na = 0, nb = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            na += a[i] * a[i];
            nb += b[i] * b[i];
        }
        return na > 0 && nb > 0 ? dot / (Math.sqrt(na) * Math.sqrt(nb)) : 0;
    }

    private synchronized void persist() {
        try {
            STORE_FILE.getParentFile().mkdirs();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            // Strip vectors from serialization to keep file small (keep them for now)
            mapper.writeValue(STORE_FILE, entries);
        } catch (Exception e) {
            log.error("Failed to persist vector store", e);
        }
    }
}

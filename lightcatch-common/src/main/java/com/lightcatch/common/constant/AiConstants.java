package com.lightcatch.common.constant;

public class AiConstants {
    public static final String MODEL_TYPE_LLM = "chat";
    public static final String MODEL_TYPE_EMBED = "embedding";
    public static final String MODEL_TYPE_IMAGE = "image";
    public static final String MODEL_TYPE_VOICE = "voice";
    public static final String MODEL_TYPE_VIDEO = "video";

    public static final String KNOWLEDGE_TYPE_KNOWLEDGE = "knowledge";
    public static final String KNOWLEDGE_TYPE_MEMORY = "memory";

    public static final String DOC_STATUS_PROCESSING = "0";
    public static final String DOC_STATUS_READY = "1";
    public static final String DOC_STATUS_FAILED = "2";

    public static final String MESSAGE_ROLE_USER = "user";
    public static final String MESSAGE_ROLE_AI = "assistant";
    public static final String MESSAGE_ROLE_TOOL = "tool";

    public static final String CONV_TYPE_CHAT = "chat";
    public static final String CONV_TYPE_FLOW = "workflow";

    public static final int DEFAULT_CHUNK_SIZE = 500;
    public static final int DEFAULT_CHUNK_OVERLAP = 50;
    public static final int DEFAULT_TOP_K = 5;
    public static final double DEFAULT_SIMILARITY_THRESHOLD = 0.75;
    public static final int DEFAULT_EMBEDDING_DIM = 1536;
}

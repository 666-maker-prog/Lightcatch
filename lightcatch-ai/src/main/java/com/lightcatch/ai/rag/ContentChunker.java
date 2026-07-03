package com.lightcatch.ai.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ContentChunker {
    private static final Pattern HTML_TABLE_PATTERN = Pattern.compile("(?is)<table\\b.*?</table>");
    private static final String[] SEPARATORS = {"\n\n", "\n", ". ", " ", ""};

    public List<String> chunk(String content, int chunkSize, int overlap) {
        Matcher tableMatcher = HTML_TABLE_PATTERN.matcher(content);
        if (tableMatcher.find()) {
            return chunkWithHtmlTables(content, chunkSize, overlap);
        }
        return recursiveChunk(content, chunkSize, overlap);
    }

    private List<String> chunkWithHtmlTables(String content, int chunkSize, int overlap) {
        List<String> result = new ArrayList<>();
        Matcher matcher = HTML_TABLE_PATTERN.matcher(content);
        int lastEnd = 0;
        while (matcher.find()) {
            String before = content.substring(lastEnd, matcher.start());
            if (!before.isBlank()) {
                result.addAll(recursiveChunk(before, chunkSize, overlap));
            }
            result.add(matcher.group());
            lastEnd = matcher.end();
        }
        String remaining = content.substring(lastEnd);
        if (!remaining.isBlank()) {
            result.addAll(recursiveChunk(remaining, chunkSize, overlap));
        }
        return result;
    }

    private List<String> recursiveChunk(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.length() <= chunkSize) {
            if (text != null && !text.isBlank()) chunks.add(text);
            return chunks;
        }
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            if (end < text.length()) {
                int splitPos = findSplitPosition(text, start, end);
                if (splitPos > start) end = splitPos;
            }
            chunks.add(text.substring(start, end).trim());
            start = end - overlap;
            if (start < 0) start = 0;
        }
        return chunks;
    }

    private int findSplitPosition(String text, int start, int end) {
        String segment = text.substring(start, end);
        for (String sep : SEPARATORS) {
            if (sep.isEmpty()) break;
            int idx = segment.lastIndexOf(sep);
            if (idx > 0) return start + idx + sep.length();
        }
        return end;
    }
}

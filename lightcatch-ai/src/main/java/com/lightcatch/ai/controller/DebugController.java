package com.lightcatch.ai.controller;

import com.lightcatch.common.annotation.IgnoreAuth;
import com.lightcatch.common.model.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/debug")
public class DebugController {

    private static final String FRONTEND_LOG = "logs/frontend.log";

    @IgnoreAuth
    @PostMapping("/log")
    public Result<?> receiveLogs(@RequestBody Map<String, Object> body) {
        Object data = body.get("logs");
        if (data instanceof List) {
            try {
                Files.createDirectories(Paths.get("logs"));
                try (PrintWriter pw = new PrintWriter(new FileWriter(FRONTEND_LOG, true))) {
                    for (Object entry : (List<?>) data) {
                        if (entry instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> m = (Map<String, Object>) entry;
                            pw.printf("[%s][%s] %s%n",
                                    String.valueOf(m.getOrDefault("time", "")),
                                    String.valueOf(m.getOrDefault("type", "")),
                                    String.valueOf(m.getOrDefault("message", "")));
                            Object detail = m.get("data");
                            if (detail != null && !detail.toString().isEmpty()) {
                                pw.println("  => " + detail);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Failed to write frontend log", e);
            }
        }
        return Result.ok();
    }

    @IgnoreAuth
    @GetMapping("/frontend-log")
    public Result<String> getFrontendLog() {
        try {
            String content = Files.readString(Paths.get(FRONTEND_LOG));
            return Result.ok(content);
        } catch (Exception e) {
            return Result.ok("(日志文件尚不存在)");
        }
    }
}

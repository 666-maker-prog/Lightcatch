package com.lightcatch.ai.controller;

import com.lightcatch.common.model.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Value("${lightcatch.ai.upload-path:./upload}")
    private String uploadPath;

    @PostMapping("/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String ext = file.getOriginalFilename();
            ext = ext != null && ext.contains(".") ? ext.substring(ext.lastIndexOf(".")) : ".jpg";
            String filename = UUID.randomUUID().toString().replace("-", "") + ext;
            Path dir = Path.of(uploadPath, "images");
            Files.createDirectories(dir);
            File dest = dir.resolve(filename).toFile();
            file.transferTo(dest);
            String url = "/uploads/images/" + filename;
            return Result.ok(url);
        } catch (Exception e) {
            return Result.error("上传失败: " + e.getMessage());
        }
    }
}

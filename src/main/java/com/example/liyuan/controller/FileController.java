package com.example.liyuan.controller;

import com.example.liyuan.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/file")
public class FileController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 上传音频文件
     */
    @PostMapping("/upload/audio")
    public ResponseEntity<?> uploadAudio(
            @RequestParam("file") MultipartFile file,
            @RequestParam("audioName") String audioName,
            @RequestParam(value = "duration", required = false) Integer duration,
            HttpServletRequest request) {

        try {
            Long userId = jwtUtils.getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("未授权访问"));
            }

            // 验证文件类型
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("文件名不能为空"));
            }

            String filenameLower = originalFilename.toLowerCase();
            if (!filenameLower.endsWith(".mp3") && !filenameLower.endsWith(".wav") && !filenameLower.endsWith(".aac")) {
                return ResponseEntity.badRequest().body(createErrorResponse("只支持MP3、WAV、AAC格式文件"));
            }

            // 创建按日期分组的目录结构
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String datePath = dateFormat.format(new Date());
            String userPath = "user_" + userId;

            // 完整保存路径
            String savePath = Paths.get(uploadDir, "audios", datePath, userPath).toString();

            // 创建目录
            Path directory = Paths.get(savePath);
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }

            // 生成唯一文件名
            String fileExtension = getFileExtension(originalFilename);
            String newFilename = UUID.randomUUID().toString() + fileExtension;
            String fullPath = Paths.get(savePath, newFilename).toString();

            // 保存文件
            File destFile = new File(fullPath);
            file.transferTo(destFile);

            // 构建可访问的URL（注意：由于设置了 server.servlet.context-path=/api，需要加上 /api 前缀）
            // 相对路径：/uploads/audios/2025-12-05/user_1/filename.mp3
            // 实际访问URL：http://localhost:8080/api/uploads/audios/2025-12-05/user_1/filename.mp3
            String relativePath = "audios/" + datePath + "/" + userPath + "/" + newFilename;
            String fileUrl = "/api/uploads/" + relativePath;

            // 转换为URL格式（正斜杠）
            fileUrl = fileUrl.replace("\\", "/");

            // 返回文件信息
            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("originalName", originalFilename);
            fileInfo.put("fileName", newFilename);
            fileInfo.put("filePath", fullPath); // 完整的本地文件路径（uploads\...）
            fileInfo.put("fileUrl", fileUrl);   // 可访问的URL（/api/uploads/...）
            fileInfo.put("relativePath", relativePath); // 相对路径
            fileInfo.put("fileSize", file.getSize());
            fileInfo.put("audioName", audioName);
            fileInfo.put("duration", duration);
            fileInfo.put("uploadTime", new Date());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "文件上传成功");
            response.put("data", fileInfo);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(createErrorResponse("文件上传失败: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(createErrorResponse("服务器内部错误: " + e.getMessage()));
        }
    }

    /**
     * 上传文件通用接口（支持多种文件类型）
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "audio") String type,
            HttpServletRequest request) {

        try {
            Long userId = jwtUtils.getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("未授权访问"));
            }

            // 验证文件类型
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("文件名不能为空"));
            }

            // 根据文件类型确定保存目录
            String subDirectory;
            String[] allowedExtensions;

            switch (type.toLowerCase()) {
                case "image":
                    subDirectory = "images";
                    allowedExtensions = new String[]{".jpg", ".jpeg", ".png", ".gif", ".bmp"};
                    break;
                case "audio":
                    subDirectory = "audios";
                    allowedExtensions = new String[]{".mp3", ".wav", ".aac", ".m4a"};
                    break;
                case "video":
                    subDirectory = "videos";
                    allowedExtensions = new String[]{".mp4", ".avi", ".mov", ".wmv"};
                    break;
                case "document":
                    subDirectory = "documents";
                    allowedExtensions = new String[]{".pdf", ".doc", ".docx", ".txt"};
                    break;
                default:
                    return ResponseEntity.badRequest().body(createErrorResponse("不支持的文件类型"));
            }

            // 检查文件扩展名
            String fileExtension = getFileExtension(originalFilename).toLowerCase();
            boolean validExtension = false;
            for (String ext : allowedExtensions) {
                if (fileExtension.equals(ext)) {
                    validExtension = true;
                    break;
                }
            }

            if (!validExtension) {
                return ResponseEntity.badRequest().body(createErrorResponse("不支持的文件格式，请上传" + Arrays.toString(allowedExtensions) + "格式"));
            }

            // 创建按日期分组的目录结构
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String datePath = dateFormat.format(new Date());
            String userPath = "user_" + userId;

            // 完整保存路径
            String savePath = Paths.get(uploadDir, subDirectory, datePath, userPath).toString();

            // 创建目录
            Path directory = Paths.get(savePath);
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }

            // 生成唯一文件名
            String newFilename = UUID.randomUUID().toString() + fileExtension;
            String fullPath = Paths.get(savePath, newFilename).toString();

            // 保存文件
            File destFile = new File(fullPath);
            file.transferTo(destFile);

            // 构建可访问的URL
            String relativePath = subDirectory + "/" + datePath + "/" + userPath + "/" + newFilename;
            String fileUrl = "/api/uploads/" + relativePath;
            fileUrl = fileUrl.replace("\\", "/");

            // 返回文件信息
            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("originalName", originalFilename);
            fileInfo.put("fileName", newFilename);
            fileInfo.put("filePath", fullPath);
            fileInfo.put("fileUrl", fileUrl);
            fileInfo.put("relativePath", relativePath);
            fileInfo.put("fileSize", file.getSize());
            fileInfo.put("fileType", type);
            fileInfo.put("uploadTime", new Date());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "文件上传成功");
            response.put("data", fileInfo);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(createErrorResponse("文件上传失败: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(createErrorResponse("服务器内部错误: " + e.getMessage()));
        }
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteFile(
            @RequestBody Map<String, String> requestData,
            HttpServletRequest request) {

        try {
            Long userId = jwtUtils.getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("未授权访问"));
            }

            String filePath = requestData.get("filePath");
            if (filePath == null || filePath.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("文件路径不能为空"));
            }

            // 验证文件是否属于当前用户
            // 这里可以添加更严格的验证逻辑，比如检查文件路径是否包含用户的ID

            File file = new File(filePath);
            if (!file.exists()) {
                return ResponseEntity.badRequest().body(createErrorResponse("文件不存在"));
            }

            // 检查文件是否属于当前用户（简单验证）
            if (!filePath.contains("user_" + userId)) {
                return ResponseEntity.status(403).body(createErrorResponse("无权删除此文件"));
            }

            boolean deleted = file.delete();
            if (deleted) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "文件删除成功");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(500).body(createErrorResponse("文件删除失败"));
            }

        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("删除文件失败: " + e.getMessage()));
        }
    }

    /**
     * 获取服务器文件列表（调试用）
     */
    @GetMapping("/list")
    public ResponseEntity<?> listFiles(
            @RequestParam(value = "type", defaultValue = "audio") String type,
            @RequestParam(value = "date", required = false) String date,
            HttpServletRequest request) {

        try {
            Long userId = jwtUtils.getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("未授权访问"));
            }

            // 构建目录路径
            String subDirectory = type.toLowerCase();
            String datePath = date != null ? date : new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            String userPath = "user_" + userId;

            String directoryPath = Paths.get(uploadDir, subDirectory, datePath, userPath).toString();

            File directory = new File(directoryPath);

            Map<String, Object> result = new HashMap<>();
            result.put("directoryPath", directoryPath);
            result.put("exists", directory.exists());

            if (directory.exists() && directory.isDirectory()) {
                File[] files = directory.listFiles();
                if (files != null) {
                    List<Map<String, Object>> fileList = new ArrayList<>();
                    for (File file : files) {
                        if (file.isFile()) {
                            Map<String, Object> fileInfo = new HashMap<>();
                            fileInfo.put("name", file.getName());
                            fileInfo.put("path", file.getAbsolutePath());
                            fileInfo.put("size", file.length());
                            fileInfo.put("lastModified", new Date(file.lastModified()));

                            // 构建访问URL
                            String relativePath = subDirectory + "/" + datePath + "/" + userPath + "/" + file.getName();
                            String fileUrl = "/api/uploads/" + relativePath;
                            fileInfo.put("url", fileUrl);

                            fileList.add(fileInfo);
                        }
                    }
                    result.put("files", fileList);
                    result.put("fileCount", fileList.size());
                }
            } else {
                result.put("files", Collections.emptyList());
                result.put("fileCount", 0);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("获取文件列表失败: " + e.getMessage()));
        }
    }

    /**
     * 测试路径转换（调试用）
     */
    @GetMapping("/test/path")
    public ResponseEntity<?> testPath() {
        try {
            Map<String, Object> result = new HashMap<>();

            // 当前配置信息
            result.put("uploadDir", uploadDir);
            result.put("serverContextPath", "/api");

            // 示例路径
            String exampleRelativePath = "audios/2025-12-05/user_1/test.mp3";
            String exampleLocalPath = Paths.get(uploadDir, exampleRelativePath).toString();
            String exampleUrl = "/api/uploads/" + exampleRelativePath;
            String exampleFullUrl = "http://localhost:8080" + exampleUrl;

            result.put("exampleRelativePath", exampleRelativePath);
            result.put("exampleLocalPath", exampleLocalPath);
            result.put("exampleUrl", exampleUrl);
            result.put("exampleFullUrl", exampleFullUrl);

            // 检查目录是否存在
            Path uploadPath = Paths.get(uploadDir);
            result.put("uploadPathExists", Files.exists(uploadPath));
            if (Files.exists(uploadPath)) {
                result.put("uploadPath", uploadPath.toAbsolutePath().toString());
            }

            // 创建测试目录（如果不存在）
            Path testDir = Paths.get(uploadDir, "audios", "2025-12-05", "user_1");
            if (!Files.exists(testDir)) {
                Files.createDirectories(testDir);
                result.put("testDirCreated", true);
                result.put("testDirPath", testDir.toAbsolutePath().toString());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("测试失败: " + e.getMessage()));
        }
    }

    /**
     * 检查文件是否存在
     */
    @PostMapping("/check")
    public ResponseEntity<?> checkFile(@RequestBody Map<String, String> requestData) {
        try {
            String filePath = requestData.get("filePath");
            if (filePath == null || filePath.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("文件路径不能为空"));
            }

            File file = new File(filePath);
            Map<String, Object> result = new HashMap<>();
            result.put("exists", file.exists());
            result.put("path", file.getAbsolutePath());
            result.put("isFile", file.isFile());

            if (file.exists()) {
                result.put("size", file.length());
                result.put("lastModified", new Date(file.lastModified()));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("检查文件失败: " + e.getMessage()));
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".mp3"; // 默认扩展名
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        return errorResponse;
    }
}
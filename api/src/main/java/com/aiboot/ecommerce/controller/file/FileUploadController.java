package com.aiboot.ecommerce.controller.file;

import com.aiboot.ecommerce.common.api.ApiResponse;
import com.aiboot.ecommerce.dto.file.FileUploadResponse;
import com.aiboot.ecommerce.security.AuthHelper;
import com.aiboot.ecommerce.service.FileStorageService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final FileStorageService fileStorageService;

    public FileUploadController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    public ApiResponse<FileUploadResponse> upload(@RequestParam("file") MultipartFile file) {
        AuthHelper.requireBackendUser();
        return ApiResponse.success("上传成功", fileStorageService.uploadFile(file));
    }
}

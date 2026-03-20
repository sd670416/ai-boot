package com.aiboot.ecommerce.service;

import com.aiboot.ecommerce.dto.file.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    FileUploadResponse uploadFile(MultipartFile file);
}

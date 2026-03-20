package com.aiboot.ecommerce.dto.file;

import lombok.Data;

@Data
public class FileUploadResponse {

    private String objectKey;
    private String url;
    private String originalFilename;
}

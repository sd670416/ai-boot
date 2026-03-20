package com.aiboot.ecommerce.config.oss;

import lombok.Data;

@Data
public class OssRuntimeConfig {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
    private String domain;
    private String basePath;
}

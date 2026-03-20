package com.aiboot.ecommerce.service.impl.file;

import com.aiboot.ecommerce.config.oss.OssProperties;
import com.aiboot.ecommerce.dto.file.FileUploadResponse;
import com.aiboot.ecommerce.service.FileStorageService;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class OssFileStorageServiceImpl implements FileStorageService {

    private final OssProperties ossProperties;

    public OssFileStorageServiceImpl(OssProperties ossProperties) {
        this.ossProperties = ossProperties;
    }

    @Override
    public FileUploadResponse uploadFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf('.'))
                : ".bin";
        String folder = resolveFolder(file.getContentType());
        String datePath = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
        String objectKey = normalizeBasePath(ossProperties.getBasePath()) + "/" + folder + "/" + datePath + "/"
                + UUID.randomUUID().toString().replace("-", "") + extension;

        OSS ossClient = new OSSClientBuilder().build(
                ossProperties.getEndpoint(),
                ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret());
        try (InputStream inputStream = file.getInputStream()) {
            ossClient.putObject(ossProperties.getBucketName(), objectKey, inputStream);
        } catch (Exception exception) {
            throw new RuntimeException("OSS上传失败: " + exception.getMessage(), exception);
        } finally {
            ossClient.shutdown();
        }

        FileUploadResponse response = new FileUploadResponse();
        response.setObjectKey(objectKey);
        response.setOriginalFilename(originalFilename);
        response.setUrl(buildUrl(objectKey));
        return response;
    }

    private String resolveFolder(String contentType) {
        if (contentType != null) {
            if (contentType.startsWith("image/")) {
                return "images";
            }
            if (contentType.startsWith("video/")) {
                return "videos";
            }
        }
        return "files";
    }

    private String normalizeBasePath(String basePath) {
        if (basePath == null || basePath.trim().isEmpty()) {
            return "ai-boot/uploads";
        }
        return basePath.endsWith("/") ? basePath.substring(0, basePath.length() - 1) : basePath;
    }

    private String buildUrl(String objectKey) {
        if (ossProperties.getDomain() != null && !ossProperties.getDomain().trim().isEmpty()) {
            return ossProperties.getDomain().replaceAll("/$", "") + "/" + objectKey;
        }
        return "https://" + ossProperties.getBucketName() + "." + ossProperties.getEndpoint() + "/" + objectKey;
    }
}

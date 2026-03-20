package com.aiboot.ecommerce.common.constant;

public final class SystemParamConstants {

    public static final String TYPE_SYSTEM = "SYSTEM";
    public static final String TYPE_BUSINESS = "BUSINESS";

    public static final String GROUP_OSS = "oss";

    public static final String OSS_ENDPOINT = "oss.endpoint";
    public static final String OSS_ACCESS_KEY_ID = "oss.accessKeyId";
    public static final String OSS_ACCESS_KEY_SECRET = "oss.accessKeySecret";
    public static final String OSS_BUCKET_NAME = "oss.bucketName";
    public static final String OSS_DOMAIN = "oss.domain";
    public static final String OSS_BASE_PATH = "oss.basePath";

    public static final String REDIS_KEY_PREFIX = "sys:param:";

    private SystemParamConstants() {
    }
}

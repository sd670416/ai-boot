USE `ai-boot`;

INSERT INTO `backend_users` (`username`, `password`, `nickname`, `phone`, `email`, `role_code`, `status`) VALUES
('admin', '123456', '系统管理员', '13800000000', 'admin@aiboot.com', 'SUPER_ADMIN', 1),
('operator01', '123456', '运营小王', '13800000002', 'operator01@aiboot.com', 'OPERATOR', 1);

INSERT INTO `frontend_users` (`username`, `password`, `nickname`, `phone`, `email`, `status`) VALUES
('buyer01', '123456', '张三', '13900000001', 'buyer01@aiboot.com', 1);

INSERT INTO `sys_param_config` (`param_type`, `param_group`, `param_name`, `param_key`, `param_value`, `value_type`, `remark`, `status`) VALUES
('SYSTEM', 'oss', 'OSS Endpoint', 'oss.endpoint', 'http://oss-cn-beijing.aliyuncs.com', 'STRING', '阿里云 OSS 访问节点', 1),
('SYSTEM', 'oss', 'OSS AccessKeyId', 'oss.accessKeyId', '', 'PASSWORD', '请在后台参数配置页面维护真实值', 1),
('SYSTEM', 'oss', 'OSS AccessKeySecret', 'oss.accessKeySecret', '', 'PASSWORD', '请在后台参数配置页面维护真实值', 1),
('SYSTEM', 'oss', 'OSS Bucket', 'oss.bucketName', 'pst-test', 'STRING', 'OSS Bucket 名称', 1),
('SYSTEM', 'oss', 'OSS Domain', 'oss.domain', 'https://pst-test.oss-cn-beijing.aliyuncs.com', 'STRING', '文件访问域名', 1),
('SYSTEM', 'oss', 'OSS BasePath', 'oss.basePath', 'wftest/ai-boot/uploads', 'STRING', '上传根目录', 1),
('BUSINESS', 'shop', '商城名称', 'shop.name', 'AI Boot Mall', 'STRING', '前后台可复用的商城展示名称', 1),
('BUSINESS', 'order', '自动取消订单分钟数', 'order.autoCancelMinutes', '30', 'NUMBER', '超时未支付自动取消订单', 1);

INSERT INTO `user_addresses` (`user_id`, `receiver_name`, `receiver_phone`, `province`, `city`, `district`, `detail_address`, `postal_code`, `is_default`, `status`) VALUES
(1, '张三', '13900000001', '上海市', '上海市', '浦东新区', 'XX路88号1201室', '200120', 1, 1),
(1, '张三', '13900000001', '江苏省', '苏州市', '工业园区', '星湖街100号', '215000', 0, 1);

INSERT INTO `categories` (`name`, `description`, `sort_order`, `status`) VALUES
('手机数码', '手机与智能设备', 1, 1),
('电脑办公', '电脑与办公设备', 2, 1),
('家用电器', '常用家电商品', 3, 1);

INSERT INTO `products` (`id`, `category_id`, `name`, `subtitle`, `description`, `price`, `stock`, `cover_image`, `detail_images`, `status`) VALUES
(1, 1, '旗舰手机 X1', '高性能 5G 手机', '<p>8G+256G，OLED 高刷屏，5000mAh 电池</p>', 4599.00, 100, 'https://test-ai-boot-bucket.oss-cn-hangzhou.aliyuncs.com/demo/phone-x1.jpg', '["https://test-ai-boot-bucket.oss-cn-hangzhou.aliyuncs.com/demo/phone-x1-detail-1.jpg","https://test-ai-boot-bucket.oss-cn-hangzhou.aliyuncs.com/demo/phone-x1-detail-2.jpg"]', 1),
(2, 2, '轻薄笔记本 Air', '便携办公本', '<p>16G+512G SSD，13.3 英寸</p>', 5599.00, 50, 'https://test-ai-boot-bucket.oss-cn-hangzhou.aliyuncs.com/demo/laptop-air.jpg', '["https://test-ai-boot-bucket.oss-cn-hangzhou.aliyuncs.com/demo/laptop-air-detail-1.jpg"]', 1),
(3, 3, '智能空气炸锅', '健康烹饪小家电', '<p>6L 大容量，支持多档温控</p>', 399.00, 80, 'https://test-ai-boot-bucket.oss-cn-hangzhou.aliyuncs.com/demo/fryer.jpg', '["https://test-ai-boot-bucket.oss-cn-hangzhou.aliyuncs.com/demo/fryer-detail-1.jpg"]', 1);

INSERT INTO `product_skus` (`product_id`, `sku_code`, `sku_name`, `spec_values`, `image`, `sale_price`, `stock`, `status`) VALUES
(1, 'PHONE-X1-BLK-8-256', '旗舰手机 X1 黑色 8G+256G', '颜色:黑色;内存:8G+256G', 'https://test-ai-boot-bucket.oss-cn-hangzhou.aliyuncs.com/demo/phone-x1-black.jpg', 4599.00, 40, 1),
(1, 'PHONE-X1-WHT-12-512', '旗舰手机 X1 白色 12G+512G', '颜色:白色;内存:12G+512G', 'https://test-ai-boot-bucket.oss-cn-hangzhou.aliyuncs.com/demo/phone-x1-white.jpg', 5299.00, 60, 1),
(2, 'AIR-13-SILVER-16-512', '轻薄笔记本 Air 银色 16G+512G', '颜色:银色;配置:16G+512G', 'https://test-ai-boot-bucket.oss-cn-hangzhou.aliyuncs.com/demo/laptop-air-silver.jpg', 5599.00, 30, 1),
(2, 'AIR-13-GRAY-32-1T', '轻薄笔记本 Air 深空灰 32G+1T', '颜色:深空灰;配置:32G+1T', 'https://test-ai-boot-bucket.oss-cn-hangzhou.aliyuncs.com/demo/laptop-air-gray.jpg', 6999.00, 20, 1),
(3, 'FRYER-6L-WHITE', '智能空气炸锅 白色 6L', '颜色:白色;容量:6L', 'https://test-ai-boot-bucket.oss-cn-hangzhou.aliyuncs.com/demo/fryer-white.jpg', 399.00, 50, 1),
(3, 'FRYER-8L-BLACK', '智能空气炸锅 黑色 8L', '颜色:黑色;容量:8L', 'https://test-ai-boot-bucket.oss-cn-hangzhou.aliyuncs.com/demo/fryer-black.jpg', 499.00, 30, 1);

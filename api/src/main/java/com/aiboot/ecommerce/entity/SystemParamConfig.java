package com.aiboot.ecommerce.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_param_config")
public class SystemParamConfig extends BaseEntity {

    private String paramType;
    private String paramGroup;
    private String paramName;
    private String paramKey;
    private String paramValue;
    private String valueType;
    private String remark;
    private Integer status;
}

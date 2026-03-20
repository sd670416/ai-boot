package com.aiboot.ecommerce.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SystemParamSaveRequest {

    @NotBlank(message = "参数类型不能为空")
    private String paramType;

    private String paramGroup;

    @NotBlank(message = "参数名称不能为空")
    private String paramName;

    @NotBlank(message = "参数键不能为空")
    private String paramKey;

    private String paramValue;

    @NotBlank(message = "值类型不能为空")
    private String valueType;

    private String remark;

    @NotNull(message = "状态不能为空")
    private Integer status;
}

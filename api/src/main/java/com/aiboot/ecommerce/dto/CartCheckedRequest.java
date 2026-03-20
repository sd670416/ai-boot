package com.aiboot.ecommerce.dto;

import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartCheckedRequest {

    @NotNull(message = "勾选状态不能为空")
    private Integer checked;
}

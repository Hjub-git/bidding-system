package com.bidding.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 修改招标项目入参（部分更新）。
 * 与创建不同：所有字段都可选——管理员可能只改 status，招标单位也可能不改 project_number。
 * 后端用 COALESCE 语义（MyBatis-Plus updateById 忽略 null 字段）只更新传入的字段。
 */
@Data
public class TenderUpdateRequest {

    private String projectNumber;
    private String equipmentName;
    private String model;
    private String packagingRequirements;
    private Integer quantity;
    private LocalDate deadline;
    private BigDecimal deposit;
    private String description;
    private String status;
}

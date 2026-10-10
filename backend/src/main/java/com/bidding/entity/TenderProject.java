package com.bidding.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 招标项目表。
 */
@Data
@TableName("tender_projects")
public class TenderProject {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String projectNumber;
    private Long tenderUnitId;
    private String equipmentName;
    private String model;
    private String packagingRequirements;
    private Integer quantity;
    private LocalDate deadline;

    /** 金额用 BigDecimal，避免 double 的精度丢失 */
    private BigDecimal deposit;

    private String description;

    /** open / closed / cancelled */
    private String status;

    /** 逻辑删除标记：1=已删除，0=正常 */
    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

package com.bidding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bidding.common.BusinessException;
import com.bidding.common.PageResult;
import com.bidding.common.ResultCode;
import com.bidding.dto.TenderRequest;
import com.bidding.dto.TenderUpdateRequest;
import com.bidding.entity.TenderProject;
import com.bidding.entity.TenderUnit;
import com.bidding.mapper.TenderProjectMapper;
import com.bidding.mapper.TenderUnitMapper;
import com.bidding.security.SecurityUtil;
import com.bidding.service.TenderService;
import com.bidding.vo.TenderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TenderServiceImpl implements TenderService {

    private final TenderProjectMapper tenderProjectMapper;
    private final TenderUnitMapper tenderUnitMapper;

    @Override
    public PageResult<TenderVO> listTenders(String keyword, String status, long page, long pageSize) {
        IPage<TenderVO> result = tenderProjectMapper.selectTenderPage(
                new Page<>(page, pageSize), normalize(status), normalize(keyword));
        return new PageResult<>(result.getRecords(), result.getTotal());
    }

    @Override
    public TenderVO getTender(Long id) {
        TenderVO vo = tenderProjectMapper.selectTenderVOById(id);
        if (vo == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "招标项目不存在");
        }
        return vo;
    }

    @Override
    public TenderVO createTender(TenderRequest request) {
        Long unitId = getCurrentTenderUnitId();
        TenderProject tp = new TenderProject();
        tp.setProjectNumber(request.getProjectNumber());
        tp.setTenderUnitId(unitId);
        tp.setEquipmentName(request.getEquipmentName());
        tp.setModel(request.getModel());
        tp.setPackagingRequirements(request.getPackagingRequirements());
        tp.setQuantity(request.getQuantity());
        tp.setDeadline(request.getDeadline());
        tp.setDeposit(request.getDeposit());
        tp.setDescription(request.getDescription());
        tp.setStatus("open");
        try {
            tenderProjectMapper.insert(tp);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "项目编号已存在，请更换唯一编号");
        }
        return tenderProjectMapper.selectTenderVOById(tp.getId());
    }

    @Override
    public void updateTender(Long id, TenderUpdateRequest request) {
        TenderProject tp = tenderProjectMapper.selectById(id);
        if (tp == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "招标项目不存在");
        }
        // 越权防护：招标单位只能改本单位项目（admin 可改任意）
        if ("tender".equals(SecurityUtil.getCurrentRole())
                && !getCurrentTenderUnitId().equals(tp.getTenderUnitId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能修改本单位发布的招标项目");
        }

        // 只更新传入的非空字段（MyBatis-Plus updateById 默认忽略 null，等价于 SQL 的 COALESCE）
        TenderProject update = new TenderProject();
        update.setId(id);
        update.setProjectNumber(normalize(request.getProjectNumber()));
        update.setEquipmentName(normalize(request.getEquipmentName()));
        update.setModel(normalize(request.getModel()));
        update.setPackagingRequirements(normalize(request.getPackagingRequirements()));
        update.setQuantity(request.getQuantity());
        update.setDeadline(request.getDeadline());
        update.setDeposit(request.getDeposit());
        update.setDescription(normalize(request.getDescription()));
        update.setStatus(normalize(request.getStatus()));
        tenderProjectMapper.updateById(update);
    }

    @Override
    public void deleteTender(Long id) {
        TenderProject tp = tenderProjectMapper.selectById(id);
        if (tp == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "招标项目不存在");
        }
        if ("tender".equals(SecurityUtil.getCurrentRole())
                && !getCurrentTenderUnitId().equals(tp.getTenderUnitId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能删除本单位发布的招标项目");
        }
        // 逻辑删除：@TableLogic 会把 DELETE 转成 UPDATE deleted=1
        tenderProjectMapper.deleteById(id);
    }

    @Override
    public List<TenderVO> listMyTenders() {
        return tenderProjectMapper.selectMyTenderVOList(getCurrentTenderUnitId());
    }

    private Long getCurrentTenderUnitId() {
        Long userId = SecurityUtil.getCurrentUserId();
        TenderUnit unit = tenderUnitMapper.selectOne(
                new LambdaQueryWrapper<TenderUnit>().eq(TenderUnit::getUserId, userId));
        if (unit == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "招标单位信息不存在");
        }
        return unit.getId();
    }

    /** 空串/空白串归一为 null，避免把空值写进数据库 */
    private String normalize(String s) {
        return StringUtils.hasText(s) ? s : null;
    }
}

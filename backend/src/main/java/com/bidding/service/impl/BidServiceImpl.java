package com.bidding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bidding.common.BusinessException;
import com.bidding.common.ResultCode;
import com.bidding.dto.BidRequest;
import com.bidding.dto.BidUpdateRequest;
import com.bidding.entity.Bid;
import com.bidding.entity.Manufacturer;
import com.bidding.entity.TenderProject;
import com.bidding.entity.TenderUnit;
import com.bidding.mapper.BidMapper;
import com.bidding.mapper.ManufacturerMapper;
import com.bidding.mapper.TenderProjectMapper;
import com.bidding.mapper.TenderUnitMapper;
import com.bidding.security.SecurityUtil;
import com.bidding.service.BidService;
import com.bidding.vo.BidVO;
import com.bidding.vo.MyBidVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BidServiceImpl implements BidService {

    private static final String SUBMITTED = "submitted";
    private static final String ACCEPTED = "accepted";
    private static final String REJECTED = "rejected";

    private final BidMapper bidMapper;
    private final ManufacturerMapper manufacturerMapper;
    private final TenderProjectMapper tenderProjectMapper;
    private final TenderUnitMapper tenderUnitMapper;

    @Override
    public Long createBid(BidRequest request) {
        Long manufacturerId = getCurrentManufacturerId();

        TenderProject project = tenderProjectMapper.selectById(request.getTenderProjectId());
        if (project == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "招标项目不存在");
        }
        if (!"open".equals(project.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该招标项目当前不可投标");
        }

        // 重复投标防护：同一厂家对同一项目只允许一条投标
        Long exists = bidMapper.selectCount(new LambdaQueryWrapper<Bid>()
                .eq(Bid::getManufacturerId, manufacturerId)
                .eq(Bid::getTenderProjectId, request.getTenderProjectId()));
        if (exists != null && exists > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "您已对该项目投过标，请勿重复投标");
        }

        Bid bid = new Bid();
        bid.setTenderProjectId(request.getTenderProjectId());
        bid.setManufacturerId(manufacturerId);
        bid.setBidDate(request.getBidDate() != null ? request.getBidDate() : LocalDate.now());
        bid.setProductionDate(request.getProductionDate());
        bid.setWarrantyPeriod(request.getWarrantyPeriod());
        bid.setTotalAmount(request.getTotalAmount());
        bid.setDepositPaymentDate(request.getDepositPaymentDate());
        bid.setBidDocument(request.getBidDocument());
        bid.setStatus(SUBMITTED);
        try {
            bidMapper.insert(bid);
        } catch (DuplicateKeyException e) {
            // 数据库唯一约束兜底，防并发下重复投标
            throw new BusinessException(ResultCode.BAD_REQUEST, "您已对该项目投过标，请勿重复投标");
        }
        return bid.getId();
    }

    @Override
    public List<BidVO> listBids(Long tenderProjectId) {
        return bidMapper.selectBidVOList(tenderProjectId);
    }

    @Override
    public List<MyBidVO> listMyBids() {
        return bidMapper.selectMyBidVOList(getCurrentManufacturerId());
    }

    @Override
    public void updateBid(Long id, BidUpdateRequest request) {
        Bid bid = getOwnedBid(id);
        if (!SUBMITTED.equals(bid.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该投标已处理，无法修改");
        }
        Bid update = new Bid();
        update.setId(id);
        update.setProductionDate(request.getProductionDate());
        update.setWarrantyPeriod(request.getWarrantyPeriod());
        update.setTotalAmount(request.getTotalAmount());
        update.setDepositPaymentDate(request.getDepositPaymentDate());
        update.setBidDocument(request.getBidDocument());
        bidMapper.updateById(update);
    }

    @Override
    public void withdrawBid(Long id) {
        Bid bid = getOwnedBid(id);
        if (!SUBMITTED.equals(bid.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该投标已处理，无法撤回");
        }
        // 逻辑删除（@TableLogic 转 UPDATE deleted=1）
        bidMapper.deleteById(bid.getId());
    }

    @Override
    public void acceptBid(Long id) {
        decideBid(id, ACCEPTED);
    }

    @Override
    public void rejectBid(Long id) {
        decideBid(id, REJECTED);
    }

    /** 处理投标（接受/拒绝）：招标单位只能处理本单位项目的投标 */
    private void decideBid(Long id, String status) {
        Bid bid = bidMapper.selectById(id);
        if (bid == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "投标不存在");
        }
        if (!SUBMITTED.equals(bid.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该投标已处理");
        }

        TenderProject project = tenderProjectMapper.selectById(bid.getTenderProjectId());
        if (project == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "招标项目不存在");
        }
        // 越权防护：招标单位只能处理本单位项目的投标（admin 可处理任意）
        if ("tender".equals(SecurityUtil.getCurrentRole())
                && !getCurrentTenderUnitId().equals(project.getTenderUnitId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能处理本单位招标项目的投标");
        }

        Bid update = new Bid();
        update.setId(bid.getId());
        update.setStatus(status);
        bidMapper.updateById(update);
    }

    /** 校验投标属于当前厂家，并返回 */
    private Bid getOwnedBid(Long id) {
        Bid bid = bidMapper.selectById(id);
        if (bid == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "投标不存在");
        }
        if (!getCurrentManufacturerId().equals(bid.getManufacturerId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能操作自己的投标");
        }
        return bid;
    }

    private Long getCurrentManufacturerId() {
        Long userId = SecurityUtil.getCurrentUserId();
        Manufacturer m = manufacturerMapper.selectOne(
                new LambdaQueryWrapper<Manufacturer>().eq(Manufacturer::getUserId, userId));
        if (m == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "厂家信息不存在");
        }
        return m.getId();
    }

    private Long getCurrentTenderUnitId() {
        Long userId = SecurityUtil.getCurrentUserId();
        TenderUnit t = tenderUnitMapper.selectOne(
                new LambdaQueryWrapper<TenderUnit>().eq(TenderUnit::getUserId, userId));
        if (t == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "招标单位信息不存在");
        }
        return t.getId();
    }
}

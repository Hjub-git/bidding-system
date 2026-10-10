package com.bidding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bidding.common.BusinessException;
import com.bidding.common.ResultCode;
import com.bidding.dto.LoginRequest;
import com.bidding.dto.RegisterRequest;
import com.bidding.entity.Manufacturer;
import com.bidding.entity.TenderUnit;
import com.bidding.entity.User;
import com.bidding.mapper.ManufacturerMapper;
import com.bidding.mapper.TenderUnitMapper;
import com.bidding.mapper.UserMapper;
import com.bidding.security.JwtUtil;
import com.bidding.service.AuthService;
import com.bidding.vo.LoginVO;
import com.bidding.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final ManufacturerMapper manufacturerMapper;
    private final TenderUnitMapper tenderUnitMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * 注册：创建用户 + 按角色创建厂家/招标单位档案。
     * @Transactional 保证两步要么都成功要么都回滚（面试高频：事务的原子性）。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO register(RegisterRequest request) {
        // 唯一性校验（数据库唯一约束兜底并发场景）
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername())
                .or()
                .eq(User::getEmail, request.getEmail()));
        if (count != null && count > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户名或邮箱已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        // 密码必须加密存储，绝不存明文
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        userMapper.insert(user);

        if ("manufacturer".equals(request.getRole())) {
            Manufacturer m = new Manufacturer();
            m.setUserId(user.getId());
            m.setCompanyName(request.getCompanyName());
            m.setLegalPerson(request.getLegalPerson());
            m.setContactPhone(request.getContactPhone());
            m.setAddress(request.getAddress());
            m.setBusinessLicense(request.getBusinessLicense());
            m.setCompanyProfile(request.getCompanyProfile());
            m.setStatus("pending");
            manufacturerMapper.insert(m);
        } else if ("tender".equals(request.getRole())) {
            TenderUnit t = new TenderUnit();
            t.setUserId(user.getId());
            t.setCompanyName(request.getCompanyName());
            t.setLegalPerson(request.getLegalPerson());
            t.setContactPhone(request.getContactPhone());
            t.setAddress(request.getAddress());
            t.setBusinessLicense(request.getBusinessLicense());
            t.setCompanyProfile(request.getCompanyProfile());
            t.setStatus("pending");
            tenderUnitMapper.insert(t);
        }

        return buildLoginVO(user);
    }

    @Override
    public LoginVO login(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));
        // 统一提示“用户名或密码错误”，不暴露到底哪个错（防用户名枚举）
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }
        return buildLoginVO(user);
    }

    @Override
    public UserInfoVO getMe(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setRole(user.getRole());
        return vo;
    }

    @Override
    public void updateMe(Long userId, String email) {
        User user = new User();
        user.setId(userId);
        user.setEmail(email);
        userMapper.updateById(user);
    }

    private LoginVO buildLoginVO(User user) {
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        return new LoginVO(user.getId(), user.getUsername(), user.getEmail(), user.getRole(), token);
    }
}

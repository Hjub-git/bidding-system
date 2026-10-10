package com.bidding.controller;

import com.bidding.common.Result;
import com.bidding.dto.UpdateUserInfoRequest;
import com.bidding.security.SecurityUtil;
import com.bidding.service.AuthService;
import com.bidding.vo.UserInfoVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 当前登录用户信息接口（任何已登录角色都可调用）。
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;

    @GetMapping("/info")
    public Result<UserInfoVO> getInfo() {
        return Result.ok(authService.getMe(SecurityUtil.getCurrentUserId()));
    }

    @PutMapping("/info")
    public Result<Void> updateInfo(@Valid @RequestBody UpdateUserInfoRequest request) {
        authService.updateMe(SecurityUtil.getCurrentUserId(), request.getEmail());
        return Result.ok();
    }
}

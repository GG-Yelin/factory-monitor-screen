package com.factory.monitor.controller;

import com.factory.monitor.dto.ApiResponse;
import com.factory.monitor.dto.LoginRequest;
import com.factory.monitor.dto.LoginResponse;
import com.factory.monitor.security.UserPrincipal;
import com.factory.monitor.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ApiResponse.success("登录成功", response);
        } catch (Exception e) {
            log.error("登录失败", e);
            return ApiResponse.error(401, e.getMessage());
        }
    }

    /**
     * 刷新Token
     */
    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refreshToken(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            if (refreshToken == null || refreshToken.isEmpty()) {
                return ApiResponse.badRequest("refreshToken不能为空");
            }
            LoginResponse response = authService.refreshToken(refreshToken);
            return ApiResponse.success("刷新成功", response);
        } catch (Exception e) {
            log.error("刷新Token失败", e);
            return ApiResponse.error(401, e.getMessage());
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ApiResponse<LoginResponse.UserInfo> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            return ApiResponse.unauthorized("未登录");
        }
        // 这里可以扩展返回更详细的用户信息
        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .id(userPrincipal.getUserId())
                .username(userPrincipal.getUsername())
                .role(userPrincipal.getRole())
                .build();
        return ApiResponse.success(userInfo);
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                            @RequestBody Map<String, String> request) {
        if (userPrincipal == null) {
            return ApiResponse.unauthorized("未登录");
        }
        try {
            String oldPassword = request.get("oldPassword");
            String newPassword = request.get("newPassword");

            if (oldPassword == null || newPassword == null) {
                return ApiResponse.badRequest("参数不完整");
            }

            if (newPassword.length() < 6) {
                return ApiResponse.badRequest("新密码长度不能少于6位");
            }

            authService.changePassword(userPrincipal.getUserId(), oldPassword, newPassword);
            return ApiResponse.success("修改成功", null);
        } catch (Exception e) {
            log.error("修改密码失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 退出登录（客户端清除Token即可，此接口仅用于记录日志）
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal != null) {
            log.info("用户 {} 退出登录", userPrincipal.getUsername());
        }
        return ApiResponse.success("退出成功", null);
    }
}

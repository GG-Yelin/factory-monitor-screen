package com.factory.monitor.controller;

import com.factory.monitor.dto.ApiResponse;
import com.factory.monitor.entity.User;
import com.factory.monitor.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<Page<User>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> result = userService.findByCondition(keyword, role, teamId, status, pageable);
        // 清除密码
        result.getContent().forEach(u -> u.setPassword(null));
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<User> getById(@PathVariable Long id) {
        User user = userService.findById(id);
        if (user == null) {
            return ApiResponse.notFound("用户不存在");
        }
        user.setPassword(null);
        return ApiResponse.success(user);
    }

    @GetMapping("/team/{teamId}")
    public ApiResponse<List<User>> getByTeamId(@PathVariable Long teamId) {
        List<User> users = userService.findByTeamId(teamId);
        users.forEach(u -> u.setPassword(null));
        return ApiResponse.success(users);
    }

    @GetMapping("/role/{role}")
    public ApiResponse<List<User>> getByRole(@PathVariable String role) {
        List<User> users = userService.findByRole(role);
        users.forEach(u -> u.setPassword(null));
        return ApiResponse.success(users);
    }

    @PostMapping
    public ApiResponse<User> create(@RequestBody User user) {
        try {
            User created = userService.create(user);
            created.setPassword(null);
            return ApiResponse.success("创建成功", created);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ApiResponse<User> update(@PathVariable Long id, @RequestBody User user) {
        try {
            user.setId(id);
            User updated = userService.update(user);
            updated.setPassword(null);
            return ApiResponse.success("更新成功", updated);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        try {
            userService.delete(id);
            return ApiResponse.success("删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> request) {
        try {
            userService.updateStatus(id, request.get("status"));
            return ApiResponse.success("状态更新成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/reset-password")
    public ApiResponse<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String newPassword = request.getOrDefault("password", "123456");
            userService.resetPassword(id, newPassword);
            return ApiResponse.success("密码重置成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}

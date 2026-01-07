package com.factory.monitor.service;

import com.factory.monitor.dto.LoginRequest;
import com.factory.monitor.dto.LoginResponse;
import com.factory.monitor.entity.Team;
import com.factory.monitor.entity.User;
import com.factory.monitor.repository.TeamRepository;
import com.factory.monitor.repository.UserRepository;
import com.factory.monitor.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务
 */
@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    private static final Map<String, String> ROLE_NAME_MAP = new HashMap<>();

    static {
        ROLE_NAME_MAP.put("ADMIN", "系统管理员");
        ROLE_NAME_MAP.put("MANAGER", "厂长/主管");
        ROLE_NAME_MAP.put("LEADER", "班组长");
        ROLE_NAME_MAP.put("OPERATOR", "操作工");
        ROLE_NAME_MAP.put("INSPECTOR", "质检员");
    }

    public AuthService(UserRepository userRepository, TeamRepository teamRepository,
                       PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * 用户登录
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // 根据用户名/工号/手机号查找用户
        User user = userRepository.findByAccount(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 检查用户状态
        if (user.getStatus() != 1) {
            throw new RuntimeException("账号已被禁用");
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);

        // 生成Token
        String accessToken = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername());

        // 获取班组名称
        String teamName = null;
        if (user.getTeamId() != null) {
            teamName = teamRepository.findById(user.getTeamId())
                    .map(Team::getName)
                    .orElse(null);
        }

        // 构建响应
        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .employeeNo(user.getEmployeeNo())
                .realName(user.getRealName())
                .phone(user.getPhone())
                .role(user.getRole())
                .roleName(ROLE_NAME_MAP.getOrDefault(user.getRole(), user.getRole()))
                .teamId(user.getTeamId())
                .teamName(teamName)
                .avatar(user.getAvatar())
                .build();

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(86400L)  // 24小时
                .userInfo(userInfo)
                .build();
    }

    /**
     * 刷新Token
     */
    public LoginResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("无效的刷新令牌");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (user.getStatus() != 1) {
            throw new RuntimeException("账号已被禁用");
        }

        // 生成新Token
        String newAccessToken = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername());

        // 获取班组名称
        String teamName = null;
        if (user.getTeamId() != null) {
            teamName = teamRepository.findById(user.getTeamId())
                    .map(Team::getName)
                    .orElse(null);
        }

        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .employeeNo(user.getEmployeeNo())
                .realName(user.getRealName())
                .phone(user.getPhone())
                .role(user.getRole())
                .roleName(ROLE_NAME_MAP.getOrDefault(user.getRole(), user.getRole()))
                .teamId(user.getTeamId())
                .teamName(teamName)
                .avatar(user.getAvatar())
                .build();

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(86400L)
                .userInfo(userInfo)
                .build();
    }

    /**
     * 修改密码
     */
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * 初始化管理员账号（如果不存在）
     */
    @Transactional
    public void initAdminUser() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .realName("系统管理员")
                    .password(passwordEncoder.encode("admin123"))
                    .role("ADMIN")
                    .status(1)
                    .build();
            userRepository.save(admin);
            log.info("初始化管理员账号成功: admin/admin123");
        }
    }
}

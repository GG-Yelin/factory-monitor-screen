package com.factory.monitor.service;

import com.factory.monitor.entity.Team;
import com.factory.monitor.entity.User;
import com.factory.monitor.repository.TeamRepository;
import com.factory.monitor.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, TeamRepository teamRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public Page<User> findByCondition(String keyword, String role, Long teamId, Integer status, Pageable pageable) {
        return userRepository.findByCondition(keyword, role, teamId, status, pageable);
    }

    public List<User> findByTeamId(Long teamId) {
        return userRepository.findByTeamId(teamId);
    }

    public List<User> findByRole(String role) {
        return userRepository.findByRole(role);
    }

    @Transactional
    public User create(User user) {
        // 验证用户名唯一性
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        if (user.getEmployeeNo() != null && userRepository.existsByEmployeeNo(user.getEmployeeNo())) {
            throw new RuntimeException("工号已存在");
        }
        if (user.getPhone() != null && userRepository.existsByPhone(user.getPhone())) {
            throw new RuntimeException("手机号已存在");
        }

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    @Transactional
    public User update(User user) {
        User existing = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 更新基本信息
        existing.setRealName(user.getRealName());
        existing.setPhone(user.getPhone());
        existing.setRole(user.getRole());
        existing.setTeamId(user.getTeamId());
        existing.setStatus(user.getStatus());
        existing.setAvatar(user.getAvatar());

        // 如果提供了新密码则更新密码
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existing.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        return userRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public void updateStatus(Long id, Integer status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setStatus(status);
        userRepository.save(user);
    }

    @Transactional
    public void resetPassword(Long id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}

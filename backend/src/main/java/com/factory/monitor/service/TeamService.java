package com.factory.monitor.service;

import com.factory.monitor.entity.Team;
import com.factory.monitor.entity.User;
import com.factory.monitor.repository.TeamRepository;
import com.factory.monitor.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    public TeamService(TeamRepository teamRepository, UserRepository userRepository) {
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
    }

    public Team findById(Long id) {
        return teamRepository.findById(id).orElse(null);
    }

    public List<Team> findAll() {
        return teamRepository.findAll();
    }

    public List<Team> findAllActive() {
        return teamRepository.findByStatus(1);
    }

    @Transactional
    public Team create(Team team) {
        if (team.getCode() != null && teamRepository.existsByCode(team.getCode())) {
            throw new RuntimeException("班组编码已存在");
        }
        return teamRepository.save(team);
    }

    @Transactional
    public Team update(Team team) {
        Team existing = teamRepository.findById(team.getId())
                .orElseThrow(() -> new RuntimeException("班组不存在"));

        existing.setName(team.getName());
        existing.setDescription(team.getDescription());
        existing.setLeaderId(team.getLeaderId());
        existing.setStatus(team.getStatus());

        // 更新班组长姓名
        if (team.getLeaderId() != null) {
            User leader = userRepository.findById(team.getLeaderId()).orElse(null);
            if (leader != null) {
                existing.setLeaderName(leader.getRealName());
            }
        } else {
            existing.setLeaderName(null);
        }

        return teamRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        // 清除该班组下员工的班组关联
        List<User> users = userRepository.findByTeamId(id);
        for (User user : users) {
            user.setTeamId(null);
            userRepository.save(user);
        }
        teamRepository.deleteById(id);
    }

    public List<User> findTeamMembers(Long teamId) {
        return userRepository.findByTeamId(teamId);
    }
}

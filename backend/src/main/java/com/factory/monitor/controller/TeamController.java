package com.factory.monitor.controller;

import com.factory.monitor.dto.ApiResponse;
import com.factory.monitor.entity.Team;
import com.factory.monitor.entity.User;
import com.factory.monitor.service.TeamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping
    public ApiResponse<List<Team>> list() {
        return ApiResponse.success(teamService.findAll());
    }

    @GetMapping("/active")
    public ApiResponse<List<Team>> listActive() {
        return ApiResponse.success(teamService.findAllActive());
    }

    @GetMapping("/{id}")
    public ApiResponse<Team> getById(@PathVariable Long id) {
        Team team = teamService.findById(id);
        if (team == null) {
            return ApiResponse.notFound("班组不存在");
        }
        return ApiResponse.success(team);
    }

    @GetMapping("/{id}/members")
    public ApiResponse<List<User>> getMembers(@PathVariable Long id) {
        List<User> members = teamService.findTeamMembers(id);
        members.forEach(u -> u.setPassword(null));
        return ApiResponse.success(members);
    }

    @PostMapping
    public ApiResponse<Team> create(@RequestBody Team team) {
        try {
            Team created = teamService.create(team);
            return ApiResponse.success("创建成功", created);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ApiResponse<Team> update(@PathVariable Long id, @RequestBody Team team) {
        try {
            team.setId(id);
            Team updated = teamService.update(team);
            return ApiResponse.success("更新成功", updated);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        try {
            teamService.delete(id);
            return ApiResponse.success("删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}

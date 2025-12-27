package com.factory.monitor.controller;

import com.factory.monitor.model.DashboardData;
import com.factory.monitor.model.DataPoint;
import com.factory.monitor.model.DeviceInfo;
import com.factory.monitor.model.ProjectInfo;
import com.factory.monitor.service.DashboardService;
import com.factory.monitor.service.XinjeCloudService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
public class DashboardController {

    private final DashboardService dashboardService;
    private final XinjeCloudService xinjeCloudService;

    public DashboardController(DashboardService dashboardService, XinjeCloudService xinjeCloudService) {
        this.dashboardService = dashboardService;
        this.xinjeCloudService = xinjeCloudService;
    }

    /**
     * 获取大屏数据
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardData> getDashboardData() {
        DashboardData data = dashboardService.getDashboardData();
        return ResponseEntity.ok(data);
    }

    /**
     * 获取项目列表
     */
    @GetMapping("/projects")
    public ResponseEntity<List<ProjectInfo>> getProjects() {
        List<ProjectInfo> projects = xinjeCloudService.getProjectList();
        return ResponseEntity.ok(projects);
    }

    /**
     * 获取设备列表（按项目）
     */
    @GetMapping("/devices")
    public ResponseEntity<List<DeviceInfo>> getDevices(@RequestParam String itemId) {
        List<DeviceInfo> devices = xinjeCloudService.getDeviceList(itemId);
        return ResponseEntity.ok(devices);
    }

    /**
     * 获取所有设备列表（包含在线状态）
     */
    @GetMapping("/devices/all")
    public ResponseEntity<List<DeviceInfo>> getAllDevices(
            @RequestParam(defaultValue = "100") int pageSize,
            @RequestParam(defaultValue = "1") int currentPage) {
        List<DeviceInfo> devices = xinjeCloudService.getAllDevicesWithStatus(pageSize, currentPage);
        return ResponseEntity.ok(devices);
    }

    /**
     * 获取项目数据
     */
    @GetMapping("/data")
    public ResponseEntity<List<DataPoint>> getData(@RequestParam String itemId) {
        List<DataPoint> dataPoints = xinjeCloudService.getItemData(itemId);
        return ResponseEntity.ok(dataPoints);
    }

    /**
     * 设置数据点值
     */
    @PostMapping("/data/set")
    public ResponseEntity<Map<String, Object>> setDataValue(
            @RequestParam String itemId,
            @RequestParam String dataPointId,
            @RequestParam String value) {

        boolean success = xinjeCloudService.setDataValue(itemId, dataPointId, value);

        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "设置成功" : "设置失败");

        return ResponseEntity.ok(result);
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(result);
    }
}

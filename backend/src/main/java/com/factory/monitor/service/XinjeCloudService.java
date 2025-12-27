package com.factory.monitor.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.factory.monitor.config.XinjeConfig;
import com.factory.monitor.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class XinjeCloudService {

    private final XinjeConfig xinjeConfig;
    private final HttpClient httpClient;

    private String token;
    private long tokenExpireTime;

    // 缓存数据
    private final ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<>();

    public XinjeCloudService(XinjeConfig xinjeConfig) {
        this.xinjeConfig = xinjeConfig;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * 登录获取Token
     */
    public String login() {
        try {
            String encodedUsername = URLEncoder.encode(xinjeConfig.getUsername(), StandardCharsets.UTF_8);
            String encodedPassword = URLEncoder.encode(xinjeConfig.getPassword(), StandardCharsets.UTF_8);
            String url = String.format("%s/api/login?username=%s&password=%s&mode=pwd&isEncrypt=false",
                    xinjeConfig.getBaseUrl(),
                    encodedUsername,
                    encodedPassword);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject json = JSON.parseObject(response.body());
                if (json.getInteger("code") == 200) {
                    this.token = json.getString("token");
                    this.tokenExpireTime = System.currentTimeMillis() + 6 * 24 * 60 * 60 * 1000; // 6天有效期
                    log.info("Login successful, token obtained");
                    return this.token;
                }
            }
            log.error("Login failed: {}", response.body());
            return null;
        } catch (Exception e) {
            log.error("Login error", e);
            return null;
        }
    }

    /**
     * 获取有效Token
     */
    public String getValidToken() {
        if (token == null || System.currentTimeMillis() > tokenExpireTime) {
            login();
        }
        return token;
    }

    /**
     * 获取项目列表
     */
    public List<ProjectInfo> getProjectList() {
        try {
            String validToken = getValidToken();
            if (validToken == null) {
                return new ArrayList<>();
            }

            String url = xinjeConfig.getBaseUrl() + "/api/v1/Item/list";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + validToken)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            List<ProjectInfo> projects = new ArrayList<>();
            if (response.statusCode() == 200) {
                JSONObject json = JSON.parseObject(response.body());
                if (json.getInteger("code") == 200) {
                    JSONObject data = json.getJSONObject("data");
                    JSONArray list = data.getJSONArray("list");
                    if (list != null) {
                        for (int i = 0; i < list.size(); i++) {
                            JSONObject item = list.getJSONObject(i);
                            ProjectInfo project = ProjectInfo.builder()
                                    .itemId(item.getString("itemId"))
                                    .itemName(item.getString("itemName"))
                                    .lnglat(item.getString("lnglat"))
                                    .parentGroupId(item.getString("parentGroupId"))
                                    .build();
                            projects.add(project);
                        }
                    }
                }
            }
            cache.put("projects", projects);
            return projects;
        } catch (Exception e) {
            log.error("Get project list error", e);
            return (List<ProjectInfo>) cache.getOrDefault("projects", new ArrayList<>());
        }
    }

    /**
     * 获取设备列表
     */
    public List<DeviceInfo> getDeviceList(String itemId) {
        try {
            String validToken = getValidToken();
            if (validToken == null) {
                return new ArrayList<>();
            }

            String url = xinjeConfig.getBaseUrl() + "/api/v1/deviceconfig/deviceList?itemId=" + itemId;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + validToken)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            List<DeviceInfo> devices = new ArrayList<>();
            if (response.statusCode() == 200) {
                JSONObject json = JSON.parseObject(response.body());
                if (json.getInteger("code") == 200) {
                    JSONArray data = json.getJSONArray("data");
                    if (data != null) {
                        for (int i = 0; i < data.size(); i++) {
                            JSONObject item = data.getJSONObject(i);
                            DeviceInfo device = DeviceInfo.builder()
                                    .deviceId(item.getString("deviceId"))
                                    .deviceName(item.getString("deviceName"))
                                    .deviceType(item.getString("deviceType"))
                                    .itemId(itemId)
                                    .status(1) // 默认在线
                                    .build();
                            devices.add(device);
                        }
                    }
                }
            }
            return devices;
        } catch (Exception e) {
            log.error("Get device list error", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取项目加工数据
     */
    public List<DataPoint> getItemData(String itemId) {
        try {
            String validToken = getValidToken();
            if (validToken == null) {
                return new ArrayList<>();
            }

            String url = xinjeConfig.getBaseUrl() + "/api/v1/ItemData/GetItemData?itemId=" + itemId + "&viewId=0";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + validToken)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            List<DataPoint> dataPoints = new ArrayList<>();
            if (response.statusCode() == 200) {
                JSONObject json = JSON.parseObject(response.body());
                if (json.getInteger("code") == 200) {
                    JSONArray data = json.getJSONArray("data");
                    if (data != null) {
                        for (int i = 0; i < data.size(); i++) {
                            JSONObject deviceData = data.getJSONObject(i);
                            String deviceId = deviceData.getString("deviceId");
                            String deviceName = deviceData.getString("deviceName");
                            JSONArray points = deviceData.getJSONArray("data");
                            if (points != null) {
                                for (int j = 0; j < points.size(); j++) {
                                    JSONObject point = points.getJSONObject(j);
                                    DataPoint dataPoint = DataPoint.builder()
                                            .id(point.getString("id"))
                                            .name(point.getString("name"))
                                            .dataType(point.getInteger("data_type"))
                                            .set(point.getBoolean("set"))
                                            .isCoil(point.getBoolean("isCoil"))
                                            .unit(point.getString("unit"))
                                            .value(point.getString("value"))
                                            .valueString(point.getString("valueString"))
                                            .deviceId(deviceId)
                                            .deviceName(deviceName)
                                            .build();
                                    dataPoints.add(dataPoint);
                                }
                            }
                        }
                    }
                }
            }
            return dataPoints;
        } catch (Exception e) {
            log.error("Get item data error", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取基础数据
     */
    public List<DataPoint> getBaseData(String itemId) {
        try {
            String validToken = getValidToken();
            if (validToken == null) {
                return new ArrayList<>();
            }

            String url = xinjeConfig.getBaseUrl() + "/api/v1/itemdata/getitembasedata?itemid=" + itemId;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + validToken)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            List<DataPoint> dataPoints = new ArrayList<>();
            if (response.statusCode() == 200) {
                JSONObject json = JSON.parseObject(response.body());
                if (json.getInteger("code") == 200) {
                    JSONArray data = json.getJSONArray("data");
                    if (data != null) {
                        for (int i = 0; i < data.size(); i++) {
                            JSONObject deviceData = data.getJSONObject(i);
                            String deviceId = deviceData.getString("deviceId");
                            String deviceName = deviceData.getString("deviceName");
                            JSONArray points = deviceData.getJSONArray("data");
                            if (points != null) {
                                for (int j = 0; j < points.size(); j++) {
                                    JSONObject point = points.getJSONObject(j);
                                    DataPoint dataPoint = DataPoint.builder()
                                            .id(point.getString("id"))
                                            .name(point.getString("name"))
                                            .dataType(point.getInteger("data_type"))
                                            .set(point.getBoolean("set"))
                                            .isCoil(point.getBoolean("isCoil"))
                                            .unit(point.getString("unit"))
                                            .value(point.getString("value"))
                                            .valueString(point.getString("valueString"))
                                            .deviceId(deviceId)
                                            .deviceName(deviceName)
                                            .build();
                                    dataPoints.add(dataPoint);
                                }
                            }
                        }
                    }
                }
            }
            return dataPoints;
        } catch (Exception e) {
            log.error("Get base data error", e);
            return new ArrayList<>();
        }
    }

    /**
     * 设置数据点值
     */
    public boolean setDataValue(String itemId, String dataPointId, String value) {
        try {
            String validToken = getValidToken();
            if (validToken == null) {
                return false;
            }

            String url = xinjeConfig.getBaseUrl() + "/api/v1/ItemData/SetValue";

            JSONObject body = new JSONObject();
            body.put("itemId", itemId);
            body.put("viewId", "0");
            body.put("id", dataPointId);
            body.put("value", value);
            body.put("bitMark", -1);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + validToken)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(body.toJSONString()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject json = JSON.parseObject(response.body());
                return json.getInteger("code") == 200;
            }
            return false;
        } catch (Exception e) {
            log.error("Set data value error", e);
            return false;
        }
    }
}

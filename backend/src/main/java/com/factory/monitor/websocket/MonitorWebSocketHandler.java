package com.factory.monitor.websocket;

import com.alibaba.fastjson2.JSON;
import com.factory.monitor.model.DashboardData;
import com.factory.monitor.service.DashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Component
public class MonitorWebSocketHandler extends TextWebSocketHandler {

    private final DashboardService dashboardService;
    private static final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    public MonitorWebSocketHandler(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        log.info("WebSocket connected: {}, total sessions: {}", session.getId(), sessions.size());

        // 连接后立即发送当前数据
        sendDashboardData(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.debug("Received message: {}", payload);

        // 处理客户端请求
        if ("refresh".equals(payload)) {
            sendDashboardData(session);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        log.info("WebSocket disconnected: {}, total sessions: {}", session.getId(), sessions.size());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket error: {}", session.getId(), exception);
        sessions.remove(session);
    }

    /**
     * 发送大屏数据到指定会话
     */
    private void sendDashboardData(WebSocketSession session) {
        try {
            DashboardData data = dashboardService.getDashboardData();
            String json = JSON.toJSONString(data);
            session.sendMessage(new TextMessage(json));
        } catch (IOException e) {
            log.error("Send dashboard data error", e);
        }
    }

    /**
     * 广播大屏数据到所有连接
     */
    public void broadcastDashboardData() {
        if (sessions.isEmpty()) {
            return;
        }

        try {
            DashboardData data = dashboardService.getDashboardData();
            String json = JSON.toJSONString(data);
            TextMessage message = new TextMessage(json);

            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(message);
                    } catch (IOException e) {
                        log.error("Broadcast to session {} error", session.getId(), e);
                    }
                }
            }
            log.debug("Broadcast dashboard data to {} sessions", sessions.size());
        } catch (Exception e) {
            log.error("Broadcast dashboard data error", e);
        }
    }

    /**
     * 获取当前连接数
     */
    public int getSessionCount() {
        return sessions.size();
    }
}

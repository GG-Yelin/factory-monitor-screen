package com.factory.monitor.repository;

import com.factory.monitor.entity.DeviceStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceStatusLogRepository extends JpaRepository<DeviceStatusLog, Long> {

    /** 查询设备最近的状态记录 */
    Optional<DeviceStatusLog> findTopByDeviceIdOrderByLogTimeDesc(String deviceId);

    /** 查询设备在时间范围内的状态日志 */
    List<DeviceStatusLog> findByDeviceIdAndLogTimeBetweenOrderByLogTimeAsc(
            String deviceId, LocalDateTime startTime, LocalDateTime endTime);

    /** 统计某天设备在线的记录数 */
    @Query("SELECT COUNT(d) FROM DeviceStatusLog d WHERE d.deviceId = :deviceId AND d.status = 1 AND DATE(d.logTime) = :date")
    Integer countOnlineLogsByDate(@Param("deviceId") String deviceId, @Param("date") java.time.LocalDate date);
}

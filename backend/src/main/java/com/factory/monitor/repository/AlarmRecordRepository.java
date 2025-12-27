package com.factory.monitor.repository;

import com.factory.monitor.entity.AlarmRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlarmRecordRepository extends JpaRepository<AlarmRecord, Long> {

    /** 查询未处理的报警 */
    List<AlarmRecord> findByStatusOrderByAlarmTimeDesc(Integer status);

    /** 查询设备的报警记录 */
    List<AlarmRecord> findByDeviceIdOrderByAlarmTimeDesc(String deviceId);

    /** 查询时间范围内的报警 */
    List<AlarmRecord> findByAlarmTimeBetweenOrderByAlarmTimeDesc(LocalDateTime startTime, LocalDateTime endTime);

    /** 统计时间范围内的报警数量 */
    @Query("SELECT COUNT(a) FROM AlarmRecord a WHERE a.alarmTime BETWEEN :startTime AND :endTime")
    Integer countAlarmsBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /** 统计某日的报警数量 */
    @Query("SELECT COUNT(a) FROM AlarmRecord a WHERE DATE(a.alarmTime) = :date")
    Integer countAlarmsByDate(@Param("date") java.time.LocalDate date);

    /** 查询最近N条报警 */
    List<AlarmRecord> findTop10ByOrderByAlarmTimeDesc();
}

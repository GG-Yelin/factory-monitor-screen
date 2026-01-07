package com.factory.monitor.repository;

import com.factory.monitor.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmployeeNo(String employeeNo);

    Optional<User> findByPhone(String phone);

    @Query("SELECT u FROM User u WHERE u.username = :account OR u.employeeNo = :account OR u.phone = :account")
    Optional<User> findByAccount(@Param("account") String account);

    boolean existsByUsername(String username);

    boolean existsByEmployeeNo(String employeeNo);

    boolean existsByPhone(String phone);

    List<User> findByTeamId(Long teamId);

    List<User> findByRole(String role);

    List<User> findByStatus(Integer status);

    @Query("SELECT u FROM User u WHERE " +
           "(:keyword IS NULL OR u.realName LIKE %:keyword% OR u.employeeNo LIKE %:keyword% OR u.phone LIKE %:keyword%) " +
           "AND (:role IS NULL OR u.role = :role) " +
           "AND (:teamId IS NULL OR u.teamId = :teamId) " +
           "AND (:status IS NULL OR u.status = :status)")
    Page<User> findByCondition(@Param("keyword") String keyword,
                               @Param("role") String role,
                               @Param("teamId") Long teamId,
                               @Param("status") Integer status,
                               Pageable pageable);
}

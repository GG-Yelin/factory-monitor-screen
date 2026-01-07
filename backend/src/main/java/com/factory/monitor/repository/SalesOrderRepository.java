package com.factory.monitor.repository;

import com.factory.monitor.entity.SalesOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    Optional<SalesOrder> findByOrderNo(String orderNo);

    boolean existsByOrderNo(String orderNo);

    List<SalesOrder> findByStatus(String status);

    List<SalesOrder> findByStatusIn(List<String> statuses);

    @Query("SELECT o FROM SalesOrder o WHERE " +
           "(:keyword IS NULL OR o.orderNo LIKE %:keyword% OR o.productName LIKE %:keyword% OR o.customerName LIKE %:keyword%) " +
           "AND (:status IS NULL OR o.status = :status) " +
           "AND (:startDate IS NULL OR o.deliveryDate >= :startDate) " +
           "AND (:endDate IS NULL OR o.deliveryDate <= :endDate) " +
           "ORDER BY o.priority DESC, o.deliveryDate ASC")
    Page<SalesOrder> findByCondition(@Param("keyword") String keyword,
                                     @Param("status") String status,
                                     @Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate,
                                     Pageable pageable);

    @Query("SELECT COUNT(o) FROM SalesOrder o WHERE o.status = :status")
    Long countByStatus(@Param("status") String status);

    @Query("SELECT o FROM SalesOrder o WHERE o.deliveryDate <= :date AND o.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<SalesOrder> findOverdueOrders(@Param("date") LocalDate date);
}

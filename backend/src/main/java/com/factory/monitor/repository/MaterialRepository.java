package com.factory.monitor.repository;

import com.factory.monitor.entity.Material;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {

    Optional<Material> findByCode(String code);

    boolean existsByCode(String code);

    List<Material> findByStatus(Integer status);

    @Query("SELECT m FROM Material m WHERE m.stockQuantity <= m.warningThreshold AND m.warningThreshold IS NOT NULL")
    List<Material> findLowStockMaterials();

    @Query("SELECT m FROM Material m WHERE " +
           "(:keyword IS NULL OR m.name LIKE %:keyword% OR m.code LIKE %:keyword%) " +
           "AND (:status IS NULL OR m.status = :status)")
    Page<Material> findByCondition(@Param("keyword") String keyword,
                                   @Param("status") Integer status,
                                   Pageable pageable);
}

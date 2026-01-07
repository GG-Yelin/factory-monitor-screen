package com.factory.monitor.repository;

import com.factory.monitor.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByCode(String code);

    boolean existsByCode(String code);

    List<Product> findByStatus(Integer status);

    @Query("SELECT p FROM Product p WHERE " +
           "(:keyword IS NULL OR p.name LIKE %:keyword% OR p.code LIKE %:keyword%) " +
           "AND (:status IS NULL OR p.status = :status)")
    Page<Product> findByCondition(@Param("keyword") String keyword,
                                  @Param("status") Integer status,
                                  Pageable pageable);
}

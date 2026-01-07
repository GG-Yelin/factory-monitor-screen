package com.factory.monitor.repository;

import com.factory.monitor.entity.MaterialBom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialBomRepository extends JpaRepository<MaterialBom, Long> {

    List<MaterialBom> findByProductId(Long productId);

    void deleteByProductId(Long productId);

    boolean existsByProductIdAndMaterialId(Long productId, Long materialId);
}

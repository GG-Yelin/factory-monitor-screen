package com.factory.monitor.repository;

import com.factory.monitor.entity.ProcessTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessTemplateRepository extends JpaRepository<ProcessTemplate, Long> {

    Optional<ProcessTemplate> findByCode(String code);

    boolean existsByCode(String code);

    List<ProcessTemplate> findByStatus(Integer status);
}

package com.factory.monitor.repository;

import com.factory.monitor.entity.ProcessStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessStepRepository extends JpaRepository<ProcessStep, Long> {

    List<ProcessStep> findByTemplateIdOrderBySortOrder(Long templateId);

    List<ProcessStep> findByTemplateIdAndStatus(Long templateId, Integer status);

    void deleteByTemplateId(Long templateId);
}

package com.factory.monitor.service;

import com.factory.monitor.entity.Product;
import com.factory.monitor.entity.ProcessStep;
import com.factory.monitor.entity.ProcessTemplate;
import com.factory.monitor.repository.ProcessStepRepository;
import com.factory.monitor.repository.ProcessTemplateRepository;
import com.factory.monitor.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProcessTemplateRepository processTemplateRepository;
    private final ProcessStepRepository processStepRepository;

    public ProductService(ProductRepository productRepository,
                          ProcessTemplateRepository processTemplateRepository,
                          ProcessStepRepository processStepRepository) {
        this.productRepository = productRepository;
        this.processTemplateRepository = processTemplateRepository;
        this.processStepRepository = processStepRepository;
    }

    // ========== 产品管理 ==========

    public Product findProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    public Page<Product> findProductsByCondition(String keyword, Integer status, Pageable pageable) {
        return productRepository.findByCondition(keyword, status, pageable);
    }

    public List<Product> findAllActiveProducts() {
        return productRepository.findByStatus(1);
    }

    @Transactional
    public Product createProduct(Product product) {
        if (productRepository.existsByCode(product.getCode())) {
            throw new RuntimeException("产品编码已存在");
        }
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Product product) {
        Product existing = productRepository.findById(product.getId())
                .orElseThrow(() -> new RuntimeException("产品不存在"));

        existing.setName(product.getName());
        existing.setSpecification(product.getSpecification());
        existing.setUnit(product.getUnit());
        existing.setTemplateId(product.getTemplateId());
        existing.setImageUrl(product.getImageUrl());
        existing.setDescription(product.getDescription());
        existing.setStatus(product.getStatus());

        return productRepository.save(existing);
    }

    @Transactional
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    // ========== 工序模板管理 ==========

    public ProcessTemplate findTemplateById(Long id) {
        return processTemplateRepository.findById(id).orElse(null);
    }

    public List<ProcessTemplate> findAllActiveTemplates() {
        return processTemplateRepository.findByStatus(1);
    }

    public List<ProcessTemplate> findAllTemplates() {
        return processTemplateRepository.findAll();
    }

    @Transactional
    public ProcessTemplate createTemplate(ProcessTemplate template) {
        if (template.getCode() != null && processTemplateRepository.existsByCode(template.getCode())) {
            throw new RuntimeException("模板编码已存在");
        }
        return processTemplateRepository.save(template);
    }

    @Transactional
    public ProcessTemplate updateTemplate(ProcessTemplate template) {
        ProcessTemplate existing = processTemplateRepository.findById(template.getId())
                .orElseThrow(() -> new RuntimeException("模板不存在"));

        existing.setName(template.getName());
        existing.setDescription(template.getDescription());
        existing.setStatus(template.getStatus());

        return processTemplateRepository.save(existing);
    }

    @Transactional
    public void deleteTemplate(Long id) {
        // 先删除关联的工序步骤
        processStepRepository.deleteByTemplateId(id);
        processTemplateRepository.deleteById(id);
    }

    // ========== 工序步骤管理 ==========

    public List<ProcessStep> findStepsByTemplateId(Long templateId) {
        return processStepRepository.findByTemplateIdOrderBySortOrder(templateId);
    }

    @Transactional
    public ProcessStep createStep(ProcessStep step) {
        return processStepRepository.save(step);
    }

    @Transactional
    public ProcessStep updateStep(ProcessStep step) {
        ProcessStep existing = processStepRepository.findById(step.getId())
                .orElseThrow(() -> new RuntimeException("工序步骤不存在"));

        existing.setName(step.getName());
        existing.setCode(step.getCode());
        existing.setSortOrder(step.getSortOrder());
        existing.setUnitPrice(step.getUnitPrice());
        existing.setStandardTime(step.getStandardTime());
        existing.setNeedInspection(step.getNeedInspection());
        existing.setInspectionStandard(step.getInspectionStandard());
        existing.setRequirement(step.getRequirement());
        existing.setDrawingUrl(step.getDrawingUrl());
        existing.setCustomField1Name(step.getCustomField1Name());
        existing.setCustomField1Type(step.getCustomField1Type());
        existing.setCustomField1Options(step.getCustomField1Options());
        existing.setCustomField2Name(step.getCustomField2Name());
        existing.setCustomField2Type(step.getCustomField2Type());
        existing.setCustomField2Options(step.getCustomField2Options());
        existing.setCustomField3Name(step.getCustomField3Name());
        existing.setCustomField3Type(step.getCustomField3Type());
        existing.setCustomField3Options(step.getCustomField3Options());
        existing.setStatus(step.getStatus());

        return processStepRepository.save(existing);
    }

    @Transactional
    public void deleteStep(Long id) {
        processStepRepository.deleteById(id);
    }

    @Transactional
    public void saveSteps(Long templateId, List<ProcessStep> steps) {
        // 删除现有步骤
        processStepRepository.deleteByTemplateId(templateId);

        // 保存新步骤
        for (int i = 0; i < steps.size(); i++) {
            ProcessStep step = steps.get(i);
            step.setTemplateId(templateId);
            step.setSortOrder(i + 1);
            processStepRepository.save(step);
        }
    }
}

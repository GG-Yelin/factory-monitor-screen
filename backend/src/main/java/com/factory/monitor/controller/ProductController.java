package com.factory.monitor.controller;

import com.factory.monitor.dto.ApiResponse;
import com.factory.monitor.entity.ProcessStep;
import com.factory.monitor.entity.ProcessTemplate;
import com.factory.monitor.entity.Product;
import com.factory.monitor.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // ========== 产品管理 ==========

    @GetMapping("/products")
    public ApiResponse<Page<Product>> listProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ApiResponse.success(productService.findProductsByCondition(keyword, status, pageable));
    }

    @GetMapping("/products/all")
    public ApiResponse<List<Product>> listAllActiveProducts() {
        return ApiResponse.success(productService.findAllActiveProducts());
    }

    @GetMapping("/products/{id}")
    public ApiResponse<Product> getProduct(@PathVariable Long id) {
        Product product = productService.findProductById(id);
        if (product == null) {
            return ApiResponse.notFound("产品不存在");
        }
        return ApiResponse.success(product);
    }

    @PostMapping("/products")
    public ApiResponse<Product> createProduct(@RequestBody Product product) {
        try {
            return ApiResponse.success("创建成功", productService.createProduct(product));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/products/{id}")
    public ApiResponse<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        try {
            product.setId(id);
            return ApiResponse.success("更新成功", productService.updateProduct(product));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/products/{id}")
    public ApiResponse<Void> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ApiResponse.success("删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    // ========== 工序模板管理 ==========

    @GetMapping("/process-templates")
    public ApiResponse<List<ProcessTemplate>> listTemplates() {
        return ApiResponse.success(productService.findAllTemplates());
    }

    @GetMapping("/process-templates/active")
    public ApiResponse<List<ProcessTemplate>> listActiveTemplates() {
        return ApiResponse.success(productService.findAllActiveTemplates());
    }

    @GetMapping("/process-templates/{id}")
    public ApiResponse<ProcessTemplate> getTemplate(@PathVariable Long id) {
        ProcessTemplate template = productService.findTemplateById(id);
        if (template == null) {
            return ApiResponse.notFound("模板不存在");
        }
        return ApiResponse.success(template);
    }

    @PostMapping("/process-templates")
    public ApiResponse<ProcessTemplate> createTemplate(@RequestBody ProcessTemplate template) {
        try {
            return ApiResponse.success("创建成功", productService.createTemplate(template));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/process-templates/{id}")
    public ApiResponse<ProcessTemplate> updateTemplate(@PathVariable Long id, @RequestBody ProcessTemplate template) {
        try {
            template.setId(id);
            return ApiResponse.success("更新成功", productService.updateTemplate(template));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/process-templates/{id}")
    public ApiResponse<Void> deleteTemplate(@PathVariable Long id) {
        try {
            productService.deleteTemplate(id);
            return ApiResponse.success("删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    // ========== 工序步骤管理 ==========

    @GetMapping("/process-templates/{templateId}/steps")
    public ApiResponse<List<ProcessStep>> listSteps(@PathVariable Long templateId) {
        return ApiResponse.success(productService.findStepsByTemplateId(templateId));
    }

    @PostMapping("/process-templates/{templateId}/steps")
    public ApiResponse<ProcessStep> createStep(@PathVariable Long templateId, @RequestBody ProcessStep step) {
        try {
            step.setTemplateId(templateId);
            return ApiResponse.success("创建成功", productService.createStep(step));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/process-steps/{id}")
    public ApiResponse<ProcessStep> updateStep(@PathVariable Long id, @RequestBody ProcessStep step) {
        try {
            step.setId(id);
            return ApiResponse.success("更新成功", productService.updateStep(step));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/process-steps/{id}")
    public ApiResponse<Void> deleteStep(@PathVariable Long id) {
        try {
            productService.deleteStep(id);
            return ApiResponse.success("删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/process-templates/{templateId}/steps/batch")
    public ApiResponse<Void> saveSteps(@PathVariable Long templateId, @RequestBody List<ProcessStep> steps) {
        try {
            productService.saveSteps(templateId, steps);
            return ApiResponse.success("保存成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}

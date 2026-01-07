package com.factory.monitor.controller;

import com.factory.monitor.dto.ApiResponse;
import com.factory.monitor.entity.SalesOrder;
import com.factory.monitor.entity.WorkOrder;
import com.factory.monitor.entity.WorkOrderProcess;
import com.factory.monitor.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // ========== 销售订单管理 ==========

    @GetMapping("/sales-orders")
    public ApiResponse<Page<SalesOrder>> listSalesOrders(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ApiResponse.success(orderService.findSalesOrders(keyword, status, startDate, endDate, pageable));
    }

    @GetMapping("/sales-orders/pending")
    public ApiResponse<List<SalesOrder>> listPendingOrders() {
        return ApiResponse.success(orderService.findPendingOrders());
    }

    @GetMapping("/sales-orders/{id}")
    public ApiResponse<SalesOrder> getSalesOrder(@PathVariable Long id) {
        SalesOrder order = orderService.findSalesOrderById(id);
        if (order == null) {
            return ApiResponse.notFound("订单不存在");
        }
        return ApiResponse.success(order);
    }

    @PostMapping("/sales-orders")
    public ApiResponse<SalesOrder> createSalesOrder(@RequestBody SalesOrder order) {
        try {
            return ApiResponse.success("创建成功", orderService.createSalesOrder(order));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/sales-orders/{id}")
    public ApiResponse<SalesOrder> updateSalesOrder(@PathVariable Long id, @RequestBody SalesOrder order) {
        try {
            order.setId(id);
            return ApiResponse.success("更新成功", orderService.updateSalesOrder(order));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/sales-orders/{id}/status")
    public ApiResponse<Void> updateSalesOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            orderService.updateSalesOrderStatus(id, request.get("status"));
            return ApiResponse.success("状态更新成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/sales-orders/{id}")
    public ApiResponse<Void> deleteSalesOrder(@PathVariable Long id) {
        try {
            orderService.deleteSalesOrder(id);
            return ApiResponse.success("删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    // ========== 派工单管理 ==========

    @GetMapping("/work-orders")
    public ApiResponse<Page<WorkOrder>> listWorkOrders(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ApiResponse.success(orderService.findWorkOrders(keyword, status, teamId, startDate, endDate, pageable));
    }

    @GetMapping("/work-orders/{id}")
    public ApiResponse<WorkOrder> getWorkOrder(@PathVariable Long id) {
        WorkOrder workOrder = orderService.findWorkOrderById(id);
        if (workOrder == null) {
            return ApiResponse.notFound("派工单不存在");
        }
        return ApiResponse.success(workOrder);
    }

    @GetMapping("/work-orders/no/{workOrderNo}")
    public ApiResponse<WorkOrder> getWorkOrderByNo(@PathVariable String workOrderNo) {
        WorkOrder workOrder = orderService.findWorkOrderByNo(workOrderNo);
        if (workOrder == null) {
            return ApiResponse.notFound("派工单不存在");
        }
        return ApiResponse.success(workOrder);
    }

    @GetMapping("/sales-orders/{salesOrderId}/work-orders")
    public ApiResponse<List<WorkOrder>> listWorkOrdersBySalesOrder(@PathVariable Long salesOrderId) {
        return ApiResponse.success(orderService.findWorkOrdersBySalesOrderId(salesOrderId));
    }

    @PostMapping("/work-orders")
    public ApiResponse<WorkOrder> createWorkOrder(@RequestBody Map<String, Object> request) {
        try {
            Long salesOrderId = Long.valueOf(request.get("salesOrderId").toString());
            Long teamId = request.get("teamId") != null ? Long.valueOf(request.get("teamId").toString()) : null;
            LocalDate planStartDate = request.get("planStartDate") != null
                    ? LocalDate.parse(request.get("planStartDate").toString()) : null;
            LocalDate planEndDate = request.get("planEndDate") != null
                    ? LocalDate.parse(request.get("planEndDate").toString()) : null;

            WorkOrder workOrder = orderService.createWorkOrder(salesOrderId, teamId, planStartDate, planEndDate);
            return ApiResponse.success("派工单创建成功", workOrder);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/work-orders/{id}/status")
    public ApiResponse<Void> updateWorkOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            orderService.updateWorkOrderStatus(id, request.get("status"));
            return ApiResponse.success("状态更新成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/work-orders/{id}")
    public ApiResponse<Void> deleteWorkOrder(@PathVariable Long id) {
        try {
            orderService.deleteWorkOrder(id);
            return ApiResponse.success("删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    // ========== 派工单工序管理 ==========

    @GetMapping("/work-orders/{workOrderId}/processes")
    public ApiResponse<List<WorkOrderProcess>> listProcesses(@PathVariable Long workOrderId) {
        return ApiResponse.success(orderService.findProcessesByWorkOrderId(workOrderId));
    }

    @GetMapping("/work-order-processes/{id}")
    public ApiResponse<WorkOrderProcess> getProcess(@PathVariable Long id) {
        WorkOrderProcess process = orderService.findProcessById(id);
        if (process == null) {
            return ApiResponse.notFound("工序不存在");
        }
        return ApiResponse.success(process);
    }

    @GetMapping("/work-order-processes/qr/{qrCode}")
    public ApiResponse<WorkOrderProcess> getProcessByQrCode(@PathVariable String qrCode) {
        WorkOrderProcess process = orderService.findProcessByQrCode(qrCode);
        if (process == null) {
            return ApiResponse.notFound("工序不存在");
        }
        return ApiResponse.success(process);
    }

    @PutMapping("/work-order-processes/{id}/assign")
    public ApiResponse<Void> assignOperator(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            Long operatorId = Long.valueOf(request.get("operatorId").toString());
            String operatorName = request.get("operatorName").toString();
            orderService.assignOperator(id, operatorId, operatorName);
            return ApiResponse.success("分配成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}

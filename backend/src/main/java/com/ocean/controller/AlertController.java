package com.ocean.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ocean.dto.R;
import com.ocean.entity.Alert;
import com.ocean.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 预警管理接口
 */
@Tag(name = "预警管理", description = "极端海况预警查询与配置")
@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @Operation(summary = "获取当前活跃预警")
    @GetMapping("/active")
    public R<List<Alert>> getActive() {
        return R.ok(alertService.getActiveAlerts());
    }

    @Operation(summary = "分页查询预警")
    @GetMapping
    public R<Page<Alert>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String level) {
        return R.ok(alertService.page(pageNum, pageSize, type, level));
    }

    @Operation(summary = "获取预警详情")
    @GetMapping("/{id}")
    public R<Alert> getById(@PathVariable Long id) {
        return R.ok(alertService.getById(id));
    }

    @Operation(summary = "发布预警（管理员）")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public R<Void> add(@RequestBody Alert alert) {
        alertService.add(alert);
        return R.ok("发布成功");
    }

    @Operation(summary = "解除预警（管理员）")
    @PutMapping("/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public R<Void> resolve(@PathVariable Long id) {
        alertService.resolve(id);
        return R.ok("已解除");
    }
}

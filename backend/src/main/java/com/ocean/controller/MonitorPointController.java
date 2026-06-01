package com.ocean.controller;

import com.ocean.dto.R;
import com.ocean.entity.MonitorPoint;
import com.ocean.service.MonitorPointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 监测点位接口
 */
@Tag(name = "监测点位", description = "污染监测点位管理")
@RestController
@RequestMapping("/api/monitor-points")
@RequiredArgsConstructor
public class MonitorPointController {

    private final MonitorPointService monitorPointService;

    @Operation(summary = "获取所有监测点位（地图标注）")
    @GetMapping
    public R<List<MonitorPoint>> list(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        return R.ok(monitorPointService.list(type, status));
    }

    @Operation(summary = "获取单个点位详情")
    @GetMapping("/{id}")
    public R<MonitorPoint> getById(@PathVariable Long id) {
        return R.ok(monitorPointService.getById(id));
    }

    @Operation(summary = "添加监测点位（管理员）")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public R<Void> add(@RequestBody MonitorPoint point) {
        monitorPointService.add(point);
        return R.ok("添加成功");
    }

    @Operation(summary = "修改监测点位（管理员）")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public R<Void> update(@PathVariable Long id, @RequestBody MonitorPoint point) {
        point.setId(id);
        monitorPointService.update(point);
        return R.ok("修改成功");
    }

    @Operation(summary = "删除监测点位（管理员）")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public R<Void> delete(@PathVariable Long id) {
        monitorPointService.delete(id);
        return R.ok("删除成功");
    }
}

package com.ocean.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ocean.entity.Alert;
import com.ocean.mapper.AlertMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 预警管理服务
 */
@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertMapper alertMapper;

    /**
     * 获取当前活跃预警（首页展示）
     */
    public List<Alert> getActiveAlerts() {
        return alertMapper.selectList(
                new LambdaQueryWrapper<Alert>()
                        .eq(Alert::getStatus, "ACTIVE")
                        .orderByDesc(Alert::getLevel)
                        .orderByDesc(Alert::getCreateTime)
        );
    }

    /**
     * 分页查询预警
     */
    public Page<Alert> page(int pageNum, int pageSize, String type, String level) {
        LambdaQueryWrapper<Alert> wrapper = new LambdaQueryWrapper<>();
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Alert::getType, type);
        }
        if (level != null && !level.isEmpty()) {
            wrapper.eq(Alert::getLevel, level);
        }
        wrapper.orderByDesc(Alert::getCreateTime);
        return alertMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public Alert getById(Long id) {
        return alertMapper.selectById(id);
    }

    public void add(Alert alert) {
        alertMapper.insert(alert);
    }

    public void update(Alert alert) {
        alertMapper.updateById(alert);
    }

    public void resolve(Long id) {
        Alert alert = new Alert();
        alert.setId(id);
        alert.setStatus("RESOLVED");
        alertMapper.updateById(alert);
    }
}

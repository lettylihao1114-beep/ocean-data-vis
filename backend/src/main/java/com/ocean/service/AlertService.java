package com.ocean.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ocean.dto.PageVO;
import com.ocean.entity.Alert;
import com.ocean.mapper.AlertMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 预警管理服务 — 活跃预警短 TTL 缓存，写操作即时清缓存
 */
@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertMapper alertMapper;

    /**
     * 活跃预警（首页滚动条，高频读，缓存 1 min）
     */
    @Cacheable(value = "alert:active", key = "'active'")
    public List<Alert> getActiveAlerts() {
        return alertMapper.selectList(
                new LambdaQueryWrapper<Alert>()
                        .eq(Alert::getStatus, "ACTIVE")
                        .orderByDesc(Alert::getLevel)
                        .orderByDesc(Alert::getCreateTime)
        );
    }

    /**
     * 分页查询预警（低频，不缓存）
     */
    public PageVO<Alert> page(int pageNum, int pageSize, String type, String level) {
        LambdaQueryWrapper<Alert> wrapper = new LambdaQueryWrapper<>();
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Alert::getType, type);
        }
        if (level != null && !level.isEmpty()) {
            wrapper.eq(Alert::getLevel, level);
        }
        wrapper.orderByDesc(Alert::getCreateTime);
        Page<Alert> mpPage = alertMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return PageVO.from(mpPage);
    }

    public Alert getById(Long id) {
        return alertMapper.selectById(id);
    }

    @CacheEvict(value = "alert:active", allEntries = true)
    public void add(Alert alert) {
        alertMapper.insert(alert);
    }

    @CacheEvict(value = "alert:active", allEntries = true)
    public void update(Alert alert) {
        alertMapper.updateById(alert);
    }

    @CacheEvict(value = "alert:active", allEntries = true)
    public void resolve(Long id) {
        Alert alert = new Alert();
        alert.setId(id);
        alert.setStatus("RESOLVED");
        alertMapper.updateById(alert);
    }
}

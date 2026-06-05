package com.ocean.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ocean.entity.MonitorPoint;
import com.ocean.mapper.MonitorPointMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 监测点位服务 — 点位数据极少变动，长 TTL 缓存，写操作即时清缓存
 */
@Service
@RequiredArgsConstructor
public class MonitorPointService {

    private final MonitorPointMapper monitorPointMapper;

    /**
     * 点位列表（地图标注，高频读，缓存 1 h）
     */
    @Cacheable(value = "monitor:list", key = "(#type ?: 'all') + ':' + (#status ?: 'all')")
    public List<MonitorPoint> list(String type, String status) {
        LambdaQueryWrapper<MonitorPoint> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(type)) {
            wrapper.eq(MonitorPoint::getType, type);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(MonitorPoint::getStatus, status);
        }
        wrapper.orderByAsc(MonitorPoint::getId);
        return monitorPointMapper.selectList(wrapper);
    }

    public MonitorPoint getById(Long id) {
        return monitorPointMapper.selectById(id);
    }

    @CacheEvict(value = "monitor:list", allEntries = true)
    public void add(MonitorPoint point) {
        monitorPointMapper.insert(point);
    }

    @CacheEvict(value = "monitor:list", allEntries = true)
    public void update(MonitorPoint point) {
        monitorPointMapper.updateById(point);
    }

    @CacheEvict(value = "monitor:list", allEntries = true)
    public void delete(Long id) {
        monitorPointMapper.deleteById(id);
    }
}

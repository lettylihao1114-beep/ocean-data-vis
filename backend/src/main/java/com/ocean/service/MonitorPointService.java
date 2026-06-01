package com.ocean.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ocean.entity.MonitorPoint;
import com.ocean.mapper.MonitorPointMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 监测点位服务
 */
@Service
@RequiredArgsConstructor
public class MonitorPointService {

    private final MonitorPointMapper monitorPointMapper;

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

    public void add(MonitorPoint point) {
        monitorPointMapper.insert(point);
    }

    public void update(MonitorPoint point) {
        monitorPointMapper.updateById(point);
    }

    public void delete(Long id) {
        monitorPointMapper.deleteById(id);
    }
}

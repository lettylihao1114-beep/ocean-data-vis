package com.ocean.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ocean.dto.OceanDataQuery;
import com.ocean.dto.OceanDataVO;
import com.ocean.entity.OceanData;
import com.ocean.mapper.OceanDataMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 海洋数据服务
 */
@Service
@RequiredArgsConstructor
public class OceanDataService {

    private final OceanDataMapper oceanDataMapper;

    /**
     * 分页查询
     */
    public Page<OceanDataVO> pageQuery(int pageNum, int pageSize, OceanDataQuery query) {
        LambdaQueryWrapper<OceanData> wrapper = buildQueryWrapper(query);
        wrapper.orderByDesc(OceanData::getDataTime);

        Page<OceanData> page = oceanDataMapper.selectPage(
                new Page<>(pageNum, pageSize), wrapper
        );

        Page<OceanDataVO> voPage = new Page<>(pageNum, pageSize, page.getTotal());
        voPage.setRecords(page.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList()));

        return voPage;
    }

    /**
     * 列表查询（不分页）
     */
    public List<OceanDataVO> list(OceanDataQuery query) {
        LambdaQueryWrapper<OceanData> wrapper = buildQueryWrapper(query);
        wrapper.orderByDesc(OceanData::getDataTime);
        wrapper.last("LIMIT 500");

        return oceanDataMapper.selectList(wrapper).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取各海域最新数据（大屏概览）
     */
    @Cacheable(value = "ocean:overview", unless = "#result == null || #result.isEmpty()")
    public Map<String, OceanDataVO> getLatestByArea() {
        // 获取所有海域列表
        List<String> areas = oceanDataMapper.selectList(
                new LambdaQueryWrapper<OceanData>()
                        .select(OceanData::getSeaArea)
                        .groupBy(OceanData::getSeaArea)
                        .last("LIMIT 10")
        ).stream().map(OceanData::getSeaArea).collect(Collectors.toList());

        // 每个海域取最新一条
        return areas.stream().collect(Collectors.toMap(
                area -> area,
                area -> {
                    OceanData data = oceanDataMapper.selectOne(
                            new LambdaQueryWrapper<OceanData>()
                                    .eq(OceanData::getSeaArea, area)
                                    .orderByDesc(OceanData::getDataTime)
                                    .last("LIMIT 1")
                    );
                    return data != null ? toVO(data) : null;
                }
        ));
    }

    /**
     * 获取某段时间的温度趋势
     */
    public List<OceanDataVO> getTrend(String seaArea, String start, String end) {
        LambdaQueryWrapper<OceanData> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(seaArea)) {
            wrapper.eq(OceanData::getSeaArea, seaArea);
        }
        if (StringUtils.hasText(start)) {
            wrapper.ge(OceanData::getDataTime, start);
        }
        if (StringUtils.hasText(end)) {
            wrapper.le(OceanData::getDataTime, end);
        }
        wrapper.orderByAsc(OceanData::getDataTime);
        wrapper.last("LIMIT 200");

        return oceanDataMapper.selectList(wrapper).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    private LambdaQueryWrapper<OceanData> buildQueryWrapper(OceanDataQuery query) {
        LambdaQueryWrapper<OceanData> wrapper = new LambdaQueryWrapper<>();
        if (query == null) return wrapper;

        if (StringUtils.hasText(query.getSeaArea())) {
            wrapper.eq(OceanData::getSeaArea, query.getSeaArea());
        }
        if (query.getStartTime() != null) {
            wrapper.ge(OceanData::getDataTime, query.getStartTime());
        }
        if (query.getEndTime() != null) {
            wrapper.le(OceanData::getDataTime, query.getEndTime());
        }
        if (query.getMinLat() != null) {
            wrapper.ge(OceanData::getLatitude, query.getMinLat());
        }
        if (query.getMaxLat() != null) {
            wrapper.le(OceanData::getLatitude, query.getMaxLat());
        }
        if (query.getMinLng() != null) {
            wrapper.ge(OceanData::getLongitude, query.getMinLng());
        }
        if (query.getMaxLng() != null) {
            wrapper.le(OceanData::getLongitude, query.getMaxLng());
        }
        return wrapper;
    }

    private OceanDataVO toVO(OceanData entity) {
        OceanDataVO vo = new OceanDataVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}

package com.ocean.controller;

import com.ocean.dto.OceanDataQuery;
import com.ocean.dto.OceanDataVO;
import com.ocean.dto.PageVO;
import com.ocean.dto.R;
import com.ocean.service.OceanDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 海洋数据接口
 */
@Tag(name = "海洋数据", description = "海水温盐、洋流、潮汐数据查询与可视化")
@RestController
@RequestMapping("/api/ocean-data")
@RequiredArgsConstructor
public class OceanDataController {

    private final OceanDataService oceanDataService;

    @Operation(summary = "分页查询")
    @PostMapping("/page")
    public R<PageVO<OceanDataVO>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestBody(required = false) OceanDataQuery query) {
        return R.ok(oceanDataService.pageQuery(pageNum, pageSize, query));
    }

    @Operation(summary = "列表查询（地图标注用）")
    @PostMapping("/list")
    public R<List<OceanDataVO>> list(@RequestBody(required = false) OceanDataQuery query) {
        return R.ok(oceanDataService.list(query));
    }

    @Operation(summary = "大屏概览：各海域最新数据")
    @GetMapping("/overview")
    public R<Map<String, OceanDataVO>> overview() {
        return R.ok(oceanDataService.getLatestByArea());
    }

    @Operation(summary = "趋势数据：用于折线图/柱状图")
    @GetMapping("/trend")
    public R<List<OceanDataVO>> trend(
            @RequestParam(required = false) String seaArea,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {
        return R.ok(oceanDataService.getTrend(seaArea, start, end));
    }
}

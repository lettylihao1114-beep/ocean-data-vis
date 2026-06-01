package com.ocean.controller;

import com.ocean.dto.OceanDataQuery;
import com.ocean.dto.OceanDataVO;
import com.ocean.dto.R;
import com.ocean.service.ExportService;
import com.ocean.service.OceanDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据导出接口
 */
@Tag(name = "数据导出", description = "海洋数据 Excel/CSV 导出")
@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;
    private final OceanDataService oceanDataService;

    @Operation(summary = "导出 Excel")
    @PostMapping("/excel")
    public void exportExcel(HttpServletResponse response,
                            @RequestBody(required = false) OceanDataQuery query,
                            @RequestAttribute(required = false) Long userId) throws Exception {
        List<OceanDataVO> data = oceanDataService.list(query);
        exportService.exportExcel(response, data, userId);
    }

    @Operation(summary = "导出 CSV")
    @PostMapping("/csv")
    public void exportCsv(HttpServletResponse response,
                          @RequestBody(required = false) OceanDataQuery query,
                          @RequestAttribute(required = false) Long userId) throws Exception {
        List<OceanDataVO> data = oceanDataService.list(query);
        exportService.exportCsv(response, data, userId);
    }
}

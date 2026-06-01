package com.ocean.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.ocean.dto.OceanDataVO;
import com.ocean.entity.ExportLog;
import com.ocean.mapper.ExportLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 数据导出服务
 */
@Service
@RequiredArgsConstructor
public class ExportService {

    private final ExportLogMapper exportLogMapper;

    /**
     * 导出 Excel
     */
    public void exportExcel(HttpServletResponse response, List<OceanDataVO> data, Long userId) throws IOException {
        String fileName = "海洋数据导出_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition",
                "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8) + ".xlsx");

        EasyExcel.write(response.getOutputStream(), OceanDataVO.class)
                .sheet("海洋观测数据")
                .doWrite(data);

        // 记录导出日志
        ExportLog log = new ExportLog();
        log.setUserId(userId);
        log.setExportType("EXCEL");
        log.setFileName(fileName + ".xlsx");
        log.setParams("count=" + data.size());
        log.setStatus("SUCCESS");
        exportLogMapper.insert(log);
    }

    /**
     * 导出 CSV
     */
    public void exportCsv(HttpServletResponse response, List<OceanDataVO> data, Long userId) throws IOException {
        String fileName = "海洋数据导出_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8) + ".csv");

        // CSV BOM 头（Excel 识别 UTF-8）
        response.getOutputStream().write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

        // CSV 表头
        StringBuilder sb = new StringBuilder();
        sb.append("时间,经度,纬度,海域,水温(℃),盐度(PSU),溶解氧(mg/L),pH,洋流速度(m/s),洋流方向,潮汐(m),浪高(m),气压(hPa)\n");

        // 数据行
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (OceanDataVO d : data) {
            sb.append(d.getDataTime() != null ? d.getDataTime().format(fmt) : "").append(",");
            sb.append(d.getLongitude()).append(",");
            sb.append(d.getLatitude()).append(",");
            sb.append(d.getSeaArea() != null ? d.getSeaArea() : "").append(",");
            sb.append(d.getTemperature()).append(",");
            sb.append(d.getSalinity()).append(",");
            sb.append(d.getOxygen()).append(",");
            sb.append(d.getPh()).append(",");
            sb.append(d.getCurrentSpeed()).append(",");
            sb.append(d.getCurrentDir() != null ? d.getCurrentDir() : "").append(",");
            sb.append(d.getTideLevel()).append(",");
            sb.append(d.getWaveHeight()).append(",");
            sb.append(d.getPressure()).append("\n");
        }

        response.getOutputStream().write(sb.toString().getBytes(StandardCharsets.UTF_8));

        ExportLog log = new ExportLog();
        log.setUserId(userId);
        log.setExportType("CSV");
        log.setFileName(fileName + ".csv");
        log.setParams("count=" + data.size());
        log.setStatus("SUCCESS");
        exportLogMapper.insert(log);
    }
}

package com.ocean.controller;

import com.ocean.dto.PageVO;
import com.ocean.dto.R;
import com.ocean.entity.Knowledge;
import com.ocean.service.KnowledgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 科普知识接口
 */
@Tag(name = "科普知识", description = "海洋科普图文浏览与管理")
@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    @Operation(summary = "科普文章列表（公开）")
    @GetMapping("/public")
    public R<PageVO<Knowledge>> publicList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String category) {
        return R.ok(knowledgeService.page(pageNum, pageSize, category));
    }

    @Operation(summary = "文章详情（公开）")
    @GetMapping("/public/{id}")
    public R<Knowledge> detail(@PathVariable Long id) {
        return R.ok(knowledgeService.getById(id));
    }

    @Operation(summary = "管理列表（管理员）")
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public R<PageVO<Knowledge>> adminList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String category) {
        return R.ok(knowledgeService.adminPage(pageNum, pageSize, category));
    }

    @Operation(summary = "发布文章（管理员）")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public R<Void> add(@RequestBody Knowledge knowledge) {
        knowledgeService.add(knowledge);
        return R.ok("发布成功");
    }

    @Operation(summary = "编辑文章（管理员）")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public R<Void> update(@PathVariable Long id, @RequestBody Knowledge knowledge) {
        knowledge.setId(id);
        knowledgeService.update(knowledge);
        return R.ok("修改成功");
    }

    @Operation(summary = "删除文章（管理员）")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public R<Void> delete(@PathVariable Long id) {
        knowledgeService.delete(id);
        return R.ok("删除成功");
    }
}

package com.ocean.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ocean.dto.PageVO;
import com.ocean.entity.Knowledge;
import com.ocean.mapper.KnowledgeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 科普知识服务 — 读操作 DTO 层缓存，写操作自动清缓存
 */
@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final KnowledgeMapper knowledgeMapper;

    /**
     * 分页查询（公开，高频读，缓存 30 min）
     */
    @Cacheable(value = "knowledge:page", key = "#pageNum + ':' + #pageSize + ':' + (#category ?: 'all')")
    public PageVO<Knowledge> page(int pageNum, int pageSize, String category) {
        LambdaQueryWrapper<Knowledge> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Knowledge::getStatus, "PUBLISHED");
        if (category != null && !category.isEmpty()) {
            wrapper.eq(Knowledge::getCategory, category);
        }
        wrapper.orderByDesc(Knowledge::getCreateTime);
        Page<Knowledge> mpPage = knowledgeMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return PageVO.from(mpPage);
    }

    /**
     * 获取文章详情（含浏览量+1，属写操作，不缓存）
     */
    public Knowledge getById(Long id) {
        Knowledge knowledge = knowledgeMapper.selectById(id);
        if (knowledge != null) {
            knowledge.setViewCount(knowledge.getViewCount() + 1);
            knowledgeMapper.updateById(knowledge);
        }
        return knowledge;
    }

    /**
     * 管理员 - 文章列表（含草稿，低频，不缓存）
     */
    public PageVO<Knowledge> adminPage(int pageNum, int pageSize, String category) {
        LambdaQueryWrapper<Knowledge> wrapper = new LambdaQueryWrapper<>();
        if (category != null && !category.isEmpty()) {
            wrapper.eq(Knowledge::getCategory, category);
        }
        wrapper.orderByDesc(Knowledge::getCreateTime);
        Page<Knowledge> mpPage = knowledgeMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return PageVO.from(mpPage);
    }

    @CacheEvict(value = "knowledge:page", allEntries = true)
    public void add(Knowledge knowledge) {
        knowledgeMapper.insert(knowledge);
    }

    @CacheEvict(value = "knowledge:page", allEntries = true)
    public void update(Knowledge knowledge) {
        knowledgeMapper.updateById(knowledge);
    }

    @CacheEvict(value = "knowledge:page", allEntries = true)
    public void delete(Long id) {
        knowledgeMapper.deleteById(id);
    }
}

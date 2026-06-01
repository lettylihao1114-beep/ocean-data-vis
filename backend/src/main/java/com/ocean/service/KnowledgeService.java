package com.ocean.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ocean.entity.Knowledge;
import com.ocean.mapper.KnowledgeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 科普知识服务
 */
@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final KnowledgeMapper knowledgeMapper;

    /**
     * 分页查询（公开）
     */
    public Page<Knowledge> page(int pageNum, int pageSize, String category) {
        LambdaQueryWrapper<Knowledge> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Knowledge::getStatus, "PUBLISHED");
        if (category != null && !category.isEmpty()) {
            wrapper.eq(Knowledge::getCategory, category);
        }
        wrapper.orderByDesc(Knowledge::getCreateTime);
        return knowledgeMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    /**
     * 获取文章详情（公开）
     */
    public Knowledge getById(Long id) {
        Knowledge knowledge = knowledgeMapper.selectById(id);
        if (knowledge != null) {
            // 浏览量 +1
            knowledge.setViewCount(knowledge.getViewCount() + 1);
            knowledgeMapper.updateById(knowledge);
        }
        return knowledge;
    }

    /**
     * 管理员 - 文章列表（含草稿）
     */
    public Page<Knowledge> adminPage(int pageNum, int pageSize, String category) {
        LambdaQueryWrapper<Knowledge> wrapper = new LambdaQueryWrapper<>();
        if (category != null && !category.isEmpty()) {
            wrapper.eq(Knowledge::getCategory, category);
        }
        wrapper.orderByDesc(Knowledge::getCreateTime);
        return knowledgeMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public void add(Knowledge knowledge) {
        knowledgeMapper.insert(knowledge);
    }

    public void update(Knowledge knowledge) {
        knowledgeMapper.updateById(knowledge);
    }

    public void delete(Long id) {
        knowledgeMapper.deleteById(id);
    }
}

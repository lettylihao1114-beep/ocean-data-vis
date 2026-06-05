package com.ocean.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ocean.dto.PageVO;
import com.ocean.entity.User;
import com.ocean.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 用户管理服务（管理员专用，低频，不缓存）
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    /**
     * 分页查询用户（不返回密码）
     */
    public PageVO<User> pageUsers(int pageNum, int pageSize, String keyword) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(User::getUsername, keyword).or().like(User::getEmail, keyword);
        }
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> result = userMapper.selectPage(page, wrapper);
        // 脱敏：清除密码字段
        result.getRecords().forEach(u -> u.setPassword(null));
        return PageVO.from(result);
    }

    /**
     * 修改角色
     */
    public void updateRole(Long id, String role) {
        if (!"ADMIN".equals(role) && !"USER".equals(role)) {
            throw new IllegalArgumentException("无效的角色：" + role);
        }
        User user = new User();
        user.setId(id);
        user.setRole(role);
        userMapper.updateById(user);
    }

    /**
     * 切换启用/禁用状态
     */
    public void updateStatus(Long id, Integer status) {
        if (status != 0 && status != 1) {
            throw new IllegalArgumentException("无效的状态值");
        }
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        userMapper.updateById(user);
    }
}

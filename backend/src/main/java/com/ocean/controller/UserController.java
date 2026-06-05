package com.ocean.controller;

import com.ocean.dto.PageVO;
import com.ocean.dto.R;
import com.ocean.entity.User;
import com.ocean.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户管理接口（仅管理员）
 */
@Tag(name = "用户管理", description = "管理员管理用户角色与状态")
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @Operation(summary = "用户列表（不含密码）")
    @GetMapping
    public R<PageVO<User>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        return R.ok(userService.pageUsers(pageNum, pageSize, keyword));
    }

    @Operation(summary = "修改用户角色")
    @PutMapping("/{id}/role")
    public R<Void> updateRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String role = body.get("role");
        userService.updateRole(id, role);
        return R.ok("角色已更新");
    }

    @Operation(summary = "切换账号状态（启用/禁用）")
    @PutMapping("/{id}/status")
    public R<Void> toggleStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        userService.updateStatus(id, status);
        return R.ok(status == 1 ? "账号已启用" : "账号已禁用");
    }
}

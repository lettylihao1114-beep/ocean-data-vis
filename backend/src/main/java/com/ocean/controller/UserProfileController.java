package com.ocean.controller;

import com.ocean.dto.ChangePasswordRequest;
import com.ocean.dto.R;
import com.ocean.dto.UpdateProfileRequest;
import com.ocean.service.UserProfileService;
import com.ocean.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 个人中心接口（所有已登录用户可用）
 */
@Tag(name = "个人中心", description = "查看/编辑个人资料、修改密码")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final JwtUtil jwtUtil;

    /**
     * 从 Header 中解析当前用户 ID（JWT 已在 Filter 中校验通过）
     */
    private Long getCurrentUserId(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.getUserId(token);
    }

    @Operation(summary = "获取个人资料")
    @GetMapping("/profile")
    public R<Map<String, Object>> getProfile(@RequestHeader("Authorization") String authHeader) {
        Long userId = getCurrentUserId(authHeader);
        return R.ok(userProfileService.getProfile(userId));
    }

    @Operation(summary = "更新个人资料")
    @PutMapping("/profile")
    public R<Void> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UpdateProfileRequest request) {
        Long userId = getCurrentUserId(authHeader);
        userProfileService.updateProfile(userId, request);
        return R.ok("个人资料已更新");
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public R<Void> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ChangePasswordRequest request) {
        Long userId = getCurrentUserId(authHeader);
        userProfileService.changePassword(userId, request);
        return R.ok("密码已修改，请重新登录");
    }
}

package com.secondhand.controller;

import com.secondhand.common.response.Result;
import com.secondhand.model.entity.User;
import com.secondhand.model.vo.UserVO;
import com.secondhand.service.UserService;
import com.secondhand.common.utils.JwtUtil;
import com.secondhand.model.dto.UserUpdateDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminUserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    // 获取所有用户列表
    @GetMapping("/users")
    public Result<List<UserVO>> getAllUsers(@RequestHeader("Authorization") String token) {
        try {
            // 验证管理员权限
            String username = jwtUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            User currentUser = userService.getUserByUsername(username);
            
            if (currentUser.getRole() != 2) {
                return Result.error("需要管理员权限");
            }

            List<UserVO> users = userService.getAllUsers();
            return Result.success(users);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 获取所有商家列表
    @GetMapping("/merchants")
    public Result<List<UserVO>> getAllMerchants(@RequestHeader("Authorization") String token) {
        try {
            // 验证管理员权限
            String username = jwtUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            User currentUser = userService.getUserByUsername(username);
            
            if (currentUser.getRole() != 2) {
                return Result.error("需要管理员权限");
            }

            List<UserVO> merchants = userService.getAllUsers().stream()
                .filter(user -> user.getUserType() == 1 && user.getRole() != 2)
                .collect(java.util.stream.Collectors.toList());
            return Result.success(merchants);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 更新用户状态
    @PostMapping("/user/status")
    public Result<?> updateUserStatus(
            @RequestHeader("Authorization") String token,
            @RequestParam Long userId,
            @RequestParam Integer status) {
        try {
            // 验证管理员权限
            String username = jwtUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            User currentUser = userService.getUserByUsername(username);
            
            if (currentUser.getRole() != 2) {
                return Result.error("需要管理员权限");
            }

            userService.toggleUserStatus(userId, status);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 审核商家
    @PostMapping("/merchant/audit")
    public Result<?> auditMerchant(
            @RequestHeader("Authorization") String token,
            @RequestParam Long userId,
            @RequestParam Integer auditStatus,
            @RequestParam(required = false) String reason) {
        try {
            // 验证管理员权限
            String username = jwtUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            User currentUser = userService.getUserByUsername(username);
            
            if (currentUser.getRole() != 2) {
                return Result.error("需要管理员权限");
            }

            userService.auditMerchant(userId, auditStatus, reason);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 更新用户信息
    @PostMapping("/user/update")
    public Result<?> updateUserInfo(
            @RequestHeader("Authorization") String token,
            @RequestBody UserUpdateDTO updateDTO) {
        try {
            // 验证管理员权限
            String username = jwtUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            User currentUser = userService.getUserByUsername(username);
            
            if (currentUser.getRole() != 2) {
                return Result.error("需要管理员权限");
            }

            User updatedUser = userService.updateUserInfo(updateDTO, token);
            return Result.success(updatedUser);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
} 
package com.secondhand.controller;

import com.secondhand.model.dto.LoginRequest;
import com.secondhand.model.dto.LoginResponse;
import com.secondhand.model.entity.User;
import com.secondhand.repository.UserRepository;
import com.secondhand.common.utils.JwtUtil;
import com.secondhand.common.utils.PasswordEncoder;
import com.secondhand.common.response.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<Result<LoginResponse>> login(@RequestBody LoginRequest request) {
        try {
            // 查找用户
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

            // 验证密码（直接比较明文）
            if (!request.getPassword().equals(user.getPassword())) {
                throw new RuntimeException("用户名或密码错误");
            }

            // 检查账号状态
            if (!user.isEnabled()) {
                if (user.isLocked()) {
                    throw new RuntimeException("账号已被禁用");
                }
                if (user.getAuditStatus() != 1) {
                    throw new RuntimeException("账号未通过审核");
                }
            }

            // 生成 token
            String token = jwtUtil.generateToken(user);

            // 返回登录响应
            LoginResponse response = new LoginResponse(
                token,
                user.getUsername(),
                user.getRole(),
                user.getUserType(),
                user.getRealName(),
                user.getAvatar()
            );

            return ResponseEntity.ok(Result.success(response));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "登录失败：" + e.getMessage()));
        }
    }
} 
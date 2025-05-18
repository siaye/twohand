package com.secondhand.controller;

import com.secondhand.common.response.Result;
import com.secondhand.model.dto.UserRegisterDTO;
import com.secondhand.model.dto.UserLoginDTO;
import com.secondhand.model.vo.UserVO;
import com.secondhand.service.UserService;
import com.secondhand.common.utils.CaptchaGenerator;
import cn.hutool.captcha.LineCaptcha;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    // 用户注册
    @PostMapping("/register")
    public Result<Void> register(@RequestBody UserRegisterDTO registerDTO) {
        userService.register(registerDTO);
        return Result.success(null);
    }

    // 用户登录
    @PostMapping("/login")
    public Result<String> login(@RequestBody UserLoginDTO loginDTO) {
        String token = userService.login(loginDTO);
        return Result.success(token);
    }

    // 获取用户信息
    @GetMapping("/info")
    public Result<UserVO> getUserInfo(@RequestParam String identifier) {
        UserVO userVO = userService.getUserInfo(identifier);
        return Result.success(userVO);
    }

    // 获取验证码
    @GetMapping("/captcha")
    public void getCaptcha(HttpServletResponse response, HttpSession session) throws IOException {
        // 生成验证码
        LineCaptcha lineCaptcha = CaptchaGenerator.create(150, 40, 4, 10);
        // 将验证码内容存入session
        session.setAttribute("captcha", lineCaptcha.getCode());
        // 设置响应类型
        response.setContentType("image/png");
        // 输出图片流
        lineCaptcha.write(response.getOutputStream());
    }

    // 管理员审核商家注册
    @PostMapping("/audit")
    public Result<Void> auditMerchant(@RequestParam Long userId,
                                      @RequestParam Integer auditStatus,
                                      @RequestParam(required = false) String reason) {
        userService.auditMerchant(userId, auditStatus, reason);
        return Result.success(null);
    }
} 
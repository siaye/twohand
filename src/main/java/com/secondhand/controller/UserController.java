package com.secondhand.controller;

import com.secondhand.common.response.Result;
import com.secondhand.model.dto.LoginResponse;
import com.secondhand.model.dto.UserRegisterDTO;
import com.secondhand.model.dto.UserLoginDTO;
import com.secondhand.model.dto.UserUpdateDTO;
import com.secondhand.model.dto.UserProfileDTO;
import com.secondhand.model.vo.UserVO;
import com.secondhand.model.entity.User;
import com.secondhand.service.UserService;
import com.secondhand.service.UserPointsService;
import com.secondhand.common.utils.CaptchaGenerator;
import com.secondhand.common.utils.JwtUtil;
import com.secondhand.utils.SecurityUtils;
import cn.hutool.captcha.LineCaptcha;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserPointsService userPointsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private HttpSession session;

    // 生成验证码
    @GetMapping("/captcha")
    public void getCaptcha(HttpServletResponse response) throws IOException {
        // 生成验证码
        LineCaptcha lineCaptcha = CaptchaGenerator.create(150, 40, 4, 10);
        String code = lineCaptcha.getCode();
        
        // 将验证码内容存入session，并设置过期时间（2分钟）
        session.setAttribute("captcha", code);
        session.setMaxInactiveInterval(120);
        
        // 添加调试日志
        System.out.println("【验证码生成】code=" + code);
        
        // 设置响应头，禁止缓存
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/png");
        
        // 输出图片流
        lineCaptcha.write(response.getOutputStream());
    }

    // 用户注册
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        try {
            // 注册用户
            userService.register(registerDTO);
            
            // 获取注册后的用户信息
            User user = userService.getUserByUsername(registerDTO.getUsername());
            
            // 只返回用户ID和基本信息，不返回token
            Map<String, Object> response = new HashMap<>();
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            response.put("userType", user.getUserType());
            
            return Result.success(response);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 用户登录
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody UserLoginDTO loginDTO) {
        System.out.println("【登录请求】收到登录请求：");
        System.out.println("用户名：" + loginDTO.getUsername());
        System.out.println("密码：" + loginDTO.getPassword());
        System.out.println("验证码：" + loginDTO.getCaptcha());
        
        try {
            String token = userService.login(loginDTO);
            System.out.println("【登录成功】生成token：" + token);
            
            // 获取用户信息
            User user = userService.getUserByUsername(loginDTO.getUsername());
            LoginResponse response = new LoginResponse(
                token,
                user.getUsername(),
                user.getRole(),
                user.getUserType(),
                user.getRealName(),
                user.getAvatar()
            );
            
            System.out.println("【登录响应】返回数据：" + response);
            return Result.success(response);
        } catch (Exception e) {
            System.out.println("【登录失败】" + e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    // 获取用户信息
    @GetMapping("/info")
    public Result<UserVO> getUserInfo(@RequestHeader("Authorization") String token) {
        try {
            UserVO userInfo = userService.getCurrentUserInfo(token);
            return Result.success(userInfo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 更新用户信息
    @PostMapping("/update")
    public Result<User> updateUserInfo(@Valid @RequestBody UserUpdateDTO updateDTO, @RequestHeader("Authorization") String token) {
        try {
            System.out.println("【更新用户信息】收到请求：");
            System.out.println("- 请求数据：" + updateDTO);
            System.out.println("- 原始Token：" + token);

            // 处理 token
            String cleanToken = token;
            if (token != null && token.startsWith("Bearer ")) {
                cleanToken = token.substring(7).trim();
            }
            System.out.println("- 处理后的Token：" + cleanToken);

            // 验证权限
            String currentUsername = jwtUtil.getUsernameFromToken(cleanToken);
            System.out.println("- 当前用户：" + currentUsername);
            
            User currentUser = userService.getUserByUsername(currentUsername);
            System.out.println("- 用户角色：" + currentUser.getRole());
            
            // 检查是否是管理员或本人
            if (updateDTO.getId() != null && !updateDTO.getId().equals(currentUser.getId()) && currentUser.getRole() != 2) {
                System.out.println("- 权限验证失败：无权修改其他用户信息");
                return Result.error("无权修改其他用户信息");
            }
            
            // 如果更新的是自己的信息，确保ID一致
            if (updateDTO.getId() == null) {
                updateDTO.setId(currentUser.getId());
            }

            // 处理输入数据
            if (updateDTO.getRealName() != null) {
                updateDTO.setRealName(updateDTO.getRealName().trim());
            }
            if (updateDTO.getPhone() != null) {
                updateDTO.setPhone(updateDTO.getPhone().trim());
            }
            if (updateDTO.getEmail() != null) {
                updateDTO.setEmail(updateDTO.getEmail().trim());
            }
            
            // 更新用户信息
            User updatedUser = userService.updateUserInfo(updateDTO, cleanToken);
            System.out.println("- 更新成功：" + updatedUser);
            return Result.success(updatedUser);
        } catch (Exception e) {
            System.out.println("- 更新失败：" + e.getMessage());
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    // 上传头像
    @PostMapping("/upload/avatar")
    public Result<String> uploadAvatar(
        @RequestParam("file") MultipartFile file,
        @RequestHeader("Authorization") String token
    ) {
        try {
            String username = jwtUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            User user = userService.getUserByUsername(username);
            
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.error("只能上传图片文件");
            }

            // 验证文件大小（20MB）
            if (file.getSize() > 20 * 1024 * 1024) {
                return Result.error("头像大小不能超过20MB");
            }

            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? 
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String filename = UUID.randomUUID().toString() + extension;

            // 获取项目根目录
            String projectRoot = System.getProperty("user.dir");
            // 创建上传目录
            String uploadDir = projectRoot + File.separator + "uploads" + File.separator + "avatar";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 保存文件
            File destFile = new File(dir, filename);
            file.transferTo(destFile);

            // 更新用户头像
            String imageUrl = "/uploads/avatar/" + filename;
            user.setAvatar(imageUrl);
            userService.updateUserInfo(new UserUpdateDTO() {{
                setId(user.getId());
                setAvatar(imageUrl);
            }}, token);

            return Result.success(imageUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    // 上传身份证照片
    @PostMapping("/upload/id-card")
    public Result<String> uploadIdCardImage(@RequestBody String base64Image, @RequestHeader("Authorization") String token) {
        try {
            String username = jwtUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            User user = userService.getUserByUsername(username);
            String imageUrl = userService.uploadIdCardImage(base64Image, user.getId());
            return Result.success(imageUrl);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 获取用户积分
    @GetMapping("/points")
    public Result<Integer> getUserPoints() {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }
            int points = userPointsService.getUserPoints(userId);
            return Result.success(points);
        } catch (Exception e) {
            return Result.error("获取用户积分失败: " + e.getMessage());
        }
    }

    // 上传营业执照
    @PostMapping("/upload/business-license")
    public Result<String> uploadBusinessLicenseImage(@RequestBody String base64Image, @RequestHeader("Authorization") String token) {
        try {
            String username = jwtUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            User user = userService.getUserByUsername(username);
            String imageUrl = userService.uploadBusinessLicenseImage(base64Image, user.getId());
            return Result.success(imageUrl);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 完善用户信息
    @PostMapping("/complete-profile")
    public Result<Void> completeProfile(@Valid @RequestBody UserProfileDTO profileDTO, @RequestHeader("X-User-ID") Long userId) {
        try {
            System.out.println("【完善信息】收到请求:");
            System.out.println("- 用户ID: " + userId);
            System.out.println("- 请求数据: " + profileDTO);
            
            // 使用用户ID而不是token
            userService.completeProfile(userId, profileDTO);
            System.out.println("【完善信息】处理成功");
            return Result.success(null);
        } catch (Exception e) {
            System.out.println("【完善信息】处理失败: " + e.getMessage());
            e.printStackTrace();  // 打印完整堆栈信息
            return Result.error(e.getMessage());
        }
    }

    // 上传图片
    @PostMapping("/upload")
    public Result<String> uploadImage(
        @RequestParam("file") MultipartFile file,
        @RequestParam("type") String type,
        @RequestHeader("X-User-ID") String userId
    ) {
        try {
            Long userIdLong = Long.parseLong(userId);
            User user = userService.getUserById(userIdLong);
            String imageUrl = userService.uploadImage(file, type, userIdLong);
            return Result.success(imageUrl);
        } catch (NumberFormatException e) {
            return Result.error("无效的用户ID");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    // 修改密码
    @PostMapping("/change-password")
    public Result<Void> changePassword(
            @RequestBody Map<String, String> request,
            @RequestHeader("Authorization") String token) {
        try {
            String username = jwtUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");

            // 调用 Service 层处理修改密码逻辑
            userService.changePassword(username, currentPassword, newPassword);

            return Result.success(null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("修改密码失败");
        }
    }

    // 获取用户信息（完善信息阶段使用）
    @GetMapping("/profile")
    public Result<UserVO> getUserProfile(@RequestHeader("X-User-ID") String userId) {
        try {
            Long userIdLong = Long.parseLong(userId);
            User user = userService.getUserById(userIdLong);
            if (user == null) {
                return Result.error("用户不存在");
            }
            UserVO userVO = new UserVO();
            userVO.setId(user.getId());
            userVO.setUsername(user.getUsername());
            userVO.setRealName(user.getRealName());
            userVO.setPhone(user.getPhone());
            userVO.setEmail(user.getEmail());
            userVO.setCity(user.getCity());
            userVO.setGender(user.getGender());
            userVO.setBankAccount(user.getBankAccount());
            userVO.setIdCard(user.getIdCard());
            userVO.setIdCardImage(user.getIdCardImage());
            userVO.setBusinessLicense(user.getBusinessLicense());
            userVO.setBusinessLicenseImage(user.getBusinessLicenseImage());
            userVO.setUserType(user.getUserType());
            return Result.success(userVO);
        } catch (NumberFormatException e) {
            return Result.error("无效的用户ID");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
} 
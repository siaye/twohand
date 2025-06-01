package com.secondhand.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.secondhand.model.dto.UserRegisterDTO;
import com.secondhand.model.dto.UserLoginDTO;
import com.secondhand.model.dto.UserUpdateDTO;
import com.secondhand.model.dto.UserProfileDTO;
import com.secondhand.model.vo.UserVO;
import com.secondhand.model.entity.User;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface UserService extends IService<User> {
    // 用户注册
    void register(UserRegisterDTO registerDTO);

    // 用户登录，返回token
    String login(UserLoginDTO loginDTO);

    // 根据用户名/手机号/邮箱查询用户信息
    UserVO getUserInfo(String identifier);

    // 获取当前登录用户信息
    UserVO getCurrentUserInfo(String token);

    // 根据用户名获取用户
    User getUserByUsername(String username);

    // 根据用户ID获取用户
    User getUserById(Long id);

    // 获取待审核商家列表
    List<UserVO> getPendingMerchants();

    // 获取所有用户列表
    List<UserVO> getAllUsers();

    // 审核商家注册
    void auditMerchant(Long userId, Integer auditStatus, String reason);

    // 更新用户信息
    User updateUserInfo(UserUpdateDTO updateDTO, String token);

    // 禁用/启用用户
    void toggleUserStatus(Long userId, Integer status);

    // 删除用户
    void deleteUser(Long userId);

    // 上传头像
    String uploadAvatar(String base64Image, Long userId);

    // 上传身份证照片
    String uploadIdCardImage(String base64Image, Long userId);

    // 上传营业执照照片
    String uploadBusinessLicenseImage(String base64Image, Long userId);

    // 完善用户信息
    void completeProfile(Long userId, UserProfileDTO profileDTO);

    // 上传图片
    String uploadImage(MultipartFile file, String type, Long userId);

    // 修改密码
    void changePassword(String username, String currentPassword, String newPassword);
} 
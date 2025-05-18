package com.secondhand.service;

import com.secondhand.model.dto.UserRegisterDTO;
import com.secondhand.model.dto.UserLoginDTO;
import com.secondhand.model.vo.UserVO;

public interface UserService {
    // 用户注册
    void register(UserRegisterDTO registerDTO);

    // 用户登录，返回token
    String login(UserLoginDTO loginDTO);

    // 根据用户名/手机号/邮箱查询用户信息
    UserVO getUserInfo(String identifier);

    // 管理员审核商家注册
    void auditMerchant(Long userId, Integer auditStatus, String reason);
} 
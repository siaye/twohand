package com.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.secondhand.model.dto.UserRegisterDTO;
import com.secondhand.model.dto.UserLoginDTO;
import com.secondhand.model.vo.UserVO;
import com.secondhand.model.entity.User;
import com.secondhand.repository.UserMapper;
import com.secondhand.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.secondhand.common.utils.JwtUtil;

import jakarta.servlet.http.HttpSession;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private HttpSession session;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public void register(UserRegisterDTO registerDTO) {
        // 1. 校验验证码
        String sessionCaptcha = (String) session.getAttribute("captcha");
        // 添加日志，打印 session 里的验证码和前端传来的验证码
        System.out.println("【注册调试】sessionCaptcha=" + sessionCaptcha + ", inputCaptcha=" + registerDTO.getCaptcha());
        // 添加日志，打印前端输入的注册信息，包括密码和确认密码
        System.out.println("【注册调试】Input Username=" + registerDTO.getUsername() 
            + ", Input Password=" + registerDTO.getPassword() 
            + ", Input ConfirmPassword=" + registerDTO.getConfirmPassword()
            + ", Phone=" + registerDTO.getPhone() 
            + ", Email=" + registerDTO.getEmail());

        if (sessionCaptcha == null || !sessionCaptcha.equalsIgnoreCase(registerDTO.getCaptcha())) {
            throw new RuntimeException("验证码错误");
        }
        // 添加日志，打印前端输入的注册信息
        System.out.println("【注册调试】Input Username=" + registerDTO.getUsername() + ", Input Password=" + registerDTO.getPassword() + ", Phone=" + registerDTO.getPhone() + ", Email=" + registerDTO.getEmail());

        // 2. 校验用户名、手机号、邮箱唯一性
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.eq(User::getUsername, registerDTO.getUsername())
             .or().eq(User::getPhone, registerDTO.getPhone());
        if (registerDTO.getEmail() != null) {
            query.or().eq(User::getEmail, registerDTO.getEmail());
        }
        if (userMapper.selectCount(query) > 0) {
            throw new RuntimeException("用户名、手机号或邮箱已存在");
        }
        // 3. 校验两次密码一致
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new RuntimeException("两次密码输入不一致");
        }
        // 4. 保存用户
        User user = new User();
        BeanUtils.copyProperties(registerDTO, user);
        user.setStatus(1); // 默认正常
        user.setRole(registerDTO.getUserType() == 1 ? 1 : 0); // 商家1，普通用户0
        user.setAuditStatus(registerDTO.getUserType() == 1 ? 0 : 1); // 商家待审核，个人直接通过
        userMapper.insert(user);
    }

    @Override
    public String login(UserLoginDTO loginDTO) {
        // 1. 校验验证码
        String sessionCaptcha = (String) session.getAttribute("captcha");
        System.out.println("【登录调试】sessionCaptcha=" + sessionCaptcha + ", inputCaptcha=" + loginDTO.getCaptcha());
        if (sessionCaptcha == null || !sessionCaptcha.equalsIgnoreCase(loginDTO.getCaptcha())) {
            throw new RuntimeException("验证码错误");
        }
        // 2. 查询用户
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.eq(User::getUsername, loginDTO.getUsername())
             .or().eq(User::getPhone, loginDTO.getUsername())
             .or().eq(User::getEmail, loginDTO.getUsername());
        User user = userMapper.selectOne(query);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        // 添加日志，打印前端输入的用户名/密码和数据库查询到的用户名/密码
        System.out.println("【登录调试】Input Username=" + loginDTO.getUsername() + ", Input Password=" + loginDTO.getPassword());
        System.out.println("【登录调试】DB Username=" + user.getUsername() + ", DB Password=" + user.getPassword());
        // 3. 校验密码（明文，后续建议加密）
        if (!user.getPassword().equals(loginDTO.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        // 4. 校验状态
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }
        // 5. 校验商家审核
        if (user.getUserType() == 1 && user.getAuditStatus() != 1) {
            throw new RuntimeException("商家账号未通过审核");
        }
        // 6. 登录成功，生成token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return token;
    }

    @Override
    public UserVO getUserInfo(String identifier) {
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.eq(User::getUsername, identifier)
             .or().eq(User::getPhone, identifier)
             .or().eq(User::getEmail, identifier);
        User user = userMapper.selectOne(query);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    @Override
    public void auditMerchant(Long userId, Integer auditStatus, String reason) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getUserType() != 1) {
            throw new RuntimeException("商家用户不存在");
        }
        user.setAuditStatus(auditStatus);
        // 可选：记录审核备注（如需扩展可加字段）
        userMapper.updateById(user);
    }
    
} 
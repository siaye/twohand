package com.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.secondhand.model.dto.UserRegisterDTO;
import com.secondhand.model.dto.UserLoginDTO;
import com.secondhand.model.dto.UserUpdateDTO;
import com.secondhand.model.dto.UserProfileDTO;
import com.secondhand.model.vo.UserVO;
import com.secondhand.model.entity.User;
import com.secondhand.repository.UserMapper;
import com.secondhand.service.UserService;
import com.secondhand.service.WalletService;
import com.secondhand.common.utils.JwtUtil;
import com.secondhand.common.utils.PasswordEncoder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.secondhand.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
@Transactional
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private HttpSession session;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletService walletService;

    @Override
    public void register(UserRegisterDTO registerDTO) {
        // 1. 校验验证码
        String sessionCaptcha = (String) session.getAttribute("captcha");
        if (sessionCaptcha == null || !sessionCaptcha.equalsIgnoreCase(registerDTO.getCaptcha())) {
            throw new RuntimeException("验证码错误");
        }

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
        // 直接使用明文密码
        user.setPassword(registerDTO.getPassword());
        user.setStatus(1); // 默认正常
        user.setRole(registerDTO.getUserType() == 1 ? 1 : 0); // 商家1，普通用户0
        user.setAuditStatus(0); // 所有用户都需要审核
        
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        user.setCreateTime(now);
        user.setUpdateTime(now);
        
        try {
            userMapper.insert(user);
            
            // 5. 初始化钱包
            walletService.initializeWallet(user.getId());
            System.out.println("【注册成功】用户 " + user.getUsername() + " 的钱包已初始化");
            
        } catch (Exception e) {
            System.out.println("【注册失败】保存用户信息或初始化钱包时出错：" + e.getMessage());
            throw new RuntimeException("注册失败：" + e.getMessage());
        }
    }

    @Override
    public String login(UserLoginDTO loginDTO) {
        System.out.println("【登录处理】开始处理登录请求");
        
        // 1. 校验验证码
        String sessionCaptcha = (String) session.getAttribute("captcha");
        System.out.println("【验证码校验】");
        System.out.println("- session中的验证码：" + sessionCaptcha);
        System.out.println("- 用户输入的验证码：" + loginDTO.getCaptcha());
        System.out.println("- 验证码是否为空：" + (sessionCaptcha == null));
        
        if (sessionCaptcha == null) {
            System.out.println("【验证码校验失败】验证码已过期");
            throw new RuntimeException("验证码已过期，请重新获取");
        }
        if (!sessionCaptcha.equalsIgnoreCase(loginDTO.getCaptcha())) {
            System.out.println("【验证码校验失败】验证码不匹配");
            throw new RuntimeException("验证码错误");
        }
        System.out.println("【验证码校验成功】");
        
        // 验证码使用后立即清除
        session.removeAttribute("captcha");
        System.out.println("【验证码已清除】");

        // 2. 查找用户
        System.out.println("【用户查询】开始查询用户：" + loginDTO.getUsername());
        User user = userRepository.findByUsername(loginDTO.getUsername())
                .orElseThrow(() -> {
                    System.out.println("【用户查询失败】用户不存在");
                    return new RuntimeException("用户不存在");
                });
        System.out.println("【用户查询成功】用户ID：" + user.getId());

        // 3. 验证密码（直接比较明文）
        System.out.println("【密码校验】");
        System.out.println("- 用户输入的密码：" + loginDTO.getPassword());
        System.out.println("- 数据库中的密码：" + user.getPassword());
        
        if (!loginDTO.getPassword().equals(user.getPassword())) {
            System.out.println("【密码校验失败】密码不匹配");
            throw new RuntimeException("密码错误");
        }
        System.out.println("【密码校验成功】");

        // 4. 检查账号状态
        System.out.println("【账号状态检查】");
        System.out.println("- 账号状态：" + user.getStatus());
        System.out.println("- 用户类型：" + user.getUserType());
        System.out.println("- 审核状态：" + user.getAuditStatus());
        
        if (user.getStatus() != 1) {
            System.out.println("【账号状态检查失败】账号已被禁用");
            throw new RuntimeException("账号已被禁用");
        }
        if (user.getAuditStatus() != 1) {
            System.out.println("【账号状态检查失败】账号未通过审核");
            throw new RuntimeException("账号未通过审核，请等待管理员审核");
        }
        System.out.println("【账号状态检查通过】");
        
        // 5. 登录成功，生成token
        String token = jwtUtil.generateToken(user);
        System.out.println("【登录成功】生成token：" + token);
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
        user.setAuditReason(reason);
        user.setAuditTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public User updateUserInfo(UserUpdateDTO updateDTO, String token) {
        System.out.println("【UserService】更新用户信息：");
        System.out.println("- 更新数据：" + updateDTO);
        
        // 处理 token
        String cleanToken = token;
        if (token != null && token.startsWith("Bearer ")) {
            cleanToken = token.substring(7).trim();
        }
        System.out.println("- 处理后的Token：" + cleanToken);
        
        // 从 token 中获取当前用户信息
        String currentUsername = jwtUtil.getUsernameFromToken(cleanToken);
        System.out.println("- 当前用户：" + currentUsername);
        
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        System.out.println("- 当前用户角色：" + currentUser.getRole());
        System.out.println("- 当前用户ID：" + currentUser.getId());
        System.out.println("- 目标用户ID：" + updateDTO.getId());

        // 查找要更新的用户
        User user = userRepository.selectById(updateDTO.getId());
        if (user == null) {
            System.out.println("- 错误：用户不存在");
            throw new RuntimeException("用户不存在");
        }
        System.out.println("- 目标用户：" + user.getUsername());

        // 验证权限：只能更新自己的信息，除非是管理员
        if (!currentUsername.equals(user.getUsername()) && currentUser.getRole() != 2) {
            System.out.println("- 错误：无权修改其他用户信息");
            System.out.println("- 当前用户：" + currentUsername);
            System.out.println("- 目标用户：" + user.getUsername());
            System.out.println("- 当前用户角色：" + currentUser.getRole());
            throw new RuntimeException("无权修改其他用户信息");
        }
        System.out.println("- 权限验证通过");

        // 验证手机号唯一性
        if (updateDTO.getPhone() != null && !updateDTO.getPhone().equals(user.getPhone())) {
            if (userRepository.existsByPhone(updateDTO.getPhone())) {
                System.out.println("- 错误：手机号已被使用");
                throw new RuntimeException("手机号已被使用");
            }
        }

        // 验证邮箱唯一性
        if (updateDTO.getEmail() != null && !updateDTO.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(updateDTO.getEmail())) {
                System.out.println("- 错误：邮箱已被使用");
                throw new RuntimeException("邮箱已被使用");
            }
        }

        // 更新用户信息
        if (updateDTO.getRealName() != null) {
            user.setRealName(updateDTO.getRealName());
        }
        if (updateDTO.getPhone() != null) {
            user.setPhone(updateDTO.getPhone());
        }
        if (updateDTO.getEmail() != null) {
            user.setEmail(updateDTO.getEmail());
        }
        if (updateDTO.getCity() != null) {
            user.setCity(updateDTO.getCity());
        }
        if (updateDTO.getGender() != null) {
            user.setGender(updateDTO.getGender());
        }
        if (updateDTO.getBankAccount() != null) {
            user.setBankAccount(updateDTO.getBankAccount());
        }
        if (updateDTO.getAvatar() != null) {
            user.setAvatar(updateDTO.getAvatar());
        }

        // 如果是商家用户，更新营业执照
        if (user.getUserType() == 1 && updateDTO.getBusinessLicense() != null) {
            user.setBusinessLicense(updateDTO.getBusinessLicense());
        }

        // 如果是商家用户，更新商家等级和手续费率
        if (user.getUserType() == 1) {
            if (updateDTO.getMerchantLevel() != null) {
                user.setMerchantLevel(updateDTO.getMerchantLevel());
            }
            if (updateDTO.getMerchantFee() != null) {
                user.setMerchantFee(updateDTO.getMerchantFee());
            }
        }

        user.setUpdateTime(LocalDateTime.now());
        userRepository.updateById(user);
        System.out.println("- 更新成功");
        return user;
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    @Override
    public User getUserById(Long id) {
        if (id == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return user;
    }

    @Override
    public UserVO getCurrentUserInfo(String token) {
        String username = jwtUtil.getUsernameFromToken(token.replace("Bearer ", ""));
        User user = getUserByUsername(username);
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);

        // 手动格式化并设置创建时间
        if (user.getCreateTime() != null) {
            vo.setCreateTime(user.getCreateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        // 手动格式化并设置更新时间（如果需要）
        if (user.getUpdateTime() != null) {
             vo.setUpdateTime(user.getUpdateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }

        return vo;
    }

    @Override
    public List<UserVO> getPendingMerchants() {
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.eq(User::getUserType, 1)  // 商家用户
             .eq(User::getAuditStatus, 0);  // 待审核状态
        List<User> merchants = userMapper.selectList(query);
        return merchants.stream()
                .map(merchant -> {
                    UserVO vo = new UserVO();
                    BeanUtils.copyProperties(merchant, vo);
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<UserVO> getAllUsers() {
        List<User> users = userMapper.selectList(null);
        return users.stream().map(user -> {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(user, vo);
            // 确保创建时间不为空
            if (user.getCreateTime() != null) {
                vo.setCreateTime(user.getCreateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void toggleUserStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Override
    public void deleteUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        userMapper.deleteById(userId);
    }

    @Override
    public String uploadAvatar(String base64Image, Long userId) {
        try {
            // 验证文件大小（20MB）
            if (base64Image.length() > 20 * 1024 * 1024) {
                throw new RuntimeException("头像大小不能超过20MB");
            }

            // 生成文件名
            String filename = UUID.randomUUID().toString() + ".jpg";

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
            // 将 base64 转换为图片文件
            byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Image);
            java.nio.file.Files.write(destFile.toPath(), imageBytes);

            // 返回文件URL（使用相对路径）
            String avatarUrl = "/uploads/avatar/" + filename;
            
            // 更新用户头像
            User user = userMapper.selectById(userId);
            if (user != null) {
                user.setAvatar(avatarUrl);
                userMapper.updateById(user);
            }

            return avatarUrl;
        } catch (IOException e) {
            throw new RuntimeException("头像上传失败：" + e.getMessage());
        }
    }

    @Override
    public String uploadIdCardImage(String base64Image, Long userId) {
        // TODO: 实现身份证照片上传逻辑
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        // 这里应该实现实际的文件上传逻辑，比如上传到云存储
        String imageUrl = "id_card_url"; // 临时返回
        user.setIdCardImage(imageUrl);
        userMapper.updateById(user);
        return imageUrl;
    }

    @Override
    public String uploadBusinessLicenseImage(String base64Image, Long userId) {
        // TODO: 实现营业执照照片上传逻辑
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (user.getUserType() != 1) {
            throw new RuntimeException("只有商家用户才能上传营业执照");
        }
        // 这里应该实现实际的文件上传逻辑，比如上传到云存储
        String imageUrl = "business_license_url"; // 临时返回
        user.setBusinessLicenseImage(imageUrl);
        userMapper.updateById(user);
        return imageUrl;
    }

    @Override
    public void completeProfile(Long userId, UserProfileDTO profileDTO) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证手机号唯一性
        if (!profileDTO.getPhone().equals(user.getPhone())) {
            if (userRepository.existsByPhone(profileDTO.getPhone())) {
                throw new RuntimeException("手机号已被使用");
            }
        }

        // 验证邮箱唯一性
        if (!profileDTO.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(profileDTO.getEmail())) {
                throw new RuntimeException("邮箱已被使用");
            }
        }

        // 如果是商家用户，验证商家特有字段
        if (user.getUserType() == 1) {
            // 验证身份证号
            if (profileDTO.getIdCard() == null || profileDTO.getIdCard().trim().isEmpty()) {
                throw new RuntimeException("身份证号不能为空");
            }
            if (!profileDTO.getIdCard().matches("(^\\d{15}$)|(^\\d{18}$)|(^\\d{17}(\\d|X|x)$)")) {
                throw new RuntimeException("身份证号格式不正确");
            }
            if (!profileDTO.getIdCard().equals(user.getIdCard())) {
                // 使用LambdaQueryWrapper检查身份证号唯一性
                LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
                query.eq(User::getIdCard, profileDTO.getIdCard());
                if (userMapper.selectCount(query) > 0) {
                    throw new RuntimeException("身份证号已被使用");
                }
            }

            // 验证身份证照片
            if (profileDTO.getIdCardImage() == null || profileDTO.getIdCardImage().trim().isEmpty()) {
                throw new RuntimeException("请上传身份证照片");
            }

            // 验证营业执照号
            if (profileDTO.getBusinessLicense() == null || profileDTO.getBusinessLicense().trim().isEmpty()) {
                throw new RuntimeException("营业执照号不能为空");
            }

            // 验证营业执照照片
            if (profileDTO.getBusinessLicenseImage() == null || profileDTO.getBusinessLicenseImage().trim().isEmpty()) {
                throw new RuntimeException("请上传营业执照照片");
            }
        }

        // 更新用户信息
        user.setRealName(profileDTO.getRealName());
        user.setPhone(profileDTO.getPhone());
        user.setEmail(profileDTO.getEmail());
        user.setCity(profileDTO.getCity());
        user.setGender(profileDTO.getGender());
        user.setBankAccount(profileDTO.getBankAccount());

        // 如果是商家用户，更新商家特有信息
        if (user.getUserType() == 1) {
            user.setIdCard(profileDTO.getIdCard());
            user.setIdCardImage(profileDTO.getIdCardImage());
            user.setBusinessLicense(profileDTO.getBusinessLicense());
            user.setBusinessLicenseImage(profileDTO.getBusinessLicenseImage());
        }

        // 设置审核状态为待审核
        user.setAuditStatus(0);
        user.setUpdateTime(LocalDateTime.now());

        userMapper.updateById(user);
    }

    @Override
    public String uploadImage(MultipartFile file, String type, Long userId) {
        try {
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("只能上传图片文件");
            }

            // 验证文件大小（4MB）
            if (file.getSize() > 4 * 1024 * 1024) {
                throw new RuntimeException("图片大小不能超过4MB");
            }

            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? 
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String filename = UUID.randomUUID().toString() + extension;

            // 根据类型确定存储路径
            String path;
            switch (type) {
                case "idCard":
                    path = "idcard/";
                    break;
                case "businessLicense":
                    path = "license/";
                    break;
                case "avatar":
                    path = "avatar/";
                    break;
                default:
                    throw new RuntimeException("不支持的图片类型");
            }

            // 获取项目根目录
            String projectRoot = System.getProperty("user.dir");
            // 创建上传目录
            String uploadDir = projectRoot + File.separator + "uploads" + File.separator + path;
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 保存文件
            File destFile = new File(dir, filename);
            file.transferTo(destFile);

            // 返回文件URL（使用相对路径）
            String imageUrl = "/uploads/" + path + filename;

            // 如果是头像，更新用户信息
            if ("avatar".equals(type)) {
                User user = userMapper.selectById(userId);
                if (user != null) {
                    user.setAvatar(imageUrl);
                    userMapper.updateById(user);
                }
            }

            return imageUrl;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public Class<User> getEntityClass() {
        return User.class;
    }

    @Override
    public void changePassword(String username, String currentPassword, String newPassword) {
        // 1. 根据用户名查找用户
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 2. 验证当前密码（注意：这里是明文比较，实际应用中应使用加密后的密码比较）
        if (!user.getPassword().equals(currentPassword)) {
            throw new RuntimeException("当前密码不正确");
        }

        // 3. 更新密码
        user.setPassword(newPassword); // 注意：这里直接保存明文密码，实际应用中应加密后再保存
        user.setUpdateTime(LocalDateTime.now());
        userRepository.updateById(user);

        // 4. 强制用户重新登录（可选，但推荐）
        // 在这里可能需要处理 JWT Token 的失效，例如将旧 token 加入黑名单或更新 token 的生成逻辑
        // 为了简化，这里只修改数据库密码，前端可以提示用户重新登录
    }
} 
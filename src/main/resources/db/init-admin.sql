-- 初始化管理员账号
INSERT INTO `user` (
    `username`,
    `password`,
    `real_name`,
    `phone`,
    `email`,
    `city`,
    `gender`,
    `bank_account`,
    `avatar`,
    `user_type`,
    `audit_status`,
    `business_license`,
    `role`,
    `status`
) VALUES (
    'admin',
    'admin123',  -- 注意：实际生产环境应该使用加密后的密码
    '系统管理员',
    '13800000000',
    'admin@example.com',
    '杭州',
    1,  -- 性别：男
    NULL,  -- 银行账号暂不设置
    NULL,  -- 头像暂不设置
    0,  -- 用户类型：个人用户
    1,  -- 审核状态：已审核
    NULL,  -- 营业执照：不需要
    2,  -- 角色：管理员
    1   -- 状态：正常
); 
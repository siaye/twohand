package com.secondhand.common.utils;

import org.springframework.stereotype.Component;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class PasswordEncoder {
    private static final String ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16;

    // 加密密码
    public String encode(String rawPassword) {
        try {
            // 生成随机盐值
            byte[] salt = generateSalt();
            
            // 将密码和盐值组合
            byte[] saltedPassword = combinePasswordAndSalt(rawPassword.getBytes(), salt);
            
            // 使用 SHA-256 进行哈希
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            byte[] hashedPassword = md.digest(saltedPassword);
            
            // 将盐值和哈希后的密码组合并转换为 Base64 字符串
            byte[] combined = new byte[salt.length + hashedPassword.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hashedPassword, 0, combined, salt.length, hashedPassword.length);
            
            return Base64.getEncoder().encodeToString(combined);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }

    // 验证密码
    public boolean matches(String rawPassword, String encodedPassword) {
        try {
            // 如果密码为空，直接返回 false
            if (rawPassword == null || encodedPassword == null) {
                return false;
            }

            // 解码 Base64 字符串
            byte[] combined = Base64.getDecoder().decode(encodedPassword);
            
            // 检查长度是否合法
            if (combined.length <= SALT_LENGTH) {
                return false;
            }
            
            // 提取盐值
            byte[] salt = new byte[SALT_LENGTH];
            System.arraycopy(combined, 0, salt, 0, SALT_LENGTH);
            
            // 将密码和盐值组合
            byte[] saltedPassword = combinePasswordAndSalt(rawPassword.getBytes(), salt);
            
            // 使用 SHA-256 进行哈希
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            byte[] hashedPassword = md.digest(saltedPassword);
            
            // 比较哈希值
            int hashLength = combined.length - SALT_LENGTH;
            byte[] storedHash = new byte[hashLength];
            System.arraycopy(combined, SALT_LENGTH, storedHash, 0, hashLength);
            
            return MessageDigest.isEqual(hashedPassword, storedHash);
        } catch (Exception e) {
            // 任何异常都返回 false
            return false;
        }
    }

    // 生成随机盐值
    private byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }

    // 将密码和盐值组合
    private byte[] combinePasswordAndSalt(byte[] password, byte[] salt) {
        byte[] combined = new byte[password.length + salt.length];
        System.arraycopy(password, 0, combined, 0, password.length);
        System.arraycopy(salt, 0, combined, password.length, salt.length);
        return combined;
    }
} 
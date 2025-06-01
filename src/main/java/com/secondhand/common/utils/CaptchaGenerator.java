package com.secondhand.common.utils;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.CodeGenerator;
import cn.hutool.captcha.generator.RandomGenerator;
import cn.hutool.core.util.RandomUtil;

public class CaptchaGenerator {
    
    // 验证码字符集
    private static final String CAPTCHA_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    
    /**
     * 生成验证码图片对象
     * @param width 宽度
     * @param height 高度
     * @param codeCount 验证码字符数
     * @param lineCount 干扰线数量
     * @return LineCaptcha
     */
    public static LineCaptcha create(int width, int height, int codeCount, int lineCount) {
        // 创建自定义验证码生成器
        CodeGenerator codeGenerator = new RandomGenerator(CAPTCHA_CHARS, codeCount);
        
        // 创建验证码对象
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(width, height, codeCount, lineCount);
        captcha.setGenerator(codeGenerator);
        
        return captcha;
    }
} 
package com.secondhand.common.utils;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;

public class CaptchaGenerator {
    /**
     * 生成验证码图片对象
     * @param width 宽度
     * @param height 高度
     * @param codeCount 验证码字符数
     * @param lineCount 干扰线数量
     * @return LineCaptcha
     */
    public static LineCaptcha create(int width, int height, int codeCount, int lineCount) {
        return CaptchaUtil.createLineCaptcha(width, height, codeCount, lineCount);
    }
} 
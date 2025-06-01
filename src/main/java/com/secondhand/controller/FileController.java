package com.secondhand.controller;

import com.secondhand.model.vo.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class FileController {
    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    @Value("${file.upload-dir}")
    private String uploadDir;

    @PostMapping("/upload")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择文件");
        }

        try {
            // 获取文件后缀
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            
            // 生成新的文件名
            String newFilename = UUID.randomUUID().toString() + suffix;
            
            // 根据文件类型选择保存目录
            String saveDir;
            if (file.getContentType().startsWith("image/")) {
                // 如果是图片，保存到 products 目录
                saveDir = uploadDir + "products/";
            } else {
                return Result.error("不支持的文件类型");
            }

            // 确保目录存在
            File dir = new File(saveDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 保存文件
            File dest = new File(saveDir + newFilename);
            log.info("开始保存文件：{}，大小：{} bytes", dest.getAbsolutePath(), file.getSize());
            file.transferTo(dest);
            log.info("文件保存成功");

            // 返回文件访问路径
            String fileUrl = "/uploads/products/" + newFilename;
            return Result.success(fileUrl);
        } catch (IOException e) {
            log.error("文件上传失败：", e);
            return Result.error("文件上传失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("未知错误：", e);
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }
} 
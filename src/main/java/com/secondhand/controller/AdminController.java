package com.secondhand.controller;

import com.secondhand.common.response.Result;
import com.secondhand.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * 获取仪表盘数据
     */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardData() {
        try {
            Map<String, Object> data = adminService.getDashboardData();
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取仪表盘数据失败", e);
            return Result.error("获取仪表盘数据失败");
        }
    }
} 
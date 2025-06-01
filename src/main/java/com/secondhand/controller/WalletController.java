package com.secondhand.controller;

import com.secondhand.common.response.Result;
import com.secondhand.model.entity.Wallet;
import com.secondhand.service.WalletService;
import com.secondhand.common.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;
import com.secondhand.model.vo.TransactionVO;
import com.secondhand.service.TransactionService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TransactionService transactionService;

    /**
     * 获取用户钱包余额
     * 需要登录认证
     * @param token JWT Token
     * @return 钱包余额
     */
    @GetMapping("/balance")
    public Result<BigDecimal> getBalance(@RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
            Wallet wallet = walletService.getWalletByUserId(userId);
            if (wallet == null) {
                // 如果用户没有钱包（理论上注册时应该初始化），可以考虑创建或者返回错误
                // 这里暂时返回错误，需要确保注册时钱包初始化成功
                return Result.error("用户钱包不存在");
            }
            return Result.success(wallet.getBalance());
        } catch (Exception e) {
            return Result.error("获取钱包余额失败: " + e.getMessage());
        }
    }

    /**
     * 用户充值
     * 需要登录认证
     * @param token JWT Token
     * @param request 充值请求体，包含充值金额 amount
     * @return 更新后的钱包余额
     */
    @PostMapping("/recharge")
    public Result<BigDecimal> recharge(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> request) {
        try {
            Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
            BigDecimal amount = new BigDecimal(request.get("amount").toString());

            // 校验充值金额是否合法（大于0）
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return Result.error("充值金额必须大于0");
            }

            Wallet updatedWallet = walletService.updateBalance(userId, amount);
            // TODO: 记录充值交易流水
            return Result.success(updatedWallet.getBalance());
        } catch (NumberFormatException e) {
            return Result.error("充值金额格式错误");
        } catch (Exception e) {
            return Result.error("充值失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户交易记录
     * 需要登录认证
     * @param token JWT Token
     * @param page 页码
     * @param size 每页数量
     * @param keyword 关键词 (可选，用于搜索)
     * @param type 交易类型 (可选，用于筛选)
     * @return 交易记录列表 (分页)
     */
    @GetMapping("/transactions")
    public Result<?> getTransactions(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer type
    ) {
        try {
            Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
            Page<TransactionVO> transactions = transactionService.getTransactionsByUserId(userId, page, size, keyword, type);
            return Result.success(transactions);
        } catch (Exception e) {
            return Result.error("获取交易记录失败: " + e.getMessage());
        }
    }
} 
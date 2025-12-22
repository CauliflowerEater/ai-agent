package com.shawn.aiagent.api.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查接口控制器
 * 提供应用健康状态检查
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    /**
     * Intent: 健康检查接口
     * Input: 无
     * Output: String ("ok")
     * SideEffects: 无
     * Failure: 无
     * Idempotency: 幂等
     */
    @GetMapping
    public String healthCheck() {
        return "ok";
    }
}


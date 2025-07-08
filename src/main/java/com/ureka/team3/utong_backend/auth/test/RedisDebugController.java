package com.ureka.team3.utong_backend.auth.test;

import java.util.Map;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ureka.team3.utong_backend.common.dto.ApiResponse;

@RestController
@RequestMapping("/debug/redis")
public class RedisDebugController {
    
    private final RedisDebugService redisDebugService;
    
    public RedisDebugController(RedisDebugService redisDebugService) {
        this.redisDebugService = redisDebugService;
    }
    
    @GetMapping("/keys")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllKeys() {
        return ResponseEntity.ok(redisDebugService.getAllKeys());
    }
    
    @GetMapping("/keys/detailed")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDetailedKeys() {
        return ResponseEntity.ok(redisDebugService.getDetailedKeys());
    }
    
    @GetMapping("/refresh-tokens")
    public ResponseEntity<ApiResponse<Set<String>>> getRefreshTokenKeys() {
        return ResponseEntity.ok(redisDebugService.getRefreshTokenKeys());
    }
    
    @GetMapping("/blacklist")
    public ResponseEntity<ApiResponse<Set<String>>> getBlacklistKeys() {
        return ResponseEntity.ok(redisDebugService.getBlacklistKeys());
    }
    
    @GetMapping("/get/{key}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getKeyInfo(@PathVariable String key) {
        return ResponseEntity.ok(redisDebugService.getKeyInfo(key));
    }
    
    @GetMapping("/refresh-token/{accountId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRefreshToken(@PathVariable String accountId) {
        return ResponseEntity.ok(redisDebugService.getRefreshToken(accountId));
    }
    
    @DeleteMapping("/delete/{key}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteKey(@PathVariable String key) {
        return ResponseEntity.ok(redisDebugService.deleteKey(key));
    }
}
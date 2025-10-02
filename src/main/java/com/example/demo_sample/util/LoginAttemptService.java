package com.example.demo_sample.util;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginAttemptService {

    private final int MAX_ATTEMPTS = 3;
    private final long LOCK_TIME_MS = 1 * 60 * 1000; // 1 phút

    private final Map<String, Attempt> attemptsCache = new ConcurrentHashMap<>();

    public void loginSucceeded(String username) {
        attemptsCache.remove(username);
    } //khi đăng nhâp thành công sẽ reset login thất bại

    //ghi lại số lâ nhập sai
    public void loginFailed(String username) {
        Attempt attempt = attemptsCache.getOrDefault(username, new Attempt(0, 0));
        attempt.count++;
        attempt.lastAttempt = Instant.now().toEpochMilli();
        attemptsCache.put(username, attempt);
    }

    public boolean isBlocked(String username) {
        Attempt attempt = attemptsCache.get(username);
        if (attempt == null) return false;

        if (attempt.count >= MAX_ATTEMPTS) {
            //tính tgian bị khoá đã đủ chưa
            long elapsed = Instant.now().toEpochMilli() - attempt.lastAttempt;
            if (elapsed < LOCK_TIME_MS) {
                return true;
            } else {
                // Reset sau khi hết thời gian khóa
                attemptsCache.remove(username);
                return false;
            }
        }
        return false;
    }

    private static class Attempt {
        int count; //số lần đăng nhập thất bại
        long lastAttempt; // thời điểm lần login cuôí cùng
        Attempt(int count, long lastAttempt) {
            this.count = count;
            this.lastAttempt = lastAttempt;
        }
    }
}

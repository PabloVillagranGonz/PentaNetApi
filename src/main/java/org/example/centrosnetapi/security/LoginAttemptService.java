package org.example.centrosnetapi.security;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPT = 5;
    private static final long BLOCK_DURATION_MS = TimeUnit.MINUTES.toMillis(15);

    // Caché en memoria: clave = IP, valor = intentos
    private final ConcurrentHashMap<String, Integer> attemptsCache = new ConcurrentHashMap<>();
    // Caché en memoria: clave = IP, valor = tiempo de desbloqueo (timestamp)
    private final ConcurrentHashMap<String, Long> blockTimeCache = new ConcurrentHashMap<>();

    public void loginSucceeded(String key) {
        attemptsCache.remove(key);
        blockTimeCache.remove(key);
    }

    public void loginFailed(String key) {
        int attempts = attemptsCache.getOrDefault(key, 0);
        attempts++;
        attemptsCache.put(key, attempts);
        if (attempts >= MAX_ATTEMPT) {
            blockTimeCache.put(key, System.currentTimeMillis() + BLOCK_DURATION_MS);
        }
    }

    public boolean isBlocked(String key) {
        if (blockTimeCache.containsKey(key)) {
            long blockedUntil = blockTimeCache.get(key);
            if (System.currentTimeMillis() > blockedUntil) {
                // El bloqueo expiró
                blockTimeCache.remove(key);
                attemptsCache.remove(key);
                return false;
            }
            return true;
        }
        return false;
    }
}

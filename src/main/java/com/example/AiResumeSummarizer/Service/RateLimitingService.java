package com.example.AiResumeSummarizer.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class RateLimitingService {
  // Store request counts for each user
  private final Map<String, UserRequestCount> userRequests = new ConcurrentHashMap<>();

  // Limit: 10 requests per minute per user
  private static final int MAX_REQUESTS = 10;
  private static final long TIME_WINDOW_MS = 60 * 1000; // 1 minute

  public boolean tryConsume(String clientId) {
    UserRequestCount counter = userRequests.computeIfAbsent(clientId, k -> new UserRequestCount());

    return counter.tryConsume();
  }

  public long getRemainingTokens(String clientId) {
    UserRequestCount counter = userRequests.get(clientId);
    if (counter == null) return MAX_REQUESTS;
    return counter.getRemaining();
  }

  // Inner class to track requests per user
  private static class UserRequestCount {
    private int count = 0;
    private long windowStart = System.currentTimeMillis();

    public synchronized boolean tryConsume() {
      long now = System.currentTimeMillis();

      // Reset if time window has passed
      if (now - windowStart > TIME_WINDOW_MS) {
        count = 0;
        windowStart = now;
      }

      // Check if under limit
      if (count < MAX_REQUESTS) {
        count++;
        return true;
      }

      return false; // Rate limit exceeded
    }

    public long getRemaining() {
      long now = System.currentTimeMillis();
      if (now - windowStart > TIME_WINDOW_MS) {
        return MAX_REQUESTS;
      }
      return Math.max(0, MAX_REQUESTS - count);
    }
  }
}

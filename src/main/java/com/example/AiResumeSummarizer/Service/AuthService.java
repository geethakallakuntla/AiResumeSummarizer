package com.example.AiResumeSummarizer.Service;

public class AuthService {
  // For demo purposes - in production, use proper JWT validation
  public String extractUsername(String token) {
    // Simple extraction for demo
    // In production, you'd validate JWT properly
    try {
      // This is just a placeholder - implement proper JWT validation
      return "user";
    } catch (Exception e) {
      return null;
    }
  }
}

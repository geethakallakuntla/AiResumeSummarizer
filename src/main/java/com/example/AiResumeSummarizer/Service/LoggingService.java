package com.example.AiResumeSummarizer.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LoggingService {

  // Create loggers for different parts of your app
  private static final Logger appLogger = LoggerFactory.getLogger("APPLICATION");
  private static final Logger errorLogger = LoggerFactory.getLogger("ERROR");
  private static final Logger performanceLogger = LoggerFactory.getLogger("PERFORMANCE");

  public void logInfo(String message) {
    appLogger.info(" INFO: {}", message);
  }

  public void logError(String message, Exception e) {
    errorLogger.error(" ERROR: {} - {}", message, e.getMessage(), e);
  }

  public void logWarning(String message) {
    appLogger.warn(" WARNING: {}", message);
  }

  public void logPerformance(String operation, long timeMs) {
    performanceLogger.info("⏱ PERFORMANCE: {} took {} ms", operation, timeMs);
  }

  public void logResumeProcessing(String filename, String status) {
    appLogger.info(" RESUME: {} - Status: {}", filename, status);
  }

  public void logApiCall(String endpoint, String method, String ipAddress) {
    appLogger.info(" API CALL: {} {} from IP: {}", method, endpoint, ipAddress);
  }
}

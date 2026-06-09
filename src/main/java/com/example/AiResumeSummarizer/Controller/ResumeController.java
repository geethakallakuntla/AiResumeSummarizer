package com.example.AiResumeSummarizer.Controller;

import com.example.AiResumeSummarizer.DTOs.TaskStatus;
import com.example.AiResumeSummarizer.Service.AiService;
import com.example.AiResumeSummarizer.Service.FileTextExtractorService;
import com.example.AiResumeSummarizer.Service.LoggingService;
import com.example.AiResumeSummarizer.Service.MetricsService;
import com.example.AiResumeSummarizer.Service.RateLimitingService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/*----------This is the MAIN CONTROLLER, that handles resume summarization for end Users--------- */
@RestController
@RequestMapping("/api/resume")
@CrossOrigin(origins = "*") // ← ADD THIS LINE
/* POST http://localhost:8080/api/resume/summarize
Content-Type: application/json
"Paste your resume text here..." */
public class ResumeController {

  private static final Logger logger = LoggerFactory.getLogger(ResumeController.class);

  @Autowired private AiService aiService;

  @Autowired private FileTextExtractorService fileTextExtractorService;

  // NEW: Services for production features
  @Autowired private LoggingService loggingService;

  @Autowired private MetricsService metricsService;

  @Autowired private RateLimitingService rateLimitingService;

  // Store async tasks (in production, use database)
  private final ConcurrentHashMap<String, TaskStatus> asyncTasks = new ConcurrentHashMap<>();

  /* ================================================
  EXISTING ENDPOINT 1: Summarize raw Resume Text (WITH ENHANCEMENTS)
  ================================================ */
  @PostMapping("/summarize")
  public ResponseEntity<Map<String, Object>> summarizeResume(
      @RequestBody String resumeText, HttpServletRequest request) {

    Map<String, Object> response = new HashMap<>();
    long startTime = System.currentTimeMillis();

    try {
      // 1. LOGGING: Log the request
      String clientId = getClientId(request);
      loggingService.logApiCall("/api/resume/summarize", "POST", clientId);
      loggingService.logInfo("Summarize endpoint hit. Resume text length: " + resumeText.length());

      // 2. RATE LIMITING: Check if user can make request
      if (!rateLimitingService.tryConsume(clientId)) {
        loggingService.logWarning("Rate limit exceeded for: " + clientId);
        response.put("error", "Rate limit exceeded. Please try again later.");
        response.put("retryAfter", "60 seconds");
        return ResponseEntity.status(429).body(response);
      }

      // 3. METRICS: Record API call
      metricsService.recordApiCall();

      // 4. VALIDATION: Check input
      if (resumeText == null || resumeText.trim().isEmpty()) {
        loggingService.logWarning("Empty resume text received");
        response.put("error", "Resume text cannot be empty");
        response.put("message", "Please provide resume content");
        return ResponseEntity.badRequest().body(response);
      }

      if (resumeText.length() > 50000) {
        loggingService.logWarning("Resume text too long: " + resumeText.length());
        response.put("error", "Resume text too long");
        response.put("message", "Maximum 50,000 characters allowed");
        return ResponseEntity.badRequest().body(response);
      }

      // 5. PROCESS: Generate summary with metrics tracking
      String summary =
          metricsService.recordProcessingTime(() -> aiService.summarizeResumeWithAI(resumeText));

      // 6. RECORD SUCCESS METRICS
      metricsService.recordSuccessfulResume();
      long processingTime = System.currentTimeMillis() - startTime;
      loggingService.logPerformance("Resume summarization", processingTime);

      // 7. RETURN RESPONSE
      response.put("summary", summary);
      response.put("length", summary.length());
      response.put("message", "Resume summarized successfully");
      response.put("processingTimeMs", processingTime);

      logger.info("Summarization completed in {} ms", processingTime);
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      // ERROR HANDLING: Log and return graceful error
      metricsService.recordFailedResume();
      loggingService.logError("Error summarizing resume", e);

      response.put("error", "Summarization failed");
      response.put("message", e.getMessage());
      response.put("timestamp", LocalDateTime.now());
      return ResponseEntity.status(500).body(response);
    }
  }

  /* ================================================
  EXISTING ENDPOINT 2: Upload and Summarize Resume File (SYNC - WITH ENHANCEMENTS)
  ================================================ */
  @PostMapping("/upload-and-summarize")
  public ResponseEntity<Map<String, Object>> uploadAndSummarizeResume(
      @RequestParam("file") MultipartFile file, HttpServletRequest request) {

    Map<String, Object> response = new HashMap<>();
    long startTime = System.currentTimeMillis();

    try {
      // 1. LOGGING & RATE LIMITING
      String clientId = getClientId(request);
      loggingService.logApiCall("/api/resume/upload-and-summarize", "POST", clientId);

      if (!rateLimitingService.tryConsume(clientId)) {
        response.put("error", "Rate limit exceeded");
        return ResponseEntity.status(429).body(response);
      }

      metricsService.recordApiCall();

      // 2. FILE VALIDATION
      if (file.isEmpty()) {
        loggingService.logWarning("Empty file upload attempted");
        response.put("error", "File is empty");
        response.put("message", "Please select a file to upload");
        return ResponseEntity.badRequest().body(response);
      }

      // 3. FILE SIZE VALIDATION (Max 5MB)
      if (file.getSize() > 5 * 1024 * 1024) {
        loggingService.logWarning("File too large: " + file.getSize());
        response.put("error", "File too large");
        response.put("message", "Maximum file size is 5MB");
        return ResponseEntity.badRequest().body(response);
      }

      // 4. FILE TYPE VALIDATION
      String contentType = file.getContentType();
      if (!isValidFileType(contentType)) {
        loggingService.logWarning("Invalid file type: " + contentType);
        response.put("error", "Invalid file type");
        response.put("message", "Only PDF, DOCX, and TXT files are allowed");
        return ResponseEntity.badRequest().body(response);
      }

      logger.info(
          "File uploaded: {} ({} bytes, {})",
          file.getOriginalFilename(),
          file.getSize(),
          contentType);

      // 5. PROCESS WITH TIMING
      String extractedText =
          metricsService.recordProcessingTime(
              () -> fileTextExtractorService.extractTextFromFile(file));

      if (extractedText.trim().isEmpty()) {
        response.put("error", "No text could be extracted");
        response.put("message", "The file might be empty or corrupted");
        return ResponseEntity.badRequest().body(response);
      }

      String summary =
          metricsService.recordProcessingTime(() -> aiService.summarizeResumeWithAI(extractedText));

      // 6. SUCCESS METRICS
      metricsService.recordSuccessfulResume();
      long processingTime = System.currentTimeMillis() - startTime;
      loggingService.logPerformance("File upload & summarization", processingTime);
      loggingService.logResumeProcessing(file.getOriginalFilename(), "SUCCESS");

      // 7. RETURN RESPONSE
      response.put("fileName", file.getOriginalFilename());
      response.put("summary", summary);
      response.put("length", summary.length());
      response.put("message", "Resume uploaded and summarized successfully");
      response.put("processingTimeMs", processingTime);
      response.put("fileSize", file.getSize());

      logger.info("File summarization completed in {} ms", processingTime);
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      metricsService.recordFailedResume();
      loggingService.logError("Error processing uploaded file: " + file.getOriginalFilename(), e);

      response.put("error", "Upload and summarization failed");
      response.put("message", e.getMessage());
      response.put("timestamp", LocalDateTime.now());
      return ResponseEntity.status(500).body(response);
    }
  }

  /* ================================================
  NEW ENDPOINT 3: Async Upload and Summarize (NO WAITING)
  ================================================ */
  @PostMapping("/async-upload")
  public ResponseEntity<Map<String, String>> asyncUploadAndSummarize(
      @RequestParam("file") MultipartFile file, HttpServletRequest request) {

    String clientId = getClientId(request);

    // Rate limiting check
    if (!rateLimitingService.tryConsume(clientId)) {
      return ResponseEntity.status(429)
          .body(Map.of("error", "Rate limit exceeded. Please try again later."));
    }

    // Generate task ID
    String taskId = UUID.randomUUID().toString();

    // Create task status
    TaskStatus status = new TaskStatus();
    status.setTaskId(taskId);
    status.setStatus("PENDING");
    status.setCreatedAt(LocalDateTime.now());
    asyncTasks.put(taskId, status);

    // Process asynchronously
    processResumeAsync(file, taskId, clientId);

    loggingService.logInfo("Async task created: " + taskId);

    return ResponseEntity.accepted()
        .body(
            Map.of(
                "taskId",
                taskId,
                "status",
                "PENDING",
                "message",
                "Your resume is being processed. Use /api/resume/status/"
                    + taskId
                    + " to check progress"));
  }

  /* ================================================
  NEW ENDPOINT 4: Check Async Task Status
  ================================================ */
  @GetMapping("/status/{taskId}")
  public ResponseEntity<TaskStatus> getTaskStatus(@PathVariable String taskId) {
    TaskStatus status = asyncTasks.get(taskId);

    if (status == null) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(status);
  }

  /* ================================================
  NEW ENDPOINT 5: Get Metrics (Admin only)
  ================================================ */
  @GetMapping("/metrics")
  public ResponseEntity<Map<String, Object>> getMetrics(HttpServletRequest request) {
    // Check if user is admin (you can implement proper auth)
    String clientId = getClientId(request);

    if (!isAdmin(clientId)) {
      return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin only."));
    }

    Map<String, Object> metrics = new HashMap<>();
    metrics.put("successfulResumes", metricsService.getSuccessRate());
    metrics.put("failedResumes", metricsService.getFailedCount());
    metrics.put("successRate", metricsService.getSuccessRate() + "%");
    metrics.put("totalApiCalls", metricsService.getApiCallCount());

    return ResponseEntity.ok(metrics);
  }

  /*isAdmin() method goes OUTSIDE the getMetrics() method*/
  private boolean isAdmin(String clientId) {
    return true; // For development - allow all access
  }

  /* ================================================
  NEW ENDPOINT 6: Health Check
  ================================================ */
  @GetMapping("/health")
  public ResponseEntity<Map<String, Object>> healthCheck() {
    Map<String, Object> health = new HashMap<>();
    health.put("status", "UP");
    health.put("timestamp", LocalDateTime.now());
    health.put("service", "Resume Summarizer");
    health.put("version", "2.0.0");

    // Check if services are working
    try {
      health.put("ollamaStatus", "CONNECTED");
      health.put("rateLimitingActive", true);
      health.put("asyncProcessingActive", true);
    } catch (Exception e) {
      health.put("ollamaStatus", "ERROR");
    }

    return ResponseEntity.ok(health);
  }

  /* ================================================
  PRIVATE HELPER METHODS
  ================================================ */

  private void processResumeAsync(MultipartFile file, String taskId, String clientId) {
    CompletableFuture.runAsync(
        () -> {
          try {
            // Update status to PROCESSING
            TaskStatus status = asyncTasks.get(taskId);
            status.setStatus("PROCESSING");
            status.setProgressPercentage(20);

            // Extract text
            status.setProgressPercentage(40);
            String extractedText = fileTextExtractorService.extractTextFromFile(file);

            // Generate summary
            status.setProgressPercentage(70);
            String summary = aiService.summarizeResumeWithAI(extractedText);

            // Complete
            status.setProgressPercentage(100);
            status.setStatus("COMPLETED");
            status.setResult(summary);
            status.setUpdatedAt(LocalDateTime.now());

            metricsService.recordSuccessfulResume();
            loggingService.logResumeProcessing(file.getOriginalFilename(), "ASYNC_SUCCESS");

          } catch (Exception e) {
            TaskStatus status = asyncTasks.get(taskId);
            status.setStatus("FAILED");
            status.setErrorMessage(e.getMessage());
            status.setUpdatedAt(LocalDateTime.now());

            metricsService.recordFailedResume();
            loggingService.logError("Async processing failed for task: " + taskId, e);
          }
        });
  }

  private String getClientId(HttpServletRequest request) {
    // Try to get authenticated user
    /*  String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String token = authHeader.substring(7);
      try {
        String username = jwtUtil.extractUsername(token);
        if (username != null) {
          return username;
        }
      } catch (Exception e) {
        // Token invalid, fallback to IP
      }
    }*/

    // Fallback to IP address
    String ip = request.getHeader("X-Forwarded-For");
    if (ip == null) {
      ip = request.getRemoteAddr();
    }
    return ip;
  }

  private boolean isValidFileType(String contentType) {
    return contentType != null
        && (contentType.equals("application/pdf")
            || contentType.equals(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
            || contentType.equals("text/plain"));
  }

  /*  private boolean isAdmin(String clientId) {
    // Implement admin check based on your auth system
    return clientId.equals("admin") || clientId.startsWith("admin@");
  }*/
}

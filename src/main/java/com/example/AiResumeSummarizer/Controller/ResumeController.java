package com.example.AiResumeSummarizer.Controller;

import com.example.AiResumeSummarizer.Service.AiService;
import com.example.AiResumeSummarizer.Service.FileTextExtractorService;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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
  @Autowired // ← ADD THIS LINE HERE FIRST
  private FileTextExtractorService fileTextExtractorService;

  /*Endpoint: Summarize raw  Resume Text*/

  @PostMapping("/summarize")
  public ResponseEntity<Map<String, Object>> summarizeResume(@RequestBody String resumeText) {
    Map<String, Object> response = new HashMap<>();
    try {
      logger.info("Summarize endpoint hit. Resume text length: {}", resumeText.length());

      String summary = aiService.summarizeResumeWithAI(resumeText);

      response.put("summary", summary);
      response.put("length", summary.length());
      response.put("message", "Resume summarized successfully");

      logger.info("Summarization completed. Summary length: {}", summary.length());
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      logger.error("Error summarizing resume", e);
      response.put("error", "Summarization failed");
      response.put("message", e.getMessage());
      return ResponseEntity.status(500).body(response);
    }
  }

  /* ================================================
  ENDPOINT 2: Upload and Summarize Resume File (NEW!)
  Accepts: Multipart file (PDF, DOCX, TXT)
  ================================================ */
  @PostMapping("/upload-and-summarize")
  /*POST http://localhost:8080/api/resume/upload-file
  Content-Type: multipart/form-data
  Key: file   Value: [Choose your PDF/DOCX/TXT file] */
  public ResponseEntity<Map<String, Object>> uploadAndSummarizeResume(
      @RequestParam("file") MultipartFile file) {
    Map<String, Object> response = new HashMap<>();
    try {
      if (file.isEmpty()) {
        response.put("error", "File is empty");
        response.put("message", "Please select a file to upload");
        return ResponseEntity.badRequest().body(response);
      }

      logger.info(
          "File uploaded: {} ({} bytes, {})",
          file.getOriginalFilename(),
          file.getSize(),
          file.getContentType());

      // Extract text
      String extractedText = fileTextExtractorService.extractTextFromFile(file);

      if (extractedText.trim().isEmpty()) {
        response.put("error", "No text could be extracted");
        response.put("message", "The file might be empty or corrupted");
        return ResponseEntity.badRequest().body(response);
      }

      // Summarize
      String summary = aiService.summarizeResumeWithAI(extractedText);

      response.put("fileName", file.getOriginalFilename());
      response.put("summary", summary);
      response.put("length", summary.length());
      response.put("message", "Resume uploaded and summarized successfully");

      logger.info("File summarization completed: {} characters", summary.length());
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      logger.error("Error processing uploaded file", e);
      response.put("error", "Upload and summarization failed");
      response.put("message", e.getMessage());
      return ResponseEntity.status(500).body(response);
    }
  }
}

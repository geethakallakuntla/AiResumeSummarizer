package com.example.AiResumeSummarizer;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.example.AiResumeSummarizer.Service.AiService;
import java.util.HashMap;
import java.util.Map;

public class ResumeLambdaHandler
    implements RequestHandler<Map<String, String>, Map<String, Object>> {

  private final AiService aiService;

  // Constructor - manually create AiService (no Spring in Lambda)
  public ResumeLambdaHandler() {
    this.aiService = new AiService();
  }

  @Override
  public Map<String, Object> handleRequest(Map<String, String> input, Context context) {
    Map<String, Object> response = new HashMap<>();

    try {
      // Get resume text from input
      String resumeText = input.get("resumeText");

      if (resumeText == null || resumeText.isEmpty()) {
        response.put("error", "No resume text provided");
        return response;
      }

      // Call AiService to summarize
      String summary = aiService.summarizeResumeWithAI(resumeText);

      response.put("summary", summary);
      response.put("length", summary.length());
      response.put("message", "Resume summarized successfully");

    } catch (Exception e) {
      response.put("error", "Summarization failed");
      response.put("message", e.getMessage());
    }

    return response;
  }
}

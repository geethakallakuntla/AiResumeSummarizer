package com.example.AiResumeSummarizer.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class AiService {

  /*WebClient setup for Ollama Configuration */
  private final WebClient webClient =
      WebClient.create("http://localhost:11434"); /*Ollama runs locally on port 11434. */
  private final ObjectMapper objectMapper =
      new ObjectMapper(); /*ObjectMapper converts JSON to Java Objects so that we can work with data cleanly */

  /*Creates intelligent prompt for AI analysis
   * Calls Ollama API with proper JSON Strcucture`
   * Extracts and cleans response text
   */
  public String summarizeResumeWithAI(String resumeText) {
    String prompt =
        "Create a CONCISE resume summary in exactly this format (max 300 words total):\n\n"
            + "QUALIFICATION:\n"
            + "[Current job title] with [total years] years of experience in [field]\n\n"
            + "SKILLS:\n"
            + "- [Skill 1]\n"
            + "- [Skill 2]\n"
            + "- [Skill 3]\n"
            + "- [Skill 4]\n"
            + "- [Skill 5]\n\n"
            + "EXPERIENCE (Last 2 companies):\n"
            + "1. [Most recent company]: [Role] ([Dates])\n"
            + "   - Key achievement with metric\n"
            + "   - Key achievement with metric\n"
            + "2. [Previous company]: [Role] ([Dates])\n"
            + "   - Key achievement with metric\n"
            + "   - Key achievement with metric\n\n"
            + "EDUCATION:\n"
            + "[Degree] in [Major], [University] ([Year])\n\n"
            + "CERTIFICATIONS:\n"
            + "- [Certification 1]\n"
            + "- [Certification 2]\n\n"
            + "IMPORTANT RULES:\n"
            + "1. MAXIMUM 100 WORDS TOTAL\n"
            + "2. Include NUMBERS/METRICS for achievements\n"
            + "3. Use ONLY the most recent/important information\n"
            + "4. Keep it VERY concise\n"
            + "5. No contact information\n"
            + "6. No full sentences, just bullet points\n\n"
            + "Resume to summarize:\n"
            + resumeText;

    try {
      // Create proper Ollama request
      Map<String, Object> requestBody =
          Map.of(
              "model", /*Java objects model, role, content */
              "llama3", // Use exact model name you downloaded
              "messages",
              List.of(Map.of("role", "user", "content", prompt)),
              "stream",
              false);

      String response =
          webClient
              .post()
              .uri("/api/chat") // Use /api/chat endpoint
              .bodyValue(requestBody) // Use Map instead of formatted string
              .retrieve()
              .bodyToMono(String.class)
              .block();

      return extractCleanText(response);
    } catch (Exception e) {
      return "Error calling Ollama API: " + e.getMessage();
    }
  }

  private String extractCleanText(String jsonResponse) {
    try {
      // Parse JSON and get only the content
      JsonNode root = objectMapper.readTree(jsonResponse);
      String content = root.path("message").path("content").asText();

      // Clean up any JSON escape characters
      return content
          .replace("\\n", "\n") // Convert \n to actual newlines
          .replace("\\\"", "\""); // Convert \" to actual quotes

    } catch (Exception e) {
      return "Failed to parse AI response: " + jsonResponse;
    }
  }
}

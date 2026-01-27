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
   * Calls Ollama API with proper JSON Strcucture
   * Extrcats and cleans response text
   */
  public String summarizeResumeWithAI(String resumeText) {
    String prompt =
        "You are an expert resume summarizer.Your task is to summarize the following resume into"
            + " **one page**(around 500-600),keeping all key information intact.Organize the"
            + " summary into clear sections: **Skills, Experience, Education, and Achievements**."
            + " Use bullet points where appropriate, and keep each section concise. Do not add any"
            + " information that is not present in the resume."
            + "\nResume:\n"
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

package com.example.AiResumeSummarizer.Test;

import static org.junit.jupiter.api.Assertions.*;

import com.example.AiResumeSummarizer.Service.AiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AiServiceTest {

  @Autowired private AiService aiService;

  @Test
  void testSummarizeResume() {
    // Sample resume text
    String resumeText =
        "John has 5 years of experience in JavaScript, Angular, React, and Spring Boot.";

    // Call the summarizeResume method
    String summary = aiService.summarizeResumeWithAI(resumeText);

    // Check if the summary is not null
    assertNotNull(summary);

    // Check if the summary contains expected keyword
    assertTrue(summary.contains("Java") || summary.contains("Spring"));
  }
}

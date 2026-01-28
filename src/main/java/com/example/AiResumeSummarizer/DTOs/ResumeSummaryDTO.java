package com.example.AiResumeSummarizer.DTOs;

import java.util.List;
import lombok.Data;

@Data
public class ResumeSummaryDTO {
  private String filename;
  private String summarizeResumeWithAI;
  private String professionalTitle;

  private String experience;
  private String education;
  private String skills;
  private String achievements;
  private List<String> Certifications;
}

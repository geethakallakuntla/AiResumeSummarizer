package com.example.AiResumeSummarizer.DTOs;

import java.time.LocalDateTime;

public class TaskStatus {
  private String taskId;
  private String status; // PENDING, PROCESSING, COMPLETED, FAILED
  private String result;
  private String errorMessage;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private int progressPercentage;

  public TaskStatus() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
    this.progressPercentage = 0;
  }

  // Getters and Setters
  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getResult() {
    return result;
  }

  public void setResult(String result) {
    this.result = result;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public int getProgressPercentage() {
    return progressPercentage;
  }

  public void setProgressPercentage(int progressPercentage) {
    this.progressPercentage = progressPercentage;
  }
}

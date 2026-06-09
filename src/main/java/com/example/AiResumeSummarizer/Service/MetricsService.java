package com.example.AiResumeSummarizer.Service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

/*This file is used for monitoring service */
@Service
public class MetricsService {

  private final Counter resumesProcessedCounter;
  private final Counter resumesFailedCounter;
  private final Timer processingTimer;
  private final Counter apiCallsCounter;

  public MetricsService(MeterRegistry meterRegistry) {
    this.resumesProcessedCounter =
        Counter.builder("resumes.processed.total")
            .description("Total number of resumes successfully processed")
            .register(meterRegistry);

    this.resumesFailedCounter =
        Counter.builder("resumes.failed.total")
            .description("Total number of resume processing failures")
            .register(meterRegistry);

    this.processingTimer =
        Timer.builder("resume.processing.duration")
            .description("Time taken to process a resume")
            .register(meterRegistry);

    this.apiCallsCounter =
        Counter.builder("api.calls.total")
            .description("Total API calls")
            .tag("application", "resume-summarizer")
            .register(meterRegistry);
  }

  public void recordSuccessfulResume() {
    resumesProcessedCounter.increment();
  }

  public void recordFailedResume() {
    resumesFailedCounter.increment();
  }

  public <T> T recordProcessingTime(java.util.concurrent.Callable<T> callable) throws Exception {
    return processingTimer.recordCallable(callable);
  }

  public void recordApiCall() {
    apiCallsCounter.increment();
  }

  public double getSuccessfulCount() {
    return resumesProcessedCounter.count();
  }

  public double getFailedCount() {
    return resumesFailedCounter.count();
  }

  public double getApiCallCount() {
    return apiCallsCounter.count();
  }

  public double getSuccessRate() {
    double total = resumesProcessedCounter.count() + resumesFailedCounter.count();
    if (total == 0) return 100.0;
    return (resumesProcessedCounter.count() / total) * 100;
  }
}

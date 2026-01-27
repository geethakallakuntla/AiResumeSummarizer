
##  AI Resume Summarizer (Spring Boot + Ollama)
## Project Overview
An AI-powered Resume Summarizer that automatically extracts resume content and generates a professional summary using a local Large Language Model (Ollama).
This project focuses on converting raw resume data into a concise, human-readable summary without using any external APIs.
## Key features
1.Upload resume files (PDF, DOCX, TXT)
2.Extract clean text using Apache Tika
3.Generate AI-based professional resume summary
4.Identify key skills and experience highlights
5.Uses local LLM (Ollama) – no API keys, no cloud cost
6.REST API built with Spring Boot
7.Docker-ready for deployment

## Frontend (Basic UI)
- Simple frontend built using HTML, CSS, and JavaScript
- Allows users to upload a resume file
- Sends resume to Spring Boot REST API
- Displays AI-generated resume summary in the browser
- Used for demo and testing purposes
 
 ## Tech Stack & Tools Used -Backend 
 -Java 17 
 -Spring Boot 3 
 -Spring Web(REST API)
 -Apache Tika → Extract text from PDF, DOCX, TXT -REST API 
 
 ## AI / LLM Integration
-LLM Server: Ollama (local
-Model Used: llama3:latest
LLM Responsibilities:
1.Resume summarization
2.Professional profile generation
3.Key skills & experience highlighting
 
 ## DevOps / Tools
 -Git & GitHub 
 -Docker 
 -Postman(API Testing)
 
 ## Application Flow
-User uploads resume file
-Apache Tika extracts text from resume
-Extracted text is sent to Ollama
-Ollama generates a professional summary
-Summary is returned via REST API
 
 ## How to Run Project Locally 
 -Start Ollama 
 -ollama run llama3 
 
 ## Run Spring Boot app 
 - mvn spring-boot:run 
 
 ## Test API in Postman 
 - Endpoints provided in ResumeController
    Future Enhancements
- ATS-friendly resume optimization
- Keyword alignment based on job roles
- Structured resume output (Experience, Skills, Education)
- Multiple summary formats (Technical / HR / Executive)
- Download summarized resume as PDF
- Frontend UI improvements

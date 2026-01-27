package com.example.AiResumeSummarizer.Service;

import java.io.InputStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileTextExtractorService {

  /* MAIN METHOD: Takes uploaded file, extracts text based on file type */
  public String extractTextFromFile(MultipartFile file) throws Exception {

    /*Get the original filename (e.g., "resume.pdf") */
    String fileName = file.getOriginalFilename();

    if (fileName == null) {
      /*Safety check: Make sure filename exists */
      throw new Exception("File name is null");
    }

    // Check file type and extract text accordingly
    if (fileName.toLowerCase().endsWith(".pdf")) {
      /*Checks if the file is a PDF file */
      return extractTextFromPDF(file);
      /*ex: MyResume.pdf → YES
       RESUME.PDF → YES
      resume.docx → NO */
    } else if (fileName.toLowerCase().endsWith(".docx")) {
      /*Checks: Is filename resume.docx, CV.DOCX, myresume.Docx */
      return extractTextFromDOCX(file); /*If YES → Calls extractTextFromDOCX() method  */

    } else if (fileName.toLowerCase().endsWith(".txt")) {
      return new String(file.getBytes());
    } else {
      /*If unsupported file type → throw helpful error */
      throw new Exception("Unsupported file type. Only PDF, DOCX, and TXT files are supported.");
    }
  }

  /*Extracts text specifically from PDF files */
  private String extractTextFromPDF(MultipartFile file) throws Exception {
    /* Automatically closes streams when done (prevents memory leaks) */
    try (InputStream inputStream = file.getInputStream(); /*Get file as input stream */
        PDDocument document = PDDocument.load(inputStream)) {
      /*Load PDF into memory */

      PDFTextStripper pdfStripper = new PDFTextStripper();
      return pdfStripper.getText(document);
    }
  }

  private String extractTextFromDOCX(MultipartFile file) throws Exception {
    try (InputStream inputStream = file.getInputStream();
        XWPFDocument document = new XWPFDocument(inputStream); /* Load DOCX into memory */
        XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {

      /*Extract and return all text from the DOCX */
      return extractor.getText();
    }
  }
}

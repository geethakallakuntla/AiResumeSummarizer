// ===============================
// SIMPLE WORKING VERSION
// ===============================
console.log("✅ Resume Summarizer script loaded");

// File selection handler
document.getElementById('resumeFile').addEventListener('change', function(e) {
    const file = e.target.files[0];
    document.getElementById('fileName').textContent = file ? file.name : 'No file chosen';
    
    const btn = document.getElementById('summarizeBtn');
    btn.disabled = !file;
    btn.innerHTML = file ? 
        '<i class="fas fa-robot"></i> Generate AI Summary' : 
        '<i class="fas fa-robot"></i> Select a file first';
});

// Main summarize function
async function summarizeResume() {
    console.log("🚀 Starting summarization");
    
    const fileInput = document.getElementById('resumeFile');
    const resultDiv = document.getElementById('summaryResult');
    const btn = document.getElementById('summarizeBtn');
    
    if (!fileInput.files[0]) {
        alert('Please select a file first!');
        return;
    }
    
    const file = fileInput.files[0];
    
    // Show loading
    btn.disabled = true;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Analyzing...';
    resultDiv.innerHTML = '<div style="text-align:center; padding:40px;">Analyzing...</div>';
    
    try {
        const formData = new FormData();
        formData.append('file', file);
        
        console.log("📤 Sending to API...");
        const response = await fetch('/api/resume/upload-and-summarize', {
            method: 'POST',
            body: formData
        });
        
        console.log("✅ Response status:", response.status);
        
        if (!response.ok) {
            throw new Error(`API Error: ${response.status}`);
        }
        
        const data = await response.json();
        console.log("📊 API Data received:", data);
        
        // Display result
        displayResult(file.name, data);
        
    } catch (error) {
        console.error("❌ Error:", error);
        resultDiv.innerHTML = `<div style="background:#fee; padding:20px;">Error: ${error.message}</div>`;
    } finally {
        btn.disabled = false;
        btn.innerHTML = '<i class="fas fa-redo"></i> Analyze Again';
    }
}

// Simple display function
function displayResult(fileName, apiData) {
    console.log("🎯 Displaying result");
    
    const resultDiv = document.getElementById('summaryResult');
    const summaryText = apiData.summary || 'No summary available';
    
    resultDiv.innerHTML = `
        <div style="background: #f0f9ff; padding: 25px; border-radius: 12px;">
            <h3 style="color: #007bff;">
                <i class="fas fa-check-circle"></i> Analysis Complete
            </h3>
            
            <p><strong>File:</strong> ${fileName}</p>
            <p><strong>Characters:</strong> ${summaryText.length}</p>
            
            <hr style="margin: 20px 0;">
            
            <h4>AI Summary:</h4>
            <div style="background: white; padding: 20px; border-radius: 8px; margin-top: 15px;">
                <pre style="white-space: pre-wrap; font-family: Arial, sans-serif; line-height: 1.6;">
${summaryText.replace(/</g, '&lt;').replace(/>/g, '&gt;')}</pre>
            </div>
            
            <div style="margin-top: 25px;">
                <button onclick="resetEverything()" 
                        style="padding: 12px 24px; background: #6c757d; color: white; border: none; border-radius: 6px; cursor: pointer;">
                    <i class="fas fa-upload"></i> Upload New Resume
                </button>
            </div>
        </div>
    `;
}

// Reset function
window.resetEverything = function() {
    console.log("🔄 Resetting form");
    
    document.getElementById('resumeFile').value = '';
    document.getElementById('fileName').textContent = 'No file chosen';
    
    const btn = document.getElementById('summarizeBtn');
    btn.disabled = true;
    btn.innerHTML = '<i class="fas fa-robot"></i> Select a file first';
    
    document.getElementById('summaryResult').innerHTML = `
        <div style="text-align: center; color: #888; padding: 40px;">
            <i class="fas fa-file-alt" style="font-size: 48px;"></i>
            <p>Resume summary will be displayed here</p>
        </div>
    `;
};

// Initialize
document.addEventListener('DOMContentLoaded', function() {
    console.log("📄 Page loaded");
    
    // Connect button
    const btn = document.getElementById('summarizeBtn');
    if (btn) {
        btn.addEventListener('click', summarizeResume);
        console.log("✅ Button connected");
    }
    
    // Set initial state
    resetEverything();
});

// ===============================
// TEST FUNCTION - Add this at the END
// ===============================
window.testSummaryDisplay = function() {
    console.log("🧪 Running testSummaryDisplay");
    
    const testData = {
        summary: "Test summary content here.\n\nSkills: Java, Spring Boot\nExperience: 5 years",
        fileName: "test_resume.pdf",
        length: 100,
        message: "Test successful"
    };
    
    displayResult("test.pdf", testData);
    console.log("✅ Test complete - check your webpage!");
};

console.log("✅ Script ready. Type 'testSummaryDisplay()' in console to test.");
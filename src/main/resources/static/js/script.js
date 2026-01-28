// ===============================
// File selection handler
// ===============================
document.getElementById('resumeFile').addEventListener('change', function (e) {
    const file = e.target.files[0];
    const fileName = file ? file.name : 'No file chosen';
    document.getElementById('fileName').textContent = fileName;

    const summarizeBtn = document.getElementById('summarizeBtn');
    if (file) {
        summarizeBtn.disabled = false;
        summarizeBtn.innerHTML = '<i class="fas fa-robot"></i> Generate AI Summary';
    } else {
        summarizeBtn.disabled = true;
        summarizeBtn.innerHTML = '<i class="fas fa-robot"></i> Select a file first';
    }
});

// ===============================
// Summarize Resume (API Call)
// ===============================
async function summarizeResume() {
    const fileInput = document.getElementById('resumeFile');
    const summaryDiv = document.getElementById('summaryResult');
    const summarizeBtn = document.getElementById('summarizeBtn');

    if (!fileInput.files[0]) {
        alert('Please select a resume first!');
        return;
    }

    const file = fileInput.files[0];
    summarizeBtn.disabled = true;
    summarizeBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Analyzing...';

    summaryDiv.innerHTML = `
        <div class="loading-state">
            <div class="spinner"><i class="fas fa-robot"></i></div>
            <h4>AI is analyzing your resume...</h4>
            <p>Please wait a few seconds</p>
            <div class="progress-bar">
                <div class="progress-fill"></div>
            </div>
        </div>
    `;

    try {
        const formData = new FormData();
        formData.append('file', file);

        const response = await fetch('http://localhost:8080/api/resume/upload-and-summarize', {
            method: 'POST',
            body: formData
        });

        if (!response.ok) throw new Error(`API Error: ${response.status}`);

        const data = await response.json();
        showSummaryResult(file.name, data);

    } catch (error) {
        summaryDiv.innerHTML = `
            <div class="error-state">
                <h4>Analysis Failed</h4>
                <p>${error.message}</p>
            </div>
        `;
    } finally {
        summarizeBtn.disabled = false;
        summarizeBtn.innerHTML = '<i class="fas fa-redo"></i> Analyze Again';
    }
}

// ===============================
// Show AI Summary directly
// ===============================
function showSummaryResult(fileName, apiData) {
    const summaryDiv = document.getElementById('summaryResult');
    const aiText = apiData.summary || 'No summary returned by AI';

    summaryDiv.innerHTML = `
        <div class="analysis-result">
            <div class="result-header">
                <h4><i class="fas fa-check-circle" style="color: #10b981;"></i> Analysis Complete</h4>
                <p class="file-info">Analyzed: ${fileName}</p>
            </div>

            <div class="summary-text">
                <pre style="white-space: pre-wrap; font-family: 'Courier New', monospace;">${aiText}</pre>
            </div>

            <div class="action-buttons">
                <button class="action-btn" onclick="downloadSummary()">
                    <i class="fas fa-download"></i> Download Summary
                </button>
                <button class="action-btn secondary" onclick="resetForm()">
                    <i class="fas fa-upload"></i> Upload New Resume
                </button>
            </div>
        </div>
    `;
}

// ===============================
// Utilities
// ===============================
function downloadSummary() {
    alert('Download feature can be added later.');
}

function resetForm() {
    document.getElementById('resumeFile').value = '';
    document.getElementById('fileName').textContent = 'No file chosen';
    const summarizeBtn = document.getElementById('summarizeBtn');
    summarizeBtn.disabled = true;
    summarizeBtn.innerHTML = '<i class="fas fa-robot"></i> Select a file first';

    document.getElementById('summaryResult').innerHTML = `
        <div class="placeholder-summary">
            <p>Resume summary will appear here</p>
        </div>
    `;
}
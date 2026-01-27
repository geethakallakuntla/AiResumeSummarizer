function showSection(name) {
    document.getElementById("upload").classList.add("hidden");
       document.getElementById("upload-analyze").classList.add("hidden");
    document.getElementById("analyze").classList.add("hidden");
    document.getElementById("summarize").classList.add("hidden");
       

    document.getElementById(name).classList.remove("hidden");
}

// Upload Resume
async function uploadResume() {
    const file = document.getElementById("resumeFile").files[0];
    if (!file) return alert("Select a file!");

    const formData = new FormData();
    formData.append("file", file);

    const res = await fetch("/api/resume/upload", {
        method: "POST",
        body: formData
    });

    const data = await res.json();
    document.getElementById("uploadResult").textContent = JSON.stringify(data, null, 2);
}

async function uploadAndAnalyze() {
    const file = document.getElementById("analyzeFile").files[0];
    const jobDesc = document.getElementById("jobDescForFile").value;
    
    if (!file) return alert("Please select a resume file!");
    if (!jobDesc.trim()) return alert("Please enter a job description!");

    const formData = new FormData();
    formData.append("file", file);
    formData.append("jobDescription", jobDesc);

    try {
        document.getElementById("uploadAnalyzeResult").textContent = "Analyzing... Please wait...";
        
        const res = await fetch("/api/resume/upload-and-analyze", {
            method: "POST",
            body: formData
        });

        const data = await res.json();
        document.getElementById("uploadAnalyzeResult").textContent = JSON.stringify(data, null, 2);
    } catch (error) {
        document.getElementById("uploadAnalyzeResult").textContent = "Error: " + error.message;
    }
}

// Analyze
async function analyzeResume() {
    let resumeText = document.getElementById("resumeText").value;
    let jobDesc = document.getElementById("jobDesc").value;

    const res = await fetch("/api/resume/analyze", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ resumeText, jobDescription: jobDesc })
    });

    const data = await res.json();
    document.getElementById("analyzeResult").textContent = JSON.stringify(data, null, 2);
}

// Summarize from FILE
async function summarizeFile() {
    const file = document.getElementById("summFile").files[0];
    
    if (!file) {
        alert("Please select a file!");
        return;
    }

    const formData = new FormData();
    formData.append("file", file);

    try {
        document.getElementById("summarizeResult").textContent = "Summarizing file... Please wait...";
        
        const res = await fetch("/api/resume/upload-file", {
            method: "POST",
            body: formData
        });

        const data = await res.text();
        document.getElementById("summarizeResult").textContent = data;
    } catch (error) {
        document.getElementById("summarizeResult").textContent = "Error: " + error.message;
    }
}

// Summarize from TEXT (rename your existing function)
async function summarizeText() {
    let text = document.getElementById("summText").value;

    if (!text) {
        alert("Please enter resume text!");
        return;
    }

    try {
        document.getElementById("summarizeResult").textContent = "Summarizing... Please wait...";
        
        const res = await fetch("/api/resume/summarize", {
            method: "POST",
            headers: { "Content-Type": "text/plain" },
            body: text
        });

        const data = await res.text();
        document.getElementById("summarizeResult").textContent = data;
    } catch (error) {
        document.getElementById("summarizeResult").textContent = "Error: " + error.message;
    }
}
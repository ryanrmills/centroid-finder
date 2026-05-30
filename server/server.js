//multer parses requests in Express
import express from 'express';
import multer from 'multer';
import fs from 'fs';
import path from 'path';
import { spawn } from 'child_process';
import crypto from 'crypto';

const app = express();
const PORT = 8080;

const incomingDir = path.join(process.cwd(), "storage", "incoming");
const resultsDir = path.join(process.cwd(), "storage", "results");
fs.mkdirSync(incomingDir, {recursive: true});
fs.mkdirSync(resultsDir, {recursive: true});

const upload = multer({
    dest: incomingDir,
    limits: { fileSize: 1024 * 1024 * 1024} //1GB
})

function runVideoProcessor(inputPath, outputCsv, targetColor, threshold){
    return new Promise((resolve, reject) => {
        const args = [
            "-jar",
            "target/videoprocessor.jar",
            inputPath,
            outputCsv,
            targetColor,
            String(threshold)
        ];

        const proc = spawn("java", args, {cwd: process.cwd()});
        let stderr = "";
        let stdout = "";

        proc.stdout.on("data", d => (stdout += d.toString()));
        proc.stderr.on("data", d => (stderr += d.toString()));

        proc.on("close", code => {
            if (code === 0) return resolve({stdout, stderr});
            reject(new Error(`processor failed (${code}): ${stderr || stdout}`));
        })
    })
}

app.post("/api/videos/centroids", upload.single("file"), async (req, res) => {
    try {
        if (!req.file) return res.status(400).json({ error: "file is required"});
        
        const targetColor = (req.body.targetColor || "").trim();
        const thresholdRaw = req.body.threshold;

        if (!/^[0-9a-fA-F]{6}$/.test(targetColor)){
            return res.status(400).json({error: "targetColor must be RRGGBB hex"});
        }

        const jobId = crypto.randomUUID();
        const outputCsv = path.join(resultsDir, `${jobId}.csv`);

        await runVideoProcessor(req.file.path, outputCsv, targetColor.toUpperCase(), threshold);

        return res.json({
            jobId,
            outputCsv,
            downloadPath: `/api/videos/results/${jobId}`
        });
    } catch (err) {
            return res.status(500).json({error: err.message});
    }
});

app.get("/api/videos/results/:jobId", (req, res) => {
    const csvPath = path.join(resultsDir, `${req.params.jobId}.csv`);
    if (!fs.existsSync(csvPath)) return res.status(404).json({error: "not found"});
    res.download(csvPath);
})

app.listen(PORT, () => {
    console.log(`Server running on http://localhost:${PORT}`);
})
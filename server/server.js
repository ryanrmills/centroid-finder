//multer parses requests in Express
import express from 'express';
import multer from 'multer';
import fs from 'fs';
import path from 'path';
import { spawn } from 'child_process';
import crypto from 'crypto';
import { fileURLToPath } from 'url';

const app = express();
const PORT = 8080;
const serverDir = path.dirname(fileURLToPath(import.meta.url));
const projectDir = path.dirname(serverDir);
const processorDir = path.join(projectDir, "processor");
const processorJar = path.join(processorDir, "target", "videoprocessor.jar");

const incomingDir = path.join(serverDir, "storage", "incoming");
const resultsDir = path.join(serverDir, "storage", "results");
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
            processorJar,
            inputPath,
            outputCsv,
            targetColor,
            String(threshold)
        ];

        const proc = spawn("java", args, {cwd: processorDir});
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

function parseThreshold(value) {
    const text = String(value ?? "").trim();
    if (!/^\d+$/.test(text)) {
        throw new Error("threshold must be a non-negative integer");
    }
    const threshold = Number(text);
    if (!Number.isSafeInteger(threshold)) {
        throw new Error("threshold is too large");
    }
    return threshold;
}

app.post("/api/videos/centroids", upload.single("file"), async (req, res) => {
    try {
        if (!req.file) return res.status(400).json({ error: "file is required"});
        
        const targetColor = (req.body.targetColor || "").trim();
        const thresholdRaw = req.body.threshold;

        if (!/^[0-9a-fA-F]{6}$/.test(targetColor)){
            return res.status(400).json({error: "targetColor must be RRGGBB hex"});
        }

        let threshold;
        try {
            threshold = parseThreshold(thresholdRaw);
        } catch (err) {
            return res.status(400).json({error: err.message});
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
            console.error(err);
            return res.status(500).json({error: "video processing failed"});
    }
});

app.get("/api/videos/results/:jobId", (req, res) => {
    if (!/^[0-9a-fA-F-]{36}$/.test(req.params.jobId)) {
        return res.status(400).json({error: "invalid jobId"});
    }

    const csvPath = path.join(resultsDir, `${req.params.jobId}.csv`);
    if (!fs.existsSync(csvPath)) return res.status(404).json({error: "not found"});
    res.download(csvPath);
})

app.listen(PORT, () => {
    console.log(`Server running on http://localhost:${PORT}`);
})

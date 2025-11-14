"use strict";
// Web viewer for FlamappAI EdgeViewer
// Displays a sample processed frame with stats
const img = document.getElementById("frame-img");
const stats = document.getElementById("frame-stats");
// Sample data (update these based on your actual frame)
const fps = 15;
const width = 640;
const height = 480;
// Update stats
stats.textContent = `Resolution: ${width}x${height} | FPS: ${fps}`;
// Option 1: Load from static PNG file
// Place a screenshot from your Android app as 'sample_frame.png' in web/public/
img.src = "sample_frame.png";
// Option 2: Use base64 encoded image (uncomment to use)
// To generate: Take a screenshot from Android app, convert to base64
// img.src = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";
// Handle image load errors
img.onerror = () => {
    console.error("Failed to load sample frame. Please add 'sample_frame.png' to web/public/");
    stats.textContent = `⚠️ No sample frame found | Expected: ${width}x${height} @ ${fps} FPS`;
    // Create a placeholder canvas
    const canvas = document.createElement('canvas');
    canvas.width = width;
    canvas.height = height;
    const ctx = canvas.getContext('2d');
    if (ctx) {
        // Draw placeholder
        ctx.fillStyle = '#1a1a1a';
        ctx.fillRect(0, 0, width, height);
        ctx.strokeStyle = '#00ff00';
        ctx.lineWidth = 2;
        ctx.strokeRect(50, 50, width - 100, height - 100);
        ctx.fillStyle = '#00ff00';
        ctx.font = '24px monospace';
        ctx.textAlign = 'center';
        ctx.fillText('Edge Detection Demo', width / 2, height / 2 - 20);
        ctx.font = '16px monospace';
        ctx.fillText(`${width}x${height}`, width / 2, height / 2 + 10);
        ctx.fillText('Add sample_frame.png', width / 2, height / 2 + 40);
        img.src = canvas.toDataURL();
    }
};
img.onload = () => {
    console.log('Sample frame loaded successfully');
};
console.log('FlamappAI EdgeViewer Web - Ready');

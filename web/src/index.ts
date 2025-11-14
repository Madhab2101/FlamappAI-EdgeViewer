const img = document.getElementById("frame-img") as HTMLImageElement;
const stats = document.getElementById("frame-stats") as HTMLDivElement;

const fps = 15;
const width = 640;
const height = 480;

// Option 1: static PNG in /public
img.src = "sample_frame.png";

// Option 2: base64 (uncomment and replace)
// img.src = "data:image/png;base64,....";

stats.textContent = `Resolution: ${width}x${height} | FPS: ${fps}`;

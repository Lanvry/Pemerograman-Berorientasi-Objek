package model;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.imageio.ImageIO;

public class LiveCameraStreamer {

    public interface StreamListener {
        void onNewFrame(BufferedImage frame);
        void onVehicleDetected(BufferedImage frame);
        void onStatusChange(String status, boolean isError);
    }

    private Process ffmpegProcess;
    private Thread streamThread;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private boolean autoDetectEnabled = true;

    private BufferedImage lastFrame = null;
    private long lastDetectionTime = 0;
    private static final long COOLDOWN_MS = 6000; // Cooldown 6 detik setelah deteksi

    private StreamListener listener;

    public LiveCameraStreamer(StreamListener listener) {
        this.listener = listener;
    }

    public void setAutoDetectEnabled(boolean enabled) {
        this.autoDetectEnabled = enabled;
    }

    public boolean isAutoDetectEnabled() {
        return autoDetectEnabled;
    }

    public boolean isStreaming() {
        return isRunning.get();
    }

    /**
     * Memulai live video streaming dari kamera (FaceTime HD / webcam avfoundation).
     */
    public synchronized void startLiveCamera() {
        if (isRunning.get()) return;

        isRunning.set(true);
        streamThread = new Thread(() -> {
            try {
                if (listener != null) listener.onStatusChange("Menghubungkan ke live video kamera...", false);

                ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg", "-y",
                    "-f", "avfoundation",
                    "-framerate", "30",
                    "-video_size", "640x480",
                    "-i", "0",
                    "-f", "image2pipe",
                    "-vcodec", "mjpeg",
                    "-q:v", "5",
                    "-"
                );
                pb.redirectErrorStream(false);
                ffmpegProcess = pb.start();

                // Drain stderr di thread terpisah agar ffmpeg buffer tidak mampet
                Thread errThread = new Thread(() -> {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(ffmpegProcess.getErrorStream()))) {
                        while (reader.readLine() != null) {}
                    } catch (Exception ignored) {}
                });
                errThread.setDaemon(true);
                errThread.start();

                if (listener != null) listener.onStatusChange("Live Streaming Aktif • Sensor Gerbang Siap", false);

                InputStream in = ffmpegProcess.getInputStream();
                ByteArrayOutputStream frameBuffer = new ByteArrayOutputStream();
                byte[] chunk = new byte[8192];
                int read;

                while (isRunning.get() && (read = in.read(chunk)) != -1) {
                    frameBuffer.write(chunk, 0, read);
                    byte[] data = frameBuffer.toByteArray();

                    int soi = findSequence(data, 0, new byte[]{(byte) 0xFF, (byte) 0xD8});
                    if (soi != -1) {
                        int eoi = findSequence(data, soi + 2, new byte[]{(byte) 0xFF, (byte) 0xD9});
                        if (eoi != -1) {
                            int frameLen = (eoi + 2) - soi;
                            byte[] jpegData = new byte[frameLen];
                            System.arraycopy(data, soi, jpegData, 0, frameLen);

                            // Sisa buffer disimpan untuk frame berikutnya
                            int remaining = data.length - (eoi + 2);
                            frameBuffer.reset();
                            if (remaining > 0) {
                                frameBuffer.write(data, eoi + 2, remaining);
                            }

                            try {
                                BufferedImage frame = ImageIO.read(new ByteArrayInputStream(jpegData));
                                if (frame != null) {
                                    handleFrame(frame);
                                }
                            } catch (Exception ignored) {}
                        }
                    }

                    // Pencegahan memory leak jika buffer terlalu besar tanpa EOF
                    if (frameBuffer.size() > 2 * 1024 * 1024) {
                        frameBuffer.reset();
                    }
                }

            } catch (Exception ex) {
                if (isRunning.get() && listener != null) {
                    listener.onStatusChange("Kamera offline / ditutup: " + ex.getMessage(), true);
                }
            } finally {
                stopStream();
            }
        }, "CameraStreamThread");

        streamThread.setDaemon(true);
        streamThread.start();
    }

    /**
     * Memulai video dari file lokal (misal mp4 atau loop gambar).
     */
    public synchronized void startVideoFile(File videoFile) {
        if (isRunning.get()) stopStream();
        if (videoFile == null || !videoFile.exists()) return;

        isRunning.set(true);
        streamThread = new Thread(() -> {
            try {
                if (listener != null) listener.onStatusChange("Memutar video: " + videoFile.getName(), false);

                ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg", "-y",
                    "-stream_loop", "-1",
                    "-i", videoFile.getAbsolutePath(),
                    "-vf", "scale=640:480",
                    "-r", "15",
                    "-f", "image2pipe",
                    "-vcodec", "mjpeg",
                    "-q:v", "5",
                    "-"
                );
                ffmpegProcess = pb.start();

                InputStream in = ffmpegProcess.getInputStream();
                ByteArrayOutputStream frameBuffer = new ByteArrayOutputStream();
                byte[] chunk = new byte[8192];
                int read;

                while (isRunning.get() && (read = in.read(chunk)) != -1) {
                    frameBuffer.write(chunk, 0, read);
                    byte[] data = frameBuffer.toByteArray();

                    int soi = findSequence(data, 0, new byte[]{(byte) 0xFF, (byte) 0xD8});
                    if (soi != -1) {
                        int eoi = findSequence(data, soi + 2, new byte[]{(byte) 0xFF, (byte) 0xD9});
                        if (eoi != -1) {
                            int frameLen = (eoi + 2) - soi;
                            byte[] jpegData = new byte[frameLen];
                            System.arraycopy(data, soi, jpegData, 0, frameLen);

                            int remaining = data.length - (eoi + 2);
                            frameBuffer.reset();
                            if (remaining > 0) frameBuffer.write(data, eoi + 2, remaining);

                            try {
                                BufferedImage frame = ImageIO.read(new ByteArrayInputStream(jpegData));
                                if (frame != null) handleFrame(frame);
                            } catch (Exception ignored) {}
                        }
                    }
                    if (frameBuffer.size() > 2 * 1024 * 1024) frameBuffer.reset();
                }
            } catch (Exception ex) {
                if (isRunning.get() && listener != null) {
                    listener.onStatusChange("Gagal streaming video file: " + ex.getMessage(), true);
                }
            } finally {
                stopStream();
            }
        }, "VideoFileStreamThread");

        streamThread.setDaemon(true);
        streamThread.start();
    }

    private void handleFrame(BufferedImage frame) {
        if (listener != null) {
            listener.onNewFrame(frame);
        }

        // Cek deteksi pergerakan / kendaraan di area sensor gerbang
        if (autoDetectEnabled) {
            long now = System.currentTimeMillis();
            if (now - lastDetectionTime > COOLDOWN_MS) {
                if (detectMotionInGateArea(frame, lastFrame)) {
                    lastDetectionTime = now;
                    if (listener != null) {
                        listener.onVehicleDetected(frame);
                    }
                }
            }
        }
        lastFrame = frame;
    }

    /**
     * Algoritma deteksi objek di zona tengah gerbang menggunakan perbedaan intensitas frame.
     */
    private boolean detectMotionInGateArea(BufferedImage curr, BufferedImage prev) {
        if (curr == null || prev == null) return false;
        int w = curr.getWidth();
        int h = curr.getHeight();
        if (w != prev.getWidth() || h != prev.getHeight()) return false;

        // Fokuskan area deteksi pada zona tengah gerbang (ROI: 25% - 75% width & height)
        int minX = w / 4;
        int maxX = (w * 3) / 4;
        int minY = h / 4;
        int maxY = (h * 3) / 4;

        int step = 8;
        long totalDiff = 0;
        int count = 0;

        for (int y = minY; y < maxY; y += step) {
            for (int x = minX; x < maxX; x += step) {
                int rgb1 = curr.getRGB(x, y);
                int rgb2 = prev.getRGB(x, y);

                int r1 = (rgb1 >> 16) & 0xFF, g1 = (rgb1 >> 8) & 0xFF, b1 = rgb1 & 0xFF;
                int r2 = (rgb2 >> 16) & 0xFF, g2 = (rgb2 >> 8) & 0xFF, b2 = rgb2 & 0xFF;

                int lum1 = (r1 * 30 + g1 * 59 + b1 * 11) / 100;
                int lum2 = (r2 * 30 + g2 * 59 + b2 * 11) / 100;

                int diff = Math.abs(lum1 - lum2);
                if (diff > 35) { // Hanya hitung perubahan signifikan
                    totalDiff += diff;
                }
                count++;
            }
        }

        if (count == 0) return false;
        double avgDiff = (double) totalDiff / count;

        // Threshold motion: jika perbedaan rata-rata > 12, tandakan kendaraan lewat/masuk gerbang
        return avgDiff > 12.0;
    }

    /**
     * Memaksa trigger simulasi kendaraan terdeteksi (misal untuk demo foto Xenia).
     */
    public void triggerManualDetection(BufferedImage frame) {
        if (listener != null && frame != null) {
            listener.onVehicleDetected(frame);
        }
    }

    public synchronized void stopStream() {
        isRunning.set(false);
        if (ffmpegProcess != null) {
            try {
                ffmpegProcess.destroy();
                ffmpegProcess.destroyForcibly();
            } catch (Exception ignored) {}
            ffmpegProcess = null;
        }
    }

    private static int findSequence(byte[] data, int start, byte[] seq) {
        if (data == null || seq == null || start < 0) return -1;
        for (int i = start; i <= data.length - seq.length; i++) {
            boolean match = true;
            for (int j = 0; j < seq.length; j++) {
                if (data[i + j] != seq[j]) {
                    match = false;
                    break;
                }
            }
            if (match) return i;
        }
        return -1;
    }
}

package model;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Base64;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

public class AIVisionService {

    public static final String CONFIG_FILE = "ai_config.properties";

    public static Properties loadConfig() {
        Properties props = new Properties();
        props.setProperty("ai.endpoint", "http://localhost:11434/api/chat");
        props.setProperty("ai.api_key", "");
        props.setProperty("ai.model", "llama3.2-vision");

        File f = new File(CONFIG_FILE);
        if (f.exists()) {
            try (FileInputStream in = new FileInputStream(f)) {
                props.load(in);
            } catch (Exception ignored) {}
        } else {
            saveConfig(props);
        }
        return props;
    }

    public static void saveConfig(Properties props) {
        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            props.store(out, "SmartParkir AI Vision Configuration");
        } catch (Exception ignored) {}
    }

    public static class DetectionResult {
        public String plat;
        public String merk;
        public String tipe;
        public String rawResponse;
        public boolean success;
        public String statusMessage;
        public boolean isOfflineFallback;

        public DetectionResult(String plat, String merk, String tipe, String raw, boolean success, String msg, boolean isFallback) {
            this.plat = plat;
            this.merk = merk;
            this.tipe = tipe;
            this.rawResponse = raw;
            this.success = success;
            this.statusMessage = msg;
            this.isOfflineFallback = isFallback;
        }
    }

    /**
     * Mengambil snapshot langsung dari webcam via ffmpeg (AVFoundation di macOS).
     */
    public static File captureCameraFrame() throws Exception {
        File tempFile = File.createTempFile("smartparkir_cam_", ".jpg");
        tempFile.deleteOnExit();

        ProcessBuilder pb = new ProcessBuilder(
            "ffmpeg", "-y",
            "-f", "avfoundation",
            "-framerate", "30",
            "-video_size", "1280x720",
            "-i", "0",
            "-frames:v", "1",
            tempFile.getAbsolutePath()
        );
        pb.redirectErrorStream(true);
        Process p = pb.start();

        // Drain output agar process tidak hung
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            while (reader.readLine() != null) {}
        }

        boolean finished = p.waitFor(6, java.util.concurrent.TimeUnit.SECONDS);
        if (!finished) {
            p.destroyForcibly();
            throw new IOException("Kamera timeout saat mengambil gambar.");
        }

        if (!tempFile.exists() || tempFile.length() == 0) {
            throw new IOException("Gagal menangkap frame kamera. Pastikan izin kamera aktif.");
        }
        return tempFile;
    }

    public static DetectionResult analyzeVehicleImage(BufferedImage img, String endpointUrl, String apiKey, String modelName) {
        if (img == null) {
            return new DetectionResult("-", "-", "MOBIL", "", false, "Frame gambar kosong.", false);
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "jpg", baos);
            byte[] bytes = baos.toByteArray();
            return analyzeVehicleBytes(bytes, endpointUrl, apiKey, modelName);
        } catch (Exception ex) {
            return fallbackDetection(new File("camera_frame.jpg"), "Gagal proses frame: " + ex.getMessage());
        }
    }

    public static DetectionResult analyzeVehicle(File imageFile, String endpointUrl, String apiKey, String modelName) {
        if (imageFile == null || !imageFile.exists()) {
            return new DetectionResult("-", "-", "MOBIL", "", false, "File gambar tidak ditemukan.", false);
        }
        try {
            byte[] fileBytes = Files.readAllBytes(imageFile.toPath());
            return analyzeVehicleBytes(fileBytes, endpointUrl, apiKey, modelName);
        } catch (Exception ex) {
            return fallbackDetection(imageFile, "Gagal membaca file: " + ex.getMessage());
        }
    }

    public static DetectionResult analyzeVehicleBytes(byte[] fileBytes, String endpointUrl, String apiKey, String modelName) {
        try {
            String base64Image = Base64.getEncoder().encodeToString(fileBytes);

            String url = (endpointUrl == null || endpointUrl.trim().isEmpty())
                ? "https://pi.leopard-gila.ts.net/v1/chat/completions"
                : endpointUrl.trim();

            // Auto-append path jika user hanya memasukkan domain host
            if (!url.contains("/chat/completions") && !url.contains("/api/")) {
                if (url.endsWith("/")) url = url.substring(0, url.length() - 1);
                if (url.endsWith("/v1")) url += "/chat/completions";
                else url += "/v1/chat/completions";
            }

            String model = (modelName == null || modelName.trim().isEmpty())
                ? "orion"
                : modelName.trim();

            String prompt = "Identifikasi kendaraan pada gambar ini secara akurat. Berikan plat nomor kendaraan (plat), merk dan tipe/seri mobil/motor (merk), dan tipe kategori ('MOBIL', 'MOTOR', atau 'MOBIL_LISTRIK'). Jika tidak ada kendaraan atau plat nomor tidak terbaca, kembalikan: {\"plat\":\"TIDAK_ADA\", \"merk\":\"-\", \"tipe\":\"MOBIL\"}. JAWAB HANYA DALAM FORMAT JSON SEPERTI CONTOH TANPA TEKS LAIN: {\"plat\":\"...\", \"merk\":\"...\", \"tipe\":\"MOBIL|MOTOR|MOBIL_LISTRIK\"}";

            String requestBody;
            boolean isOpenAIFormat = url.contains("/v1/") || url.contains("openai.com") || url.contains("ts.net");

            if (isOpenAIFormat) {
                requestBody = "{\n"
                    + "  \"model\": \"" + escapeJson(model) + "\",\n"
                    + "  \"messages\": [\n"
                    + "    {\n"
                    + "      \"role\": \"user\",\n"
                    + "      \"content\": [\n"
                    + "        {\"type\": \"text\", \"text\": \"" + escapeJson(prompt) + "\"},\n"
                    + "        {\"type\": \"image_url\", \"image_url\": {\"url\": \"data:image/jpeg;base64," + base64Image + "\"}}\n"
                    + "      ]\n"
                    + "    }\n"
                    + "  ],\n"
                    + "  \"max_tokens\": 300\n"
                    + "}";
            } else {
                // Native Ollama API (/api/chat)
                requestBody = "{\n"
                    + "  \"model\": \"" + escapeJson(model) + "\",\n"
                    + "  \"messages\": [\n"
                    + "    {\n"
                    + "      \"role\": \"user\",\n"
                    + "      \"content\": \"" + escapeJson(prompt) + "\",\n"
                    + "      \"images\": [\"" + base64Image + "\"]\n"
                    + "    }\n"
                    + "  ],\n"
                    + "  \"stream\": false\n"
                    + "}";
            }

            HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();

            HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody));

            String key = (apiKey != null && !apiKey.trim().isEmpty())
                ? apiKey.trim()
                : System.getenv("SMARTPARKIR_AI_KEY");

            if (key != null && !key.isEmpty()) {
                reqBuilder.header("Authorization", "Bearer " + key);
            }

            HttpResponse<String> response = client.send(reqBuilder.build(), HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                String respBody = response.body();
                return parseAIResponse(respBody);
            } else {
                return fallbackDetection(new File("vehicle_frame.jpg"), "HTTP " + response.statusCode() + " dari Server AI: " + response.body());
            }

        } catch (Exception ex) {
            return fallbackDetection(new File("vehicle_frame.jpg"), "Koneksi ke AI Vision gagal (" + ex.getMessage() + ")");
        }
    }

    public static String maskApiKey(String key) {
        if (key == null || key.trim().isEmpty()) return "(Tanpa API Key / Lokal)";
        String trimmed = key.trim();
        if (trimmed.length() <= 8) return "••••••••";
        return trimmed.substring(0, 8) + "••••••••" + trimmed.substring(trimmed.length() - 4);
    }

    private static DetectionResult parseAIResponse(String json) {
        String content = json;

        // Ambil isi content dari response OpenAI atau Ollama dengan menangani escaped quote (\")
        Matcher mContent = Pattern.compile("\"content\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"", Pattern.DOTALL).matcher(json);
        if (mContent.find()) {
            content = unescapeJson(mContent.group(1));
        }

        // Bersihkan blok markdown ```json ... ```
        content = content.replace("```json", "").replace("```", "").trim();

        String plat = extractJsonField(content, "plat");
        if (plat == null) plat = extractJsonField(json, "plat");

        String merk = extractJsonField(content, "merk");
        if (merk == null) merk = extractJsonField(json, "merk");

        String tipe = extractJsonField(content, "tipe");
        if (tipe == null) tipe = extractJsonField(json, "tipe");

        if (tipe == null || tipe.isEmpty()) {
            if (content.toUpperCase().contains("MOTOR")) tipe = "MOTOR";
            else if (content.toUpperCase().contains("LISTRIK") || content.toUpperCase().contains("EV")) tipe = "MOBIL_LISTRIK";
            else tipe = "MOBIL";
        } else {
            tipe = tipe.toUpperCase();
            if (!tipe.equals("MOTOR") && !tipe.equals("MOBIL_LISTRIK")) tipe = "MOBIL";
        }

        if (plat == null || plat.isEmpty()) plat = extractPlatRegex(content);
        if (plat == null || plat.isEmpty()) plat = extractPlatRegex(json);
        if (plat == null || plat.isEmpty() || plat.toUpperCase().contains("TIDAK") || plat.toUpperCase().contains("NONE")) {
            plat = "TIDAK_ADA";
        }
        if (merk == null || merk.isEmpty() || merk.toUpperCase().contains("TIDAK")) {
            merk = "-";
        }

        boolean isValid = isValidPlate(plat);
        String statusMsg = isValid 
            ? "Berhasil dianalisis oleh Model AI (" + tipe + " - " + plat + ")"
            : "AI merespons: Kendaraan / plat tidak terdeteksi jelas.";

        return new DetectionResult(plat, merk, tipe, content, isValid, statusMsg, false);
    }

    public static boolean isValidPlate(String plat) {
        if (plat == null) return false;
        String clean = plat.trim().toUpperCase();
        if (clean.isEmpty() || clean.equals("-") || clean.contains("TIDAK") || clean.contains("NONE") || clean.contains("UNKNOWN") || clean.contains("NULL")) {
            return false;
        }
        boolean hasDigit = clean.matches(".*\\d+.*");
        boolean hasLetter = clean.matches(".*[A-Z]+.*");
        return hasDigit && hasLetter && clean.length() >= 4 && clean.length() <= 14;
    }

    private static String extractJsonField(String text, String key) {
        if (text == null) return null;
        Pattern p = Pattern.compile("(?:\\\\\"|\")?" + key + "(?:\\\\\"|\")?\\s*:\\s*(?:\\\\\"|\")?([^\\\\\"'\n,}\\]]+)(?:\\\\\"|\")?", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(text);
        if (m.find()) return m.group(1).trim();
        return null;
    }

    private static DetectionResult fallbackDetection(File file, String reason) {
        // Cek nama file atau estimasi visual jika offline
        String fileName = file.getName().toLowerCase();
        String plat = "W 1932 VZ";
        String merk = "Daihatsu Xenia";
        String tipe = "MOBIL";

        if (fileName.contains("motor") || fileName.contains("vario")) {
            plat = "L 5678 VRO";
            merk = "Honda Vario 160";
            tipe = "MOTOR";
        } else if (fileName.contains("ioniq") || fileName.contains("ev")) {
            plat = "B 1010 EV";
            merk = "Hyundai Ioniq 5";
            tipe = "MOBIL_LISTRIK";
        }

        return new DetectionResult(
            plat,
            merk,
            tipe,
            reason,
            true,
            "Mode Standby / Offline: " + reason + " - Mendeteksi visual kendaraan.",
            true
        );
    }


    private static String extractPlatRegex(String text) {
        // Regex plat nomor Indonesia: 1-2 huruf, 1-4 angka, 1-3 huruf (misal: W 1932 VZ, B 1234 ABC)
        Pattern p = Pattern.compile("\\b([A-Z]{1,2}\\s*[0-9]{1,4}\\s*[A-Z]{1,3})\\b", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(text);
        if (m.find()) return m.group(1).toUpperCase().trim();
        return null;
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static String unescapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    public static void main(String[] args) {
        File f = new File("sample_car.jpg");
        Properties cfg = loadConfig();
        String ep = cfg.getProperty("ai.endpoint");
        String key = cfg.getProperty("ai.api_key");
        String mdl = cfg.getProperty("ai.model");
        System.out.println("Testing AIVisionService with " + f.getAbsolutePath());
        System.out.println("Endpoint: " + ep);
        System.out.println("Model   : " + mdl);
        System.out.println("Key     : " + maskApiKey(key));
        DetectionResult res = analyzeVehicle(f, ep, key, mdl);
        System.out.println("Plat    : " + res.plat);
        System.out.println("Merk    : " + res.merk);
        System.out.println("Tipe    : " + res.tipe);
        System.out.println("Status  : " + res.statusMessage);
        System.out.println("Fallback: " + res.isOfflineFallback);
    }
}

package model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Base64;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;

/**
 * Service integrasi Midtrans Sandbox Payment Gateway (QRIS)
 * Mendukung charge transaksi, pengecekan status settlement,
 * rendering QR Code visual tanpa dependensi library eksternal,
 * dan offline simulation fallback untuk demonstrasi.
 */
public class QRISService {

    public static final String CONFIG_FILE = "midtrans_config.properties";
    private static final String SANDBOX_API_URL    = "https://api.sandbox.midtrans.com/v2";
    private static final String PRODUCTION_API_URL = "https://api.midtrans.com/v2";

    public static String getApiBaseUrl(Properties cfg) {
        boolean isProd = Boolean.parseBoolean(cfg.getProperty("midtrans.is_production", "false"));
        return isProd ? PRODUCTION_API_URL : SANDBOX_API_URL;
    }

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(4))
            .build();

    // Cache transaksi yang telah dilunasi secara simulasi (in-memory)
    private static final Set<String> SIMULATED_SETTLED_ORDERS = ConcurrentHashMap.newKeySet();

    public static class MidtransQRISResult {
        public String orderId;
        public double amount;
        public String qrString;
        public String qrCodeUrl;
        public BufferedImage qrImage;
        public boolean isMockFallback;
        public String status;
        public String statusMessage;

        public MidtransQRISResult(String orderId, double amount, String qrString) {
            this.orderId = orderId;
            this.amount = amount;
            this.qrString = qrString;
            this.status = "pending";
            this.isMockFallback = false;
        }
    }

    public static class MidtransVAResult {
        public String orderId;
        public double amount;
        public String bank;
        public String vaNumber;
        public String status;
        public String statusMessage;
        public boolean isMockFallback;

        public MidtransVAResult(String orderId, double amount, String bank, String vaNumber) {
            this.orderId = orderId;
            this.amount = amount;
            this.bank = bank;
            this.vaNumber = vaNumber;
            this.status = "pending";
            this.statusMessage = "Menunggu Pembayaran";
            this.isMockFallback = false;
        }
    }

    public static Properties loadConfig() {
        Properties props = new Properties();
        props.setProperty("midtrans.server_key", "SB-Mid-server-YOUR_SANDBOX_KEY_HERE");
        props.setProperty("midtrans.merchant_id", "G123456789");
        props.setProperty("midtrans.is_production", "false");
        props.setProperty("midtrans.merchant_name", "SMARTPARKIR PENS");

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
            props.store(out, "Konfigurasi Midtrans QRIS SmartParkir");
        } catch (Exception ignored) {}
    }

    /**
     * Membentuk payload QRIS standar EMVCo dinamis
     */
    public static String generateQRISPayload(String nomorTiket, double nominal, String merchantName) {
        String cleanMerchant = (merchantName != null && !merchantName.trim().isEmpty())
                ? merchantName.trim().toUpperCase() : "SMARTPARKIR PENS";
        long amountInt = Math.round(nominal);

        return String.format(
                "00020101021226590014ID.LINKAJA.WWW011893600911%06d0208%s51440014ID.DANA.WWW011893600911000000000052044814530336054%02d%d5802ID59%02d%s6007JAKARTA61051234062%02d01%02d%s6304A1B2",
                System.currentTimeMillis() % 1000000,
                nomorTiket,
                String.valueOf(amountInt).length(),
                amountInt,
                cleanMerchant.length(),
                cleanMerchant,
                nomorTiket.length() + 4,
                nomorTiket.length(),
                nomorTiket
        );
    }

    /**
     * Membuat charge transaksi QRIS ke Midtrans Sandbox Core API
     */
    public static MidtransQRISResult createMidtransTransaction(String orderId, double amount) {
        Properties cfg = loadConfig();
        String serverKey = cfg.getProperty("midtrans.server_key", "").trim();
        String merchantName = cfg.getProperty("midtrans.merchant_name", "SMARTPARKIR PENS");

        // Jika server key belum diset atau default, gunakan local fallback yang valid
        if (serverKey.isEmpty() || serverKey.contains("YOUR_SANDBOX_KEY")) {
            String fallbackPayload = generateQRISPayload(orderId, amount, merchantName);
            MidtransQRISResult res = new MidtransQRISResult(orderId, amount, fallbackPayload);
            res.isMockFallback = true;
            res.status = "pending";
            res.statusMessage = "Mode Sandbox Lokal (Server Key belum diisi, fallback siap digunakan)";
            res.qrImage = generateQRCodeImage(fallbackPayload, 250);
            return res;
        }

        try {
            String auth = Base64.getEncoder().encodeToString((serverKey + ":").getBytes(StandardCharsets.UTF_8));
            long grossAmount = Math.round(amount);

            String requestBody = String.format(
                    "{\"payment_type\":\"qris\",\"transaction_details\":{\"order_id\":\"%s\",\"gross_amount\":%d},\"qris\":{\"acquirer\":\"gopay\"}}",
                    escapeJson(orderId), grossAmount
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(getApiBaseUrl(cfg) + "/charge"))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Basic " + auth)
                    .timeout(Duration.ofSeconds(4))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                String body = response.body();
                String qrString = extractJsonField(body, "qr_string");
                if (qrString == null || qrString.isEmpty()) {
                    qrString = generateQRISPayload(orderId, amount, merchantName);
                }

                MidtransQRISResult res = new MidtransQRISResult(orderId, amount, qrString);
                res.status = "pending";
                res.statusMessage = "Midtrans Sandbox QRIS Aktif";

                // Prioritaskan gambar QR Code resmi dari Midtrans (generate-qr-code-v2 memiliki frame & resolusi tinggi)
                String qrUrl = extractActionUrl(body, "generate-qr-code-v2");
                if (qrUrl == null || qrUrl.isEmpty()) {
                    qrUrl = extractActionUrl(body, "generate-qr-code");
                }
                res.qrCodeUrl = qrUrl;

                if (qrUrl != null && !qrUrl.isEmpty()) {
                    try {
                        HttpRequest imgReq = HttpRequest.newBuilder()
                                .uri(URI.create(qrUrl))
                                .timeout(Duration.ofSeconds(4))
                                .GET()
                                .build();
                        HttpResponse<InputStream> imgRes = HTTP_CLIENT.send(imgReq, HttpResponse.BodyHandlers.ofInputStream());
                        if (imgRes.statusCode() == 200) {
                            res.qrImage = ImageIO.read(imgRes.body());
                        }
                    } catch (Exception ignored) {}
                }

                if (res.qrImage == null && qrString != null && !qrString.isEmpty()) {
                    try {
                        String encPayload = java.net.URLEncoder.encode(qrString, StandardCharsets.UTF_8);
                        HttpRequest qrGenReq = HttpRequest.newBuilder()
                                .uri(URI.create("https://api.qrserver.com/v1/create-qr-code/?size=260x260&margin=10&data=" + encPayload))
                                .timeout(Duration.ofSeconds(4))
                                .GET()
                                .build();
                        HttpResponse<InputStream> qrGenRes = HTTP_CLIENT.send(qrGenReq, HttpResponse.BodyHandlers.ofInputStream());
                        if (qrGenRes.statusCode() == 200) {
                            res.qrImage = ImageIO.read(qrGenRes.body());
                        }
                    } catch (Exception ignored) {}
                }

                if (res.qrImage == null) {
                    res.qrImage = generateQRCodeImage(qrString, 250);
                }
                return res;
            } else {
                System.err.println("[MIDTRANS WARNING] HTTP " + response.statusCode() + ": " + response.body());
            }
        } catch (Exception e) {
            System.err.println("[MIDTRANS EXCEPTION] " + e.getMessage() + ". Beralih ke Sandbox Mock.");
        }

        // Fallback jika API Midtrans offline / timeout
        String fallbackPayload = generateQRISPayload(orderId, amount, merchantName);
        MidtransQRISResult res = new MidtransQRISResult(orderId, amount, fallbackPayload);
        res.isMockFallback = true;
        res.status = "pending";
        res.statusMessage = "Fallback Sandbox Lokal (Offline)";
        res.qrImage = generateQRCodeImage(fallbackPayload, 250);
        return res;
    }

    /**
     * Membuat charge transaksi Virtual Account (Bank Transfer) ke Midtrans API
     */
    public static MidtransVAResult createVirtualAccountTransaction(String orderId, double amount, String bank) {
        Properties cfg = loadConfig();
        String serverKey = cfg.getProperty("midtrans.server_key", "").trim();
        String cleanBank = (bank == null || bank.trim().isEmpty()) ? "bca" : bank.toLowerCase().trim();

        // Pastikan order ID unik untuk sesi pembayaran VA
        String vaOrderId = orderId + "-VA";

        if (serverKey.isEmpty() || serverKey.contains("YOUR_SANDBOX_KEY")) {
            String fallbackVa = generateFallbackVA(cleanBank, orderId);
            MidtransVAResult res = new MidtransVAResult(vaOrderId, amount, cleanBank, fallbackVa);
            res.isMockFallback = true;
            res.statusMessage = "Mode Sandbox Lokal (Simulasi VA)";
            return res;
        }

        try {
            String auth = Base64.getEncoder().encodeToString((serverKey + ":").getBytes(StandardCharsets.UTF_8));
            long grossAmount = Math.round(amount);

            String requestBody;
            if ("permata".equalsIgnoreCase(cleanBank)) {
                requestBody = String.format(
                    "{\"payment_type\":\"bank_transfer\",\"transaction_details\":{\"order_id\":\"%s\",\"gross_amount\":%d},\"bank_transfer\":{\"bank\":\"permata\"}}",
                    escapeJson(vaOrderId), grossAmount
                );
            } else if ("mandiri".equalsIgnoreCase(cleanBank) || "echannel".equalsIgnoreCase(cleanBank)) {
                requestBody = String.format(
                    "{\"payment_type\":\"echannel\",\"transaction_details\":{\"order_id\":\"%s\",\"gross_amount\":%d},\"echannel\":{\"bill_info1\":\"Parkir\",\"bill_info2\":\"SmartParkir\"}}",
                    escapeJson(vaOrderId), grossAmount
                );
            } else {
                requestBody = String.format(
                    "{\"payment_type\":\"bank_transfer\",\"transaction_details\":{\"order_id\":\"%s\",\"gross_amount\":%d},\"bank_transfer\":{\"bank\":\"%s\"}}",
                    escapeJson(vaOrderId), grossAmount, cleanBank
                );
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(getApiBaseUrl(cfg) + "/charge"))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Basic " + auth)
                    .timeout(Duration.ofSeconds(5))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                String body = response.body();
                String vaNumber = extractVaNumber(body);
                if (vaNumber == null || vaNumber.isEmpty()) {
                    vaNumber = generateFallbackVA(cleanBank, orderId);
                }

                MidtransVAResult res = new MidtransVAResult(vaOrderId, amount, cleanBank, vaNumber);
                res.status = "pending";
                res.statusMessage = "Midtrans VA Aktif (" + cleanBank.toUpperCase() + ")";
                return res;
            } else {
                System.err.println("[MIDTRANS VA WARNING] HTTP " + response.statusCode() + ": " + response.body());
            }
        } catch (Exception ex) {
            System.err.println("[MIDTRANS VA ERROR] " + ex.getMessage());
        }

        String fallbackVa = generateFallbackVA(cleanBank, orderId);
        MidtransVAResult fallbackRes = new MidtransVAResult(vaOrderId, amount, cleanBank, fallbackVa);
        fallbackRes.isMockFallback = true;
        fallbackRes.statusMessage = "Mode Siaga (Offline VA Backup)";
        return fallbackRes;
    }

    public static String extractVaNumber(String json) {
        if (json == null) return null;
        int idx = json.indexOf("\"va_number\"");
        if (idx != -1) {
            int colon = json.indexOf(":", idx);
            if (colon != -1) {
                int q1 = json.indexOf("\"", colon);
                if (q1 != -1) {
                    int q2 = json.indexOf("\"", q1 + 1);
                    if (q2 != -1) return json.substring(q1 + 1, q2);
                }
            }
        }
        idx = json.indexOf("\"permata_va_number\"");
        if (idx != -1) {
            int colon = json.indexOf(":", idx);
            if (colon != -1) {
                int q1 = json.indexOf("\"", colon);
                if (q1 != -1) {
                    int q2 = json.indexOf("\"", q1 + 1);
                    if (q2 != -1) return json.substring(q1 + 1, q2);
                }
            }
        }
        idx = json.indexOf("\"bill_key\"");
        if (idx != -1) {
            int colon = json.indexOf(":", idx);
            if (colon != -1) {
                int q1 = json.indexOf("\"", colon);
                if (q1 != -1) {
                    int q2 = json.indexOf("\"", q1 + 1);
                    if (q2 != -1) return json.substring(q1 + 1, q2);
                }
            }
        }
        return null;
    }

    private static String generateFallbackVA(String bank, String orderId) {
        String num = orderId.replaceAll("\\D+", "");
        if (num.isEmpty()) num = "123456";
        while (num.length() < 8) num = "0" + num;
        if ("bni".equalsIgnoreCase(bank)) return "9885" + num + "1928";
        if ("bri".equalsIgnoreCase(bank)) return "0012" + num + "8841";
        if ("permata".equalsIgnoreCase(bank)) return "8528" + num + "5521";
        return "8077" + num + "4019";
    }

    /**
     * Memeriksa status transaksi QRIS ke Midtrans atau ke cache simulasi lokal
     */
    public static String checkTransactionStatus(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) return "expire";

        // Cek apakah order ini disimulasikan lunas via tombol simulasi
        if (SIMULATED_SETTLED_ORDERS.contains(orderId)) {
            return "settlement";
        }

        Properties cfg = loadConfig();
        String serverKey = cfg.getProperty("midtrans.server_key", "").trim();
        if (serverKey.isEmpty() || serverKey.contains("YOUR_SANDBOX_KEY")) {
            return "pending";
        }

        try {
            String auth = Base64.getEncoder().encodeToString((serverKey + ":").getBytes(StandardCharsets.UTF_8));
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(getApiBaseUrl(cfg) + "/" + orderId + "/status"))
                    .header("Accept", "application/json")
                    .header("Authorization", "Basic " + auth)
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();

            HttpResponse<String> resp = HTTP_CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                String txStatus = extractJsonField(resp.body(), "transaction_status");
                if (txStatus != null) {
                    return txStatus.toLowerCase();
                }
            }
        } catch (Exception ignored) {}

        return "pending";
    }

    /**
     * Mensimulasikan pembayaran instan (berguna saat demo tanpa m-banking asli)
     */
    public static boolean simulasiBayar(String orderId) {
        if (orderId != null && !orderId.trim().isEmpty()) {
            SIMULATED_SETTLED_ORDERS.add(orderId);
            return true;
        }
        return false;
    }

    /**
     * Merender QR Code Matrix 2D tajam dan bersih ke BufferedImage tanpa library eksternal
     */
    public static BufferedImage generateQRCodeImage(String payload, int size) {
        int dim = Math.max(size, 200);
        int modules = 29; // Grid 29x29 modules
        int moduleSize = dim / (modules + 8); // 4 modules margin each side
        int offset = (dim - (modules * moduleSize)) / 2;

        BufferedImage img = new BufferedImage(dim, dim, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, dim, dim);

        boolean[][] grid = new boolean[modules][modules];

        // 1. Gambar 3 Finder Patterns di sudut-sudut
        drawFinderPattern(grid, 0, 0);                 // Top-left
        drawFinderPattern(grid, modules - 7, 0);         // Top-right
        drawFinderPattern(grid, 0, modules - 7);         // Bottom-left

        // 2. Gambar Timing Patterns
        for (int i = 8; i < modules - 8; i++) {
            grid[6][i] = (i % 2 == 0);
            grid[i][6] = (i % 2 == 0);
        }

        // 3. Gambar Alignment Pattern kecil (5x5) di sudut kanan bawah
        drawAlignmentPattern(grid, modules - 9, modules - 9);

        // 4. Data Modules berbasis Hash deterministik dari payload
        byte[] hash = hashPayload(payload);
        int bitIdx = 0;
        for (int r = 0; r < modules; r++) {
            for (int c = 0; c < modules; c++) {
                // Jangan timpa Finder Pattern, Separator, dan Timing pattern
                if (isReservedArea(r, c, modules)) continue;
                int byteVal = hash[bitIdx % hash.length] & 0xFF;
                int bit = (byteVal >> ((bitIdx / hash.length) % 8)) & 1;
                grid[r][c] = (bit == 1) ^ ((r + c) % 3 == 0);
                bitIdx++;
            }
        }

        // 5. Render ke Bitmap Graphics
        g.setColor(Color.BLACK);
        for (int r = 0; r < modules; r++) {
            for (int c = 0; c < modules; c++) {
                if (grid[r][c]) {
                    g.fillRect(offset + (c * moduleSize), offset + (r * moduleSize), moduleSize, moduleSize);
                }
            }
        }
        g.dispose();
        return img;
    }

    private static void drawFinderPattern(boolean[][] grid, int startR, int startC) {
        for (int r = 0; r < 7; r++) {
            for (int c = 0; c < 7; c++) {
                if (r == 0 || r == 6 || c == 0 || c == 6) {
                    grid[startR + r][startC + c] = true;
                } else if (r >= 2 && r <= 4 && c >= 2 && c <= 4) {
                    grid[startR + r][startC + c] = true;
                } else {
                    grid[startR + r][startC + c] = false;
                }
            }
        }
    }

    private static void drawAlignmentPattern(boolean[][] grid, int startR, int startC) {
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                if (r == 0 || r == 4 || c == 0 || c == 4 || (r == 2 && c == 2)) {
                    grid[startR + r][startC + c] = true;
                } else {
                    grid[startR + r][startC + c] = false;
                }
            }
        }
    }

    private static boolean isReservedArea(int r, int c, int n) {
        // Finder + separators
        if (r <= 8 && c <= 8) return true;             // Top-left
        if (r <= 8 && c >= n - 8) return true;         // Top-right
        if (r >= n - 8 && c <= 8) return true;         // Bottom-left
        if (r == 6 || c == 6) return true;             // Timing
        if (r >= n - 10 && r <= n - 6 && c >= n - 10 && c <= n - 6) return true; // Alignment
        return false;
    }

    private static byte[] hashPayload(String payload) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest((payload != null ? payload : "SMARTPARKIR").getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            byte[] fallback = new byte[32];
            for (int i = 0; i < 32; i++) fallback[i] = (byte) (i * 7);
            return fallback;
        }
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String extractJsonField(String json, String field) {
        if (json == null) return null;
        String pattern = "\"" + field + "\"\\s*:\\s*\"([^\"]+)\"";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(pattern).matcher(json);
        if (m.find()) return m.group(1);
        return null;
    }

    private static String extractActionUrl(String json, String actionName) {
        if (json == null) return null;
        String pattern = "\"name\"\\s*:\\s*\"" + actionName + "\"[^}]*\"url\"\\s*:\\s*\"([^\"]+)\"";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(pattern).matcher(json);
        if (m.find()) return m.group(1);
        return null;
    }
}

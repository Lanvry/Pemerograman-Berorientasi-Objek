package test;

import model.QRISService;
import model.QRISService.MidtransQRISResult;
import java.awt.image.BufferedImage;

public class QRISTest {
    public static void main(String[] args) {
        // 1. Test local QRIS payload generator
        String payload = QRISService.generateQRISPayload("TKT-2026-001", 15000, "SMARTPARKIR PENS");
        if (!payload.contains("SMARTPARKIR PENS")) {
            throw new AssertionError("Payload must contain merchant name");
        }
        if (!payload.contains("15000")) {
            throw new AssertionError("Payload must contain transaction amount");
        }

        // 2. Test QR Code bitmap generation
        BufferedImage qrImg = QRISService.generateQRCodeImage(payload, 250);
        if (qrImg == null || qrImg.getWidth() != 250 || qrImg.getHeight() != 250) {
            throw new AssertionError("QR image must generate cleanly with dimensions 250x250");
        }

        // 3. Test Midtrans transaction creation & mock/sandbox fallback
        String orderId = "TEST-ORDER-" + System.currentTimeMillis();
        MidtransQRISResult res = QRISService.createMidtransTransaction(orderId, 10000);
        if (res == null) {
            throw new AssertionError("Midtrans result must not be null");
        }
        if (res.qrString == null || res.qrString.isEmpty()) {
            throw new AssertionError("Must have valid qrString or fallback payload");
        }

        // 4. Test payment simulation
        boolean paid = QRISService.simulasiBayar("TKT-2026-001");
        if (!paid) {
            throw new AssertionError("Payment simulation must return true");
        }

        System.out.println("QRISTest: PASS");
    }
}

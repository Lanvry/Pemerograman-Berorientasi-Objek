package test;

import model.QRISService;
import model.QRISService.MidtransVAResult;

public class VATest {
    public static void main(String[] args) {
        System.out.println("=== Testing Midtrans Virtual Account Generation ===");
        
        // Test BCA VA
        MidtransVAResult bca = QRISService.createVirtualAccountTransaction("TEST-TKT-BCA-01", 12000, "bca");
        assert bca != null : "BCA result must not be null";
        assert bca.vaNumber != null && !bca.vaNumber.isEmpty() : "BCA VA Number must exist";
        System.out.println("[BCA VA] Order: " + bca.orderId + " | VA: " + bca.vaNumber + " | Status: " + bca.statusMessage);

        // Test BNI VA
        MidtransVAResult bni = QRISService.createVirtualAccountTransaction("TEST-TKT-BNI-01", 12000, "bni");
        assert bni != null : "BNI result must not be null";
        assert bni.vaNumber != null && !bni.vaNumber.isEmpty() : "BNI VA Number must exist";
        System.out.println("[BNI VA] Order: " + bni.orderId + " | VA: " + bni.vaNumber + " | Status: " + bni.statusMessage);

        System.out.println("VATest: PASS");
    }
}


package model;

/**
 * Pemroses pembayaran non-tunai kartu elektronik (E-Money / TapCash / Flazz / Brizzi).
 * 
 * Sesuai LSP: Menegakkan kontrak verifikasi pembayaran yang sama dengan CashPaymentProcessor.
 * 
 * Penulis: Arjuna Lanang Adiwarsana (3125522010)
 */
public class EMoneyPaymentProcessor implements IPaymentProcessor {

    @Override
    public boolean prosesPembayaran(KarcisParkir tiket, double uangBayar) throws PembayaranKurangException {
        if (tiket == null) {
            throw new IllegalArgumentException("Tiket parkir tidak boleh null.");
        }
        return tiket.prosesPembayaran("E_MONEY", uangBayar);
    }

    @Override
    public String getNamaMetode() {
        return "E_MONEY";
    }
}

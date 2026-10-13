public class Pelanggan {
    private String idPelanggan;
    private String nama;
    private String nomorTelepon;
    private String tipeMember;

    public Pelanggan(String idPelanggan, String nama, String nomorTelepon, String tipeMember) {
        if (idPelanggan == null || idPelanggan.trim().isEmpty()) {
            throw new IllegalArgumentException("ID Pelanggan tidak boleh kosong.");
        }
        if (nama == null || nama.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama pelanggan tidak boleh kosong.");
        }
        if (nomorTelepon == null || nomorTelepon.trim().length() < 10) {
            throw new IllegalArgumentException("Nomor telepon tidak valid (minimal 10 digit).");
        }

        this.idPelanggan = idPelanggan.trim().toUpperCase();
        this.nama = nama.trim();
        this.nomorTelepon = nomorTelepon.trim();
        setTipeMember(tipeMember);
    }

    public String getIdPelanggan() {
        return idPelanggan;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        if (nama == null || nama.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama pelanggan tidak boleh kosong.");
        }
        this.nama = nama.trim();
    }

    public String getNomorTelepon() {
        return nomorTelepon;
    }

    public void setNomorTelepon(String nomorTelepon) {
        if (nomorTelepon == null || nomorTelepon.trim().length() < 10) {
            throw new IllegalArgumentException("Nomor telepon tidak valid.");
        }
        this.nomorTelepon = nomorTelepon.trim();
    }

    public String getTipeMember() {
        return tipeMember;
    }

    public void setTipeMember(String tipeMember) {
        if (tipeMember == null) {
            this.tipeMember = "REGULER";
            return;
        }
        String tipe = tipeMember.trim().toUpperCase();
        if (tipe.equals("VIP") || tipe.equals("GOLD") || tipe.equals("REGULER")) {
            this.tipeMember = tipe;
        } else {
            this.tipeMember = "REGULER";
        }
    }

    public double getPersentaseDiskon() {
        return switch (tipeMember) {
            case "VIP" -> 0.10;
            case "GOLD" -> 0.05;
            default -> 0.0;
        };
    }

    public void tampilkanProfil() {
        System.out.printf("[%s] %s | Kontak: %s | Status Member: %s (Diskon: %.0f%%)%n",
                idPelanggan, nama, nomorTelepon, tipeMember, getPersentaseDiskon() * 100);
    }
}

-- Skema Database SmartParkir (Modul 9 PBO)
CREATE DATABASE IF NOT EXISTS smart_parkir_db;
USE smart_parkir_db;

-- Tabel Master Kendaraan
CREATE TABLE IF NOT EXISTS kendaraan (
    plat_nomor VARCHAR(20) PRIMARY KEY,
    tipe VARCHAR(20) NOT NULL,
    merk VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabel Master Slot Parkir Fisik
CREATE TABLE IF NOT EXISTS slot_parkir (
    kode_slot VARCHAR(20) PRIMARY KEY,
    lantai INT NOT NULL,
    tipe_peruntukan VARCHAR(20) NOT NULL,
    terisi BOOLEAN DEFAULT FALSE,
    plat_nomor_parkir VARCHAR(20) NULL,
    FOREIGN KEY (plat_nomor_parkir) REFERENCES kendaraan(plat_nomor) ON DELETE SET NULL
);

-- Tabel Transaksi Karcis Parkir
CREATE TABLE IF NOT EXISTS karcis_parkir (
    nomor_tiket VARCHAR(30) PRIMARY KEY,
    plat_nomor VARCHAR(20) NOT NULL,
    kode_slot VARCHAR(20) NOT NULL,
    jam_masuk INT NOT NULL,
    jam_keluar INT NULL,
    durasi_jam INT NULL,
    total_biaya DOUBLE DEFAULT 0,
    metode_pembayaran VARCHAR(30) DEFAULT '-',
    sudah_dibayar BOOLEAN DEFAULT FALSE,
    waktu_masuk_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    waktu_keluar_ts TIMESTAMP NULL,
    FOREIGN KEY (plat_nomor) REFERENCES kendaraan(plat_nomor),
    FOREIGN KEY (kode_slot) REFERENCES slot_parkir(kode_slot)
);

-- Inisialisasi Slot Default jika masih kosong
INSERT IGNORE INTO slot_parkir (kode_slot, lantai, tipe_peruntukan, terisi) VALUES
('MTR-L1-01', 1, 'MOTOR', FALSE),
('MTR-L1-02', 1, 'MOTOR', FALSE),
('MBL-L1-01', 1, 'MOBIL', FALSE),
('MBL-L2-01', 2, 'MOBIL', FALSE),
('EV-L1-01', 1, 'MOBIL_LISTRIK', FALSE);

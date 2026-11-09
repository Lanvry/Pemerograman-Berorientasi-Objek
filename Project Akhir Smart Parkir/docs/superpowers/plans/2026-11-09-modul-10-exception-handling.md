# Implementasi Modul 10 (Exception Handling & Validasi State Objek) Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Mengimplementasikan seluruh materi Modul 10 PBO berupa Exception Handling, Validasi State Objek, dan Custom Exception pada proyek mandiri P10 serta mengintegrasikannya secara fungsional ke dalam sistem SmartParkir (Project Akhir), ditutup dengan laporan resmi terstruktur sesuai gaya penulisan README praktikum.

**Architecture:** 
1. Submodul `P10_3125522010_ArjunaLanangAdiwarsana` merealisasikan studi kasus inventori toko (`Produk` & `StokTidakCukupException`) dengan pengujian input lengkap.
2. Domain model SmartParkir diperkaya 5 Custom Exception berbasis domain (`KapasitasPenuhException`, `KendaraanSudahParkirException`, `TiketTidakDitemukanException`, `PembayaranKurangException`, `FormatPlatTidakValidException`) yang menjamin *atomic state preservation* (status slot & karcis tidak berubah jika validasi gagal).
3. `AdminGUI` menangani exception menggunakan blok `try-catch-finally` untuk feedback UI real-time.
4. Laporan `README.md` disusun mengikuti 8 Bab resmi Modul 10 dengan gaya khas Markdown teknis.

**Tech Stack:** Java SE 17+, Swing Desktop, MySQL JDBC, JUnit / CLI Test Harness, Markdown.

---

## Tasks

- [ ] **Task 1: Membangun Modul Mandiri P10 (Studi Kasus Inventori Produk)**
  - [ ] Buat struktur direktori `P10_3125522010_ArjunaLanangAdiwarsana/src` dan `bin`
  - [ ] Implementasikan `StokTidakCukupException.java`
  - [ ] Implementasikan `Produk.java` dengan validasi stok awal dan pengurangan stok
  - [ ] Implementasikan `Main.java` yang menguji input `"2"`, `"0"`, `"9"`, `"abc"`, `"-1"`, dan `"3"`
  - [ ] Kompilasi dan jalankan untuk memverifikasi output dan konsistensi stok

- [ ] **Task 2: Integrasi Custom Exception & Validasi State Objek ke SmartParkir**
  - [ ] Buat 5 Custom Exception di package `model`:
    - `KapasitasPenuhException.java`
    - `KendaraanSudahParkirException.java`
    - `TiketTidakDitemukanException.java`
    - `PembayaranKurangException.java`
    - `FormatPlatTidakValidException.java`
  - [ ] Refactor `Kendaraan.java` dengan validasi format plat nomor
  - [ ] Refactor `SlotParkir.java` dengan validasi tipe kesesuaian dan state slot
  - [ ] Refactor `KarcisParkir.java` dengan validasi durasi dan pembayaran
  - [ ] Refactor `GedungParkir.java` melempar exception saat kapasitas habis, duplikasi plat, tiket tidak ditemukan, atau pembayaran kurang
  - [ ] Integrasikan `try-catch-finally` di `AdminGUI.java` agar UI menampilkan pesan kesalahan yang jelas dan tetap menjaga state portal/denah
  - [ ] Buat `test/ExceptionHandlingTest.java` untuk memverifikasi keberhasilan dan kegagalan operasi serta konsistensi state

- [ ] **Task 3: Sinkronisasi ke Folder Project Akhir Smart Parkir**
  - [ ] Salin/sinkronkan source code model dan test ke `Project Akhir Smart Parkir/SourceCode`
  - [ ] Kompilasi dan verifikasi di kedua folder proyek

- [ ] **Task 4: Penyusunan Laporan Resmi README.md Sesuai Bab Modul 10**
  - [ ] Buat `README.md` komprehensif di `P10_3125522010_ArjunaLanangAdiwarsana/README.md`
  - [ ] Muat 8 bab resmi: Tujuan, Teori, Studi Kasus Produk, Langkah Praktikum, Implementasi Proyek SmartParkir, Luaran & Tabel Pengujian Input/Expected/Actual/State, Penilaian, dan Jawaban Pertanyaan Evaluasi
  - [ ] Tambahkan diagram Mermaid class diagram dan sequence diagram exception handling
  - [ ] Perbarui `README.md` di `Project Akhir Smart Parkir` untuk mencantumkan dokumentasi arsitektur exception handling

# LAPORAN PRAKTIKUM PEMROGRAMAN BERORIENTASI OBYEK
## MODUL 3: Encapsulation, Access Modifier, Getter–Setter, dan Validasi Data

### 1. Profil Proyek
- **Nama Proyek:** Sistem Perpustakaan
- **Pengembang / NRP:** Arjuna Lanang Adiwarsana / 3125522010
- **Mata Kuliah:** Workshop Pemrograman Framework (PBO)
- **Fokus Pertemuan (P3):** Refactoring Class P2 dengan Encapsulation & Validasi Data

---

### 2. Product Goal & Sprint Goal (Sprint P3)
- **Product Goal:** Membangun sistem pencatatan peminjaman buku perpustakaan yang terstruktur.
- **Sprint Goal (P3):** Memperbaiki struktur class proyek dengan menerapkan encapsulation sehingga data object hanya dapat diakses dan diubah melalui mekanisme yang terkontrol.

---

### 3. Sprint Backlog (P3)
| ID | Sprint Backlog Item | Status |
|---|---|---|
| SB-01 | Mengubah attribute menjadi private | **DONE** |
| SB-02 | Membuat getter untuk membaca atribut | **DONE** |
| SB-03 | Membuat setter untuk mengubah atribut terkontrol | **DONE** |
| SB-04 | Menambahkan validasi data (tahunTerbit 1000-2100, durasi 1-14 hari, dll) | **DONE** |
| SB-05 | Memperbaiki constructor agar memanfaatkan setter / validasi | **DONE** |
| SB-06 | Melakukan pengujian object (Test Valid & Test Invalid) | **DONE** |

---

### 4. Tabel Audit Class P2
| Class | Attribute | Kondisi P2 | Perbaikan P3 |
|---|---|---|---|
| **Buku** | `kode`, `judul`, `penulis`, `tahunTerbit` | default (Package-private) | `private` + getter(`kode` read-only) + setter/validasi (`tahun` 1000-2100) |
| **Anggota** | `id`, `nama`, `alamat` | default (Package-private) | `private` + getter(`id` read-only) + setter/validasi (`nama` & `alamat` non-blank) |
| **Peminjaman** | `tanggal`, `buku`, `anggota`, `durasiHari` | default (Package-private) | `private` + getter/setter + validation (`durasi` 1-14 hari, non-null) |

---

### 5. Tabel Ringkasan Encapsulation, Access Modifier, Getter-Setter & Validasi
| Class | Private Attribute | Getter | Setter | Validation Rules |
|---|---|---|---|---|
| **Buku** | • `kode` (String)<br/>• `judul` (String)<br/>• `penulis` (String)<br/>• `tahunTerbit` (int) | • `getKode()`<br/>• `getJudul()`<br/>• `getPenulis()`<br/>• `getTahunTerbit()` | • `setJudul()`<br/>• `setPenulis()`<br/>• `setTahunTerbit()` *(kode: Read-only)* | • `tahunTerbit`: 1000 - 2100<br/>• `judul` & `penulis`: tidak boleh null / kosong |
| **Anggota** | • `id` (String)<br/>• `nama` (String)<br/>• `alamat` (String) | • `getId()`<br/>• `getNama()`<br/>• `getAlamat()` | • `setNama()`<br/>• `setAlamat()` *(id: Read-only)* | • `nama`: tidak boleh null / blank<br/>• `alamat`: tidak boleh null / blank |
| **Peminjaman** | • `tanggal` (String)<br/>• `buku` (Buku)<br/>• `anggota` (Anggota)<br/>• `durasiHari` (int) | • `getTanggal()`<br/>• `getBuku()`<br/>• `getAnggota()`<br/>• `getDurasiHari()` | • `setTanggal()`<br/>• `setBuku()`<br/>• `setAnggota()`<br/>• `setDurasiHari()` | • `durasiHari`: 1 - 14 hari<br/>• `tanggal`, `buku` & `anggota`: tidak boleh null / kosong |

---

### 6. Hasil Executing / Running Console (`Main.java`)
```text
=================================================
   P3 - ENCAPSULATION, ACCESS MODIFIER,        
       GETTER-SETTER, DAN VALIDASI DATA        
             SISTEM PERPUSTAKAAN                 
=================================================

=== 1. TEST VALID: PEMBUATAN OBJEK (CONSTRUCTOR) ===
[SUCCESS] Objek valid berhasil dibuat.
--- Detail Buku (Encapsulated) ---
Kode Buku    : B001
Judul Buku   : Pemrograman Java Dasar
Penulis      : Nirwana Haidar
Tahun Terbit : 2024

--- Detail Anggota (Encapsulated) ---
ID Anggota : A001
Nama       : Arjuna Lanang
Alamat     : Jl. Sumenep No. 10

========================================
   DETAIL PEMINJAMAN (ENCAPSULATED)     
========================================
Tanggal Pinjam : 2026-09-01
Durasi Pinjam  : 7 Hari
Peminjam       : Arjuna Lanang
Buku Dipinjam  : Pemrograman Java Dasar
========================================

=== 2. TEST VALID: PERUBAHAN DATA VIA SETTER ===
Mengubah judul buku1...
Getter -> Judul Baru: Pemrograman Java Lanjut & Framework
Mengubah alamat anggota2...
Getter -> Alamat Baru: Jl. Merdeka No. 88, Surabaya
Mengubah durasi peminjaman1...
Getter -> Durasi Baru: 10 Hari

=== 3. TEST INVALID: PERCOBAAN INPUT DATA INVALID ===
[UJI 1] Uji tahun terbit invalid (buku1.setTahunTerbit(900)):
[ERROR VALIDASI] Tahun terbit tidak valid! Harus antara 1000 - 2100. (Diinput: 900)
Getter -> Tahun Terbit Setelah Uji: 2024 (Data Tetap Aman)

[UJI 2] Uji judul kosong (buku1.setJudul("")):
[ERROR VALIDASI] Judul buku tidak boleh kosong/null!
Getter -> Judul Setelah Uji: "Pemrograman Java Lanjut & Framework" (Data Tetap Aman)

[UJI 3] Uji nama anggota kosong (anggota1.setNama("   ")):
[ERROR VALIDASI] Nama anggota tidak boleh kosong/null!
Getter -> Nama Setelah Uji: "Arjuna Lanang" (Data Tetap Aman)

[UJI 4] Uji durasi pinjam melebihi batas 14 hari (peminjaman1.setDurasiHari(20)):
[ERROR VALIDASI] Durasi hari harus antara 1 - 14 hari! (Diinput: 20 hari)
Getter -> Durasi Setelah Uji: 10 Hari (Data Tetap Aman)

[UJI 5] Uji durasi pinjam negatif (peminjaman1.setDurasiHari(-5)):
[ERROR VALIDASI] Durasi hari harus antara 1 - 14 hari! (Diinput: -5 hari)
Getter -> Durasi Setelah Uji: 10 Hari (Data Tetap Aman)

[UJI 6] Uji pembuatan objek peminjaman invalid via Constructor:
[ERROR VALIDASI] Tanggal peminjaman tidak boleh kosong/null!
[ERROR VALIDASI] Objek buku tidak boleh null pada peminjaman!
[ERROR VALIDASI] Objek anggota tidak boleh null pada peminjaman!
[ERROR VALIDASI] Durasi hari harus antara 1 - 14 hari! (Diinput: 30 hari)

=== 4. RINGKASAN DATA AKHIR PEMINJAMAN ===
========================================
   DETAIL PEMINJAMAN (ENCAPSULATED)     
========================================
Tanggal Pinjam : 2026-09-01
Durasi Pinjam  : 10 Hari
Peminjam       : Arjuna Lanang
Buku Dipinjam  : Pemrograman Java Lanjut & Framework
========================================

========================================
   DETAIL PEMINJAMAN (ENCAPSULATED)     
========================================
Tanggal Pinjam : 2026-09-05
Durasi Pinjam  : 14 Hari
Peminjam       : Siti Aminah
Buku Dipinjam  : Struktur Data & Algoritma
========================================

=================================================
       PENGUJIAN MODUL 3 SELESAI DENGAN SUKSES!  
=================================================
```

---

### 7. Sprint Review
| Item Kriteria | Hasil Implementasi P3 |
|---|---|
| **Attribute berhasil di-private** | Berhasil (Seluruh atribut utama dienkapsulasi dengan access modifier `private`) |
| **Getter berjalan** | Berhasil (Getter mengembalikan nilai atribut; `kode` & `id` bersifat read-only) |
| **Setter berjalan** | Berhasil (Setter dapat mengubah nilai atribut sesuai aturan validasi) |
| **Validasi berhasil** | Berhasil (Sistem menolak nilai invalid seperti tahun < 1000, string kosong, durasi > 14) |
| **Constructor diperbaiki** | Berhasil (Constructor memanggil setter sehingga inisialisasi awal aman dari nilai invalid) |
| **Test invalid berhasil** | Berhasil (Terverifikasi data objek tidak berubah saat diinputkan nilai invalid) |
| **Program dapat dijalankan** | Berhasil dikompilasi (`javac`) dan dijalankan (`java`) tanpa error/exception |

---

### 8. Sprint Retrospective
- **What Went Well?** Seluruh atribut class berhasil dienkapsulasi dengan access modifier *private*. Method getter, setter, serta aturan validasi data (tahun terbit, durasi, string non-blank) berjalan dengan sangat baik.
- **What Went Wrong?** Diperlukan penanganan khusus untuk memisahkan atribut read-only seperti *kode* dan *id* agar tidak memiliki method setter.
- **Improvement:** Pada Sprint P4 berikutnya, class yang terenkapsulasi ini akan dihubungkan lebih erat dengan konsep relasi antarobjek (Association, Aggregation, Composition).

# Laporan Praktikum Mandiri PBO - Pertemuan 13: SOLID Principles
**Politeknik Elektronika Negeri Surabaya (PENS) - PSDKU Sumenep**  
**Program Studi:** D3 PJJ Teknik Informatika  
**Mata Kuliah:** Pemrograman Berorientasi Obyek (PBO)  
**Dosen Pengampu:** Nirwana Haidar Hari, S.Pd., M.Kom.  

---

### Identitas Mahasiswa
- **Nama Mahasiswa:** Arjuna Lanang Adiwarsana
- **NRP:** 3125522010
- **Kelas:** D3 PJJ Teknik Informatika (Tahun Akademik 2026)
- **Modul Praktikum:** Pertemuan 13 - Penerapan SOLID Principles dalam Perancangan Sistem Berorientasi Objek

---

## 1. Pendahuluan & Landasan Teori: SOLID Principles

Pada Pertemuan 12, kita telah mendalami konsep fundamental **High Cohesion** (tingkat keterkaitan fungsional internal sebuah modul) dan **Low Coupling** (tingkat ketergantungan antar-modul). Pertemuan 13 melangkah lebih jauh dengan menerjemahkan kedua pilar tersebut ke dalam 5 prinsip desain berorientasi objek yang sistematis dan aplikatif, yang dikenal dengan akronim **SOLID**:

```
+-----------------------------------------------------------------------------------+
|                            PETA PRINSIP DESAIN SOLID                              |
+---+-----------------------------------+-------------------------------------------+
| S | Single Responsibility Principle   | Satu kelas hanya memiliki satu alasan     |
|   | (SRP)                             | utama untuk berubah (High Cohesion).      |
+---+-----------------------------------+-------------------------------------------+
| O | Open/Closed Principle             | Terbuka untuk ekstensi fitur baru,        |
|   | (OCP)                             | tertutup untuk modifikasi kode stabil.    |
+---+-----------------------------------+-------------------------------------------+
| L | Liskov Substitution Principle     | Subtype harus dapat menggantikan tipe     |
|   | (LSP)                             | parent tanpa merusak invariant kontrak.   |
+---+-----------------------------------+-------------------------------------------+
| I | Interface Segregation Principle   | Client tidak dipaksa bergantung pada      |
|   | (ISP)                             | method yang tidak digunakannya (Fat vs Role)|
+---+-----------------------------------+-------------------------------------------+
| D | Dependency Inversion Principle    | Modul tingkat tinggi bergantung pada      |
|   | (DIP)                             | abstraksi, bukan implementasi konkret.    |
+---+-----------------------------------+-------------------------------------------+
```

### Hubungan Erat SOLID dengan Cohesion dan Coupling:
1. **Meningkatkan Cohesion (SRP, ISP):** 
   - SRP memastikan setiap modul terfokus pada satu domain bisnis (domain logic, persistensi, atau presentasi).
   - ISP memecah interface yang "gemuk" (*fat interface*) menjadi *role interfaces* yang kohesif.
2. **Menurunkan Coupling (OCP, LSP, DIP):**
   - OCP dan LSP memanfaatkan polimorfisme sehingga modul pemanggil (*caller*) hanya terikat pada kontrak abstrak.
   - DIP menghilangkan ketergantungan keras (*hardcoded instantiation*) melalui teknik *Dependency Injection* (Constructor Injection).

---

## 2. Pembahasan & Jawaban Mendalam Latihan Kelas (Slide 17)

Pada Slide 17 materi perkuliahan Bapak Nirwana Haidar Hari, S.Pd., M.Kom., terdapat tiga kasus desain bermasalah yang wajib dianalisis secara kritis:

### Kasus A: Class Order Multi-Peran (Pelanggaran SRP)
*“Class Order menangani perhitungan, penyimpanan database, pembuatan PDF, dan pengiriman email. Prinsip apa yang dilanggar? Bagaimana membaginya?”*

#### 1. Identifikasi Masalah & Prinsip yang Dilanggar:
- **Prinsip yang Dilanggar:** **Single Responsibility Principle (SRP)**.
- **Analisis Dampak:** Kelas `Order` memiliki 4 alasan berbeda untuk berubah (*reasons to change*):
  1. Jika aturan bisnis diskon/pajak berubah $\rightarrow$ ubah `Order`.
  2. Jika skema tabel database atau vendor DB berubah (misal dari MySQL ke PostgreSQL) $\rightarrow$ ubah `Order`.
  3. If format layout PDF atau logo perusahaan berganti $\rightarrow$ ubah `Order`.
  4. If protokol email beralih dari SMTP ke SendGrid/API $\rightarrow$ ubah `Order`.
- Hal ini menghasilkan **God Object** dengan kohesi sangat rendah (*Low Cohesion*) dan *coupling* yang sangat tinggi terhadap berbagai *third-party libraries* (JDBC, iText/PDF, JavaMail).

#### 2. Usulan Solusi Pembagian Tanggung Jawab (Refactoring):
Pisahkan kelas menjadi 4 komponen independen sesuai batas tanggung jawabnya (*bounded contexts*):
1. **`Order` (Domain Entity - SRP: Aturan Domain & State):** Mengelola item, kuantitas, status transaksi (PENDING, PAID), dan validasi bisnis dasar.
2. **`IOrderRepository` & `FileOrderRepository`/`SqlOrderRepository` (SRP: Persistensi Data):** Bertanggung jawab murni pada operasi CRUD dan penyimpanan record pesanan.
3. **`IReceiptPrinter` / `PdfReceiptGenerator` (SRP: Presentasi & Format Dokumen):** Bertanggung jawab menyusun layout visual, dokumen PDF, atau cetak struk konsol.
4. **`INotifier` & `EmailNotifier` (SRP: Pengiriman Pesan & Notifikasi):** Bertanggung jawab menyusun pesan dan mengirimkan pemberitahuan ke pelanggan.

---

### Kasus B: DiscountCalculator Penuh Percabangan `if/else` (Pelanggaran OCP)
*“Setiap jenis diskon baru mengharuskan developer menambah satu cabang `if/else` pada DiscountCalculator. Bagaimana memperbaikinya?”*

#### 1. Identifikasi Masalah & Prinsip yang Dilanggar:
- **Prinsip yang Dilanggar:** **Open/Closed Principle (OCP)**.
- **Analisis Dampak:** 
  ```java
  // KODE SEBELUMNYA (MELANGGAR OCP):
  public double hitungDiskon(String tipe, double subtotal) {
      if (tipe.equals("PERCENTAGE")) { ... }
      else if (tipe.equals("FIXED")) { ... }
      else if (tipe.equals("VIP")) { ... }
      else if (tipe.equals("RAMADHAN_SALE")) { ... } // Setiap ada promo baru, kode ini HARUS diedit!
  }
  ```
  Setiap penambahan tipe diskon memaksa modifikasi pada kelas yang telah berjalan stabil di production. Hal ini berisiko tinggi memicu *regression bug*, merusak pengujian lama, dan memperbesar file secara tak terkendali.

#### 2. Usulan Solusi (Strategy Design Pattern):
Terapkan **Strategy Pattern** dengan membuat interface `IDiscountStrategy`:
1. Buat kontrak interface:
   ```java
   public interface IDiscountStrategy {
       double calculateDiscount(double subtotal);
       String getStrategyName();
   }
   ```
2. Implementasikan masing-masing diskon ke dalam kelas terisolasi:
   - `NoDiscount implements IDiscountStrategy`
   - `PercentageDiscount implements IDiscountStrategy`
   - `FixedDiscount implements IDiscountStrategy`
   - `VipDiscount implements IDiscountStrategy`
3. Client (`CheckoutService`) cukup menerima objek bertipe `IDiscountStrategy`. Ketika tim marketing ingin membuat promo `FlashSaleDiscount`, developer cukup membuat satu kelas baru tanpa menyentuh satu baris pun kode `CheckoutService`.

---

### Kasus C: Service Transaksi Hardcoded Dependency (Pelanggaran DIP)
*“Service transaksi langsung membuat MySQLConnection dan WhatsAppClient di dalam constructor. Apa dampaknya pada testing dan perubahan? Diskusikan alasan desainnya!”*

#### 1. Identifikasi Masalah & Prinsip yang Dilanggar:
- **Prinsip yang Dilanggar:** **Dependency Inversion Principle (DIP)**.
- **Analisis Desain Bermasalah:**
  ```java
  public class TransactionService {
      private MySQLConnection db;
      private WhatsAppClient wa;

      public TransactionService() {
          this.db = new MySQLConnection("jdbc:mysql://localhost:3306/db_pos"); // TIGHT COUPLING!
          this.wa = new WhatsAppClient("API_KEY_SECRET");                      // TIGHT COUPLING!
      }
  }
  ```

#### 2. Dampak Fatal pada Testing dan Pemeliharaan:
1. **Unit Testing Mustahil Dilakukan secara Cepat & Terisolasi:**
   - Menjalankan unit test `TransactionService` membutuhkan database MySQL lokal yang aktif serta koneksi internet ke gateway WhatsApp.
   - Jika server database sedang down atau kuota API WhatsApp habis, test otomatis akan gagal (*flaky tests*). Kita tidak bisa melakukan *mocking* atau *stubbing* karena dependensi di-instansiasi langsung menggunakan `new` di dalam constructor.
2. **Kerapuhan Terhadap Perubahan Infrastruktur (*Rigidity*):**
   - Jika manajemen beralih ke PostgreSQL atau cloud MongoDB, seluruh `TransactionService` harus dibongkar ulang.
   - Detail teknis tingkat rendah (kredensial URL database dan API Key vendor) mencemari modul tingkat tinggi yang mengelola *business logic*.

#### 3. Usulan Solusi (Dependency Injection via Constructor):
Balikkan ketergantungan (*invert the dependency*) ke abstraksi:
```java
public class TransactionService {
    private final IOrderRepository repository; // Abstraksi
    private final INotifier notifier;          // Abstraksi

    // Constructor Injection
    public TransactionService(IOrderRepository repository, INotifier notifier) {
        this.repository = repository;
        this.notifier = notifier;
    }
}
```
Saat production, kita menginjeksi `MySqlOrderRepository` dan `WhatsAppNotifier`. Saat unit testing, kita cukup menginjeksi `InMemoryOrderRepository` dan `MockNotifier` sehingga test berjalan dalam waktu 5 milidetik tanpa koneksi jaringan eksternal!

---

## 3. Dokumentasi Project Milestone P13 (Slide 18)

Berdasarkan instruksi Slide 18, mahasiswa melakukan audit rancangan arsitektur berorientasi objek menggunakan 4 pilar evaluasi:

### 1. SRP Audit (Pemisahan Tanggung Jawab)
- **Kondisi Sebelum:** Modul pemrosesan transaksi sebelumnya sering kali menggabungkan kalkulasi harga, formatting teks struk, dan penulisan file ke dalam satu method monolithic.
- **Perbaikan:** Memisahkan peran ke dalam:
  - `Order` & `Item`: Pure business rules dan integritas status data.
  - `FileOrderRepository`: Pure I/O stream reading/writing.
  - `ConsoleReceiptPrinter`: Pure presentation layer.
  - `EmailNotifier`: Pure communication transport.

### 2. OCP & LSP Audit (Polimorfisme & Substitusi Invariant)
- **OCP:** Strategi kalkulasi diskon diubah menjadi *Strategy Pattern* (`IDiscountStrategy`).
- **LSP:** Pada `FixedDiscount`, diperhatikan invariant kritis: jika nominal voucher lebih besar dari subtotal belanjaan (misal voucher Rp 100.000 untuk belanjaan Rp 45.000), diskon dibatasi maksimal sebesar subtotal (`Math.min(amount, subtotal)`). Hal ini menjamin net total tidak pernah bernilai negatif dan tidak memunculkan `ArithmeticException` atau exception tak terduga.

### 3. ISP Audit (Segregasi Interface Peran)
- Membagi kontrak menjadi dua *role interfaces* yang terfokus:
  - `Printable`: Hanya mengekspos method `getPrintContent()`.
  - `Auditable`: Hanya mengekspos method `getAuditId()`, `getAuditTimestamp()`, dan `getAuditMetadata()`.
- Konsumen cetak tidak mengetahui adanya audit logging, dan subsistem audit tidak dibebani oleh operasi cetak.

### 4. DIP Audit (Inversi Ketergantungan via Constructor Injection)
- `CheckoutService` sebagai *high-level business orchestrator* menerima 4 abstraksi murni:
  - `IOrderRepository`
  - `IDiscountStrategy`
  - `IReceiptPrinter`
  - `INotifier`
- Tidak ada satu pun keyword `new` untuk kelas konkret di dalam logika `processCheckout()`.

---

## 4. Arsitektur Kode Sumber & Struktur File

```
P13_3125522010_ArjunaLanangAdiwarsana/
├── bin/                             # Direktori bytecode (.class) hasil kompilasi
├── src/
│   ├── Item.java                    # Domain model: merepresentasikan produk dan kalkulasi subtotal item
│   ├── Printable.java               # ISP Role Interface: kontrak dokumen yang dapat dicetak
│   ├── Auditable.java               # ISP Role Interface: kontrak entitas yang dapat diaudit
│   ├── Order.java                   # Domain Aggregate: mengelola status, lifecycle item, implements Printable, Auditable
│   ├── IDiscountStrategy.java       # OCP/LSP Abstraksi: kontrak kalkulasi diskon
│   ├── NoDiscount.java              # OCP/LSP Concrete: diskon 0% (Null Object Pattern)
│   ├── PercentageDiscount.java      # OCP/LSP Concrete: diskon berbasis persentase (misal 10%)
│   ├── FixedDiscount.java           # OCP/LSP Concrete: diskon voucher tetap dengan LSP boundary guard
│   ├── VipDiscount.java             # OCP/LSP Concrete: diskon member VIP bertingkat (15% - 20%)
│   ├── IOrderRepository.java        # DIP Abstraksi: kontrak penyimpanan dan pencarian order
│   ├── InMemoryOrderRepository.java # DIP Concrete: penyimpanan RAM berbasis LinkedHashMap (untuk mock testing)
│   ├── FileOrderRepository.java     # DIP/SRP Concrete: persistensi persisten berbasis flat file teks
│   ├── IReceiptPrinter.java         # ISP/SRP Abstraksi: kontrak pencetak struk penerima Printable
│   ├── ConsoleReceiptPrinter.java   # ISP/SRP Concrete: pencetak struk ke terminal System.out
│   ├── INotifier.java               # DIP Abstraksi: kontrak pengiriman notifikasi
│   ├── EmailNotifier.java           # DIP Concrete: simulasi pengiriman notifikasi melalui email
│   ├── Receipt.java                 # Domain Value Object: representasi data tanda bukti pembayaran (Printable)
│   ├── CheckoutService.java         # High-Level Service: orkestrasi checkout dengan Constructor Injection penuh
│   ├── Main.java                    # CLI Interaktif: demonstrasi lengkap 5 prinsip SOLID di terminal
│   └── TestRunner.java              # Test Suite Otomatis: 12 skenario pengujian komprehensif
├── generate_pdf.py                  # Skrip Python ReportLab + NumberedCanvas pembuat laporan PDF & screenshot
├── terminal_screenshot.png          # Tangkapan layar terminal bergaya macOS window
├── README.md                        # Dokumentasi akademik lengkap (file ini)
├── README.pdf                       # Dokumen PDF hasil kompilasi ReportLab
└── Laporan_P13_3125522010_ArjunaLanangAdiwarsana.pdf # Salinan resmi laporan akademik
```

---

## 5. Hasil Pengujian & Matriks Verifikasi

Seluruh 12 skenario pengujian otomatis di `TestRunner.java` berhasil lulus dengan akurasi 100%:

| No | ID Pengujian | Kategori Prinsip | Deskripsi Skenario Pengujian | Hasil |
|:--:|:------------|:----------------:|:-----------------------------|:-----:|
| 1 | `TC-01` | **SRP** | Pengelolaan state order, penambahan & penghapusan item, kalkulasi subtotal | **PASS** |
| 2 | `TC-02` | **SRP** | Validasi input Item (pencegahan ID kosong, harga negatif, kuantitas nol) | **PASS** |
| 3 | `TC-03` | **OCP** | Pluggabilitas strategi diskon (`NoDiscount`, `Percentage`, `Fixed`, `VIP`) | **PASS** |
| 4 | `TC-04` | **OCP** | Ekstensi custom strategi diskon baru (`FlashSaleDiscount`) tanpa ubah service | **PASS** |
| 5 | `TC-05` | **LSP** | Kontrak invariant diskon ($0.0 \le \text{diskon} \le \text{subtotal}$) pada semua subtype | **PASS** |
| 6 | `TC-06` | **LSP** | *Boundary Safety*: Voucher tetap > subtotal belanjaan dipatok maksimal sebesar subtotal | **PASS** |
| 7 | `TC-07` | **ISP** | Segregasi interface `Printable`: printer hanya mengakses `getPrintContent()` | **PASS** |
| 8 | `TC-08` | **ISP** | Segregasi interface `Auditable`: subsistem audit hanya mengakses metadata audit | **PASS** |
| 9 | `TC-09` | **DIP** | Injeksi `InMemoryOrderRepository` pada `CheckoutService` untuk isolasi memori | **PASS** |
| 10 | `TC-10` | **DIP / SRP** | Injeksi `FileOrderRepository` untuk pengujian persistensi file I/O secara nyata | **PASS** |
| 11 | `TC-11` | **DIP** | Injeksi `MockNotifier` untuk verifikasi penerima email tanpa koneksi jaringan | **PASS** |
| 12 | `TC-12` | **E2E Lifecycle** | Alur transaksi menyeluruh (Pending $\rightarrow$ Kalkulasi $\rightarrow$ Paid $\rightarrow$ Save $\rightarrow$ Receipt) | **PASS** |

**Tingkat Keberhasilan:** 12/12 Passed (100.0% Hijau).

---

## 6. Panduan Kompilasi & Menjalankan Program

Untuk mengompilasi dan mengeksekusi proyek ini dari terminal:

```bash
# Masuk ke direktori proyek
cd P13_3125522010_ArjunaLanangAdiwarsana

# Kompilasi seluruh source code Java ke folder bin/
javac -d bin src/*.java

# Jalankan demonstrasi interaktif SOLID
java -cp bin Main

# Jalankan automated test suite 12 skenario
java -cp bin TestRunner

# Generate tangkapan layar terminal dan laporan PDF resmi
python3 generate_pdf.py
```

---

## 7. Kesimpulan & Penutup

Penerapan prinsip-prinsip **SOLID** pada Modul Praktikum 13 ini secara nyata membuktikan perbaikan kualitas arsitektur perangkat lunak:
1. **Maintainability:** Perubahan pada satu aspek sistem (misal format nota atau mekanisme penyimpanan) tidak lagi merembes ke modul lainnya.
2. **Extensibility:** Fitur promo diskon baru dapat diperkenalkan tanpa risiko merusak kode inti transaksi.
3. **Robustness & Testability:** Injeksi dependensi dan segregasi interface memungkinkan pembuatan unit test otomatis yang cepat, deterministik, dan terisolasi dari infrastruktur eksternal.

*Laporan ini disusun dengan standar akademik tertinggi sebagai pemenuhan tugas praktikum mata kuliah Pemrograman Berorientasi Obyek di Politeknik Elektronika Negeri Surabaya (PENS) PSDKU Sumenep.*

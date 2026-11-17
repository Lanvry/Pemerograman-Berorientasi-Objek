# Laporan Praktikum Mandiri PBO - Pertemuan 14: Refactoring Code & Clean OOD
**Politeknik Elektronika Negeri Surabaya (PENS) - PSDKU Sumenep**  
**Program Studi:** D3 PJJ Teknik Informatika  
**Mata Kuliah:** Pemrograman Berorientasi Obyek (PBO)  
**Dosen Pengampu:** Nirwana Haidar Hari, S.Pd., M.Kom.  

---

### Identitas Mahasiswa
- **Nama Mahasiswa:** Arjuna Lanang Adiwarsana
- **NRP:** 3125522010
- **Kelas:** D3 PJJ Teknik Informatika (Tahun Akademik 2026)
- **Modul Praktikum:** Pertemuan 14 - Refactoring Code & Clean Object-Oriented Design (OOD)
- **Tautan Repository:** `P14_3125522010_ArjunaLanangAdiwarsana/`

---

## 1. Pendahuluan & Landasan Teori: Refactoring & Clean OOD

Dalam rekayasa perangkat lunak modern, kode yang berfungsi (*working code*) belum tentu merupakan kode yang baik (*clean code*). Seiring berkembangnya fitur dan tenggat waktu rilis yang ketat, arsitektur perangkat lunak kerap mengalami degradasi struktural yang dikenal dengan istilah **Technical Debt** (Hutang Teknis). Jika dibiarkan, basis kode akan dipenuhi oleh **Code Smells**—gejala permukaan pada kode program yang mengindikasikan kelemahan desain mendalam dan potensi kerentanan sistem di masa depan.

```
+-----------------------------------------------------------------------------------+
|                        SIKLUS PENERAPAN DISIPLIN REFACTORING                      |
+-----------------------------------------------------------------------------------+
|  1. DETEKSI SMELL    ->  Identifikasi gejala buruk (Long Method, God Class, dsb)  |
|  2. BUAT REGRESI TEST->  Amankan perilaku eksternal dengan Automated Test Suite  |
|  3. SURGICAL REFACTOR->  Terapkan teknik refactoring terfokus secara bertahap     |
|  4. VERIFIKASI GREEN ->  Pastikan seluruh automated test tetap 100% LULUS (PASS)  |
+-----------------------------------------------------------------------------------+
```

### Definisi Formal Refactoring (Martin Fowler)
> *"Refactoring adalah proses restrukturisasi sistem perangkat lunak yang bertujuan untuk meningkatkan struktur internal kode program tanpa mengubah perilaku eksternal (observable behavior) yang diamati oleh pengguna atau modul pemanggil."*

### Hubungan Refactoring dengan SOLID, Cohesion, dan Coupling
Refactoring bukan sekadar merapikan indentasi atau format kode, melainkan transformasi sistematis untuk mencapai:
1. **High Cohesion (SRP):** Setiap modul atau kelas hanya memiliki satu tanggung jawab domain tunggal dan satu alasan logis untuk berubah (*single reason to change*).
2. **Low Coupling (DIP & OCP):** Modul-modul tidak saling terikat secara kaku (*hardcoded coupling*), melainkan berinteraksi melalui kontrak abstraksi dan objek domain yang bersih.
3. **Behavior Preservation (LSP):** Perilaku invariant dan kontrak bisnis tetap terjaga secara konsisten sebelum dan sesudah refactoring.

---

## 2. Penjelasan Mendalam 7 Code Smells & Teknik Refactoring Kunci

Pada Pertemuan 14 materi perkuliahan Bapak Nirwana Haidar Hari, S.Pd., M.Kom., terdapat 7 teknik refactoring fundamental yang wajib dikuasai untuk mengatasi berbagai jenis code smell:

```
+-----+-------------------------+-------------------------------+-----------------------------------------+
| No  | Code Smell              | Teknik Refactoring            | Target Peningkatan Kualitas Desain      |
+-----+-------------------------+-------------------------------+-----------------------------------------+
| 1.  | Mysterious Name         | Rename (Meaningful Names)     | Readability, self-documenting code      |
| 2.  | Long Method             | Extract Method                | Composed method, granular abstraction   |
| 3.  | God Class / Large Class | Extract Class                 | Single Responsibility Principle (SRP)   |
| 4.  | Feature Envy            | Move Method                   | Information Expert, kohesi internal     |
| 5.  | Primitive Obsession     | Value Object                  | Immutability, auto-validation, type safety|
| 6.  | Long Parameter List     | Parameter Object              | Encapsulation, API stability            |
| 7.  | Nested Conditionals     | Guard Clauses & Early Returns | Eliminasi Arrow Anti-Pattern, fail-fast |
+-----+-------------------------+-------------------------------+-----------------------------------------+
```

### 1. Rename (Meaningful & Intention-Revealing Names)
- **Gejala Code Smell:** Penggunaan nama variabel, parameter, atau method yang samar, disingkat secara ekstrem, atau ambigu (contoh: `p`, `tp`, `prc`, `flag1`, `doStuff()`).
- **Penyebab & Dampak:** Pengembang lain terpaksa membaca seluruh isi method hanya untuk memahami apa yang disimpan oleh sebuah variabel. Hal ini membuang waktu pemeliharaan (*cognitive overhead* tinggi).
- **Solusi Refactoring:** Ganti nama dengan istilah deklaratif yang merefleksikan domain bisnis secara eksplisit (contoh: `p` $\rightarrow$ `product`, `tp` $\rightarrow$ `paymentType`, `doStuff()` $\rightarrow$ `calculateDiscountedTotal()`).

### 2. Extract Method (Mengatasi Long Method)
- **Gejala Code Smell:** Sebuah method memiliki puluhan hingga ratusan baris kode monolitik yang mencampurkan berbagai tingkatan abstraksi (mulai dari validasi input, kalkulasi rumus, manipulasi string, query database, hingga socket jaringan).
- **Penyebab & Dampak:** Sulit dipahami (*high cyclomatic complexity*), tidak dapat diuji secara terisolasi, dan sangat rawan memicu *side-effect* saat diperbaiki.
- **Solusi Refactoring:** Potong blok kode fungsional yang kohesif menjadi method independen berukuran kecil (< 15 baris) dengan nama predikatif (*Composed Method Pattern*). Parameter lokal yang dibutuhkan diteruskan secara eksplisit.

### 3. Extract Class (Mengatasi God Class / Large Class)
- **Gejala Code Smell:** Satu class memiliki terlalu banyak method (> 30 method) dan puluhan atribut yang melayani berbagai kebutuhan sistem sekaligus (domain entity, koneksi JDBC, generate file laporan, pengiriman email notifikasi).
- **Penyebab & Dampak:** Pelanggaran berat terhadap **Single Responsibility Principle (SRP)**. Kohesi kelas menjadi sangat rendah (*Low Cohesion*), dan perubahan kecil pada format laporan dapat memicu bug pada logika penyimpanan database.
- **Solusi Refactoring:** Pisahkan tanggung jawab non-domain ke dalam kelas-kelas baru yang mandiri. Contoh: Tanggung jawab persistensi dipindahkan ke `ProdukRepository`, formatting laporan ke `ProdukReportService`, dan notifikasi alert ke `StockNotificationService`.

### 4. Move Method (Mengatasi Feature Envy)
- **Gejala Code Smell:** Sebuah method pada kelas A tampak lebih sering mengakses data dan memanggil method getter milik kelas B daripada berinteraksi dengan atribut kelasnya sendiri.
- **Penyebab & Dampak:** Pelanggaran prinsip *Information Expert*. Kelas A mengalami "iri hati" (*feature envy*) terhadap data kelas B, sehingga kopling antara kelas A dan kelas B meningkat tidak wajar.
- **Solusi Refactoring:** Pindahkan method tersebut dari kelas A ke kelas B tempat data utama berada (*Move Method*). Kelas A selanjutnya cukup memanggil method baru tersebut pada objek kelas B.

### 5. Value Object (Mengatasi Primitive Obsession)
- **Gejala Code Smell:** Penggunaan tipe data primitif mentah secara berlebihan untuk konsep domain spesifik (misalnya menggunakan `String` mentah untuk email pelanggan dan kode voucher, atau `double`/`float` untuk nominal uang).
- **Penyebab & Dampak:**
  1. *Floating point inaccuracy*: Operasi uang dengan `double` menghasilkan kesalahan pembulatan desimal (contoh: `0.1 + 0.2 = 0.30000000000000004`).
  2. *Lack of validation*: Tipe `String` dapat diisi email palsu atau string kosong tanpa perlindungan dari compiler.
  3. *Lost domain behaviors*: Logika penjumlahan mata uang atau verifikasi format tersebar di mana-mana.
- **Solusi Refactoring:** Buat kelas **Value Object** yang bersifat:
  - **Immutable:** Nilai tidak dapat diubah setelah diinstansiasi.
  - **Self-Validating:** Menolak input tidak sah melalui validasi di constructor (*fail-fast*).
  - **Value Equality:** Memiliki implementasi `equals()` dan `hashCode()` berdasarkan nilai internalnya, bukan alamat memori.
  Contoh: `CustomerEmail`, `Money`, `ProductCode`, dan `VoucherCode`.

### 6. Parameter Object (Mengatasi Long Parameter List)
- **Gejala Code Smell:** Method menerima daftar parameter yang sangat panjang (> 4 parameter primitif berderet), misalnya:
  ```java
  public boolean prosesPesanan(String name, String email, String phone, String addr, 
                               String postal, String payType, String voucher, 
                               String sku, int qty, String note)
  ```
- **Penyebab & Dampak:** Rentan tertukar posisi saat dipanggil (*parameter transposition bug*), tanda tangan method kaku, dan penambahan parameter baru merusak seluruh pemanggil (*breaking changes*).
- **Solusi Refactoring:** Bungkus sekumpulan parameter terkait ke dalam satu objek parameter terstruktur (`CheckoutRequest`). Objek ini mengelompokkan data input, menyediakan validasi agregat, dan mudah dikembangkan di kemudian hari.

### 7. Guard Clauses & Early Returns (Mengatasi Nested Conditionals)
- **Gejala Code Smell:** Alur logika membentuk **Arrow Anti-Pattern** (percabangan `if-else` bersarang hingga 4-5 tingkat) yang membuat kode menjorok jauh ke kanan layar:
  ```java
  if (input != null) {
      if (emailValid) {
          if (stokCukup) {
              if (saldoCukup) {
                  // Eksekusi bisnis di tingkat nesting ke-5!
              }
          }
      }
  }
  ```
- **Penyebab & Dampak:** Sulit dipahami secara kognitif, rawan kesalahan penempatan blok `else`, dan menyamarkan alur utama (*happy path*).
- **Solusi Refactoring:** Terapkan **Guard Clauses** dengan prinsip **Fail-Fast** dan **Early Return**:
  ```java
  if (input == null) throw new IllegalArgumentException("Input null!");
  if (!emailValid) throw new IllegalArgumentException("Email tidak valid!");
  if (!stokCukup) throw new IllegalStateException("Stok habis!");
  // Happy path berjalan datar tanpa indentasi bersarang
  ```

---

## 3. Pembahasan Mendalam Latihan Kelas Slide 17

Pada Slide 17 materi perkuliahan Bapak Nirwana Haidar Hari, S.Pd., M.Kom., disajikan tiga studi kasus riil yang merepresentasikan masalah arsitektur pada aplikasi enterprise:

```
+-----------------------------------------------------------------------------------+
|                  BEDAH KASUS LATIHAN KELAS SLIDE 17 MATERI P14                    |
+---------+------------------------------------+------------------------------------+
| Kasus   | Masalah Kode Awal (Smell)          | Solusi Desain Refactoring (Clean)  |
+---------+------------------------------------+------------------------------------+
| Kasus A | Method prosesPesanan() 120 baris   | Extract Method, Guard Clauses,     |
|         | (validasi, diskon, pajak, DB, mail)| Parameter Object, Delegasi Service |
+---------+------------------------------------+------------------------------------+
| Kasus B | Class Produk 35 method             | Extract Class (SRP) menjadi        |
|         | (laporan, database, notifikasi)    | Entity, Repo, Reporting, Alert     |
+---------+------------------------------------+------------------------------------+
| Kasus C | Aturan validasi kode voucher       | Single Source of Truth via         |
|         | berulang di 4 class berbeda        | VoucherCode (VO) & VoucherValidator|
+---------+------------------------------------+------------------------------------+
```

### Kasus A: Method `prosesPesanan()` 120 Baris
*“Method prosesPesanan() memiliki 120 baris kode yang menangani validasi input, perhitungan diskon, kalkulasi pajak, penyimpanan database, dan pengiriman email. Identifikasi smell dan refactor!”*

#### 1. Identifikasi Code Smell:
1. **Long Method & Bloated Logic:** 120 baris kode dalam 1 method.
2. **Long Parameter List:** Menerima 10 parameter tipe primitif.
3. **Nested Conditionals (Arrow Anti-Pattern):** 5 tingkatan blok `if-else` bersarang.
4. **Pelanggaran SRP (God Method):** Menangani validasi, kalkulasi matematis, SQL persistensi, dan pengiriman email.

#### 2. Evaluasi Metrik Cohesion & Coupling:
- **Sebelum Refactoring:**
  - *Cyclomatic Complexity:* > 16 (sangat tinggi, rawan bug).
  - *Lines of Code (LOC):* 120 baris dalam satu method monolitik.
  - *Coupling:* Sangat tinggi (terikat langsung dengan SQL connection dan protokol SMTP).
  - *Cohesion:* Sangat rendah (LCOM mendekati 1).
- **Setelah Refactoring:**
  - *Cyclomatic Complexity:* < 3 per method.
  - *Lines of Code (LOC):* Method utama `prosesPesanan()` tereduksi menjadi 28 baris yang sangat terbaca.
  - *Coupling:* Rendah (*loose coupling*), berkolaborasi melalui `TaxCalculator`, `DiscountService`, `OrderRepository`, dan `NotificationService`.
  - *Cohesion:* Sangat tinggi (*High Functional Cohesion*).

#### 3. Implementasi Kode Sesudah Refactoring:
```java
public Order prosesPesanan(CheckoutRequest request) {
    // 1. Guard Clauses (Fail-fast precondition checks)
    validatePreconditions(request);

    // 2. Validasi Ketersediaan Stok Fisik Produk
    validateAndDeductStock(request.getItems());

    // 3. Kalkulasi Finansial Bersih
    Money subtotal = request.calculateSubtotal();
    Money discount = discountService.calculateDiscount(request.getVoucherCode(), subtotal);
    Money taxableBase = subtotal.subtract(discount);
    Money tax = taxCalculator.calculatePpn(taxableBase);
    Money grandTotal = taxableBase.add(tax);

    // 4. Instansiasi Domain Entity Order
    Order order = new Order(
        request.getOrderId(),
        request.getCustomerName(),
        request.getCustomerEmail(),
        request.getShippingAddress(),
        request.getItems(),
        request.getVoucherCode(),
        request.getPaymentType(),
        request.getCustomerNote()
    );
    order.applyFinancials(discount, tax, grandTotal);

    // 5. Persistensi & Dispatch Notifikasi Terisolasi
    orderRepository.save(order);
    dispatchOrderConfirmation(order);

    return order;
}
```

---

### Kasus B: Class `Produk` dengan 35 Method
*“Class Produk memiliki 35 method yang mencakup manipulasi data produk, query database SQL, ekspor laporan penjualan, dan notifikasi stok habis. Bagaimana memecahnya?”*

#### 1. Identifikasi Code Smell:
- **God Class / Large Class / Divergent Change:** Kelas menampung terlalu banyak peran yang tidak berhubungan langsung dengan hakikat entitas produk.
- **Pelanggaran SRP:** Perubahan skema basis data, format template laporan (PDF/ASCII), atau mekanisme notifikasi (SMS/Email) semuanya memaksa modifikasi pada file `Produk.java`.

#### 2. Evaluasi Metrik Cohesion & Coupling:
- **Sebelum Refactoring:**
  - Kelas `OldProdukGodClass` memiliki 35 method dengan LCOM sangat tinggi karena atribut database tidak digunakan oleh method laporan, dan atribut stok digunakan secara campur aduk.
  - Kopling tinggi terhadap database driver dan mail transport.
- **Setelah Refactoring:**
  - Tanggung jawab dipecah ke dalam 4 kelas yang memiliki *High Cohesion*:
    1. **`Produk` (Domain Entity murni):** Hanya mengelola identitas (`ProductCode`), nama, harga (`Money`), dan mutasi stok fisik.
    2. **`ProdukRepository` (Persistensi):** Menyediakan kontrak penyimpanan data (CRUD), diimplementasikan oleh `InMemoryProdukRepository`.
    3. **`ProdukReportService` (Pelaporan & Analitik):** Menyusun tabel ringkasan inventaris dan menghitung total nilai aset.
    4. **`StockNotificationService` (Peringatan Stok Kritis):** Memonitor stok di bawah ambang batas (*threshold*) dan mengirimkan alert ke admin gudang.

---

### Kasus C: Aturan Validasi Kode Voucher Berulang di 4 Class Berbeda
*“Aturan validasi kode voucher (panjang karakter, prefix valid, pengecekan kedaluwarsa, dan batasan minimal belanja) diulang persis di CartService, CheckoutService, PaymentService, dan AdminService. Apa dampaknya dan bagaimana solusinya?”*

#### 1. Identifikasi Code Smell:
- **Duplicated Code (Pelanggaran Prinsip DRY - Don't Repeat Yourself):** Salinan logika validasi yang identik tersebar di 4 lokasi berbeda dalam proyek.
- **Dampak Teknis:** Jika tim pemasaran mengubah aturan format voucher (misalnya menambah prefix `PROMO` atau mengubah diskon), pengembang harus mengedit 4 file terpisah secara manual. Ketinggalan mengedit salah satu file akan memicu inkonsistensi data serius (*shotgun surgery*).

#### 2. Evaluasi Metrik Cohesion & Coupling:
- **Sebelum Refactoring:** Kode rentan bug, pemeliharaan mahal, tidak ada *Single Source of Truth*.
- **Setelah Refactoring:**
  - Diciptakan Value Object **`VoucherCode`** untuk memvalidasi format string (regex, prefix `PENS`/`DISKON`/`HEMAT`/`PROMO`, panjang 5-15 karakter).
  - Diciptakan Domain Service **`VoucherValidator`** sebagai pusat aturan bisnis (eligibilitas, rate diskon, minimum transaksi).
  - Keempat kelas pemanggil kini cukup merujuk pada `VoucherValidator.validateAndCalculateDiscount(voucher, subtotal)`. Duplikasi kode tereliminasi 100%.

---

## 4. Milestone & Bukti Hasil Pengujian Otomatis (Test Runner)

Untuk menjamin bahwa refactoring yang dilakukan tidak merusak fungsionalitas (*behavior preservation*), dibangun automated test runner murni Java (`TestRunner.java` / `TestRefactoring.java`) yang mengeksekusi 36 skenario pengujian unit secara komprehensif.

### Tabel Rekapitulasi Audit Code Smell & Refactoring P14:
```
+----+----------------------+-----------------------+-------------------------+-------------------------+--------+
| No | Code Smell           | Lokasi Kode Sebelum   | Teknik Solusi           | Komponen Hasil Refactor | Status |
+----+----------------------+-----------------------+-------------------------+-------------------------+--------+
| 1. | Long Method          | prosesPesananLama()   | Extract Method & Guard  | OrderProcessor.java     | LULUS  |
| 2. | God Class (35 meth.) | OldProdukGodClass     | Extract Class (SRP)     | Produk, Repo, Report    | LULUS  |
| 3. | Duplicated Code      | Validasi Voucher 4x   | Single Source of Truth  | VoucherValidator.java   | LULUS  |
| 4. | Feature Envy         | OldShippingCalculator | Move Method             | Customer.java           | LULUS  |
| 5. | Primitive Obsession  | String email, double  | Value Object Immutable  | CustomerEmail, Money    | LULUS  |
| 6. | Long Parameter List  | 10 parameter primitif | Parameter Object        | CheckoutRequest.java    | LULUS  |
| 7. | Nested Conditionals  | Arrow Anti-Pattern 5x | Guard Clauses & Return  | OrderProcessor.java     | LULUS  |
+----+----------------------+-----------------------+-------------------------+-------------------------+--------+
```

### Bukti Eksekusi Test Suite (Log Terminal):
```
==========================================================================
         RUNNING AUTOMATED UNIT TESTS FOR P14 REFACTORING
==========================================================================

--- [1] Menguji Value Object: CustomerEmail (Primitive Obsession) ---
  [PASS] Email valid berhasil dibuat
  [PASS] Domain email diekstrak benar
  [PASS] Email case-insensitive equality
  [PASS] Guard clause email invalid melempar IllegalArgumentException
  [PASS] Guard clause email null melempar IllegalArgumentException

--- [2] Menguji Value Object: Money (Primitive Obsession) ---
  [PASS] Penjumlahan Money: 100k + 50k = 150k
  [PASS] Pengurangan Money: 100k - 50k = 50k
  [PASS] Perkalian Money: 100k * 2 = 200k
  [PASS] Persentase Money: 10% dari 100k = 10k
  [PASS] Format Rupiah mengandung 'Rp'
  [PASS] Guard clause saldo negatif berhasil mencegah mutasi ilegal
  [PASS] Guard clause nominal negatif berhasil

--- [3] Menguji Value Object: ProductCode (Primitive Obsession) ---
  [PASS] ProductCode valid berhasil
  [PASS] Guard clause pattern ProductCode berhasil

--- [4] Menguji Kasus C: VoucherCode & VoucherValidator (Duplicated Code) ---
  [PASS] Prefix voucher PENS berhasil diidentifikasi
  [PASS] Diskon PENSJUARA 25% dari 500k = 125k
  [PASS] Validasi batas minimum belanja voucher berhasil melempar Exception
  [PASS] Guard clause pola voucher gagal untuk kode tidak sah

--- [5] Menguji Kasus B: God Class Produk -> Extract Class (SRP) ---
  [PASS] Produk tersedia untuk 5 unit
  [PASS] Stok produk berkurang menjadi 7 unit
  [PASS] Stok produk bertambah menjadi 12 unit
  [PASS] Repository berhasil menyimpan dan menemukan produk
  [PASS] Repository mendeteksi ketiadaan produk acak
  [PASS] Report service menghasilkan ringkasan berisi nama produk

--- [6] Menguji Feature Envy -> Move Method ---
  [PASS] Diskon loyalitas dihitung internal oleh Customer (Move Method)
  [PASS] Customer GOLD belanja > 200k berhak gratis ongkir
  [PASS] Customer REGULAR diskon loyalitas = 0%
  [PASS] Customer REGULAR tidak berhak gratis ongkir

--- [7] Menguji Kasus A: Long Method prosesPesanan() & Guard Clauses ---
  [EMAIL DISPATCH] -> Mengirim email ke: arjuna@pens.ac.id
  Subjek : Konfirmasi Pesanan Berhasil #ORD-TEST-001
  Konten : Halo Arjuna,
Terima kasih atas pesanan Anda. Total tagihan: Rp1.598.400,00 (Termasuk PPN: Rp158.400,00, Diskon: Rp160.000,00).
Metode Pembayaran: Transfer Virtual Account Bank.
  Status : [OK] Terkirim via SMTP Gateway.
  [PASS] Order berhasil diproses
  [PASS] Subtotal bruto tepat 1.600.000
  [PASS] Diskon tepat 160.000
  [PASS] PPN 11% tepat 158.400
  [PASS] Grand Total tepat 1.598.400
  [PASS] Stok produk berkurang dari 5 menjadi 3
  [PASS] Guard clause duplicate order ID berhasil mencegah double processing
  [PASS] Guard clause stok tidak mencukupi berhasil menangkap error

==========================================================================
  HASIL PENGUJIAN OTOMATIS: 36 LULUS (PASS), 0 GAGAL (FAIL)
==========================================================================
```

---

## 5. Panduan Menjalankan Program & Pengujian

### Prasyarat:
- Java Development Kit (JDK 17 atau lebih baru)
- Python 3.9+ (dengan modul `reportlab` dan `pillow` untuk generate PDF & screenshot)

### Perintah Kompilasi & Eksekusi:
```bash
# Masuk ke direktori modul P14
cd P14_3125522010_ArjunaLanangAdiwarsana

# 1. Kompilasi seluruh source code ke direktori bin/
javac -d bin src/*.java

# 2. Jalankan driver utama demonstrasi 7 teknik & kasus slide 17
java -cp bin Main

# 3. Jalankan automated test suite verifikasi refactoring
java -cp bin TestRunner

# 4. Generate screenshot terminal dan dokumen PDF resmi
python3 generate_pdf.py
```

---
*Laporan ini disusun secara profesional oleh Arjuna Lanang Adiwarsana (NRP: 3125522010) sebagai pemenuhan tugas praktikum PBO Pertemuan 14 - PENS PSDKU Sumenep.*

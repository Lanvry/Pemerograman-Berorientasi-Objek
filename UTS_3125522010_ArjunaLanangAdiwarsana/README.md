# UJIAN TENGAH SEMESTER (UTS) - PRAKTIKUM PBO
**Semester Genap Tahun Ajaran 2026/2027**  
**Program Studi D3 Teknik Informatika PSDKU Sumenep - PENS**

---

### Data Mahasiswa
- **Nama** : Arjuna Lanang Adiwarsana
- **NRP**  : 3125522010
- **Kelas**: 3D3 IT-PSDKU A
- **Topik**: Studi Kasus A - Sistem Rental Kendaraan

---

## 1. Pemetaan Persyaratan & Rubrik Penilaian (100%)

| No | Kriteria Soal | Ketentuan Minimal | Implementasi dalam Proyek Ini | File Sumber |
|:---:|:---|:---|:---|:---|
| 1 | **Class & Object** | Min. 5 class | 6 class & interface (`DapatDisewa`, `Kendaraan`, `Mobil`, `Motor`, `Pelanggan`, `TransaksiRental`, `Main`) | `src/*.java` |
| 2 | **Encapsulation** | Atribut utama `private` + getter/setter terkontrol | Semua field berakses `private`, setter & konstruktor memiliki validasi nilai | `Kendaraan.java`, `Mobil.java`, `Motor.java`, `Pelanggan.java`, `TransaksiRental.java` |
| 3 | **Validation** | Min. 2 business rules | 1) Durasi sewa minimal 1 hari (`durasiHari >= 1`).<br>2) Kendaraan hanya dapat disewa jika berstatus tersedia (`isTersedia() == true`).<br>3) Plat nomor tidak boleh kosong & tarif harian harus > 0. | `Kendaraan.java`, `TransaksiRental.java` |
| 4 | **Relationship** | Min. 2 relasi antarobject | 1) **Agregasi / Asosiasi**: `TransaksiRental` *has-a* `Pelanggan`<br>2) **Agregasi / Asosiasi**: `TransaksiRental` *has-a* `Kendaraan` | `TransaksiRental.java` |
| 5 | **Inheritance** | Min. 1 superclass + 2 subclass | Superclass: `Kendaraan`<br>Subclasses: `Mobil` dan `Motor` | `Mobil.java`, `Motor.java` |
| 6 | **Polymorphism** | Min. 1 method overriding dengan behavior berbeda | Overriding `hitungBiayaSewa(int durasiHari)`:<br>• Pada `Mobil`: tarif harian + biaya asuransi harian + opsi supir harian.<br>• Pada `Motor`: tarif harian + biaya sewa helm ekstra.<br>Dipanggil dinamis via referensi `Kendaraan[]`. | `Kendaraan.java`, `Mobil.java`, `Motor.java`, `Main.java` |
| 7 | **Abstract Class** | Min. 1 abstract class yang relevan | `public abstract class Kendaraan` dengan 2 abstract method: `hitungBiayaSewa()` & `tampilkanSpesifikasi()` | `Kendaraan.java` |
| 8 | **Interface** | Min. 1 interface yang relevan | `public interface DapatDisewa` dengan method: `isTersedia()`, `sewa(id)`, `kembalikan()` | `DapatDisewa.java` |
| 9 | **Pengujian** | Min. 3 object dalam skenario | 4 objek kendaraan (2 Mobil + 2 Motor), 3 objek pelanggan, dan 2 transaksi rental | `Main.java` |
| 10 | **Kreativitas** | Min. 1 fitur ciptaan sendiri | 1) **Diskon Membership**: Pelanggan VIP (10%), GOLD (5%), REGULER (0%).<br>2) **Denda Keterlambatan Dinamis**: 150% dari tarif harian per hari keterlambatan.<br>3) **Kuitansi Transparan**: Breakdown biaya sewa, diskon, denda, dan total akhir. | `Pelanggan.java`, `TransaksiRental.java` |

---

## 2. Struktur Proyek

```text
UTS_3125522010_ArjunaLanangAdiwarsana/
├── class-diagram.pdf             <- File diagram PDF (standar laporan)
├── class-diagram.png             <- File diagram PNG resolusi tinggi (300 DPI)
├── generate_diagram.py           <- Skrip visualisasi diagram UML (Pillow + ReportLab)
├── README.md                     <- Dokumentasi & panduan demo 3 menit
└── src/
    ├── DapatDisewa.java          <- Interface kontrak ketersediaan sewa
    ├── Kendaraan.java            <- Abstract Class superclass armada
    ├── Mobil.java                <- Subclass Mobil (asuransi & supir)
    ├── Motor.java                <- Subclass Motor (fasilitas helm)
    ├── Pelanggan.java            <- Entity Pelanggan & diskon membership
    ├── TransaksiRental.java      <- Transaksi sewa, denda keterlambatan, & invoice
    └── Main.java                 <- Driver pengujian skenario lengkap
```

---

## 3. Cara Kompilasi dan Eksekusi

Jalankan perintah berikut melalui terminal:

```bash
# Kompilasi seluruh file Java ke folder bin
mkdir -p bin
javac -d bin src/*.java

# Jalankan program pengujian
java -cp bin Main
```

---

## 4. Panduan Demo 3 Menit (Menjawab 3 Pertanyaan Wajib Dosen)

### Pertanyaan 1: "Mengapa saya merancang class seperti ini?"
> *"Saya merancang arsitektur ini memisahkan peran kontrak antarmuka, entitas inti, dan transaksi secara modular:*
> *1. Interface `DapatDisewa` bertindak sebagai kontrak perilaku ketersediaan dan status sewa.*
> *2. Abstract class `Kendaraan` mewakili konsep umum armada kendaraan di mana atribut bersama (plat nomor, merk, tarif harian) dienkapsulasi dengan modifier `private` dan setter terlindung.*
> *3. Subclass `Mobil` dan `Motor` mengekstensi `Kendaraan` untuk menampung spesifikasi dan komponen biaya unik (mobil memiliki transmisi dan opsi supir, sedangkan motor memiliki tipe motor dan helm ekstra).*
> *4. Class `Pelanggan` mengelola data penyewa dan level diskon membership.*
> *5. Class `TransaksiRental` menghubungkan `Pelanggan` dan `Kendaraan` melalui relasi Agregasi, memastikan kendaraan tidak disewa ganda dan menghitung biaya secara terintegrasi."*

### Pertanyaan 2: "Di mana polymorphism terjadi?"
> *"Polymorphism terjadi pada pemanggilan method `hitungBiayaSewa(durasiHari)`:*
> *1. Method ini dideklarasikan sebagai abstract method di superclass `Kendaraan`.*
> *2. Subclass `Mobil` meng-override method ini dengan memperhitungkan biaya dasar sewa, biaya proteksi asuransi harian, serta opsi tarif jasa supir harian.*
> *3. Subclass `Motor` meng-override method ini dengan menghitung biaya dasar sewa ditambah biaya sewa unit helm tambahan kedua.*
> *4. Pada `Main.java` dan `TransaksiRental.java`, pemanggilan dilakukan melalui tipe referensi superclass `Kendaraan` (`k.hitungBiayaSewa(...)`), di mana Java secara dinamis (Dynamic Method Dispatch) menjalankan implementasi spesifik sesuai objek instansiasi riilnya."*

### Pertanyaan 3: "Apa fitur kreatif yang saya tambahkan?"
> *"Saya menambahkan dua fitur logika bisnis kreatif:*
> *1. **Sistem Klasifikasi Diskon Membership Pelanggan**: Member VIP otomatis memperoleh diskon 10%, dan Member GOLD memperoleh diskon 5% dari subtotal sewa kendaraan.*
> *2. **Perhitungan Denda Keterlambatan Pengembalian Dinamis**: Jika pelanggan terlambat mengembalikan kendaraan, sistem otomatis mengenakan denda penalti sebesar 150% (1,5x) dari tarif normal kendaraan per hari keterlambatan.*
> *3. Seluruh rincian biaya, diskon, dan denda kemudian dicetak secara transparan pada kuitansi digital."*

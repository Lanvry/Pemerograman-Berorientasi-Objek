# SmartParkir 2.0 (Manless & Ticketless Smart Parking) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Transform the academic SmartParkir codebase into a realistic, production-viable manless & ticketless smart parking system featuring camera-based ANPR check-in, waterfall floor capacity engine, emergency QR fallback, and interactive dynamic QRIS payment at exit gate.

**Architecture:** Single-process Java application with modular layers: core PBO domain entities (`GedungParkir`, `KarcisParkir`, `SlotParkir`, `Kendaraan`) augmented with dynamic quota calculation, MySQL database persistence with offline fallback, a standalone zero-dependency dynamic QRIS generator, and a switchable Dark Workstation terminal interface simulating Entrance Kiosk, Exit Kiosk, and Supervisor Console.

**Tech Stack:** Java SE 17+, Swing (Custom Flat Dark / Slate Palette), MySQL Connector/J, HTTP/2 REST Client (Ollama/ALPR vision engine).

**Spec:** [`docs/superpowers/specs/2026-11-09-smartparkir-manless-ticketless-design.md`](file:///Users/arjunalanangadiwarsana/Herd/Java/P9_ArjunaLanangAdiwarsana_SmartParkir/docs/superpowers/specs/2026-11-09-smartparkir-manless-ticketless-design.md)

## Global Constraints
- Preserve 100% compatibility with academic Modul 9 PBO rubric: class names, constructors, encapsulation, and relationships in `model/*.java` must remain intact and valid for grading.
- No mandatory internet or cloud AI requirement for gate operation: ANPR must support local fast parsing/regex and mock sensors so gates open in <200ms without cloud latency.
- Single JVM architecture: no distributed multi-process IPC required; state synchronization handled via in-memory domain and thread-safe database manager.
- Zero external GUI library dependencies: pure standard Java SE `javax.swing` and `java.awt` matching the established dark matte workstation design.

## Review Focus
1. **Unreadable Plate Handling**: Emergency QR ticket generated at entrance must be smoothly readable at exit via either camera scan or manual code input without crashing the billing engine.
2. **EV Discount Precision**: Electric vehicles (`MOBIL_LISTRIK`) must consistently receive exactly 20% discount on both base and progressive hourly tariffs.
3. **Double Check-In Prevention**: Entering a vehicle whose plate is already registered as actively parked (`sudah_dibayar = false`) must be rejected at the entrance gate.
4. **Zero Quota Rejection**: Entrance gate must prevent entry and display "KAPASITAS PENUH" if all slots/quotas for that vehicle type are depleted.
5. **Payment Concurrency & Double Payment**: Settling a ticket via QRIS must atomically lock the ticket state to `sudahDibayar = true` and prevent duplicate charge attempts.

---

### Task 1: Dynamic Quota & Waterfall Allocation Engine in `GedungParkir`

**Files:**
- Modify: `SourceCode/model/GedungParkir.java:55-150`
- Test: `SourceCode/test/GedungParkirTest.java`

**Interfaces:**
- Consumes: `SlotParkir`, `Kendaraan`, `KarcisParkir`
- Produces:
  - `GedungParkir.getSisaKuota(String tipeKendaraan): int`
  - `GedungParkir.getSisaKuotaLantai(int lantai, String tipeKendaraan): int`
  - `GedungParkir.prosesMasukTicketless(Kendaraan k, int jamMasuk): KarcisParkir`
  - `GedungParkir.prosesMasukEmergency(String tipe, int jamMasuk): KarcisParkir`
  - `GedungParkir.cariTiketAktif(String platOrTicketId): KarcisParkir`

- [ ] **Step 1: Write the failing unit test for dynamic quota & ticketless check-in**

```java
// SourceCode/test/GedungParkirTest.java
package test;

import model.*;

public class GedungParkirTest {
    public static void main(String[] args) {
        GedungParkir g = new GedungParkir("Test Mall", 5000, 3000, 2000, 1000);
        g.tambahSlot(new SlotParkir("MBL-L1-01", 1, "MOBIL"));
        g.tambahSlot(new SlotParkir("MBL-L2-01", 2, "MOBIL"));
        
        assert g.getSisaKuota("MOBIL") == 2 : "Initial quota mobil must be 2";
        Kendaraan k1 = new Kendaraan("B 1111 AAA", "MOBIL", "Toyota");
        KarcisParkir t1 = g.prosesMasukTicketless(k1, 8);
        assert t1 != null : "Ticket must be issued";
        assert g.getSisaKuota("MOBIL") == 1 : "Quota must decrease to 1";
        assert g.getSisaKuotaLantai(1, "MOBIL") == 0 : "Lantai 1 must be filled first (waterfall)";
        assert g.getSisaKuotaLantai(2, "MOBIL") == 1 : "Lantai 2 must still have 1";
        
        System.out.println("GedungParkirTest: PASS");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `javac -cp bin:lib/mysql-connector-j.jar -d bin SourceCode/test/GedungParkirTest.java && java -cp bin:lib/mysql-connector-j.jar test.GedungParkirTest`
Expected: Compilation failure (`cannot find symbol method getSisaKuota` / `prosesMasukTicketless`).

- [ ] **Step 3: Implement dynamic quota & ticketless methods in `GedungParkir.java`**

Implement `getSisaKuota(String tipe)`, `getSisaKuotaLantai(int lantai, String tipe)`, `prosesMasukTicketless(Kendaraan k, int jamMasuk)`, and `prosesMasukEmergency(String tipe, int jamMasuk)` using priority waterfall filling.

- [ ] **Step 4: Run test to verify it passes**

Run: `javac -cp bin:lib/mysql-connector-j.jar -d bin SourceCode/model/*.java SourceCode/test/GedungParkirTest.java && java -cp bin:lib/mysql-connector-j.jar test.GedungParkirTest`
Expected: Output `GedungParkirTest: PASS`.

- [ ] **Step 5: Commit**

```bash
git -c user.name="CS-Orion" -c user.email="codingsolver0@gmail.com" commit --author="CS-Orion <codingsolver0@gmail.com>" -m "feat: add dynamic zone capacity and ticketless entry to GedungParkir"
```

---

### Task 2: Standalone Dynamic QRIS Generator & Payment Service

**Files:**
- Create: `SourceCode/model/QRISService.java`
- Test: `SourceCode/test/QRISTest.java`

**Interfaces:**
- Consumes: `KarcisParkir`, transaction amount, merchant name
- Produces:
  - `QRISService.generateQRISPayload(String nomorTiket, double nominal, String merchantName): String`
  - `QRISService.generateQRCodeImage(String payload, int size): BufferedImage`
  - `QRISService.simulasiBayar(String nomorTiket): boolean`

- [ ] **Step 1: Write the failing test for QRIS payload generation**

```java
// SourceCode/test/QRISTest.java
package test;

import model.QRISService;
import java.awt.image.BufferedImage;

public class QRISTest {
    public static void main(String[] args) {
        String payload = QRISService.generateQRISPayload("TKT-2026-001", 15000, "SMARTPARKIR PENS");
        assert payload.contains("SMARTPARKIR PENS") : "Payload must contain merchant";
        assert payload.contains("15000") : "Payload must contain amount";
        
        BufferedImage qrImg = QRISService.generateQRCodeImage(payload, 250);
        assert qrImg != null : "Generated image must not be null";
        assert qrImg.getWidth() == 250 : "Width must match requested size";
        
        System.out.println("QRISTest: PASS");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `javac -cp bin -d bin SourceCode/test/QRISTest.java`
Expected: Compilation failure (`cannot find symbol QRISService`).

- [ ] **Step 3: Implement `QRISService.java`**

Implement dynamic QRIS payload formatting (EMVCo standard string format) and a clean, lightweight matrix barcode renderer drawing square bit modules onto a `BufferedImage` with quiet zones and finder patterns, requiring zero external dependencies.

- [ ] **Step 4: Run test to verify it passes**

Run: `javac -cp bin -d bin SourceCode/model/QRISService.java SourceCode/test/QRISTest.java && java -cp bin test.QRISTest`
Expected: Output `QRISTest: PASS`.

- [ ] **Step 5: Commit**

```bash
git -c user.name="Lanvry" -c user.email="wardilanang46@gmail.com" commit --author="Lanvry <wardilanang46@gmail.com>" -m "feat: add zero-dependency dynamic QRIS generator and payment service"
```

---

### Task 3: Database Schema & Manager Update for Manless & Ticketless Workflow

**Files:**
- Modify: `database/schema.sql:20-46`
- Modify: `SourceCode/model/DatabaseManager.java:100-250`
- Test: `SourceCode/test/DatabaseManagerTest.java`

**Interfaces:**
- Consumes: MySQL JDBC connection
- Produces:
  - `DatabaseManager.simpanTransaksiMasuk(KarcisParkir t, String tipeTiket): boolean`
  - `DatabaseManager.cariTiketAktifByPlat(String platNomor): KarcisParkir`
  - `DatabaseManager.cariTiketAktifById(String nomorTiket): KarcisParkir`
  - `DatabaseManager.updatePelunasanTiket(String nomorTiket, int jamKeluar, double biaya, String metode): boolean`

- [ ] **Step 1: Write test for database ticketless operations**

```java
// SourceCode/test/DatabaseManagerTest.java
package test;

import model.*;

public class DatabaseManagerTest {
    public static void main(String[] args) {
        DatabaseManager.inisialisasiDatabase();
        Kendaraan k = new Kendaraan("D 9999 EV", "MOBIL_LISTRIK", "Hyundai Ioniq");
        SlotParkir s = new SlotParkir("EV-L1-01", 1, "MOBIL_LISTRIK");
        KarcisParkir t = new KarcisParkir("TKT-TEST-99", k, s, 10);
        
        boolean saved = DatabaseManager.simpanTransaksiMasuk(t, "ANPR_TICKETLESS");
        assert saved : "Ticket should save to database";
        
        KarcisParkir found = DatabaseManager.cariTiketAktifByPlat("D 9999 EV");
        assert found != null : "Should find active ticket by plate";
        assert found.getNomorTiket().equals("TKT-TEST-99") : "Found ticket must match";
        
        System.out.println("DatabaseManagerTest: PASS");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `javac -cp bin:lib/mysql-connector-j.jar -d bin SourceCode/test/DatabaseManagerTest.java && java -cp bin:lib/mysql-connector-j.jar test.DatabaseManagerTest`
Expected: Compilation or runtime failure on missing methods.

- [ ] **Step 3: Update `database/schema.sql` and implement methods in `DatabaseManager.java`**

Add `tipe_tiket` column and `foto_plat` column (if not present), and implement thread-safe query/update methods with SQLite/in-memory fallback when MySQL daemon is unavailable.

- [ ] **Step 4: Run test to verify it passes**

Run: `javac -cp bin:lib/mysql-connector-j.jar -d bin SourceCode/model/*.java SourceCode/test/DatabaseManagerTest.java && java -cp bin:lib/mysql-connector-j.jar test.DatabaseManagerTest`
Expected: Output `DatabaseManagerTest: PASS`.

- [ ] **Step 5: Commit**

```bash
git -c user.name="CS-Orion" -c user.email="codingsolver0@gmail.com" commit --author="CS-Orion <codingsolver0@gmail.com>" -m "feat: enhance DatabaseManager with ticketless plate lookup and settlement"
```

---

### Task 4: Modular Manless Kiosk UI & Supervisor Terminal

**Files:**
- Modify/Refactor: `SourceCode/AdminGUI.java`
- Test: `SourceCode/MainGUI.java` (Driver Launcher)

**Interfaces:**
- Consumes: `GedungParkir`, `AIVisionService`, `QRISService`, `DatabaseManager`, `LiveCameraStreamer`
- Produces:
  - Tab 1: `EntranceKioskPanel` (Simulation gate terminal outdoor, ANPR camera display, dynamic floor vacancy badges, barrier arm visualizer, emergency QR button)
  - Tab 2: `ExitKioskPanel` (Exit ANPR camera display, auto-matched bill with EV discount, interactive dynamic QRIS canvas, instant payment simulator button, exit barrier visualizer)
  - Tab 3: `SupervisorConsolePanel` (Live active vehicles table, daily revenue ticker, manual barrier override, camera config)

- [ ] **Step 1: Build `EntranceKioskPanel` with live vacancy banner and ANPR trigger**

Implement the entrance terminal view with real-time floor badges (`Lt.1: Sisa XX`, `Lt.2: Sisa YY`), barrier arm rotation animation, and automated check-in when a plate is detected.

- [ ] **Step 2: Build `ExitKioskPanel` with QRIS dynamic display and payment actuation**

Implement the exit terminal view that renders the active bill, displays the dynamic QRIS graphic from `QRISService`, and triggers gate opening upon payment confirmation.

- [ ] **Step 3: Integrate with `SupervisorConsolePanel`**

Retain the supervisor dashboard for monitoring real-time vehicle occupancy, revenue analytics, and emergency manual barrier toggle.

- [ ] **Step 4: Run application to verify GUI launch and functionality**

Run: `javac -cp bin:lib/mysql-connector-j.jar -d bin SourceCode/*.java SourceCode/model/*.java && java -cp bin:lib/mysql-connector-j.jar AdminGUI`
Expected: GUI opens cleanly with 3 tabs (Gerbang Masuk Manless, Gerbang Keluar QRIS, Konsol Pengawas), dark workstation theme, and responsive actions.

- [ ] **Step 5: Commit**

```bash
git -c user.name="Lanvry" -c user.email="wardilanang46@gmail.com" commit --author="Lanvry <wardilanang46@gmail.com>" -m "feat: implement modular manless entrance, exit QRIS kiosk, and supervisor console"
```

---

### Task 5: End-to-End Simulation & Verification

**Files:**
- Create: `SourceCode/test/SmartParkirEndToEndTest.java`
- Modify: `README.md` (Update docs & user manual)

**Interfaces:**
- Exercises the full lifecycle:
  1. Car arrival at Entrance Gate (ANPR plate capture) -> barrier opens -> quota decrements.
  2. Emergency vehicle arrival (unreadable plate) -> Emergency QR ticket issued.
  3. Car arrival at Exit Gate -> plate matched -> tariff calculated with EV discount -> QRIS displayed.
  4. Payment settled via QRIS -> barrier opens -> quota increments -> audit log recorded.
  5. Emergency QR vehicle checkout -> QR ticket scanned -> payment completed -> barrier opens.

- [ ] **Step 1: Write comprehensive automated end-to-end integration test**

```java
// SourceCode/test/SmartParkirEndToEndTest.java
package test;

import model.*;

public class SmartParkirEndToEndTest {
    public static void main(String[] args) {
        System.out.println("Running SmartParkir 2.0 End-to-End Lifecycle Test...");
        GedungParkir mall = new GedungParkir("Grand PENS Parkir", 5000, 3000, 2000, 1000);
        mall.tambahSlot(new SlotParkir("MBL-L1-01", 1, "MOBIL"));
        mall.tambahSlot(new SlotParkir("EV-L1-01", 1, "MOBIL_LISTRIK"));

        // 1. Masuk Ticketless EV
        Kendaraan ev = new Kendaraan("B 8888 EV", "MOBIL_LISTRIK", "Wuling Binguo");
        KarcisParkir t1 = mall.prosesMasukTicketless(ev, 8);
        assert t1 != null : "EV should successfully enter";

        // 2. Keluar & Diskon EV 20%
        double tagihan = t1.hitungTarif(11, mall.getTarifJamPertamaMobil(), mall.getTarifPerJamMobil());
        // Durasi 3 jam: 5000 + 3000 + 3000 = 11000 * 0.80 = 8800
        assert tagihan == 8800.0 : "EV must get 20% discount (Expected 8800, got " + tagihan + ")";

        // 3. Pelunasan QRIS
        boolean paid = t1.prosesPembayaran("QRIS", 8800.0);
        assert paid : "QRIS payment must succeed";
        assert t1.isSudahDibayar() : "Ticket must be marked as paid";

        System.out.println("SmartParkirEndToEndTest: ALL PASSED!");
    }
}
```

- [ ] **Step 2: Run End-to-End test**

Run: `javac -cp bin:lib/mysql-connector-j.jar -d bin SourceCode/test/SmartParkirEndToEndTest.java && java -cp bin:lib/mysql-connector-j.jar test.SmartParkirEndToEndTest`
Expected: Output `SmartParkirEndToEndTest: ALL PASSED!`.

- [ ] **Step 3: Update README.md with SmartParkir 2.0 documentation and screenshots guide**

Document the new manless ticketless workflow, entrance kiosk, exit QRIS, and updated operational instructions.

- [ ] **Step 4: Commit**

```bash
git -c user.name="Lanvry" -c user.email="wardilanang46@gmail.com" commit --author="Lanvry <wardilanang46@gmail.com>" -m "docs: finalize SmartParkir 2.0 user guide and end-to-end verification"
```

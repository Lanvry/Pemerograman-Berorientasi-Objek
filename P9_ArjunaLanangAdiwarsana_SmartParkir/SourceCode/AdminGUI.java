import model.GedungParkir;
import model.KarcisParkir;
import model.Kendaraan;
import model.SlotParkir;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Modern Clean White & Aesthetic Admin GUI untuk Smart Parking (SmartParkir).
 * Diperbarui dengan layout denah parkir gedung nyata (Real Architectural Blueprint Layout):
 * - Baris A: Zona Khusus Motor (9 Slot, Tarif Rp 2.000 / jam ke-1, Rp 1.000 / jam berikutnya)
 * - Baris B & C: Zona Mobil Island Tengah (22 Slot, Tangga Darurat & Pilar Beton)
 * - Baris D: Zona Mobil & EV Charger (14 Slot, Fasilitas Pengisian Daya Listrik Diskon 20%)
 * - Lajur Manuver Aspal dengan Marka Putih & Panah Arah
 * - Interaksi 1-Click Langsung pada Mobil Parkir untuk Check-Out Otomatis.
 */
public class AdminGUI extends JFrame {

    // Palet Warna Putih Bersih & Estetik (Clean Slate & Soft Pastels)
    private static final Color BG_LIGHT = new Color(248, 250, 252);        // #f8fafc
    private static final Color CARD_BG = new Color(255, 255, 255);         // #ffffff
    private static final Color CARD_BORDER = new Color(226, 232, 240);    // #e2e8f0
    private static final Color BORDER_ACTIVE = new Color(37, 99, 235);     // #2563eb
    private static final Color TEXT_DARK = new Color(15, 23, 42);          // #0f172a
    private static final Color TEXT_BODY = new Color(51, 65, 85);          // #334155
    private static final Color TEXT_MUTED = new Color(100, 116, 139);      // #64748b
    private static final Color ACCENT_BLUE = new Color(37, 99, 235);       // #2563eb
    private static final Color ACCENT_GREEN = new Color(16, 185, 129);     // #10b981
    private static final Color ACCENT_RED = new Color(239, 68, 68);        // #ef4444
    private static final Color ACCENT_PURPLE = new Color(139, 92, 246);    // #8b5cf6
    private static final Color ACCENT_YELLOW = new Color(245, 158, 11);    // #f59e0b

    // Soft Pastels
    private static final Color GREEN_SOFT = new Color(236, 253, 245);
    private static final Color RED_SOFT = new Color(255, 241, 242);
    private static final Color PURPLE_SOFT = new Color(245, 243, 255);
    private static final Color BLUE_SOFT = new Color(239, 246, 255);
    private static final Color AISLE_BG = new Color(241, 245, 249);
    private static final Color PILLAR_COLOR = new Color(71, 85, 105);

    private GedungParkir gedung;
    private NumberFormat rupiahFormat;

    // State Interaksi
    private SlotParkir selectedSlot = null;
    private KarcisParkir selectedTiket = null;
    private Map<String, JPanel> slotCardMap = new HashMap<>();

    // Komponen UI
    private JPanel blueprintContainer;
    private JLabel lblTotalSlot, lblTersedia, lblTerisi, lblPendapatan;
    private DefaultTableModel logTableModel;

    // Form Check-In
    private JTextField txtPlat, txtMerk, txtJamMasuk;
    private JComboBox<String> cbTipeKendaraan;
    private JLabel lblAlokasiInfo;

    // Form Check-Out
    private JTextField txtCariTiket, txtJamKeluar, txtUangBayar;
    private JComboBox<String> cbMetodeBayar;
    private JLabel lblCheckoutNotice;
    private JLabel lblBillingPreview;

    // Animasi 60 FPS
    private float pulseAlpha = 0.5f;
    private boolean pulseUp = true;
    private float gateAngle = 0.0f;
    private float targetGateAngle = 0.0f;
    private String gateStatusText = "PORTAL TERTUTUP";
    private Color gateStatusColor = ACCENT_RED;

    public AdminGUI() {
        super("SmartParkir Command Center - Blueprint Realtime Gedung Parkir");
        this.rupiahFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

        initData();
        initUI();
        startAnimationEngine();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1440, 920);
        setMinimumSize(new Dimension(1200, 800));
        setLocationRelativeTo(null);
    }

    private void initData() {
        // Tarif berbeda untuk Mobil vs Motor
        gedung = new GedungParkir(
                "Mall Grand PENS Plaza",
                5000.0, 3000.0, // Mobil: Rp 5.000 jam pertama, Rp 3.000 jam berikutnya
                2000.0, 1000.0  // Motor: Rp 2.000 jam pertama, Rp 1.000 jam berikutnya
        );

        // 1. Baris A (ряд A): 9 Slot Khusus MOTOR (MTR-A01 s/d MTR-A09)
        for (int i = 1; i <= 9; i++) {
            gedung.tambahSlot(new SlotParkir(String.format("MTR-A%02d", i), 1, "MOTOR"));
        }

        // 2. Baris B (ряд Б): 11 Slot MOBIL (MBL-B01 s/d MBL-B04, MBL-B06 s/d MBL-B12)
        // Posisi 5 adalah area Tangga & Lift darurat
        for (int i = 1; i <= 12; i++) {
            if (i == 5) continue;
            gedung.tambahSlot(new SlotParkir(String.format("MBL-B%02d", i), 1, "MOBIL"));
        }

        // 3. Baris C (ряд В): 11 Slot MOBIL (MBL-C01 s/d MBL-C04, MBL-C06 s/d MBL-C12)
        for (int i = 1; i <= 12; i++) {
            if (i == 5) continue;
            gedung.tambahSlot(new SlotParkir(String.format("MBL-C%02d", i), 1, "MOBIL"));
        }

        // 4. Baris D (ряд Г): 14 Slot (D01-D12 MOBIL, D13-D14 MOBIL_LISTRIK EV Charger)
        for (int i = 1; i <= 12; i++) {
            gedung.tambahSlot(new SlotParkir(String.format("MBL-D%02d", i), 1, "MOBIL"));
        }
        gedung.tambahSlot(new SlotParkir("EV-D13", 1, "MOBIL_LISTRIK"));
        gedung.tambahSlot(new SlotParkir("EV-D14", 1, "MOBIL_LISTRIK"));

        // Inisialisasi beberapa contoh kendaraan parkir nyata dari berbagai kategori:
        Kendaraan k1 = new Kendaraan("B 6789 PBO", "MOTOR", "Honda Vario 160");
        KarcisParkir t1 = gedung.prosesMasuk(k1, 9);

        Kendaraan k2 = new Kendaraan("B 1234 ABC", "MOBIL", "Toyota Avanza");
        KarcisParkir t2 = gedung.prosesMasuk(k2, 8);

        Kendaraan k3 = new Kendaraan("L 9988 XYZ", "MOBIL", "Honda HR-V");
        KarcisParkir t3 = gedung.prosesMasuk(k3, 10);

        Kendaraan k4 = new Kendaraan("B 1010 EV", "MOBIL_LISTRIK", "Hyundai Ioniq 5");
        KarcisParkir t4 = gedung.prosesMasuk(k4, 9);

        // Pilih t2 secara default agar form siap checkout
        if (t2 != null) {
            this.selectedSlot = t2.getSlot();
            this.selectedTiket = t2;
        }
    }

    private void initUI() {
        getContentPane().setBackground(BG_LIGHT);
        setLayout(new BorderLayout(16, 16));

        // 1. Top Navbar Header
        add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Main Content Split (Kiri: Real Architectural Blueprint Denah, Kanan: Panel Kasir & Kontrol)
        JPanel mainContent = new JPanel(new BorderLayout(16, 16));
        mainContent.setBackground(BG_LIGHT);
        mainContent.setBorder(new EmptyBorder(0, 18, 18, 18));

        mainContent.add(createLeftBlueprintPanel(), BorderLayout.CENTER);
        mainContent.add(createRightControlsPanel(), BorderLayout.EAST);

        add(mainContent, BorderLayout.CENTER);

        // Sinkronisasi data awal
        updateStats();
        updateLogTable();
        updateCheckoutPreview();
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CARD_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER),
                new EmptyBorder(14, 20, 14, 20)
        ));

        // Brand Title
        JPanel titleBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        titleBox.setOpaque(false);

        JLabel logoBadge = new JLabel("P", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ACCENT_BLUE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        logoBadge.setPreferredSize(new Dimension(36, 36));
        logoBadge.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logoBadge.setForeground(Color.WHITE);

        JPanel textGroup = new JPanel(new GridLayout(2, 1, 0, 2));
        textGroup.setOpaque(false);
        JLabel lblTitle = new JLabel("SmartParkir Command Center");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(TEXT_DARK);

        JLabel lblSub = new JLabel("Mall Grand PENS Plaza • Denah Realistis Blueprint 45 Slot (Zona Motor & Mobil Terpisah)");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(TEXT_MUTED);

        textGroup.add(lblTitle);
        textGroup.add(lblSub);

        titleBox.add(logoBadge);
        titleBox.add(textGroup);

        // Stats Badges on Top
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        statsPanel.setOpaque(false);

        lblTotalSlot = createStatBadge("TOTAL SLOT", "45", TEXT_DARK, BG_LIGHT);
        lblTersedia = createStatBadge("TERSEDIA", "41", ACCENT_GREEN, GREEN_SOFT);
        lblTerisi = createStatBadge("TERISI", "4", ACCENT_RED, RED_SOFT);
        lblPendapatan = createStatBadge("KAS MASUK", "Rp 0", ACCENT_YELLOW, new Color(254, 243, 199));

        statsPanel.add(lblTotalSlot);
        statsPanel.add(lblTersedia);
        statsPanel.add(lblTerisi);
        statsPanel.add(lblPendapatan);

        header.add(titleBox, BorderLayout.WEST);
        header.add(statsPanel, BorderLayout.EAST);
        return header;
    }

    private JLabel createStatBadge(String label, String val, Color textCol, Color bgCol) {
        JLabel l = new JLabel(String.format("<html><div style='text-align:right;'><span style='color:#64748b;font-size:9px;font-weight:600;'>%s</span><br/><b style='font-size:14px;color:%s;'>%s</b></div></html>",
                label, toHex(textCol), val));
        l.setOpaque(true);
        l.setBackground(bgCol);
        l.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1, true),
                new EmptyBorder(5, 12, 5, 12)
        ));
        return l;
    }

    private JPanel createLeftBlueprintPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        // Header Blueprint
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);

        JPanel titleGroup = new JPanel(new GridLayout(2, 1, 0, 2));
        titleGroup.setOpaque(false);
        JLabel lblSec = new JLabel("DENAH PARKIR REALTIME (ARCHITECTURAL BLUEPRINT)");
        lblSec.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSec.setForeground(TEXT_DARK);

        JLabel lblTip = new JLabel("💡 Klik langsung pada mobil di denah untuk check-out • Motor (Baris A), Mobil (Baris B, C, D)");
        lblTip.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTip.setForeground(ACCENT_BLUE);

        titleGroup.add(lblSec);
        titleGroup.add(lblTip);

        // Animated Barrier Gate Box
        JPanel gateCanvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                g2.setColor(BG_LIGHT);
                g2.fillRoundRect(0, 0, w, h, 8, 8);
                g2.setColor(CARD_BORDER);
                g2.drawRoundRect(0, 0, w - 1, h - 1, 8, 8);

                // Pillar
                int pillarX = 18;
                int pillarY = h - 8;
                g2.setColor(new Color(100, 116, 139));
                g2.fillRect(pillarX - 3, pillarY - 12, 6, 12);

                // Barrier Arm
                double rad = Math.toRadians(gateAngle);
                int armLen = 26;
                int armEndX = pillarX + (int) (armLen * Math.cos(rad));
                int armEndY = (pillarY - 8) - (int) (armLen * Math.sin(rad));

                g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(gateStatusColor);
                g2.drawLine(pillarX, pillarY - 8, armEndX, armEndY);

                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                g2.drawString(gateStatusText, pillarX + 34, h / 2 + 4);

                g2.dispose();
            }
        };
        gateCanvas.setPreferredSize(new Dimension(175, 34));
        gateCanvas.setOpaque(false);

        topHeader.add(titleGroup, BorderLayout.WEST);
        topHeader.add(gateCanvas, BorderLayout.EAST);
        panel.add(topHeader, BorderLayout.NORTH);

        // Blueprint Main Container (Architectural Layout)
        blueprintContainer = new JPanel();
        blueprintContainer.setLayout(new BoxLayout(blueprintContainer, BoxLayout.Y_AXIS));
        blueprintContainer.setBackground(CARD_BG);
        blueprintContainer.setBorder(new EmptyBorder(8, 6, 8, 6));

        renderBlueprintLayout();

        JScrollPane scrollPane = new JScrollPane(blueprintContainer);
        scrollPane.setBorder(BorderFactory.createLineBorder(CARD_BORDER, 1, true));
        scrollPane.getViewport().setBackground(CARD_BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Legend at Bottom
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 4));
        legend.setOpaque(false);
        legend.add(createLegendItem("Tersedia (Kosong)", ACCENT_GREEN));
        legend.add(createLegendItem("Terisi (Klik Check-Out)", ACCENT_RED));
        legend.add(createLegendItem("Slot EV Charger", ACCENT_PURPLE));
        legend.add(createLegendItem("Zona Motor (Baris A)", new Color(13, 148, 136)));
        legend.add(createLegendItem("Zona Mobil (Baris B, C, D)", ACCENT_BLUE));
        panel.add(legend, BorderLayout.SOUTH);

        return panel;
    }

    private void renderBlueprintLayout() {
        blueprintContainer.removeAll();
        slotCardMap.clear();

        // 1. BARIS A + ENTRANCE RAMP & CONTROL ROOM
        JPanel rowATopSection = new JPanel(new BorderLayout(8, 0));
        rowATopSection.setOpaque(false);

        JPanel rowABox = new JPanel(new BorderLayout(4, 4));
        rowABox.setOpaque(false);
        JLabel lblRowA = new JLabel("BARIS A • ZONA PARKIR MOTOR (9 SLOT)", SwingConstants.LEFT);
        lblRowA.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblRowA.setForeground(new Color(13, 148, 136));
        rowABox.add(lblRowA, BorderLayout.NORTH);

        JPanel rowASlots = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        rowASlots.setOpaque(false);
        for (int i = 1; i <= 9; i++) {
            SlotParkir s = findSlot(String.format("MTR-A%02d", i));
            if (s != null) {
                rowASlots.add(createSlotBay(s, 56, 72, "🛵"));
            }
        }
        rowABox.add(rowASlots, BorderLayout.CENTER);
        rowATopSection.add(rowABox, BorderLayout.CENTER);

        // Ruang Kontrol & Ramp Masuk
        JPanel entranceOffice = new JPanel(new GridLayout(1, 2, 4, 0));
        entranceOffice.setPreferredSize(new Dimension(190, 72));
        entranceOffice.setOpaque(false);

        JPanel office = createFacilityBox("POS & KONTROL", "Staff & CCTV", new Color(241, 245, 249));
        JPanel ramp = createFacilityBox("RAMP MASUK", "i = 18% ↗", new Color(238, 242, 255));
        entranceOffice.add(office);
        entranceOffice.add(ramp);
        rowATopSection.add(entranceOffice, BorderLayout.EAST);

        blueprintContainer.add(rowATopSection);
        blueprintContainer.add(Box.createVerticalStrut(4));

        // 2. AISLE 1 (Lajur Mobil 1 - Driving Way)
        blueprintContainer.add(createDrivewayAisle("➡ ➡ ➡  LAJUR MANUVER 1 (MASUK & PARKIR)  ➡ ➡ ➡"));
        blueprintContainer.add(Box.createVerticalStrut(4));

        // 3. BARIS B (Menghadap ke atas / Lajur 1)
        JPanel rowBBox = new JPanel(new BorderLayout(4, 4));
        rowBBox.setOpaque(false);
        JLabel lblRowB = new JLabel("BARIS B • ZONA MOBIL (11 SLOT + CORE TANGGA)", SwingConstants.LEFT);
        lblRowB.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblRowB.setForeground(ACCENT_BLUE);
        rowBBox.add(lblRowB, BorderLayout.NORTH);

        JPanel rowBSlots = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 2));
        rowBSlots.setOpaque(false);
        for (int i = 1; i <= 12; i++) {
            if (i == 5) {
                rowBSlots.add(createStairCoreBox());
                continue;
            }
            SlotParkir s = findSlot(String.format("MBL-B%02d", i));
            if (s != null) {
                rowBSlots.add(createSlotBay(s, 58, 74, "🚗"));
            }
            // Pilar beton di antara setiap 2 slot
            if (i == 2 || i == 4 || i == 6 || i == 8 || i == 10) {
                rowBSlots.add(createPillar());
            }
        }
        rowBBox.add(rowBSlots, BorderLayout.CENTER);
        blueprintContainer.add(rowBBox);
        blueprintContainer.add(Box.createVerticalStrut(4));

        // 4. BARIS C (Menghadap ke bawah / Lajur 2)
        JPanel rowCBox = new JPanel(new BorderLayout(4, 4));
        rowCBox.setOpaque(false);
        JLabel lblRowC = new JLabel("BARIS C • ZONA MOBIL (11 SLOT + CORE TANGGA)", SwingConstants.LEFT);
        lblRowC.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblRowC.setForeground(ACCENT_BLUE);
        rowCBox.add(lblRowC, BorderLayout.NORTH);

        JPanel rowCSlots = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 2));
        rowCSlots.setOpaque(false);
        for (int i = 1; i <= 12; i++) {
            if (i == 5) {
                rowCSlots.add(createStairCoreBox());
                continue;
            }
            SlotParkir s = findSlot(String.format("MBL-C%02d", i));
            if (s != null) {
                rowCSlots.add(createSlotBay(s, 58, 74, "🚗"));
            }
            if (i == 2 || i == 4 || i == 6 || i == 8 || i == 10) {
                rowCSlots.add(createPillar());
            }
        }
        rowCBox.add(rowCSlots, BorderLayout.CENTER);
        blueprintContainer.add(rowCBox);
        blueprintContainer.add(Box.createVerticalStrut(4));

        // 5. AISLE 2 (Lajur Mobil 2 - Driving Way Menuju Keluar)
        blueprintContainer.add(createDrivewayAisle("⬅ ⬅ ⬅  LAJUR MANUVER 2 (MENUJU CHECK-OUT & KELUAR)  ⬅ ⬅ ⬅"));
        blueprintContainer.add(Box.createVerticalStrut(4));

        // 6. BARIS D (Bawah: 12 Slot Mobil + 2 Slot EV Charger)
        JPanel rowDBox = new JPanel(new BorderLayout(4, 4));
        rowDBox.setOpaque(false);
        JLabel lblRowD = new JLabel("BARIS D • ZONA MOBIL & EV CHARGER (14 SLOT)", SwingConstants.LEFT);
        lblRowD.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblRowD.setForeground(new Font("Segoe UI", Font.BOLD, 11).equals(lblRowD.getFont()) ? ACCENT_BLUE : TEXT_DARK);
        rowDBox.add(lblRowD, BorderLayout.NORTH);

        JPanel rowDSlots = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 2));
        rowDSlots.setOpaque(false);
        for (int i = 1; i <= 12; i++) {
            SlotParkir s = findSlot(String.format("MBL-D%02d", i));
            if (s != null) {
                rowDSlots.add(createSlotBay(s, 56, 74, "🚗"));
            }
            if (i == 2 || i == 4 || i == 6 || i == 8 || i == 10 || i == 12) {
                rowDSlots.add(createPillar());
            }
        }
        // EV Fast Chargers
        SlotParkir ev1 = findSlot("EV-D13");
        if (ev1 != null) rowDSlots.add(createSlotBay(ev1, 56, 74, "⚡"));
        SlotParkir ev2 = findSlot("EV-D14");
        if (ev2 != null) rowDSlots.add(createSlotBay(ev2, 56, 74, "⚡"));

        rowDBox.add(rowDSlots, BorderLayout.CENTER);
        blueprintContainer.add(rowDBox);

        blueprintContainer.revalidate();
        blueprintContainer.repaint();
    }

    private SlotParkir findSlot(String id) {
        for (SlotParkir s : gedung.getDaftarSlot()) {
            if (s.getIdSlot().equalsIgnoreCase(id)) return s;
        }
        return null;
    }

    private JPanel createSlotBay(SlotParkir slot, int width, int height, String icon) {
        boolean isSelected = (selectedSlot != null && selectedSlot.getIdSlot().equalsIgnoreCase(slot.getIdSlot()));
        boolean isEV = "MOBIL_LISTRIK".equals(slot.getTipeSlot());

        JPanel bay = new JPanel(new BorderLayout(2, 2)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Latar belakang bay
                Color bg;
                if (slot.isTerisi()) {
                    bg = RED_SOFT;
                } else if (isEV) {
                    bg = PURPLE_SOFT;
                } else if ("MOTOR".equals(slot.getTipeSlot())) {
                    bg = new Color(240, 253, 250);
                } else {
                    bg = GREEN_SOFT;
                }
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, w, h, 8, 8);

                // Garis parkir mark (Parking Boundary)
                if (isSelected) {
                    g2.setColor(BORDER_ACTIVE);
                    g2.setStroke(new BasicStroke(2.5f));
                    g2.drawRoundRect(1, 1, w - 3, h - 3, 8, 8);
                } else {
                    g2.setColor(slot.isTerisi() ? new Color(254, 205, 211) : CARD_BORDER);
                    g2.setStroke(new BasicStroke(1.0f));
                    g2.drawRoundRect(0, 0, w - 1, h - 1, 8, 8);
                }

                // Efek animasi pulse denyut lembut pada slot kosong
                if (!slot.isTerisi()) {
                    g2.setColor(new Color(16, 185, 129, (int) (pulseAlpha * 40)));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(2, 2, w - 5, h - 5, 6, 6);
                }

                g2.dispose();
            }
        };
        bay.setPreferredSize(new Dimension(width, height));
        bay.setOpaque(false);
        bay.setBorder(new EmptyBorder(3, 4, 3, 4));

        // Top info: ID Singkat & Ikon
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        String shortId = slot.getIdSlot().substring(slot.getIdSlot().indexOf('-') + 1);
        JLabel lblId = new JLabel(shortId);
        lblId.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblId.setForeground(TEXT_DARK);

        JLabel lblIco = new JLabel(icon);
        lblIco.setFont(new Font("Segoe UI", Font.PLAIN, 9));

        top.add(lblId, BorderLayout.WEST);
        top.add(lblIco, BorderLayout.EAST);
        bay.add(top, BorderLayout.NORTH);

        // Body: Plat nomor atau status
        JPanel center = new JPanel(new GridLayout(2, 1, 0, 0));
        center.setOpaque(false);

        if (slot.isTerisi()) {
            Kendaraan k = slot.getKendaraanParkir();
            JLabel lblPlat = new JLabel(k.getPlatNomor(), SwingConstants.CENTER);
            lblPlat.setFont(new Font("Segoe UI", Font.BOLD, 9));
            lblPlat.setForeground(ACCENT_RED);

            // Singkatkan nama merk agar fit di bay
            String model = k.getMerk();
            if (model.length() > 8) model = model.substring(0, 8) + "..";
            JLabel lblModel = new JLabel(model, SwingConstants.CENTER);
            lblModel.setFont(new Font("Segoe UI", Font.PLAIN, 8));
            lblModel.setForeground(TEXT_MUTED);

            center.add(lblPlat);
            center.add(lblModel);
        } else {
            JLabel lblStatus = new JLabel("FREE", SwingConstants.CENTER);
            lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 9));
            lblStatus.setForeground(isEV ? ACCENT_PURPLE : ACCENT_GREEN);

            JLabel lblSub = new JLabel(isEV ? "⚡EV" : "Siap", SwingConstants.CENTER);
            lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 8));
            lblSub.setForeground(TEXT_MUTED);

            center.add(lblStatus);
            center.add(lblSub);
        }
        bay.add(center, BorderLayout.CENTER);

        // Rekursif attach click listener agar klik di bagian mana saja pada bay memicu checkout!
        attachSlotClickListener(bay, slot);
        slotCardMap.put(slot.getIdSlot(), bay);

        return bay;
    }

    private JPanel createDrivewayAisle(String label) {
        JPanel aisle = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                int w = getWidth();
                int h = getHeight();

                // Latar aspal terang
                g2.setColor(AISLE_BG);
                g2.fillRect(0, 0, w, h);

                // Garis marka putus-putus tengah
                g2.setColor(new Color(203, 213, 225));
                Stroke dashed = new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{8.0f, 6.0f}, 0.0f);
                g2.setStroke(dashed);
                g2.drawLine(10, h / 2, w - 10, h / 2);

                g2.dispose();
            }
        };
        aisle.setPreferredSize(new Dimension(800, 24));
        aisle.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, CARD_BORDER));

        JLabel lbl = new JLabel(label, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lbl.setForeground(new Color(148, 163, 184));
        aisle.add(lbl, BorderLayout.CENTER);
        return aisle;
    }

    private JPanel createPillar() {
        JPanel pillar = new JPanel();
        pillar.setPreferredSize(new Dimension(8, 74));
        pillar.setBackground(PILLAR_COLOR);
        pillar.setToolTipText("Tiang Pilar Beton Struktural");
        pillar.setBorder(BorderFactory.createLineBorder(new Color(30, 41, 59), 1));
        return pillar;
    }

    private JPanel createStairCoreBox() {
        JPanel stair = new JPanel(new BorderLayout());
        stair.setPreferredSize(new Dimension(64, 74));
        stair.setBackground(new Color(241, 245, 249));
        stair.setBorder(BorderFactory.createDashedBorder(new Color(148, 163, 184), 3, 2));

        JLabel lbl = new JLabel("<html><center style='font-size:9px;'><b>TANGGA<br/>& LIFT</b><br/>Darurat</center></html>", SwingConstants.CENTER);
        lbl.setForeground(new Color(71, 85, 105));
        stair.add(lbl, BorderLayout.CENTER);
        return stair;
    }

    private JPanel createFacilityBox(String title, String sub, Color bg) {
        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(bg);
        box.setBorder(BorderFactory.createLineBorder(CARD_BORDER, 1));

        JLabel lbl = new JLabel(String.format("<html><center style='font-size:9px;'><b>%s</b><br/><span style='color:#64748b;'>%s</span></center></html>", title, sub), SwingConstants.CENTER);
        box.add(lbl, BorderLayout.CENTER);
        return box;
    }

    private void attachSlotClickListener(Component comp, SlotParkir slot) {
        comp.setCursor(new Cursor(Cursor.HAND_CURSOR));
        comp.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onSlotClicked(slot);
            }
        });
        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                attachSlotClickListener(child, slot);
            }
        }
    }

    private void onSlotClicked(SlotParkir slot) {
        this.selectedSlot = slot;

        if (slot.isTerisi()) {
            Kendaraan k = slot.getKendaraanParkir();

            // Cari tiket aktif yang menempati slot ini
            this.selectedTiket = null;
            for (KarcisParkir t : gedung.getRiwayatTransaksi()) {
                if (!t.isSudahDibayar() && t.getSlot().getIdSlot().equalsIgnoreCase(slot.getIdSlot())) {
                    this.selectedTiket = t;
                    break;
                }
            }

            if (selectedTiket != null) {
                txtCariTiket.setText(selectedTiket.getNomorTiket());

                // Berikan default jam keluar yang logis (jamMasuk + 3)
                try {
                    int jamKeluarCurr = Integer.parseInt(txtJamKeluar.getText().trim());
                    if (jamKeluarCurr <= selectedTiket.getJamMasuk()) {
                        int defaultKeluar = Math.min(23, selectedTiket.getJamMasuk() + 3);
                        txtJamKeluar.setText(String.valueOf(defaultKeluar));
                    }
                } catch (Exception ex) {
                    txtJamKeluar.setText(String.valueOf(Math.min(23, selectedTiket.getJamMasuk() + 3)));
                }

                updateCheckoutPreview();

                lblCheckoutNotice.setText(String.format(
                        "<html><b style='color:#2563eb;'>✔ Terpilih: %s (%s)</b> di Slot %s<br/>" +
                        "<span style='color:#059669;font-size:10px;'>Siap Check-Out! Klik tombol hijau di bawah untuk proses pembayaran.</span></html>",
                        k.getPlatNomor(), k.getMerk(), slot.getIdSlot()
                ));
            } else {
                lblCheckoutNotice.setText(String.format("<html><span style='color:#e11d48;'>Tiket aktif untuk slot %s tidak ditemukan.</span></html>", slot.getIdSlot()));
            }
        } else {
            this.selectedTiket = null;
            lblCheckoutNotice.setText(String.format("<html><span style='color:#64748b;'>Slot <b>%s</b> (%s) sedang kosong. Klik mobil merah untuk check-out.</span></html>",
                    slot.getIdSlot(), slot.getTipeSlot()));
            lblBillingPreview.setText("Pilih kendaraan terisi");
        }

        renderBlueprintLayout();
    }

    private JPanel createRightControlsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(480, 780));
        panel.setOpaque(false);

        // 1. Kartu Perbandingan Tarif (Motor vs Mobil vs EV)
        panel.add(createTariffComparisonCard());
        panel.add(Box.createVerticalStrut(10));

        // 2. Form Check-In (Masuk)
        panel.add(createCheckInBox());
        panel.add(Box.createVerticalStrut(10));

        // 3. Form Check-Out & Kasir (Keluar)
        panel.add(createCheckOutBox());
        panel.add(Box.createVerticalStrut(10));

        // 4. Tabel Log Riwayat Transaksi
        panel.add(createLogTableBox());

        return panel;
    }

    private JPanel createTariffComparisonCard() {
        JPanel card = createCardBox("SKEMA TARIF PARKIR RESMI");

        JPanel tariffGrid = new JPanel(new GridLayout(1, 3, 8, 0));
        tariffGrid.setOpaque(false);

        // Motor
        JPanel motorTariff = createTariffPill("🛵 MOTOR", "Rp 2.000 / jam 1", "+Rp 1.000 / jam", "Baris A (9 Slot)", new Color(240, 253, 250), new Color(13, 148, 136));
        // Mobil
        JPanel carTariff = createTariffPill("🚗 MOBIL", "Rp 5.000 / jam 1", "+Rp 3.000 / jam", "Baris B, C, D", BLUE_SOFT, ACCENT_BLUE);
        // EV
        JPanel evTariff = createTariffPill("⚡ EV CHARGER", "Diskon 20%", "Gratis Cas Baterai", "Slot EV-D13, D14", PURPLE_SOFT, ACCENT_PURPLE);

        tariffGrid.add(motorTariff);
        tariffGrid.add(carTariff);
        tariffGrid.add(evTariff);

        card.add(tariffGrid, BorderLayout.CENTER);
        return card;
    }

    private JPanel createTariffPill(String title, String rate1, String rateNext, String loc, Color bg, Color textCol) {
        JPanel p = new JPanel(new GridLayout(4, 1, 0, 2));
        p.setBackground(bg);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1, true),
                new EmptyBorder(6, 8, 6, 8)
        ));

        JLabel l1 = new JLabel(title);
        l1.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l1.setForeground(textCol);

        JLabel l2 = new JLabel(rate1);
        l2.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l2.setForeground(TEXT_DARK);

        JLabel l3 = new JLabel(rateNext);
        l3.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        l3.setForeground(TEXT_MUTED);

        JLabel l4 = new JLabel(loc);
        l4.setFont(new Font("Segoe UI", Font.ITALIC, 8));
        l4.setForeground(textCol);

        p.add(l1);
        p.add(l2);
        p.add(l3);
        p.add(l4);
        return p;
    }

    private JPanel createCheckInBox() {
        JPanel box = createCardBox("CHECK-IN (GATE MASUK)");

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 6));
        form.setOpaque(false);

        form.add(createFieldLabel("Nomor Plat:"));
        txtPlat = createStyledTextField("B 5544 PBO");
        form.add(txtPlat);

        form.add(createFieldLabel("Tipe Kendaraan:"));
        cbTipeKendaraan = new JComboBox<>(new String[]{"MOBIL", "MOTOR", "MOBIL_LISTRIK"});
        styleComboBox(cbTipeKendaraan);
        cbTipeKendaraan.addActionListener(e -> updateCheckInAllocationHint());
        form.add(cbTipeKendaraan);

        form.add(createFieldLabel("Merk / Model:"));
        txtMerk = createStyledTextField("Mitsubishi Xpander");
        form.add(txtMerk);

        form.add(createFieldLabel("Jam Masuk (0-23):"));
        txtJamMasuk = createStyledTextField("11");
        form.add(txtJamMasuk);

        form.add(createFieldLabel("Alokasi Sistem:"));
        lblAlokasiInfo = new JLabel("🚗 Dialokasikan ke Baris B, C, atau D");
        lblAlokasiInfo.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblAlokasiInfo.setForeground(ACCENT_BLUE);
        form.add(lblAlokasiInfo);

        box.add(form, BorderLayout.CENTER);

        JButton btnMasuk = createStyledButton("🚗 TAP IN & CARI SLOT OTOMATIS", ACCENT_BLUE, Color.WHITE);
        btnMasuk.addActionListener(e -> handleCheckIn());
        box.add(btnMasuk, BorderLayout.SOUTH);

        return box;
    }

    private void updateCheckInAllocationHint() {
        String tipe = (String) cbTipeKendaraan.getSelectedItem();
        if ("MOTOR".equals(tipe)) {
            lblAlokasiInfo.setText("🛵 Dialokasikan ke Baris A (Zona Motor)");
            lblAlokasiInfo.setForeground(new Color(13, 148, 136));
        } else if ("MOBIL_LISTRIK".equals(tipe)) {
            lblAlokasiInfo.setText("⚡ Prioritas ke Slot EV-D13 / EV-D14");
            lblAlokasiInfo.setForeground(ACCENT_PURPLE);
        } else {
            lblAlokasiInfo.setText("🚗 Dialokasikan ke Baris B, C, atau D");
            lblAlokasiInfo.setForeground(ACCENT_BLUE);
        }
    }

    private JPanel createCheckOutBox() {
        JPanel box = createCardBox("CHECK-OUT & KASIR");

        JPanel formPanel = new JPanel(new BorderLayout(0, 6));
        formPanel.setOpaque(false);

        lblCheckoutNotice = new JLabel("<html><span style='color:#64748b;font-size:10px;'>💡 Klik mobil parkir pada denah untuk memilih otomatis</span></html>");
        lblCheckoutNotice.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1, true),
                new EmptyBorder(5, 8, 5, 8)
        ));
        formPanel.add(lblCheckoutNotice, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(4, 2, 8, 6));
        form.setOpaque(false);

        form.add(createFieldLabel("No. Tiket:"));
        txtCariTiket = createStyledTextField("TKT-0002");
        form.add(txtCariTiket);

        form.add(createFieldLabel("Jam Keluar (0-23):"));
        txtJamKeluar = createStyledTextField("12");
        txtJamKeluar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateCheckoutPreview(); }
            public void removeUpdate(DocumentEvent e) { updateCheckoutPreview(); }
            public void changedUpdate(DocumentEvent e) { updateCheckoutPreview(); }
        });
        form.add(txtJamKeluar);

        form.add(createFieldLabel("Metode Bayar:"));
        cbMetodeBayar = new JComboBox<>(new String[]{"E_MONEY", "QRIS", "CASH"});
        styleComboBox(cbMetodeBayar);
        form.add(cbMetodeBayar);

        form.add(createFieldLabel("Nominal Bayar (Rp):"));
        txtUangBayar = createStyledTextField("14000");
        form.add(txtUangBayar);

        formPanel.add(form, BorderLayout.CENTER);

        // Preview Tagihan Box
        JPanel previewBox = new JPanel(new BorderLayout());
        previewBox.setBackground(BLUE_SOFT);
        previewBox.setBorder(new EmptyBorder(6, 8, 6, 8));
        JLabel lblTagihanTitle = new JLabel("Estimasi Tagihan:");
        lblTagihanTitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTagihanTitle.setForeground(TEXT_MUTED);

        lblBillingPreview = new JLabel("Rp 14.000 (4 Jam)");
        lblBillingPreview.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblBillingPreview.setForeground(ACCENT_BLUE);

        previewBox.add(lblTagihanTitle, BorderLayout.WEST);
        previewBox.add(lblBillingPreview, BorderLayout.EAST);
        formPanel.add(previewBox, BorderLayout.SOUTH);

        box.add(formPanel, BorderLayout.CENTER);

        JButton btnKeluar = createStyledButton("💳 BAYAR & BUKA GATE", ACCENT_GREEN, Color.WHITE);
        btnKeluar.addActionListener(e -> handleCheckOut());
        box.add(btnKeluar, BorderLayout.SOUTH);

        return box;
    }

    private JPanel createLogTableBox() {
        JPanel box = createCardBox("LOG RIWAYAT TIKET & KASIR");

        String[] cols = {"No. Tiket", "Plat", "Tipe", "Slot", "Jam", "Status", "Biaya"};
        logTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        JTable table = new JTable(logTableModel);
        table.setBackground(CARD_BG);
        table.setForeground(TEXT_BODY);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        table.setRowHeight(24);
        table.setShowGrid(false);
        table.setSelectionBackground(BLUE_SOFT);
        table.setSelectionForeground(ACCENT_BLUE);

        table.getTableHeader().setBackground(BG_LIGHT);
        table.getTableHeader().setForeground(TEXT_MUTED);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 10));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(center);
        }

        // Klik baris pada tabel langsung menyeleksi kendaraan di denah dan mengisi kasir
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0 && row < logTableModel.getRowCount()) {
                    String noTiket = (String) logTableModel.getValueAt(row, 0);
                    for (KarcisParkir t : gedung.getRiwayatTransaksi()) {
                        if (t.getNomorTiket().equalsIgnoreCase(noTiket)) {
                            selectedTiket = t;
                            selectedSlot = t.getSlot();
                            txtCariTiket.setText(t.getNomorTiket());
                            updateCheckoutPreview();
                            lblCheckoutNotice.setText(String.format(
                                    "<html><b style='color:#2563eb;'>✔ Terpilih dari Tabel: %s</b> di Slot %s<br/>" +
                                    "<span style='color:#059669;font-size:10px;'>Klik tombol hijau di bawah untuk proses bayar.</span></html>",
                                    t.getKendaraan().getPlatNomor(), t.getSlot().getIdSlot()
                            ));
                            renderBlueprintLayout();
                            break;
                        }
                    }
                }
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(440, 140));
        sp.setBorder(BorderFactory.createLineBorder(CARD_BORDER, 1, true));
        sp.getViewport().setBackground(CARD_BG);
        box.add(sp, BorderLayout.CENTER);

        return box;
    }

    private void updateCheckoutPreview() {
        String noTiket = txtCariTiket.getText().trim();
        KarcisParkir t = null;
        for (KarcisParkir item : gedung.getRiwayatTransaksi()) {
            if (item.getNomorTiket().equalsIgnoreCase(noTiket) && !item.isSudahDibayar()) {
                t = item;
                break;
            }
        }

        if (t == null) {
            lblBillingPreview.setText("Tiket tidak aktif / lunas");
            return;
        }

        try {
            int jamKeluar = Integer.parseInt(txtJamKeluar.getText().trim());
            int durasi = t.hitungDurasi(jamKeluar);

            // Beda tarif untuk MOTOR vs MOBIL
            boolean isMotor = "MOTOR".equals(t.getKendaraan().getTipe());
            double tarifPertama = isMotor ? gedung.getTarifJamPertamaMotor() : gedung.getTarifJamPertamaMobil();
            double tarifBerikutnya = isMotor ? gedung.getTarifPerJamMotor() : gedung.getTarifPerJamMobil();

            double subtotal = tarifPertama + (durasi > 1 ? (durasi - 1) * tarifBerikutnya : 0);

            if (t.getKendaraan().isMobilListrik()) {
                subtotal = subtotal * 0.80; // Diskon EV 20%
            }

            lblBillingPreview.setText(String.format("%s (%d Jam)%s",
                    rupiahFormat.format(subtotal), durasi,
                    (t.getKendaraan().isMobilListrik() ? " [Diskon EV 20%]" : (isMotor ? " [Tarif Motor]" : ""))));

            txtUangBayar.setText(String.valueOf((long) subtotal));
        } catch (Exception ignored) {
            lblBillingPreview.setText("-");
        }
    }

    private void handleCheckIn() {
        try {
            String plat = txtPlat.getText().trim();
            String tipe = (String) cbTipeKendaraan.getSelectedItem();
            String merk = txtMerk.getText().trim();
            int jam = Integer.parseInt(txtJamMasuk.getText().trim());

            if (plat.isEmpty() || merk.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Plat nomor dan merk kendaraan tidak boleh kosong!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Kendaraan k = new Kendaraan(plat, tipe, merk);
            KarcisParkir tiket = gedung.prosesMasuk(k, jam);

            if (tiket != null) {
                triggerGateAnimation(true, "GATE MASUK DIBUKA");
                selectedSlot = tiket.getSlot();
                selectedTiket = tiket;
                txtCariTiket.setText(tiket.getNomorTiket());

                renderBlueprintLayout();
                updateStats();
                updateLogTable();
                updateCheckoutPreview();

                String zonaInfo = "MOTOR".equals(tipe) ? "Zona Motor (Baris A)" : ("MOBIL_LISTRIK".equals(tipe) ? "Zona EV Fast Charger" : "Zona Mobil");
                JOptionPane.showMessageDialog(this,
                        String.format("Kendaraan %s Berhasil Masuk!\nTiket: %s\nAlokasi: Slot %s (Lantai %d) - %s",
                                plat, tiket.getNomorTiket(), tiket.getSlot().getIdSlot(), tiket.getSlot().getLantai(), zonaInfo),
                        "Check-In Berhasil", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Maaf, Parkir Penuh untuk kategori " + tipe + "!",
                        "Parkir Penuh", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Input tidak valid: " + ex.getMessage(), "Error Input", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleCheckOut() {
        try {
            String noTiket = txtCariTiket.getText().trim();
            int jamKeluar = Integer.parseInt(txtJamKeluar.getText().trim());
            String metode = (String) cbMetodeBayar.getSelectedItem();
            double uang = Double.parseDouble(txtUangBayar.getText().trim());

            KarcisParkir tiketDitemukan = null;
            for (KarcisParkir t : gedung.getRiwayatTransaksi()) {
                if (t.getNomorTiket().equalsIgnoreCase(noTiket)) {
                    tiketDitemukan = t;
                    break;
                }
            }

            if (tiketDitemukan == null) {
                JOptionPane.showMessageDialog(this, "Tiket " + noTiket + " tidak ditemukan!\nSilakan klik salah satu mobil terisi di denah.", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (tiketDitemukan.isSudahDibayar()) {
                JOptionPane.showMessageDialog(this, "Tiket ini sudah pernah dibayar dan kendaraan telah keluar.", "Peringatan", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            boolean sukses = gedung.prosesKeluar(tiketDitemukan, jamKeluar, metode, uang);
            if (sukses) {
                triggerGateAnimation(true, "GATE KELUAR DIBUKA");

                this.selectedSlot = null;
                this.selectedTiket = null;
                lblCheckoutNotice.setText("<html><span style='color:#059669;font-weight:600;'>✔ Check-out sukses! Slot telah kosong kembali.</span></html>");
                lblBillingPreview.setText("Rp 0");

                renderBlueprintLayout();
                updateStats();
                updateLogTable();

                JOptionPane.showMessageDialog(this,
                        String.format("Pembayaran LUNAS!\nKendaraan: %s\nTotal: %s\nKembalian: %s\nPortal Keluar Terbuka, Silakan Jalan.",
                                tiketDitemukan.getKendaraan().getPlatNomor(),
                                rupiahFormat.format(tiketDitemukan.getTotalBiaya()),
                                rupiahFormat.format(uang - tiketDitemukan.getTotalBiaya())),
                        "Check-Out Sukses", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Pembayaran gagal! Pastikan nominal bayar mencukupi total tagihan.", "Gagal Bayar", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Input tidak valid: " + ex.getMessage(), "Error Input", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateStats() {
        int total = gedung.getDaftarSlot().size();
        int terisi = 0;
        for (SlotParkir s : gedung.getDaftarSlot()) {
            if (s.isTerisi()) terisi++;
        }
        int tersedia = total - terisi;

        lblTotalSlot.setText(String.format("<html><div style='text-align:right;'><span style='color:#64748b;font-size:9px;font-weight:600;'>TOTAL SLOT</span><br/><b style='font-size:14px;color:%s;'>%d</b></div></html>", toHex(TEXT_DARK), total));
        lblTersedia.setText(String.format("<html><div style='text-align:right;'><span style='color:#64748b;font-size:9px;font-weight:600;'>TERSEDIA</span><br/><b style='font-size:14px;color:%s;'>%d</b></div></html>", toHex(ACCENT_GREEN), tersedia));
        lblTerisi.setText(String.format("<html><div style='text-align:right;'><span style='color:#64748b;font-size:9px;font-weight:600;'>TERISI</span><br/><b style='font-size:14px;color:%s;'>%d</b></div></html>", toHex(ACCENT_RED), terisi));
        lblPendapatan.setText(String.format("<html><div style='text-align:right;'><span style='color:#64748b;font-size:9px;font-weight:600;'>KAS MASUK</span><br/><b style='font-size:14px;color:%s;'>%s</b></div></html>", toHex(ACCENT_YELLOW), rupiahFormat.format(gedung.getTotalPendapatan())));
    }

    private void updateLogTable() {
        logTableModel.setRowCount(0);
        for (KarcisParkir t : gedung.getRiwayatTransaksi()) {
            String jamKet = t.isSudahDibayar() ? (t.getJamMasuk() + ":00 - " + t.getJamKeluar() + ":00") : (t.getJamMasuk() + ":00 - ...");
            String status = t.isSudahDibayar() ? "LUNAS (" + t.getMetodePembayaran() + ")" : "PARKIR";
            String biaya = t.isSudahDibayar() ? rupiahFormat.format(t.getTotalBiaya()) : "-";

            logTableModel.addRow(new Object[]{
                    t.getNomorTiket(),
                    t.getKendaraan().getPlatNomor(),
                    t.getKendaraan().getTipe(),
                    t.getSlot().getIdSlot(),
                    jamKet,
                    status,
                    biaya
            });
        }
    }

    private void triggerGateAnimation(boolean open, String text) {
        targetGateAngle = open ? 75.0f : 0.0f;
        gateStatusText = text;
        gateStatusColor = open ? ACCENT_GREEN : ACCENT_RED;

        if (open) {
            Timer closeTimer = new Timer(3200, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    targetGateAngle = 0.0f;
                    gateStatusText = "PORTAL TERTUTUP";
                    gateStatusColor = ACCENT_RED;
                    ((Timer) e.getSource()).stop();
                }
            });
            closeTimer.setRepeats(false);
            closeTimer.start();
        }
    }

    private void startAnimationEngine() {
        Timer timer = new Timer(16, e -> {
            if (pulseUp) {
                pulseAlpha += 0.02f;
                if (pulseAlpha >= 0.95f) pulseUp = false;
            } else {
                pulseAlpha -= 0.02f;
                if (pulseAlpha <= 0.35f) pulseUp = true;
            }

            if (Math.abs(gateAngle - targetGateAngle) > 0.5f) {
                gateAngle += (targetGateAngle - gateAngle) * 0.15f;
            } else {
                gateAngle = targetGateAngle;
            }

            repaint();
        });
        timer.start();
    }

    private JPanel createCardBox(String title) {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_DARK);
        p.add(lbl, BorderLayout.NORTH);
        return p;
    }

    private JLabel createFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(TEXT_BODY);
        return l;
    }

    private JTextField createStyledTextField(String initVal) {
        JTextField tf = new JTextField(initVal);
        tf.setBackground(BG_LIGHT);
        tf.setForeground(TEXT_DARK);
        tf.setCaretColor(ACCENT_BLUE);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1, true),
                new EmptyBorder(4, 6, 4, 6)
        ));
        return tf;
    }

    private void styleComboBox(JComboBox<String> cb) {
        cb.setBackground(BG_LIGHT);
        cb.setForeground(TEXT_DARK);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 11));
    }

    private JButton createStyledButton(String text, Color bg, Color textCol) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setBackground(bg);
        btn.setForeground(textCol);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 12, 8, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel createLegendItem(String label, Color col) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);
        JLabel dot = new JLabel("●");
        dot.setForeground(col);
        dot.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lbl.setForeground(TEXT_MUTED);
        p.add(dot);
        p.add(lbl);
        return p;
    }

    private String toHex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            AdminGUI app = new AdminGUI();
            app.setVisible(true);
        });
    }
}

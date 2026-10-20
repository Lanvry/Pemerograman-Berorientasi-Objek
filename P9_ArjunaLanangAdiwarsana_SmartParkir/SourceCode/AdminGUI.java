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
import java.util.Locale;

/**
 * SmartParkir Command Center - Desktop Control UI.
 * Refactored strictly adhering to Antislop UI guidelines:
 * - Clean, purposeful palette (Slate & Crisp Light)
 * - Zero decorative emojis
 * - Zero endless pulsing loops
 * - Dynamic tabbed operations (Check-Out / Check-In) so the log table is never cut off
 * - Proportional blueprint bays that fit without horizontal scrollbars
 * - Fully visible license plates (two-line stacking for zero truncation)
 */
public class AdminGUI extends JFrame {

    // Palet Warna Profesional & Terstruktur (Antislop Palette)
    private static final Color BG_PAGE = new Color(248, 250, 252);        // #f8fafc
    private static final Color CARD_BG = new Color(255, 255, 255);         // #ffffff
    private static final Color BORDER_SUBTLE = new Color(226, 232, 240);    // #e2e8f0
    private static final Color BORDER_ACTIVE = new Color(37, 99, 235);     // #2563eb
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);       // #0f172a
    private static final Color TEXT_SECONDARY = new Color(51, 65, 85);     // #334155
    private static final Color TEXT_MUTED = new Color(100, 116, 139);      // #64748b

    // Brand & Status Colors
    private static final Color BRAND_BLUE = new Color(37, 99, 235);        // #2563eb
    private static final Color BRAND_BLUE_HOVER = new Color(29, 78, 216);  // #1d4ed8
    private static final Color STATUS_SUCCESS = new Color(5, 150, 105);    // #059669
    private static final Color STATUS_DANGER = new Color(220, 38, 38);     // #dc2626
    private static final Color STATUS_PURPLE = new Color(124, 58, 237);    // #7c3aed
    private static final Color STATUS_TEAL = new Color(13, 148, 136);      // #0d9488

    // Soft Surface States (High Contrast)
    private static final Color SURFACE_SUCCESS = new Color(240, 253, 244);
    private static final Color SURFACE_DANGER = new Color(254, 242, 242);
    private static final Color SURFACE_PURPLE = new Color(245, 243, 255);
    private static final Color SURFACE_TEAL = new Color(240, 253, 250);
    private static final Color AISLE_BG = new Color(241, 245, 249);
    private static final Color PILLAR_COLOR = new Color(71, 85, 105);

    private GedungParkir gedung;
    private NumberFormat rupiahFormat;

    // State Interaksi
    private SlotParkir selectedSlot = null;
    private KarcisParkir selectedTiket = null;

    // Komponen Blueprint & Header
    private JPanel blueprintContainer;
    private JLabel lblTotalSlot, lblTersedia, lblTerisi, lblPendapatan;
    private DefaultTableModel logTableModel;
    private JTable logTable;

    // Tabbed Operations
    private JTabbedPane actionTabbedPane;

    // Form Check-Out
    private JTextField txtCariTiket, txtJamKeluar, txtUangBayar;
    private JComboBox<String> cbMetodeBayar;
    private JLabel lblCheckoutNotice;
    private JLabel lblBillingPreview;
    private JLabel lblDetailPlate, lblDetailSlot, lblDetailInTime;

    // Form Check-In
    private JTextField txtPlat, txtMerk, txtJamMasuk;
    private JComboBox<String> cbTipeKendaraan;
    private JLabel lblAlokasiInfo;

    // Barrier Gate Animation State (Triggered ONLY on real action)
    private float gateAngle = 0.0f;
    private float targetGateAngle = 0.0f;
    private String gateStatusText = "PORTAL TERTUTUP";
    private Color gateStatusColor = STATUS_DANGER;
    private Timer gateAnimationTimer;

    public AdminGUI() {
        super("SmartParkir Command Center - Grand PENS Plaza");
        this.rupiahFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

        initData();
        initUI();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 860);
        setMinimumSize(new Dimension(1220, 780));
        setLocationRelativeTo(null);
    }

    private void initData() {
        gedung = new GedungParkir(
                "Mall Grand PENS Plaza",
                5000.0, 3000.0, // Mobil: Rp 5.000 jam ke-1, Rp 3.000 jam berikutnya
                2000.0, 1000.0  // Motor: Rp 2.000 jam ke-1, Rp 1.000 jam berikutnya
        );

        // Baris A: 9 Slot MOTOR
        for (int i = 1; i <= 9; i++) {
            gedung.tambahSlot(new SlotParkir(String.format("MTR-A%02d", i), 1, "MOTOR"));
        }

        // Baris B: 11 Slot MOBIL (B05 adalah Core Tangga)
        for (int i = 1; i <= 12; i++) {
            if (i == 5) continue;
            gedung.tambahSlot(new SlotParkir(String.format("MBL-B%02d", i), 1, "MOBIL"));
        }

        // Baris C: 11 Slot MOBIL (C05 adalah Core Tangga)
        for (int i = 1; i <= 12; i++) {
            if (i == 5) continue;
            gedung.tambahSlot(new SlotParkir(String.format("MBL-C%02d", i), 1, "MOBIL"));
        }

        // Baris D: 12 Slot MOBIL + 2 Slot EV Charger
        for (int i = 1; i <= 12; i++) {
            gedung.tambahSlot(new SlotParkir(String.format("MBL-D%02d", i), 1, "MOBIL"));
        }
        gedung.tambahSlot(new SlotParkir("EV-D13", 1, "MOBIL_LISTRIK"));
        gedung.tambahSlot(new SlotParkir("EV-D14", 1, "MOBIL_LISTRIK"));

        // Data awal yang realistis
        Kendaraan k1 = new Kendaraan("B 6789 PBO", "MOTOR", "Honda Vario 160");
        gedung.prosesMasuk(k1, 9);

        Kendaraan k2 = new Kendaraan("B 1234 ABC", "MOBIL", "Toyota Avanza");
        KarcisParkir t2 = gedung.prosesMasuk(k2, 8);

        Kendaraan k3 = new Kendaraan("L 9988 XYZ", "MOBIL", "Honda HR-V");
        gedung.prosesMasuk(k3, 10);

        Kendaraan k4 = new Kendaraan("B 1010 EV", "MOBIL_LISTRIK", "Hyundai Ioniq 5");
        gedung.prosesMasuk(k4, 9);

        if (t2 != null) {
            this.selectedSlot = t2.getSlot();
            this.selectedTiket = t2;
        }
    }

    private void initUI() {
        getContentPane().setBackground(BG_PAGE);
        setLayout(new BorderLayout(0, 0));

        // 1. Header Navbar
        add(createNavbarHeader(), BorderLayout.NORTH);

        // 2. Main Content (Left: Blueprint, Right: Control Panel)
        JPanel contentGrid = new JPanel(new BorderLayout(14, 0));
        contentGrid.setBackground(BG_PAGE);
        contentGrid.setBorder(new EmptyBorder(12, 16, 14, 16));

        contentGrid.add(createBlueprintPanel(), BorderLayout.CENTER);
        contentGrid.add(createOperationPanel(), BorderLayout.EAST);

        add(contentGrid, BorderLayout.CENTER);

        // Sinkronisasi data
        updateStats();
        updateLogTable();
        updateCheckoutPreview();
    }

    private JPanel createNavbarHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CARD_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_SUBTLE),
                new EmptyBorder(10, 20, 10, 20)
        ));

        // Brand
        JPanel brandBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandBox.setOpaque(false);

        JLabel brandIcon = new JLabel("P", SwingConstants.CENTER);
        brandIcon.setOpaque(true);
        brandIcon.setBackground(BRAND_BLUE);
        brandIcon.setForeground(Color.WHITE);
        brandIcon.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brandIcon.setPreferredSize(new Dimension(34, 34));
        brandIcon.setBorder(BorderFactory.createLineBorder(BRAND_BLUE_HOVER, 1));

        JPanel brandText = new JPanel(new GridLayout(2, 1, 0, 1));
        brandText.setOpaque(false);
        JLabel title = new JLabel("SmartParkir Command Center");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_PRIMARY);

        JLabel sub = new JLabel("Mall Grand PENS Plaza - Layout 45 Slot Terstruktur (Zona Motor & Mobil)");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(TEXT_MUTED);

        brandText.add(title);
        brandText.add(sub);

        brandBox.add(brandIcon);
        brandBox.add(brandText);

        // Summary Badges
        JPanel stats = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        stats.setOpaque(false);

        lblTotalSlot = createStatPill("TOTAL SLOT", "45", TEXT_PRIMARY);
        lblTersedia = createStatPill("TERSEDIA", "41", STATUS_SUCCESS);
        lblTerisi = createStatPill("TERISI", "4", STATUS_DANGER);
        lblPendapatan = createStatPill("KAS MASUK", "Rp 0", new Color(180, 83, 9));

        stats.add(lblTotalSlot);
        stats.add(lblTersedia);
        stats.add(lblTerisi);
        stats.add(lblPendapatan);

        header.add(brandBox, BorderLayout.WEST);
        header.add(stats, BorderLayout.EAST);
        return header;
    }

    private JLabel createStatPill(String label, String value, Color valColor) {
        JLabel pill = new JLabel(String.format("<html><div style='text-align:right;'><span style='color:#64748b;font-size:8.5px;font-weight:600;'>%s</span><br/><b style='font-size:13px;color:%s;'>%s</b></div></html>",
                label, toHex(valColor), value));
        pill.setOpaque(true);
        pill.setBackground(BG_PAGE);
        pill.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SUBTLE, 1),
                new EmptyBorder(4, 10, 4, 10)
        ));
        return pill;
    }

    private JPanel createBlueprintPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SUBTLE, 1),
                new EmptyBorder(12, 14, 10, 14)
        ));

        // Panel Header
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);

        JPanel titleGroup = new JPanel(new GridLayout(2, 1, 0, 1));
        titleGroup.setOpaque(false);
        JLabel secTitle = new JLabel("DENAH PARKIR REALTIME (ARCHITECTURAL BLUEPRINT)");
        secTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        secTitle.setForeground(TEXT_PRIMARY);

        JLabel tip = new JLabel("Pilih kendaraan terparkir di denah untuk langsung memproses check-out.");
        tip.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tip.setForeground(BRAND_BLUE);

        titleGroup.add(secTitle);
        titleGroup.add(tip);

        // Animated Barrier Gate Indicator
        JPanel gateCanvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                g2.setColor(BG_PAGE);
                g2.fillRoundRect(0, 0, w, h, 6, 6);
                g2.setColor(BORDER_SUBTLE);
                g2.drawRoundRect(0, 0, w - 1, h - 1, 6, 6);

                int pillarX = 16;
                int pillarY = h - 6;
                g2.setColor(PILLAR_COLOR);
                g2.fillRect(pillarX - 3, pillarY - 14, 6, 14);

                double rad = Math.toRadians(gateAngle);
                int armLen = 26;
                int armEndX = pillarX + (int) (armLen * Math.cos(rad));
                int armEndY = (pillarY - 10) - (int) (armLen * Math.sin(rad));

                g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(gateStatusColor);
                g2.drawLine(pillarX, pillarY - 10, armEndX, armEndY);

                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                g2.drawString(gateStatusText, pillarX + 34, h / 2 + 4);

                g2.dispose();
            }
        };
        gateCanvas.setPreferredSize(new Dimension(160, 30));
        gateCanvas.setOpaque(false);

        topHeader.add(titleGroup, BorderLayout.WEST);
        topHeader.add(gateCanvas, BorderLayout.EAST);
        panel.add(topHeader, BorderLayout.NORTH);

        // Blueprint Canvas
        blueprintContainer = new JPanel();
        blueprintContainer.setLayout(new BoxLayout(blueprintContainer, BoxLayout.Y_AXIS));
        blueprintContainer.setBackground(CARD_BG);
        blueprintContainer.setBorder(new EmptyBorder(4, 2, 4, 2));

        renderBlueprintRows();

        JScrollPane scrollPane = new JScrollPane(blueprintContainer);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_SUBTLE, 1));
        scrollPane.getViewport().setBackground(CARD_BG);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Legend at Bottom
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 4));
        legend.setOpaque(false);
        legend.add(createLegendChip("Tersedia", STATUS_SUCCESS));
        legend.add(createLegendChip("Terisi (Klik Check-Out)", STATUS_DANGER));
        legend.add(createLegendChip("EV Charger Ready", STATUS_PURPLE));
        legend.add(createLegendChip("Zona Motor (Baris A)", STATUS_TEAL));
        legend.add(createLegendChip("Zona Mobil (Baris B, C, D)", BRAND_BLUE));
        panel.add(legend, BorderLayout.SOUTH);

        return panel;
    }

    private void renderBlueprintRows() {
        blueprintContainer.removeAll();

        // 1. BARIS A (Motor, 9 Slot) + Ramp & Control Room
        JPanel rowAWrapper = new JPanel(new BorderLayout(8, 0));
        rowAWrapper.setOpaque(false);

        JPanel rowABox = new JPanel(new BorderLayout(2, 2));
        rowABox.setOpaque(false);
        JLabel lblRowA = new JLabel("BARIS A - ZONA PARKIR MOTOR (9 SLOT)");
        lblRowA.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblRowA.setForeground(STATUS_TEAL);
        rowABox.add(lblRowA, BorderLayout.NORTH);

        JPanel rowASlots = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        rowASlots.setOpaque(false);
        for (int i = 1; i <= 9; i++) {
            SlotParkir s = findSlot(String.format("MTR-A%02d", i));
            if (s != null) rowASlots.add(createBayComponent(s, 50, 76, "MTR"));
        }
        rowABox.add(rowASlots, BorderLayout.CENTER);
        rowAWrapper.add(rowABox, BorderLayout.CENTER);

        // Control Office & Ramp (Proportional Fit)
        JPanel entranceOffice = new JPanel(new GridLayout(1, 2, 4, 0));
        entranceOffice.setPreferredSize(new Dimension(170, 76));
        entranceOffice.setOpaque(false);
        entranceOffice.add(createFacilityTile("POS KONTROL", "Staff & CCTV", AISLE_BG));
        entranceOffice.add(createFacilityTile("RAMP MASUK", "Lantai 1", new Color(238, 242, 255)));
        rowAWrapper.add(entranceOffice, BorderLayout.EAST);

        blueprintContainer.add(rowAWrapper);
        blueprintContainer.add(Box.createVerticalStrut(3));

        // 2. LAJUR MANUVER 1
        blueprintContainer.add(createDrivewayLane("LAJUR MANUVER 1 (MASUK & PARKIR)"));
        blueprintContainer.add(Box.createVerticalStrut(3));

        // 3. BARIS B (Mobil, 11 Slot + Tangga)
        JPanel rowBBox = new JPanel(new BorderLayout(2, 2));
        rowBBox.setOpaque(false);
        JLabel lblRowB = new JLabel("BARIS B - ZONA MOBIL (11 SLOT + TANGGA DARURAT)");
        lblRowB.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblRowB.setForeground(BRAND_BLUE);
        rowBBox.add(lblRowB, BorderLayout.NORTH);

        JPanel rowBSlots = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 2));
        rowBSlots.setOpaque(false);
        for (int i = 1; i <= 12; i++) {
            if (i == 5) {
                rowBSlots.add(createStairTile());
                continue;
            }
            SlotParkir s = findSlot(String.format("MBL-B%02d", i));
            if (s != null) rowBSlots.add(createBayComponent(s, 48, 76, "MBL"));
            if (i == 2 || i == 4 || i == 6 || i == 8 || i == 10) {
                rowBSlots.add(createPillarTile());
            }
        }
        rowBBox.add(rowBSlots, BorderLayout.CENTER);
        blueprintContainer.add(rowBBox);
        blueprintContainer.add(Box.createVerticalStrut(3));

        // 4. BARIS C (Mobil, 11 Slot + Tangga)
        JPanel rowCBox = new JPanel(new BorderLayout(2, 2));
        rowCBox.setOpaque(false);
        JLabel lblRowC = new JLabel("BARIS C - ZONA MOBIL (11 SLOT + TANGGA DARURAT)");
        lblRowC.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblRowC.setForeground(BRAND_BLUE);
        rowCBox.add(lblRowC, BorderLayout.NORTH);

        JPanel rowCSlots = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 2));
        rowCSlots.setOpaque(false);
        for (int i = 1; i <= 12; i++) {
            if (i == 5) {
                rowCSlots.add(createStairTile());
                continue;
            }
            SlotParkir s = findSlot(String.format("MBL-C%02d", i));
            if (s != null) rowCSlots.add(createBayComponent(s, 48, 76, "MBL"));
            if (i == 2 || i == 4 || i == 6 || i == 8 || i == 10) {
                rowCSlots.add(createPillarTile());
            }
        }
        rowCBox.add(rowCSlots, BorderLayout.CENTER);
        blueprintContainer.add(rowCBox);
        blueprintContainer.add(Box.createVerticalStrut(3));

        // 5. LAJUR MANUVER 2
        blueprintContainer.add(createDrivewayLane("LAJUR MANUVER 2 (MENUJU KASIR & KELUAR)"));
        blueprintContainer.add(Box.createVerticalStrut(3));

        // 6. BARIS D (Mobil 12 Slot + EV 2 Slot)
        JPanel rowDBox = new JPanel(new BorderLayout(2, 2));
        rowDBox.setOpaque(false);
        JLabel lblRowD = new JLabel("BARIS D - ZONA MOBIL (12 SLOT) & EV FAST CHARGER (2 SLOT)");
        lblRowD.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblRowD.setForeground(TEXT_PRIMARY);
        rowDBox.add(lblRowD, BorderLayout.NORTH);

        JPanel rowDSlots = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 2));
        rowDSlots.setOpaque(false);
        for (int i = 1; i <= 12; i++) {
            SlotParkir s = findSlot(String.format("MBL-D%02d", i));
            if (s != null) rowDSlots.add(createBayComponent(s, 46, 76, "MBL"));
            if (i == 2 || i == 4 || i == 6 || i == 8 || i == 10 || i == 12) {
                rowDSlots.add(createPillarTile());
            }
        }
        SlotParkir ev1 = findSlot("EV-D13");
        SlotParkir ev2 = findSlot("EV-D14");
        if (ev1 != null) rowDSlots.add(createBayComponent(ev1, 46, 76, "EV"));
        if (ev2 != null) rowDSlots.add(createBayComponent(ev2, 46, 76, "EV"));

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

    private JPanel createBayComponent(SlotParkir slot, int width, int height, String typeTag) {
        boolean isSelected = (selectedSlot != null && selectedSlot.getIdSlot().equalsIgnoreCase(slot.getIdSlot()));
        boolean isEV = "MOBIL_LISTRIK".equals(slot.getTipeSlot());
        boolean isMotor = "MOTOR".equals(slot.getTipeSlot());

        JPanel bay = new JPanel(new BorderLayout(0, 2)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Surface
                Color bg;
                if (slot.isTerisi()) {
                    bg = SURFACE_DANGER;
                } else if (isEV) {
                    bg = SURFACE_PURPLE;
                } else if (isMotor) {
                    bg = SURFACE_TEAL;
                } else {
                    bg = SURFACE_SUCCESS;
                }
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, w, h, 6, 6);

                // Border
                if (isSelected) {
                    g2.setColor(BORDER_ACTIVE);
                    g2.setStroke(new BasicStroke(2.0f));
                    g2.drawRoundRect(1, 1, w - 3, h - 3, 6, 6);
                } else {
                    g2.setColor(slot.isTerisi() ? new Color(254, 205, 211) : BORDER_SUBTLE);
                    g2.setStroke(new BasicStroke(1.0f));
                    g2.drawRoundRect(0, 0, w - 1, h - 1, 6, 6);
                }

                g2.dispose();
            }
        };
        bay.setPreferredSize(new Dimension(width, height));
        bay.setOpaque(false);
        bay.setBorder(new EmptyBorder(3, 3, 3, 3));

        // Header: ID Slot & Type
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        String shortId = slot.getIdSlot().substring(slot.getIdSlot().indexOf('-') + 1);
        JLabel lblId = new JLabel(shortId);
        lblId.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lblId.setForeground(TEXT_PRIMARY);

        JLabel lblTag = new JLabel(typeTag);
        lblTag.setFont(new Font("Segoe UI", Font.BOLD, 8));
        Color tagCol = isEV ? STATUS_PURPLE : (isMotor ? STATUS_TEAL : BRAND_BLUE);
        lblTag.setForeground(tagCol);

        top.add(lblId, BorderLayout.WEST);
        top.add(lblTag, BorderLayout.EAST);
        bay.add(top, BorderLayout.NORTH);

        // Body: Plat Nomor (2 Baris agar TIDAK TERPOTONG!)
        JPanel body = new JPanel(new GridLayout(2, 1, 0, 0));
        body.setOpaque(false);

        if (slot.isTerisi()) {
            Kendaraan k = slot.getKendaraanParkir();
            String plat = k.getPlatNomor();
            String p1 = plat;
            String p2 = "";
            int firstSpace = plat.indexOf(' ');
            if (firstSpace > 0) {
                p1 = plat.substring(0, firstSpace);
                p2 = plat.substring(firstSpace + 1);
            }

            JLabel l1 = new JLabel(p1, SwingConstants.CENTER);
            l1.setFont(new Font("Segoe UI", Font.BOLD, 9));
            l1.setForeground(STATUS_DANGER);

            JLabel l2 = new JLabel(p2, SwingConstants.CENTER);
            l2.setFont(new Font("Segoe UI", Font.BOLD, 9));
            l2.setForeground(STATUS_DANGER);

            body.add(l1);
            body.add(l2);
        } else {
            JLabel lblStatus = new JLabel("KOSONG", SwingConstants.CENTER);
            lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 8));
            lblStatus.setForeground(isEV ? STATUS_PURPLE : STATUS_SUCCESS);

            JLabel lblSub = new JLabel("Siap", SwingConstants.CENTER);
            lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 8));
            lblSub.setForeground(TEXT_MUTED);

            body.add(lblStatus);
            body.add(lblSub);
        }
        bay.add(body, BorderLayout.CENTER);

        attachSlotClickListener(bay, slot);
        return bay;
    }

    private JPanel createDrivewayLane(String label) {
        JPanel lane = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                int w = getWidth();
                int h = getHeight();

                g2.setColor(AISLE_BG);
                g2.fillRect(0, 0, w, h);

                g2.setColor(new Color(203, 213, 225));
                Stroke dashed = new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{6.0f, 6.0f}, 0.0f);
                g2.setStroke(dashed);
                g2.drawLine(8, h / 2, w - 8, h / 2);

                g2.dispose();
            }
        };
        lane.setPreferredSize(new Dimension(800, 20));
        lane.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, BORDER_SUBTLE));

        JLabel lbl = new JLabel(label, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lbl.setForeground(new Color(148, 163, 184));
        lane.add(lbl, BorderLayout.CENTER);
        return lane;
    }

    private JPanel createPillarTile() {
        JPanel p = new JPanel();
        p.setPreferredSize(new Dimension(6, 76));
        p.setBackground(PILLAR_COLOR);
        p.setBorder(BorderFactory.createLineBorder(new Color(30, 41, 59), 1));
        return p;
    }

    private JPanel createStairTile() {
        JPanel s = new JPanel(new BorderLayout());
        s.setPreferredSize(new Dimension(54, 76));
        s.setBackground(AISLE_BG);
        s.setBorder(BorderFactory.createDashedBorder(new Color(148, 163, 184), 2, 2));

        JLabel l = new JLabel("<html><center style='font-size:8.5px;'><b>TANGGA</b><br/>LIFT</center></html>", SwingConstants.CENTER);
        l.setForeground(new Color(71, 85, 105));
        s.add(l, BorderLayout.CENTER);
        return s;
    }

    private JPanel createFacilityTile(String title, String sub, Color bg) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(bg);
        p.setBorder(BorderFactory.createLineBorder(BORDER_SUBTLE, 1));

        JLabel l = new JLabel(String.format("<html><center style='font-size:8.5px;'><b>%s</b><br/><span style='color:#64748b;'>%s</span></center></html>", title, sub), SwingConstants.CENTER);
        p.add(l, BorderLayout.CENTER);
        return p;
    }

    private void attachSlotClickListener(Component comp, SlotParkir slot) {
        comp.setCursor(new Cursor(Cursor.HAND_CURSOR));
        comp.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onSlotSelected(slot);
            }
        });
        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                attachSlotClickListener(child, slot);
            }
        }
    }

    private void onSlotSelected(SlotParkir slot) {
        this.selectedSlot = slot;

        if (slot.isTerisi()) {
            Kendaraan k = slot.getKendaraanParkir();

            this.selectedTiket = null;
            for (KarcisParkir t : gedung.getRiwayatTransaksi()) {
                if (!t.isSudahDibayar() && t.getSlot().getIdSlot().equalsIgnoreCase(slot.getIdSlot())) {
                    this.selectedTiket = t;
                    break;
                }
            }

            if (selectedTiket != null) {
                // Pindah langsung ke Tab Kasir Check-Out
                actionTabbedPane.setSelectedIndex(0);

                txtCariTiket.setText(selectedTiket.getNomorTiket());

                // Set detail labels
                lblDetailPlate.setText(k.getPlatNomor() + " (" + k.getMerk() + ")");
                lblDetailSlot.setText(slot.getIdSlot() + " - Lantai " + slot.getLantai());
                lblDetailInTime.setText(selectedTiket.getJamMasuk() + ":00 WIB");

                // Set default exit hour (jamMasuk + 3)
                int defaultKeluar = Math.min(23, selectedTiket.getJamMasuk() + 3);
                txtJamKeluar.setText(String.valueOf(defaultKeluar));

                updateCheckoutPreview();

                lblCheckoutNotice.setText(String.format(
                        "<html><span style='color:#059669;font-weight:600;'>Kendaraan siap check-out.</span> Total biaya telah dihitung otomatis di bawah.</html>"
                ));
            }
        } else {
            // Slot Kosong -> Arahkan ke Tab Check-In
            actionTabbedPane.setSelectedIndex(1);
            if ("MOTOR".equals(slot.getTipeSlot())) {
                cbTipeKendaraan.setSelectedItem("MOTOR");
            } else if ("MOBIL_LISTRIK".equals(slot.getTipeSlot())) {
                cbTipeKendaraan.setSelectedItem("MOBIL_LISTRIK");
            } else {
                cbTipeKendaraan.setSelectedItem("MOBIL");
            }
            lblCheckoutNotice.setText(String.format("<html>Slot <b>%s</b> sedang kosong. Silakan gunakan form Check-In untuk registrasi masuk.</html>", slot.getIdSlot()));
        }

        renderBlueprintRows();
    }

    private JPanel createOperationPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(460, 780));
        panel.setOpaque(false);

        // 1. Skema Tarif Banner (Clean & Compact)
        panel.add(createTariffBanner());
        panel.add(Box.createVerticalStrut(10));

        // 2. Operasional Kasir & Gate (Tabbed Pane: Check-Out vs Check-In)
        panel.add(createTabbedActionCard());
        panel.add(Box.createVerticalStrut(10));

        // 3. Log Riwayat Transaksi (Selalu terlihat!)
        panel.add(createLogTableCard());

        return panel;
    }

    private JPanel createTariffBanner() {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SUBTLE, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));

        JLabel lblTitle = new JLabel("SKEMA TARIF PARKIR RESMI");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTitle.setForeground(TEXT_MUTED);
        card.add(lblTitle, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(1, 3, 6, 0));
        grid.setOpaque(false);

        grid.add(createTariffItem("MOTOR (Baris A)", "Rp 2.000", "+Rp 1.000/jam", STATUS_TEAL, SURFACE_TEAL));
        grid.add(createTariffItem("MOBIL (Baris B-D)", "Rp 5.000", "+Rp 3.000/jam", BRAND_BLUE, new Color(239, 246, 255)));
        grid.add(createTariffItem("EV CHARGER", "Diskon 20%", "Gratis Cas Baterai", STATUS_PURPLE, SURFACE_PURPLE));

        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JPanel createTariffItem(String name, String rate, String sub, Color textCol, Color bg) {
        JPanel p = new JPanel(new GridLayout(3, 1, 0, 1));
        p.setBackground(bg);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SUBTLE, 1),
                new EmptyBorder(4, 6, 4, 6)
        ));

        JLabel l1 = new JLabel(name);
        l1.setFont(new Font("Segoe UI", Font.BOLD, 9));
        l1.setForeground(textCol);

        JLabel l2 = new JLabel(rate);
        l2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l2.setForeground(TEXT_PRIMARY);

        JLabel l3 = new JLabel(sub);
        l3.setFont(new Font("Segoe UI", Font.PLAIN, 8));
        l3.setForeground(TEXT_MUTED);

        p.add(l1);
        p.add(l2);
        p.add(l3);
        return p;
    }

    private JPanel createTabbedActionCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createLineBorder(BORDER_SUBTLE, 1));

        actionTabbedPane = new JTabbedPane();
        actionTabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 11));
        actionTabbedPane.setBackground(CARD_BG);

        // Tab 1: Check-Out (Kasir)
        actionTabbedPane.addTab("Check-Out & Kasir", createCheckOutTab());

        // Tab 2: Check-In (Gate Masuk)
        actionTabbedPane.addTab("Gate Masuk (Check-In)", createCheckInTab());

        card.add(actionTabbedPane, BorderLayout.CENTER);
        return card;
    }

    private JPanel createCheckOutTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(CARD_BG);
        panel.setBorder(new EmptyBorder(10, 12, 10, 12));

        // Alert info
        lblCheckoutNotice = new JLabel("<html>Klik salah satu mobil terparkir di denah untuk memproses pembayaran.</html>");
        lblCheckoutNotice.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblCheckoutNotice.setForeground(TEXT_SECONDARY);
        lblCheckoutNotice.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SUBTLE, 1),
                new EmptyBorder(4, 8, 4, 8)
        ));
        panel.add(lblCheckoutNotice, BorderLayout.NORTH);

        // Details Grid
        JPanel detailGrid = new JPanel(new GridLayout(5, 2, 8, 4));
        detailGrid.setOpaque(false);

        detailGrid.add(createFieldLabel("Nomor Tiket:"));
        txtCariTiket = createStyledTextField("TKT-0002");
        detailGrid.add(txtCariTiket);

        detailGrid.add(createFieldLabel("Kendaraan:"));
        lblDetailPlate = new JLabel("B 1234 ABC (Toyota Avanza)");
        lblDetailPlate.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblDetailPlate.setForeground(TEXT_PRIMARY);
        detailGrid.add(lblDetailPlate);

        detailGrid.add(createFieldLabel("Lokasi Slot:"));
        lblDetailSlot = new JLabel("MBL-B02 (Lantai 1)");
        lblDetailSlot.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblDetailSlot.setForeground(TEXT_MUTED);
        detailGrid.add(lblDetailSlot);

        detailGrid.add(createFieldLabel("Jam Masuk:"));
        lblDetailInTime = new JLabel("8:00 WIB");
        lblDetailInTime.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblDetailInTime.setForeground(TEXT_MUTED);
        detailGrid.add(lblDetailInTime);

        detailGrid.add(createFieldLabel("Jam Keluar (0-23):"));
        txtJamKeluar = createStyledTextField("12");
        txtJamKeluar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateCheckoutPreview(); }
            public void removeUpdate(DocumentEvent e) { updateCheckoutPreview(); }
            public void changedUpdate(DocumentEvent e) { updateCheckoutPreview(); }
        });
        detailGrid.add(txtJamKeluar);

        panel.add(detailGrid, BorderLayout.CENTER);

        // Bottom Billing & Pay Section
        JPanel bottom = new JPanel(new BorderLayout(0, 6));
        bottom.setOpaque(false);

        JPanel payRow = new JPanel(new GridLayout(1, 2, 8, 0));
        payRow.setOpaque(false);

        JPanel mBox = new JPanel(new BorderLayout(0, 2));
        mBox.setOpaque(false);
        mBox.add(createFieldLabel("Metode Bayar:"), BorderLayout.NORTH);
        cbMetodeBayar = new JComboBox<>(new String[]{"E_MONEY", "QRIS", "CASH"});
        styleComboBox(cbMetodeBayar);
        mBox.add(cbMetodeBayar, BorderLayout.CENTER);
        payRow.add(mBox);

        JPanel uBox = new JPanel(new BorderLayout(0, 2));
        uBox.setOpaque(false);
        uBox.add(createFieldLabel("Nominal Bayar (Rp):"), BorderLayout.NORTH);
        txtUangBayar = createStyledTextField("14000");
        uBox.add(txtUangBayar, BorderLayout.CENTER);
        payRow.add(uBox);

        bottom.add(payRow, BorderLayout.NORTH);

        // Billing Preview
        JPanel billPill = new JPanel(new BorderLayout());
        billPill.setBackground(new Color(239, 246, 255));
        billPill.setBorder(new EmptyBorder(6, 10, 6, 10));
        JLabel lblBTitle = new JLabel("Total Tagihan:");
        lblBTitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblBTitle.setForeground(TEXT_MUTED);

        lblBillingPreview = new JLabel("Rp 14.000 (4 Jam)");
        lblBillingPreview.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblBillingPreview.setForeground(BRAND_BLUE);

        billPill.add(lblBTitle, BorderLayout.WEST);
        billPill.add(lblBillingPreview, BorderLayout.EAST);
        bottom.add(billPill, BorderLayout.CENTER);

        JButton btnKeluar = createStyledButton("Bayar & Buka Portal", STATUS_SUCCESS, Color.WHITE);
        btnKeluar.addActionListener(e -> handleCheckOut());
        bottom.add(btnKeluar, BorderLayout.SOUTH);

        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createCheckInTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(CARD_BG);
        panel.setBorder(new EmptyBorder(10, 12, 10, 12));

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.setOpaque(false);

        form.add(createFieldLabel("Nomor Plat:"));
        txtPlat = createStyledTextField("B 9988 PBO");
        form.add(txtPlat);

        form.add(createFieldLabel("Tipe Kendaraan:"));
        cbTipeKendaraan = new JComboBox<>(new String[]{"MOBIL", "MOTOR", "MOBIL_LISTRIK"});
        styleComboBox(cbTipeKendaraan);
        cbTipeKendaraan.addActionListener(e -> updateCheckInHint());
        form.add(cbTipeKendaraan);

        form.add(createFieldLabel("Merk / Model:"));
        txtMerk = createStyledTextField("Honda HR-V");
        form.add(txtMerk);

        form.add(createFieldLabel("Jam Masuk (0-23):"));
        txtJamMasuk = createStyledTextField("11");
        form.add(txtJamMasuk);

        form.add(createFieldLabel("Alokasi Sistem:"));
        lblAlokasiInfo = new JLabel("Dialokasikan ke Baris B, C, atau D");
        lblAlokasiInfo.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblAlokasiInfo.setForeground(BRAND_BLUE);
        form.add(lblAlokasiInfo);

        panel.add(form, BorderLayout.CENTER);

        JButton btnMasuk = createStyledButton("Check-In Kendaraan & Terbitkan Tiket", BRAND_BLUE, Color.WHITE);
        btnMasuk.addActionListener(e -> handleCheckIn());
        panel.add(btnMasuk, BorderLayout.SOUTH);

        return panel;
    }

    private void updateCheckInHint() {
        String tipe = (String) cbTipeKendaraan.getSelectedItem();
        if ("MOTOR".equals(tipe)) {
            lblAlokasiInfo.setText("Dialokasikan ke Baris A (Zona Motor)");
            lblAlokasiInfo.setForeground(STATUS_TEAL);
        } else if ("MOBIL_LISTRIK".equals(tipe)) {
            lblAlokasiInfo.setText("Prioritas ke Slot EV-D13 / D14");
            lblAlokasiInfo.setForeground(STATUS_PURPLE);
        } else {
            lblAlokasiInfo.setText("Dialokasikan ke Baris B, C, atau D");
            lblAlokasiInfo.setForeground(BRAND_BLUE);
        }
    }

    private JPanel createLogTableCard() {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SUBTLE, 1),
                new EmptyBorder(8, 12, 10, 12)
        ));

        JLabel lblTitle = new JLabel("RIWAYAT TRANSAKSI KASIR");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTitle.setForeground(TEXT_MUTED);
        card.add(lblTitle, BorderLayout.NORTH);

        String[] cols = {"No. Tiket", "Plat", "Tipe", "Slot", "Waktu", "Status", "Biaya"};
        logTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        logTable = new JTable(logTableModel);
        logTable.setBackground(CARD_BG);
        logTable.setForeground(TEXT_SECONDARY);
        logTable.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        logTable.setRowHeight(22);
        logTable.setShowGrid(false);
        logTable.setSelectionBackground(new Color(239, 246, 255));
        logTable.setSelectionForeground(BRAND_BLUE);

        logTable.getTableHeader().setBackground(BG_PAGE);
        logTable.getTableHeader().setForeground(TEXT_MUTED);
        logTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 9));
        logTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_SUBTLE));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < logTable.getColumnCount(); i++) {
            logTable.getColumnModel().getColumn(i).setCellRenderer(center);
        }

        logTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = logTable.getSelectedRow();
                if (row >= 0 && row < logTableModel.getRowCount()) {
                    String noTiket = (String) logTableModel.getValueAt(row, 0);
                    for (KarcisParkir t : gedung.getRiwayatTransaksi()) {
                        if (t.getNomorTiket().equalsIgnoreCase(noTiket)) {
                            onSlotSelected(t.getSlot());
                            break;
                        }
                    }
                }
            }
        });

        JScrollPane sp = new JScrollPane(logTable);
        sp.setPreferredSize(new Dimension(420, 170));
        sp.setBorder(BorderFactory.createLineBorder(BORDER_SUBTLE, 1));
        sp.getViewport().setBackground(CARD_BG);
        card.add(sp, BorderLayout.CENTER);

        return card;
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
            lblBillingPreview.setText("Pilih kendaraan terisi");
            return;
        }

        try {
            int jamKeluar = Integer.parseInt(txtJamKeluar.getText().trim());
            int durasi = t.hitungDurasi(jamKeluar);

            boolean isMotor = "MOTOR".equals(t.getKendaraan().getTipe());
            double tarifPertama = isMotor ? gedung.getTarifJamPertamaMotor() : gedung.getTarifJamPertamaMobil();
            double tarifBerikutnya = isMotor ? gedung.getTarifPerJamMotor() : gedung.getTarifPerJamMobil();

            double subtotal = tarifPertama + (durasi > 1 ? (durasi - 1) * tarifBerikutnya : 0);
            if (t.getKendaraan().isMobilListrik()) {
                subtotal = subtotal * 0.80;
            }

            lblBillingPreview.setText(String.format("%s (%d Jam)%s",
                    rupiahFormat.format(subtotal), durasi,
                    (t.getKendaraan().isMobilListrik() ? " [Diskon EV 20%]" : (isMotor ? " [Motor]" : ""))));

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
                JOptionPane.showMessageDialog(this, "Plat nomor dan merk kendaraan tidak boleh kosong.", "Validasi Input", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Kendaraan k = new Kendaraan(plat, tipe, merk);
            KarcisParkir tiket = gedung.prosesMasuk(k, jam);

            if (tiket != null) {
                triggerGateAction(true, "PORTAL TERBUKA (MASUK)");
                selectedSlot = tiket.getSlot();
                selectedTiket = tiket;

                renderBlueprintRows();
                updateStats();
                updateLogTable();
                onSlotSelected(tiket.getSlot());

                JOptionPane.showMessageDialog(this,
                        String.format("Kendaraan %s berhasil masuk.\nNo. Tiket: %s\nAlokasi: Slot %s (Lantai %d)",
                                plat, tiket.getNomorTiket(), tiket.getSlot().getIdSlot(), tiket.getSlot().getLantai()),
                        "Check-In Sukses", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Kapasitas parkir untuk kategori " + tipe + " sudah penuh.",
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
                JOptionPane.showMessageDialog(this, "Tiket " + noTiket + " tidak ditemukan. Silakan klik mobil di denah.", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (tiketDitemukan.isSudahDibayar()) {
                JOptionPane.showMessageDialog(this, "Tiket ini sudah pernah dibayar dan kendaraan telah keluar.", "Informasi", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            boolean sukses = gedung.prosesKeluar(tiketDitemukan, jamKeluar, metode, uang);
            if (sukses) {
                triggerGateAction(true, "PORTAL TERBUKA (KELUAR)");

                this.selectedSlot = null;
                this.selectedTiket = null;
                lblCheckoutNotice.setText("<html><span style='color:#059669;font-weight:600;'>Check-out berhasil. Slot telah kosong kembali.</span></html>");
                lblBillingPreview.setText("Rp 0");

                renderBlueprintRows();
                updateStats();
                updateLogTable();

                JOptionPane.showMessageDialog(this,
                        String.format("Pembayaran LUNAS.\nKendaraan: %s\nTotal Biaya: %s\nKembalian: %s\nPortal keluar telah dibuka.",
                                tiketDitemukan.getKendaraan().getPlatNomor(),
                                rupiahFormat.format(tiketDitemukan.getTotalBiaya()),
                                rupiahFormat.format(uang - tiketDitemukan.getTotalBiaya())),
                        "Check-Out Sukses", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Pembayaran ditolak. Nominal bayar kurang dari total tagihan.", "Gagal Bayar", JOptionPane.ERROR_MESSAGE);
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

        lblTotalSlot.setText(String.format("<html><div style='text-align:right;'><span style='color:#64748b;font-size:8.5px;font-weight:600;'>TOTAL SLOT</span><br/><b style='font-size:13px;color:%s;'>%d</b></div></html>", toHex(TEXT_PRIMARY), total));
        lblTersedia.setText(String.format("<html><div style='text-align:right;'><span style='color:#64748b;font-size:8.5px;font-weight:600;'>TERSEDIA</span><br/><b style='font-size:13px;color:%s;'>%d</b></div></html>", toHex(STATUS_SUCCESS), tersedia));
        lblTerisi.setText(String.format("<html><div style='text-align:right;'><span style='color:#64748b;font-size:8.5px;font-weight:600;'>TERISI</span><br/><b style='font-size:13px;color:%s;'>%d</b></div></html>", toHex(STATUS_DANGER), terisi));
        lblPendapatan.setText(String.format("<html><div style='text-align:right;'><span style='color:#64748b;font-size:8.5px;font-weight:600;'>KAS MASUK</span><br/><b style='font-size:13px;color:%s;'>%s</b></div></html>", toHex(new Color(180, 83, 9)), rupiahFormat.format(gedung.getTotalPendapatan())));
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

    private void triggerGateAction(boolean open, String text) {
        targetGateAngle = open ? 70.0f : 0.0f;
        gateStatusText = text;
        gateStatusColor = open ? STATUS_SUCCESS : STATUS_DANGER;

        // Jalankan transisi halus lengan portal hanya saat ada aksi (bukan endless loop!)
        if (gateAnimationTimer != null && gateAnimationTimer.isRunning()) {
            gateAnimationTimer.stop();
        }

        gateAnimationTimer = new Timer(20, e -> {
            if (Math.abs(gateAngle - targetGateAngle) > 1.0f) {
                gateAngle += (targetGateAngle - gateAngle) * 0.2f;
                repaint();
            } else {
                gateAngle = targetGateAngle;
                repaint();
                ((Timer) e.getSource()).stop();
            }
        });
        gateAnimationTimer.start();

        if (open) {
            Timer closeTimer = new Timer(3000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    targetGateAngle = 0.0f;
                    gateStatusText = "PORTAL TERTUTUP";
                    gateStatusColor = STATUS_DANGER;
                    if (gateAnimationTimer != null) gateAnimationTimer.start();
                    ((Timer) e.getSource()).stop();
                }
            });
            closeTimer.setRepeats(false);
            closeTimer.start();
        }
    }

    private JLabel createFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(TEXT_SECONDARY);
        return l;
    }

    private JTextField createStyledTextField(String initVal) {
        JTextField tf = new JTextField(initVal);
        tf.setBackground(BG_PAGE);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(BRAND_BLUE);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SUBTLE, 1),
                new EmptyBorder(3, 6, 3, 6)
        ));
        return tf;
    }

    private void styleComboBox(JComboBox<String> cb) {
        cb.setBackground(BG_PAGE);
        cb.setForeground(TEXT_PRIMARY);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 11));
    }

    private JButton createStyledButton(String text, Color bg, Color textCol) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setBackground(bg);
        btn.setForeground(textCol);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(7, 12, 7, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel createLegendChip(String label, Color col) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);
        JPanel chip = new JPanel();
        chip.setPreferredSize(new Dimension(8, 8));
        chip.setBackground(col);
        chip.setBorder(BorderFactory.createLineBorder(col.darker(), 1));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lbl.setForeground(TEXT_MUTED);

        p.add(chip);
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

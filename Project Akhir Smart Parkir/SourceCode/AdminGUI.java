import model.*;
import model.AIVisionService.DetectionResult;
import java.util.List;
import java.io.IOException;
import java.nio.file.Path;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class AdminGUI extends JFrame {

    // Sistem 3 Warna: 1. Netral (Grayscale/Slate), 2. Merah (Error/Terisi), 3. Hijau (Success/Terbuka)
    private static final Color BG          = new Color(228, 230, 235);  // #e4e6eb Muted Classic Desktop Gray (tidak silau)
    private static final Color CARD_BG     = new Color(242, 244, 246);  // #f2f4f6 Soft Warm Panel
    private static final Color FIELD_BG    = new Color(255, 255, 255);  // #ffffff Clean Input Field BG
    private static final Color BORDER      = new Color(205, 209, 216);  // #cdd1d8 Subtle Border
    private static final Color BORDER_DARK = new Color(160, 166, 178);  // #a0a6b2 Definition Outline
    private static final Color PRIMARY     = new Color(30, 41, 59);     // #1e293b Dark Slate for Action & Header Accent
    private static final Color BLUE        = PRIMARY;                   // Aliased to Neutral Dark Slate
    private static final Color BLUE_LT     = new Color(220, 224, 232);  // Soft Neutral Selection
    private static final Color GREEN       = new Color(22, 163, 74);    // #16a34a Hijau Sukses / Terbuka
    private static final Color GREEN_LT    = new Color(220, 252, 231);  // #dcfce7 Soft Green
    private static final Color RED         = new Color(220, 38, 38);    // #dc2626 Merah Error / Peringatan / Terisi
    private static final Color RED_LT      = new Color(254, 226, 226);  // #fee2e2 Soft Red Pastel
    private static final Color PURPLE      = PRIMARY;                   // Aliased to Neutral
    private static final Color PURPLE_LT   = new Color(235, 238, 242);  // Neutral Light
    private static final Color TEAL        = PRIMARY;                   // Aliased to Neutral
    private static final Color TEAL_LT     = new Color(235, 238, 242);  // Neutral Light
    private static final Color AMBER       = RED;                       // Warning/Exception mapped to RED
    private static final Color AMBER_LT    = RED_LT;                    // Warning mapped to RED_LT
    private static final Color T1          = new Color(20, 24, 33);     // #141821 Deep Charcoal Text
    private static final Color T2          = new Color(75, 85, 99);     // #4b5563 Slate Subtitle / Label Text
    private static final Color T3          = new Color(156, 163, 175);  // #9ca3af Muted Gray Details
    private static final Color AISLE       = new Color(235, 237, 240);  // Maneuver Lane Soft
    private static final Color PILLAR      = new Color(175, 182, 192);  // Pillar Gray
    private static final Color WHITE       = Color.WHITE;

    private GedungParkir gedung;
    private NumberFormat rupiah;
    private SlotParkir selectedSlot;
    private KarcisParkir selectedTiket;

    private JPanel blueprintContainer;
    private JLabel lblTersedia, lblTerisi, lblPendapatan, lblDBStatus;
    private DefaultTableModel logModel;

    private JTextField txtTiket, txtJamKeluar, txtUangBayar;
    private JComboBox<String> cbMetode;
    private JLabel lblInfoKendaraan, lblTagihan, lblNotice;

    private boolean isExitGateMode = false;
    private JButton btnGateModeToggle;
    private String searchedSlotId = null;
    private JComboBox<String> cbActiveVehicles;
    private JTextField txtSearchPlate;
    private JLabel lblSearchResult;

    private JTextField txtPlat, txtMerk, txtJamMasuk;
    private JComboBox<String> cbTipe;
    private JLabel lblAlokasiHint;
    private JLabel lblQuotaLt1, lblQuotaLt2, lblQuotaEV;

    private JTabbedPane tabs;

    // Modul 11: Pengelolaan Koleksi Kendaraan UI State
    private DefaultTableModel tblModelKendaraan;
    private JTable tblKendaraanList;
    private JTextField txtCariKoleksi;
    private JTextField txtPlatBaru, txtMerkBaru;
    private JComboBox<String> cbTipeBaru;
    private JLabel lblTotalKoleksiArmada;

    private float gateAngle = 0f, targetGate = 0f;
    private String gateLabel = "TERTUTUP";
    private Color gateColor  = RED;
    private Timer gateTimer;

    // AI Camera state
    private String savedEndpoint = "https://pi.leopard-gila.ts.net/v1/chat/completions";
    private String savedApiKey   = "";
    private String savedModel    = "orion";

    // CCTV Dashboard State
    private LiveCameraStreamer cctvStreamer;
    private JLabel cctvScreen;
    private JLabel lblCCTVStatus;
    private JLabel lblCCTVLiveBadge;
    private JButton btnCCTVToggle;
    private BufferedImage currentCCTVFrame;
    private boolean cctvVehicleDetected = false;
    private String cctvHudText = "MEMANTAU GERBANG MASUK...";
    private long cctvCooldownUntil = 0;
    private String lastProcessedPlate = "";
    private long lastProcessedTime = 0;
    private final ExecutorService aiExecutor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean isAiProcessing = new AtomicBoolean(false);
    private boolean dbConnected = true;
    private BufferedImage cachedCctvBuffer = null;
    private boolean isPaymentDialogShowing = false;

    public AdminGUI() {
        super("SmartParkir: Sistem Parkir Cerdas & Monitoring Gate");
        rupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        
        // Load AI Credentials from ai_config.properties
        java.util.Properties aiProps = AIVisionService.loadConfig();
        savedEndpoint = aiProps.getProperty("ai.endpoint", "https://pi.leopard-gila.ts.net/v1/chat/completions");
        savedApiKey   = aiProps.getProperty("ai.api_key", "");
        savedModel    = aiProps.getProperty("ai.model", "orion");

        initData();
        buildUI();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1440, 880);
        setMinimumSize(new Dimension(1220, 740));
        setLocationRelativeTo(null);

        // Timer periodik cek koneksi database di background thread (tidak memblokir EDT)
        Timer dbCheckTimer = new Timer(6000, e -> {
            aiExecutor.submit(() -> {
                boolean ok = DatabaseManager.isConnected();
                SwingUtilities.invokeLater(() -> {
                    dbConnected = ok;
                    if (lblDBStatus != null) {
                        lblDBStatus.setText(fmtPill("DATABASE", ok ? "MySQL ONLINE" : "OFFLINE", ok ? T1 : RED));
                    }
                });
            });
        });
        dbCheckTimer.start();

        // Hentikan streaming kamera saat window ditutup
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                if (cctvStreamer != null) cctvStreamer.stopStream();
                dbCheckTimer.stop();
            }
        });

        // AUTO-START CCTV DAN AUTO-DETEKSI OTOMATIS SAAT PROGRAM DIJALANKAN
        SwingUtilities.invokeLater(() -> startDashboardCCTV());
    }

    private void initData() {
        gedung = new GedungParkir("Grand PENS Plaza", 5000, 3000, 2000, 1000);
        java.util.List<SlotParkir> slots = new java.util.ArrayList<>();

        for (int i = 1; i <= 9; i++)
            slots.add(new SlotParkir(String.format("A%02d", i), 1, "MOTOR"));
        for (int i = 1; i <= 12; i++) {
            if (i == 5) continue;
            slots.add(new SlotParkir(String.format("B%02d", i), 1, "MOBIL"));
        }
        for (int i = 1; i <= 12; i++) {
            if (i == 5) continue;
            slots.add(new SlotParkir(String.format("C%02d", i), 1, "MOBIL"));
        }
        for (int i = 1; i <= 12; i++)
            slots.add(new SlotParkir(String.format("D%02d", i), 1, "MOBIL"));
        slots.add(new SlotParkir("EV1", 1, "MOBIL_LISTRIK"));
        slots.add(new SlotParkir("EV2", 1, "MOBIL_LISTRIK"));
        gedung.tambahDaftarSlot(slots);

        try {
            gedung.prosesMasuk(new Kendaraan("B 6789 MTR", "MOTOR",        "Honda Vario"), 9);
            KarcisParkir t2 = gedung.prosesMasuk(new Kendaraan("B 1234 ABC", "MOBIL",  "Toyota Avanza"), 8);
            gedung.prosesMasuk(new Kendaraan("L 9988 XYZ", "MOBIL",        "Honda HR-V"), 10);
            gedung.prosesMasuk(new Kendaraan("B 1010 EV",  "MOBIL_LISTRIK","Ioniq 5"), 9);
            if (t2 != null) { selectedSlot = t2.getSlot(); selectedTiket = t2; }
        } catch (Exception ex) {
            System.err.println("[INIT WARNING] Inisialisasi kendaraan awal: " + ex.getMessage());
        }
    }

    private void buildUI() {
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(12, 0));
        body.setBackground(BG);
        body.setBorder(new EmptyBorder(12, 14, 14, 14));
        body.add(buildBlueprint(), BorderLayout.CENTER);
        body.add(buildSidebar(),   BorderLayout.EAST);
        add(body, BorderLayout.CENTER);

        syncAll();
    }

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(CARD_BG);
        h.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
            new EmptyBorder(10, 18, 10, 18)));

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brand.setOpaque(false);

        JLabel icon = new JLabel("P", SwingConstants.CENTER);
        icon.setOpaque(true);
        icon.setBackground(BLUE);
        icon.setForeground(WHITE);
        icon.setFont(new Font("Segoe UI", Font.BOLD, 17));
        icon.setPreferredSize(new Dimension(34, 34));

        JLabel name = new JLabel(String.format(
            "<html><b style='font-size:15px;color:%s;'>SmartParkir</b> "
            + "<span style='font-size:11px;color:%s;'>— Sistem Manajemen Parkir & Monitoring Gerbang</span></html>",
            hex(T1), hex(T2)));
        name.setForeground(T1);
        brand.add(icon);
        brand.add(name);

        JPanel pills = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pills.setOpaque(false);

        lblTersedia   = pill("Tersedia", "-", T1);
        lblTerisi     = pill("Terisi",   "-", RED);
        lblPendapatan = pill("Kas",      "-", T1);
        lblDBStatus   = pill("Database", "Online", T1);

        JButton btnCameraHeader = button("Pengaturan", FIELD_BG, T1);
        btnCameraHeader.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnCameraHeader.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(5, 10, 5, 10)));
        btnCameraHeader.addActionListener(e -> openCameraScannerDialog());

        pills.add(btnCameraHeader);
        pills.add(Box.createHorizontalStrut(6));
        pills.add(lblTersedia);
        pills.add(lblTerisi);
        pills.add(lblPendapatan);
        pills.add(lblDBStatus);

        h.add(brand, BorderLayout.WEST);
        h.add(pills,  BorderLayout.EAST);
        return h;
    }

    private JLabel pill(String label, String val, Color valColor) {
        JLabel lbl = new JLabel(fmtPill(label, val, valColor), SwingConstants.CENTER);
        lbl.setOpaque(true);
        lbl.setBackground(CARD_BG);
        lbl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(5, 10, 5, 10)));
        return lbl;
    }

    private JLabel createNavDivider() {
        JLabel d = new JLabel("|");
        d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        d.setForeground(BORDER);
        return d;
    }

    private String fmtPill(String label, String val, Color c) {
        return String.format(
            "<html><span style='color:%s;font-size:10px;'>%s: </span>"
            + "<b style='font-size:11px;color:%s;'>%s</b></html>",
            hex(T2), label, hex(c), val);
    }

    private JPanel buildBlueprint() {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(12, 14, 10, 14)));

        // Top Bar: Judul Denah & Palang Gerbang
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel title = new JLabel(String.format(
            "<html><b style='color:%s;font-size:13px;'>Denah Parkir</b> <span style='color:%s;font-size:11px;'>(45 Slot)</span></html>",
            hex(T1), hex(T2)));
        title.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        title.setForeground(T1);
        top.add(title, BorderLayout.WEST);

        JPanel gate = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = 14, cy = getHeight() - 6;
                g2.setColor(PILLAR); g2.fillRect(cx - 3, cy - 14, 6, 14);
                double rad = Math.toRadians(gateAngle);
                int ex = cx + (int)(28 * Math.cos(rad));
                int ey = cy - 10 - (int)(28 * Math.sin(rad));
                g2.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(gateColor);
                g2.drawLine(cx, cy - 10, ex, ey);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                g2.setColor(gateColor);
                g2.drawString("PALANG: " + gateLabel, cx + 36, getHeight() / 2 + 4);
                g2.dispose();
            }
        };
        gate.setPreferredSize(new Dimension(170, 28));
        gate.setOpaque(false);
        top.add(gate, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        blueprintContainer = new JPanel();
        blueprintContainer.setLayout(new BoxLayout(blueprintContainer, BoxLayout.Y_AXIS));
        blueprintContainer.setBackground(CARD_BG);
        blueprintContainer.setBorder(new EmptyBorder(6, 0, 6, 0));
        renderBlueprint();

        JScrollPane scroll = new JScrollPane(blueprintContainer);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        scroll.getViewport().setBackground(CARD_BG);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(14);

        // Sub-Toolbar Sleek: Kuota Ringkas (Kiri) + Search Bar Mini (Kanan)
        JPanel subToolbar = new JPanel(new BorderLayout(8, 0));
        subToolbar.setOpaque(false);
        subToolbar.setBorder(new EmptyBorder(0, 0, 6, 0));

        JPanel quotaBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        quotaBox.setOpaque(false);
        lblQuotaLt1 = new JLabel();
        lblQuotaLt2 = new JLabel();
        lblQuotaEV  = new JLabel();
        quotaBox.add(lblQuotaLt1);
        quotaBox.add(createNavDivider());
        quotaBox.add(lblQuotaLt2);
        quotaBox.add(createNavDivider());
        quotaBox.add(lblQuotaEV);
        subToolbar.add(quotaBox, BorderLayout.WEST);

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        searchBar.setOpaque(false);

        txtSearchPlate = field("");
        txtSearchPlate.setPreferredSize(new Dimension(140, 26));
        txtSearchPlate.putClientProperty("JTextField.placeholderText", "Cari plat nomor...");
        txtSearchPlate.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        txtSearchPlate.addActionListener(e -> executeVehicleSearch());

        JButton btnSearch = button("Cari", PRIMARY, WHITE);
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btnSearch.addActionListener(e -> executeVehicleSearch());

        JButton btnClearSearch = button("✕", FIELD_BG, T2);
        btnClearSearch.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btnClearSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(4, 8, 4, 8)));
        btnClearSearch.setToolTipText("Reset");
        btnClearSearch.addActionListener(e -> {
            txtSearchPlate.setText("");
            searchedSlotId = null;
            lblSearchResult.setText("");
            renderBlueprint();
        });

        searchBar.add(txtSearchPlate);
        searchBar.add(btnSearch);
        searchBar.add(btnClearSearch);
        subToolbar.add(searchBar, BorderLayout.EAST);

        lblSearchResult = new JLabel("");
        lblSearchResult.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblSearchResult.setBorder(new EmptyBorder(2, 4, 2, 4));

        JPanel topBox = new JPanel(new BorderLayout(0, 2));
        topBox.setOpaque(false);
        topBox.add(subToolbar, BorderLayout.NORTH);
        topBox.add(lblSearchResult, BorderLayout.SOUTH);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 4));
        centerPanel.setOpaque(false);
        centerPanel.add(topBox, BorderLayout.NORTH);
        centerPanel.add(scroll, BorderLayout.CENTER);

        card.add(centerPanel, BorderLayout.CENTER);

        // Footer Denah Rapi & Sleek: Status Legend (Kiri) + Tarif Bar (Kanan)
        JPanel southBox = new JPanel(new BorderLayout());
        southBox.setOpaque(false);
        southBox.setBorder(new EmptyBorder(6, 2, 2, 2));

        JPanel leg = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leg.setOpaque(false);
        leg.add(legend("Tersedia", BORDER_DARK));
        leg.add(legend("Terisi", RED));

        JPanel tariff = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        tariff.setOpaque(false);
        JLabel mtr = new JLabel("Motor: Rp 2.000/jam");
        mtr.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        mtr.setForeground(T2);
        JLabel mbl = new JLabel("Mobil: Rp 5.000/jam");
        mbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        mbl.setForeground(T2);
        JLabel ev = new JLabel("EV: Diskon 20%");
        ev.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        ev.setForeground(T2);
        tariff.add(mtr);
        tariff.add(createNavDivider());
        tariff.add(mbl);
        tariff.add(createNavDivider());
        tariff.add(ev);

        southBox.add(leg, BorderLayout.WEST);
        southBox.add(tariff, BorderLayout.EAST);
        card.add(southBox, BorderLayout.SOUTH);

        return card;
    }

    private JPanel buildTariffBar() {
        return new JPanel();
    }

    private void renderBlueprint() {
        blueprintContainer.removeAll();

        addRow("A — Motor", T1,
            new String[]{"A01","A02","A03","A04","A05","A06","A07","A08","A09"},
            false);

        blueprintContainer.add(aisle());

        addRow("B — Mobil", T1,
            new String[]{"B01","B02","B03","B04","TANGGA","B06","B07","B08","B09","B10","B11","B12"},
            true);

        addRow("C — Mobil", T1,
            new String[]{"C01","C02","C03","C04","TANGGA","C06","C07","C08","C09","C10","C11","C12"},
            true);

        blueprintContainer.add(aisle());

        addRow("D — Mobil & EV", T1,
            new String[]{"D01","D02","D03","D04","D05","D06","D07","D08","D09","D10","D11","D12","EV1","EV2"},
            false);

        blueprintContainer.revalidate();
        blueprintContainer.repaint();
    }

    private void addRow(String rowLabel, Color labelColor, String[] ids, boolean hasPillars) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 3));
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(2, 4, 4, 4));

        JLabel lbl = new JLabel(rowLabel);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(labelColor);
        wrapper.add(lbl, BorderLayout.NORTH);

        JPanel slots = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 2));
        slots.setOpaque(false);

        for (int i = 0; i < ids.length; i++) {
            String id = ids[i];
            if ("TANGGA".equals(id)) {
                slots.add(stairBlock());
            } else {
                SlotParkir s = findSlot(id);
                if (s != null) slots.add(buildBay(s));
            }
            if (hasPillars && (i == 1 || i == 3 || i == 6 || i == 8 || i == 10))
                slots.add(pillar());
        }

        wrapper.add(slots, BorderLayout.CENTER);
        blueprintContainer.add(wrapper);
    }

    private JPanel buildBay(SlotParkir slot) {
        boolean isEV    = "MOBIL_LISTRIK".equals(slot.getTipeSlot());
        boolean isMotor = "MOTOR".equals(slot.getTipeSlot());
        boolean isFull  = slot.isTerisi();
        boolean isSel   = selectedSlot != null
            && selectedSlot.getIdSlot().equalsIgnoreCase(slot.getIdSlot());

        boolean isHighlight = searchedSlotId != null
            && searchedSlotId.equalsIgnoreCase(slot.getIdSlot());

        Color bgColor = isFull ? RED_LT
            : isSel ? new Color(225, 230, 238)
            : Color.WHITE;

        Color bdColor = isFull ? RED
            : isSel ? PRIMARY
            : BORDER;

        JPanel bay = new JPanel(new BorderLayout(0, 2)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(bdColor);
                g2.setStroke(new BasicStroke(isHighlight ? 2.5f : isSel ? 2f : 1f));
                int inset = isHighlight ? 2 : isSel ? 1 : 0;
                g2.drawRoundRect(inset, inset, getWidth()-1-inset, getHeight()-1-inset, 6, 6);
                g2.dispose();
            }
        };
        bay.setPreferredSize(new Dimension(66, 68));
        bay.setOpaque(false);
        bay.setBorder(new EmptyBorder(4, 3, 4, 3));
        bay.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel id = new JLabel(slot.getIdSlot(), SwingConstants.CENTER);
        id.setFont(new Font("Segoe UI", Font.BOLD, 10));
        id.setForeground(isFull ? RED : T1);
        bay.add(id, BorderLayout.NORTH);

        if (isFull) {
            String plat = slot.getKendaraanParkir().getPlatNomor();
            String cleanPlat = plat.replaceAll("\\s+", " ").trim();
            String formattedPlat = cleanPlat;
            int firstSpace = cleanPlat.indexOf(' ');
            if (firstSpace > 0) {
                int secondSpace = cleanPlat.indexOf(' ', firstSpace + 1);
                if (secondSpace > 0) {
                    formattedPlat = cleanPlat.substring(0, secondSpace) + "<br/>" + cleanPlat.substring(secondSpace + 1);
                }
            }
            JLabel p = new JLabel("<html><center style='color:#dc2626;font-size:9px;font-weight:bold;line-height:95%;'>" + formattedPlat + "</center></html>",
                SwingConstants.CENTER);
            bay.add(p, BorderLayout.CENTER);
        } else {
            JLabel empty = new JLabel("<html><center style='color:#9ca3af;font-size:12px;'>·</center></html>", SwingConstants.CENTER);
            bay.add(empty, BorderLayout.CENTER);
        }

        if (isEV) {
            JLabel type = new JLabel("EV", SwingConstants.CENTER);
            type.setFont(new Font("Segoe UI", Font.BOLD, 8));
            type.setForeground(T2);
            bay.add(type, BorderLayout.SOUTH);
        }

        attachClick(bay, slot);
        return bay;
    }

    private void attachClick(Component c, SlotParkir slot) {
        c.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onSlotClick(slot); }
        });
        if (c instanceof Container)
            for (Component ch : ((Container) c).getComponents())
                attachClick(ch, slot);
    }

    private void onSlotClick(SlotParkir slot) {
        selectedSlot = slot;
        if (slot.isTerisi()) {
            selectedTiket = null;
            for (KarcisParkir t : gedung.getRiwayatTransaksi()) {
                if (!t.isSudahDibayar() && t.getSlot().getIdSlot().equalsIgnoreCase(slot.getIdSlot())) {
                    selectedTiket = t;
                    break;
                }
            }
            if (selectedTiket != null) {
                tabs.setSelectedIndex(0);
                Kendaraan k = slot.getKendaraanParkir();
                txtTiket.setText(selectedTiket.getNomorTiket());
                lblInfoKendaraan.setText(
                    k.getPlatNomor() + "  " + k.getMerk()
                    + "  |  Slot: " + slot.getIdSlot()
                    + "  |  Masuk: " + selectedTiket.getJamMasuk() + ":00");
                txtJamKeluar.setText(String.valueOf(Math.min(23, selectedTiket.getJamMasuk() + 2)));
                updateTagihan();
                lblNotice.setText("<html><span style='color:#16a34a;'>Kendaraan dipilih: " + k.getPlatNomor() + "</span></html>");
            }
        } else {
            tabs.setSelectedIndex(1);
            if ("MOTOR".equals(slot.getTipeSlot())) cbTipe.setSelectedItem("MOTOR");
            else if ("MOBIL_LISTRIK".equals(slot.getTipeSlot())) cbTipe.setSelectedItem("MOBIL_LISTRIK");
            else cbTipe.setSelectedItem("MOBIL");
            lblNotice.setText("<html><span style='color:#4b5563;'>Slot " + slot.getIdSlot() + " dipilih (Kosong)</span></html>");
        }
        renderBlueprint();
    }

    private Component aisle() {
        JPanel lane = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(BORDER);
                g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    10f, new float[]{4f, 8f}, 0f));
                g2.drawLine(8, getHeight()/2, getWidth()-8, getHeight()/2);
                g2.dispose();
            }
        };
        lane.setPreferredSize(new Dimension(800, 10));
        lane.setOpaque(false);
        return lane;
    }

    private JPanel stairBlock() {
        JPanel p = new JPanel(new BorderLayout());
        p.setPreferredSize(new Dimension(50, 72));
        p.setBackground(AISLE);
        p.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        JLabel l = new JLabel("<html><center style='font-size:9px;font-weight:600;color:#64748b;'>TANGGA<br/>LIFT</center></html>",
            SwingConstants.CENTER);
        l.setForeground(T2);
        p.add(l, BorderLayout.CENTER);
        return p;
    }

    private JPanel pillar() {
        JPanel p = new JPanel();
        p.setPreferredSize(new Dimension(5, 72));
        p.setBackground(PILLAR);
        return p;
    }

    private JPanel legend(String label, Color col) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);
        JPanel dot = new JPanel();
        dot.setPreferredSize(new Dimension(10, 10));
        dot.setBackground(col);
        dot.setBorder(BorderFactory.createLineBorder(col.darker(), 1));
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(T2);
        p.add(dot); p.add(l);
        return p;
    }

    private JComponent buildSidebar() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setOpaque(false);
        
        side.add(buildCCTVCard());
        side.add(Box.createVerticalStrut(8));
        side.add(buildOperationCard());
        side.add(Box.createVerticalStrut(8));
        side.add(buildLogCard());

        JScrollPane scroll = new JScrollPane(side);
        scroll.setPreferredSize(new Dimension(460, 780));
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        return scroll;
    }

    /**
     * Card CCTV Kamera Live tersemat langsung di dashboard untuk pemantauan realtime (Format Kompak)
     */
    private JPanel buildCCTVCard() {
        JPanel card = new JPanel(new BorderLayout(0, 5));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(8, 10, 8, 10)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 315));
        card.setPreferredSize(new Dimension(438, 305));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = sectionTitle("Kamera Gerbang");
        lblCCTVLiveBadge = new JLabel("● Siaran Aktif");
        lblCCTVLiveBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblCCTVLiveBadge.setForeground(GREEN);
        top.add(title, BorderLayout.WEST);
        top.add(lblCCTVLiveBadge, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        cctvScreen = new JLabel("", SwingConstants.CENTER);
        cctvScreen.setPreferredSize(new Dimension(438, 220));
        cctvScreen.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        cctvScreen.setMinimumSize(new Dimension(340, 200));
        cctvScreen.setOpaque(true);
        cctvScreen.setBackground(Color.BLACK);
        cctvScreen.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        cctvScreen.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cctvScreen.setToolTipText("Klik untuk melihat tampilan penuh");
        cctvScreen.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                openCameraScannerDialog();
            }
        });
        card.add(cctvScreen, BorderLayout.CENTER);

        JPanel bot = new JPanel(new BorderLayout(0, 5));
        bot.setOpaque(false);

        lblCCTVStatus = new JLabel(String.format("<html><span style='color:%s;font-size:10px;'>Siap memindai kendaraan</span></html>", hex(T2)));
        bot.add(lblCCTVStatus, BorderLayout.NORTH);

        JPanel actions = new JPanel(new GridLayout(1, 3, 6, 0));
        actions.setOpaque(false);

        btnGateModeToggle = button(isExitGateMode ? "Mode: Keluar" : "Mode: Masuk", FIELD_BG, T1);
        btnGateModeToggle.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btnGateModeToggle.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_DARK, 1),
            new EmptyBorder(5, 4, 5, 4)));
        btnGateModeToggle.setToolTipText("Beralih antara Gerbang Masuk & Keluar");
        btnGateModeToggle.addActionListener(e -> toggleGateMode());

        btnCCTVToggle = button("Jeda Video", FIELD_BG, T1);
        btnCCTVToggle.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btnCCTVToggle.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_DARK, 1),
            new EmptyBorder(5, 4, 5, 4)));
        btnCCTVToggle.addActionListener(e -> toggleCCTVStreaming());

        JButton btnSettings = button("Pengaturan AI", FIELD_BG, T1);
        btnSettings.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btnSettings.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_DARK, 1),
            new EmptyBorder(5, 4, 5, 4)));
        btnSettings.addActionListener(e -> openCameraScannerDialog());

        actions.add(btnGateModeToggle);
        actions.add(btnCCTVToggle);
        actions.add(btnSettings);
        bot.add(actions, BorderLayout.SOUTH);

        card.add(bot, BorderLayout.SOUTH);
        return card;
    }

    private void startDashboardCCTV() {
        if (cctvStreamer != null && cctvStreamer.isStreaming()) return;

        cctvStreamer = new LiveCameraStreamer(new LiveCameraStreamer.StreamListener() {
            @Override
            public void onNewFrame(BufferedImage frame) {
                currentCCTVFrame = frame;
                SwingUtilities.invokeLater(() -> {
                    drawCCTVScreen(frame, cctvVehicleDetected, cctvHudText);
                });
            }

            @Override
            public void onVehicleDetected(BufferedImage frame) {
                handleAutoVehicleDetected(frame);
            }

            @Override
            public void onStatusChange(String status, boolean isError) {
                SwingUtilities.invokeLater(() -> {
                    lblCCTVStatus.setText("<html><span style='color:" + (isError ? "#dc2626" : "#4b5563") + ";font-size:10px;'>" + status + "</span></html>");
                });
            }
        });

        cctvStreamer.startLiveCamera();
    }

    private void handleAutoVehicleDetected(BufferedImage frame) {
        long now = System.currentTimeMillis();
        if (now < cctvCooldownUntil) return;
        if (!isAiProcessing.compareAndSet(false, true)) return; // Mencegah penumpukan thread/frame saat AI sedang inferensi

        cctvVehicleDetected = true;
        cctvHudText = "MEMINDAI PLAT NOMOR...";

        SwingUtilities.invokeLater(() -> {
            lblCCTVStatus.setText("<html><span style='color:#4b5563;font-size:10px;'>Memindai plat nomor kendaraan...</span></html>");
            drawCCTVScreen(frame, true, cctvHudText);
        });

        // Eksekusi AI vision satset di background worker
        aiExecutor.submit(() -> {
            try {
                DetectionResult res = AIVisionService.analyzeVehicleImage(frame, savedEndpoint, savedApiKey, savedModel);
                SwingUtilities.invokeLater(() -> {
                    processAIDetectionResult(res);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    cctvVehicleDetected = false;
                    cctvHudText = "ERROR AI: " + ex.getMessage();
                    lblCCTVStatus.setText("<html><span style='color:#dc2626;font-size:10px;'>Error AI: " + ex.getMessage() + "</span></html>");
                    startCCTVCooldownCountdown(1);
                });
            } finally {
                isAiProcessing.set(false);
            }
        });
    }

    private void processAIDetectionResult(DetectionResult res) {
        String plat = res.plat.trim();
        String merk = res.merk.trim();
        String tipe = res.tipe.trim();

        if (!AIVisionService.isValidPlate(plat)) {
            cctvVehicleDetected = false;
            cctvHudText = "STANDBY - " + (isExitGateMode ? "GERBANG KELUAR" : "GERBANG MASUK");
            lblCCTVStatus.setText("<html><span style='color:#4b5563;font-size:10px;'>Kamera aktif memantau " + (isExitGateMode ? "gerbang keluar..." : "gerbang masuk...") + "</span></html>");
            startCCTVCooldownCountdown(1);
            return;
        }

        if (isExitGateMode) {
            processExitAIDetection(plat);
            return;
        }

        SlotParkir sudahParkir = gedung.cariSlotDenganPlat(plat);
        if (sudahParkir != null) {
            // Kendaraan sudah terparkir di dalam gedung -> Otomatis proses Check-Out Keluar
            processExitAIDetection(plat);
            return;
        }

        long now = System.currentTimeMillis();
        if (plat.equalsIgnoreCase(lastProcessedPlate) && (now - lastProcessedTime < 20000)) {
            cctvVehicleDetected = false;
            cctvHudText = "DUPLIKAT BARU DIABAIKAN: " + plat;
            startCCTVCooldownCountdown(5);
            return;
        }

        int currentHour = java.time.LocalTime.now().getHour();
        try {
            Kendaraan.validasiFormatPlat(plat);
            KarcisParkir tiket = gedung.prosesMasuk(new Kendaraan(plat, tipe, merk), currentHour);

            if (tiket != null) {
                lastProcessedPlate = plat;
                lastProcessedTime = now;
                selectedSlot = tiket.getSlot();
                selectedTiket = tiket;

                // Buka portal gerbang dengan animasi
                animGate(true, "TERBUKA MASUK");

                cctvVehicleDetected = false;
                cctvHudText = "MASUK SUKSES: " + plat + " -> " + tiket.getSlot().getIdSlot();
                lblCCTVStatus.setText("<html><span style='color:#16a34a;font-size:10px;'><b>" + plat + "</b> (" + merk + ") Masuk -> Slot <b>" + tiket.getSlot().getIdSlot() + "</b> (Gate Terbuka)</span></html>");
                lblNotice.setText("<html><span style='color:#16a34a;'>Kendaraan <b>" + plat + "</b> otomatis ter-check-in ke Slot <b>" + tiket.getSlot().getIdSlot() + "</b>. Tiket: <b>" + tiket.getNomorTiket() + "</b></span></html>");

                // Sinkronisasi denah, log table, pill statistik
                syncAll();

                // Isi form check-in juga untuk display
                txtPlat.setText(plat);
                txtMerk.setText(merk);
                cbTipe.setSelectedItem(tipe);
                updateAlokasiHint();

                startCCTVCooldownCountdown(8);
            }
        } catch (KendaraanSudahParkirException e) {
            cctvVehicleDetected = false;
            cctvHudText = "KENDARAAN SUDAH PARKIR: " + plat;
            lblNotice.setText("<html><span style='color:#d97706;'>⚠️ " + e.getMessage() + " Mengalihkan ke alur keluar...</span></html>");
            processExitAIDetection(plat);
        } catch (KapasitasPenuhException e) {
            cctvVehicleDetected = false;
            cctvHudText = "PARKIR PENUH (" + tipe + ")";
            lblCCTVStatus.setText("<html><span style='color:#dc2626;font-size:10px;'>⚠️ " + e.getMessage() + "</span></html>");
            lblNotice.setText("<html><span style='color:#dc2626;'>❌ " + e.getMessage() + " Portal masuk tetap terkunci!</span></html>");
            animGate(false, "TERTUTUP (PENUH)");
            startCCTVCooldownCountdown(5);
        } catch (FormatPlatTidakValidException e) {
            cctvVehicleDetected = false;
            cctvHudText = "PLAT INVALID: " + plat;
            lblCCTVStatus.setText("<html><span style='color:#dc2626;font-size:10px;'>⚠️ " + e.getMessage() + "</span></html>");
            startCCTVCooldownCountdown(4);
        } catch (Exception e) {
            cctvVehicleDetected = false;
            cctvHudText = "GAGAL MASUK: " + e.getMessage();
            lblCCTVStatus.setText("<html><span style='color:#dc2626;font-size:10px;'>Kesalahan: " + e.getMessage() + "</span></html>");
            startCCTVCooldownCountdown(4);
        }
    }

    private void startCCTVCooldownCountdown(int seconds) {
        cctvCooldownUntil = System.currentTimeMillis() + (seconds * 1000L);
        Timer timer = new Timer(1000, null);
        final int[] remaining = new int[]{seconds};
        timer.addActionListener(e -> {
            remaining[0]--;
            if (remaining[0] > 0) {
                lblCCTVLiveBadge.setText("● Jeda Sensor (" + remaining[0] + "s)");
                lblCCTVLiveBadge.setForeground(T2);
            } else {
                timer.stop();
                lblCCTVLiveBadge.setText("● Siaran Aktif");
                lblCCTVLiveBadge.setForeground(GREEN);
                cctvHudText = "MEMANTAU GERBANG MASUK...";
                lblCCTVStatus.setText("<html><span style='color:#4b5563;font-size:10px;'>Sensor aktif: Menunggu kendaraan di gerbang masuk...</span></html>");
            }
        });
        timer.start();
    }

    private void drawCCTVScreen(BufferedImage frame, boolean isDetected, String hudStatus) {
        if (cctvScreen == null) return;
        int targetW = cctvScreen.getWidth() > 0 ? cctvScreen.getWidth() : 438;
        int targetH = 220;

        if (cachedCctvBuffer == null || cachedCctvBuffer.getWidth() != targetW || cachedCctvBuffer.getHeight() != targetH) {
            cachedCctvBuffer = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_RGB);
        }

        Graphics2D g2 = cachedCctvBuffer.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        if (frame != null) {
            double scale = Math.max((double) targetW / frame.getWidth(), (double) targetH / frame.getHeight());
            int sw = (int) (frame.getWidth() * scale);
            int sh = (int) (frame.getHeight() * scale);
            int ox = (targetW - sw) / 2;
            int oy = (targetH - sh) / 2;
            g2.drawImage(frame, ox, oy, sw, sh, null);
        } else {
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, targetW, targetH);
        }

        // Bounding Scanner Reticle Presisi
        int rw = Math.min(targetW - 70, 260), rh = 125;
        int rx = (targetW - rw) / 2;
        int ry = (targetH - rh) / 2 - 4;

        Color hudColor = isDetected ? new Color(34, 197, 94) : new Color(59, 130, 246);
        g2.setColor(hudColor);
        g2.setStroke(new BasicStroke(2.2f));

        int cLen = 18;
        g2.drawLine(rx, ry, rx + cLen, ry);
        g2.drawLine(rx, ry, rx, ry + cLen);
        g2.drawLine(rx + rw, ry, rx + rw - cLen, ry);
        g2.drawLine(rx + rw, ry, rx + rw, ry + cLen);
        g2.drawLine(rx, ry + rh, rx + cLen, ry + rh);
        g2.drawLine(rx, ry + rh, rx, ry + rh - cLen);
        g2.drawLine(rx + rw, ry + rh, rx + rw - cLen, ry + rh);
        g2.drawLine(rx + rw, ry + rh, rx + rw, ry + rh - cLen);

        g2.setColor(new Color(hudColor.getRed(), hudColor.getGreen(), hudColor.getBlue(), 20));
        g2.fillRect(rx, ry, rw, rh);

        // Header Text & Dot Indicator
        g2.setColor(new Color(15, 23, 42, 190));
        g2.fillRoundRect(8, 8, 245, 20, 4, 4);
        g2.setColor(isExitGateMode ? AMBER : Color.RED);
        g2.fillOval(14, 14, 8, 8);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
        g2.setColor(Color.WHITE);
        String camTitle = isExitGateMode ? "CAM-02 • GERBANG KELUAR REALTIME" : "CAM-01 • GERBANG MASUK REALTIME";
        g2.drawString(camTitle, 28, 22);

        // Status Badge at Bottom
        g2.setColor(new Color(15, 23, 42, 225));
        g2.fillRoundRect(rx, ry + rh - 24, rw, 22, 5, 5);
        g2.setColor(hudColor);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
        String defStatus = isExitGateMode ? "MEMANTAU GERBANG KELUAR..." : "MEMANTAU GERBANG MASUK...";
        g2.drawString(hudStatus != null ? hudStatus : defStatus, rx + 8, ry + rh - 8);

        g2.dispose();
        cctvScreen.setIcon(new ImageIcon(cachedCctvBuffer));
        cctvScreen.setText(null);
    }

    private void simulateCarArrival() {
        File s = new File("sample_car.jpg");
        if (s.exists()) {
            try {
                BufferedImage carImg = ImageIO.read(s);
                if (carImg != null) {
                    currentCCTVFrame = carImg;
                    handleAutoVehicleDetected(carImg);
                }
            } catch (Exception ex) {
                warn("Gagal memuat sample_car.jpg: " + ex.getMessage());
            }
        } else {
            warn("File sample_car.jpg tidak ditemukan.");
        }
    }

    private void toggleCCTVStreaming() {
        if (cctvStreamer != null && cctvStreamer.isStreaming()) {
            cctvStreamer.stopStream();
            btnCCTVToggle.setText("Mulai Video");
            lblCCTVLiveBadge.setText("● Video Dijeda");
            lblCCTVLiveBadge.setForeground(T3);
        } else {
            startDashboardCCTV();
            btnCCTVToggle.setText("Jeda Video");
            lblCCTVLiveBadge.setText("● Siaran Aktif");
            lblCCTVLiveBadge.setForeground(GREEN);
        }
    }

    private JPanel buildOperationCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 310));
        card.setPreferredSize(new Dimension(440, 300));

        lblNotice = new JLabel("");
        lblNotice.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblNotice.setForeground(T2);
        lblNotice.setBorder(new EmptyBorder(4, 12, 4, 12));
        card.add(lblNotice, BorderLayout.NORTH);

        tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 11));
        tabs.setBackground(CARD_BG);
        tabs.setForeground(T1);
        tabs.addTab("Check-Out", buildCheckOutPanel());
        tabs.addTab("Check-In",  buildCheckInPanel());
        tabs.addTab("Koleksi Armada", buildKendaraanCollectionPanel());
        card.add(tabs, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildCheckOutPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBackground(CARD_BG);
        p.setBorder(new EmptyBorder(8, 10, 8, 10));

        // Info & Dropdown Pilih Kendaraan Aktif
        JPanel topBox = new JPanel(new BorderLayout(0, 4));
        topBox.setOpaque(false);

        lblInfoKendaraan = new JLabel("<html><span style='color:#4b5563;'>Pilih kendaraan di denah atau daftar di bawah</span></html>");
        lblInfoKendaraan.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblInfoKendaraan.setOpaque(true);
        lblInfoKendaraan.setBackground(new Color(243, 244, 246));
        lblInfoKendaraan.setForeground(T1);
        lblInfoKendaraan.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1), new EmptyBorder(5, 8, 5, 8)));

        cbActiveVehicles = new JComboBox<>();
        styleCombo(cbActiveVehicles);
        cbActiveVehicles.addActionListener(e -> onActiveVehicleComboSelected());

        topBox.add(lblInfoKendaraan, BorderLayout.NORTH);
        topBox.add(cbActiveVehicles, BorderLayout.SOUTH);
        p.add(topBox, BorderLayout.NORTH);

        // Form Fields
        JPanel form = new JPanel(new GridLayout(4, 2, 8, 6));
        form.setOpaque(false);

        form.add(fieldLabel("No. Tiket"));
        txtTiket = field("");
        form.add(txtTiket);

        form.add(fieldLabel("Jam Keluar (0-23)"));
        JPanel jamBox = new JPanel(new BorderLayout(4, 0));
        jamBox.setOpaque(false);
        txtJamKeluar = field(String.valueOf(java.time.LocalTime.now().getHour()));
        txtJamKeluar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateTagihan(); }
            public void removeUpdate(DocumentEvent e) { updateTagihan(); }
            public void changedUpdate(DocumentEvent e) { updateTagihan(); }
        });
        JButton btnNow = button("Sekarang", FIELD_BG, T1);
        btnNow.setFont(new Font("Segoe UI", Font.BOLD, 9));
        btnNow.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(3, 8, 3, 8)));
        btnNow.addActionListener(e -> {
            txtJamKeluar.setText(String.valueOf(java.time.LocalTime.now().getHour()));
            updateTagihan();
        });
        jamBox.add(txtJamKeluar, BorderLayout.CENTER);
        jamBox.add(btnNow, BorderLayout.EAST);
        form.add(jamBox);

        form.add(fieldLabel("Metode Bayar"));
        cbMetode = new JComboBox<>(new String[]{"E_MONEY", "QRIS", "VIRTUAL_ACCOUNT", "CASH"});
        styleCombo(cbMetode);
        form.add(cbMetode);

        form.add(fieldLabel("Nominal Bayar (Rp)"));
        txtUangBayar = field("0");
        form.add(txtUangBayar);

        p.add(form, BorderLayout.CENTER);

        // Bagian Bawah: Total Tagihan & Aksi Pembayaran
        JPanel bot = new JPanel(new BorderLayout(0, 6));
        bot.setOpaque(false);

        JPanel tagihanBar = new JPanel(new BorderLayout());
        tagihanBar.setBackground(new Color(236, 239, 243));
        tagihanBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(6, 12, 6, 12)));
        JLabel tl = new JLabel("Total Tagihan:");
        tl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        tl.setForeground(T1);
        lblTagihan = new JLabel("Rp 0");
        lblTagihan.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTagihan.setForeground(T1);
        tagihanBar.add(tl, BorderLayout.WEST);
        tagihanBar.add(lblTagihan, BorderLayout.EAST);
        bot.add(tagihanBar, BorderLayout.NORTH);

        JPanel actionBox = new JPanel(new GridLayout(2, 1, 0, 4));
        actionBox.setOpaque(false);

        JPanel instantRow = new JPanel(new GridLayout(1, 4, 4, 0));
        instantRow.setOpaque(false);

        JButton btnQuickEMoney = button("E-Money", FIELD_BG, T1);
        btnQuickEMoney.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btnQuickEMoney.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(5, 2, 5, 2)));
        btnQuickEMoney.setToolTipText("Bayar lunas via E-Money");
        btnQuickEMoney.addActionListener(e -> quickPay("E_MONEY"));

        JButton btnQuickQRIS = button("QRIS", FIELD_BG, T1);
        btnQuickQRIS.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btnQuickQRIS.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(5, 2, 5, 2)));
        btnQuickQRIS.setToolTipText("Bayar via scan QRIS Midtrans");
        btnQuickQRIS.addActionListener(e -> quickPay("QRIS"));

        JButton btnQuickVA = button("Virtual Account", FIELD_BG, T1);
        btnQuickVA.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btnQuickVA.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(5, 2, 5, 2)));
        btnQuickVA.setToolTipText("Bayar via Virtual Account Midtrans");
        btnQuickVA.addActionListener(e -> quickPay("VIRTUAL_ACCOUNT"));

        JButton btnQuickCashPas = button("Tunai Pas", FIELD_BG, T1);
        btnQuickCashPas.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btnQuickCashPas.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(5, 2, 5, 2)));
        btnQuickCashPas.setToolTipText("Bayar uang pas tunai");
        btnQuickCashPas.addActionListener(e -> quickPay("CASH"));

        instantRow.add(btnQuickEMoney);
        instantRow.add(btnQuickQRIS);
        instantRow.add(btnQuickVA);
        instantRow.add(btnQuickCashPas);

        JButton btnBayarManual = button("Konfirmasi & Buka Palang", PRIMARY, WHITE);
        btnBayarManual.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnBayarManual.setBorder(new EmptyBorder(6, 6, 6, 6));
        btnBayarManual.addActionListener(e -> handleCheckOut());

        actionBox.add(instantRow);
        actionBox.add(btnBayarManual);

        bot.add(actionBox, BorderLayout.SOUTH);
        p.add(bot, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildCheckInPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(CARD_BG);
        p.setBorder(new EmptyBorder(10, 12, 10, 12));

        JPanel form = new JPanel(new GridLayout(4, 2, 8, 8));
        form.setOpaque(false);

        form.add(fieldLabel("Nomor Plat"));     txtPlat = field("B 1234 XYZ"); form.add(txtPlat);
        form.add(fieldLabel("Tipe Kendaraan"));
        cbTipe = new JComboBox<>(new String[]{"MOBIL", "MOTOR", "MOBIL_LISTRIK"});
        styleCombo(cbTipe);
        form.add(cbTipe);

        form.add(fieldLabel("Merk / Model"));  txtMerk = field("Toyota Avanza"); form.add(txtMerk);
        form.add(fieldLabel("Jam Masuk (0-23)")); txtJamMasuk = field("10"); form.add(txtJamMasuk);

        p.add(form, BorderLayout.CENTER);

        JPanel botButtons = new JPanel(new GridLayout(1, 2, 8, 0));
        botButtons.setOpaque(false);

        JButton btnEmergency = button("Karcis Darurat", CARD_BG, T1);
        btnEmergency.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btnEmergency.setToolTipText("Cetak karcis darurat jika plat nomor kotor/rusak");
        btnEmergency.addActionListener(e -> handleEmergencyQRCheckIn());

        JButton btnMasuk = button("Check-In Kendaraan", PRIMARY, WHITE);
        btnMasuk.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btnMasuk.setBorder(new EmptyBorder(6, 4, 6, 4));
        btnMasuk.addActionListener(e -> handleCheckIn());

        botButtons.add(btnEmergency);
        botButtons.add(btnMasuk);
        p.add(botButtons, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildKendaraanCollectionPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(CARD_BG);
        p.setBorder(new EmptyBorder(8, 10, 8, 10));

        // 1. Search Bar & Status Counter
        JPanel topBox = new JPanel(new BorderLayout(6, 0));
        topBox.setOpaque(false);

        JPanel searchBar = new JPanel(new BorderLayout(4, 0));
        searchBar.setOpaque(false);
        txtCariKoleksi = field("");
        txtCariKoleksi.setToolTipText("Ketik plat untuk memfilter koleksi...");

        JButton btnCari = button("Cari", PRIMARY, WHITE);
        btnCari.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btnCari.addActionListener(e -> refreshTabelKendaraan(txtCariKoleksi.getText().trim()));

        JButton btnReset = button("Semua", FIELD_BG, T1);
        btnReset.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btnReset.addActionListener(e -> {
            txtCariKoleksi.setText("");
            refreshTabelKendaraan(null);
        });

        searchBar.add(fieldLabel("Cari:"), BorderLayout.WEST);
        searchBar.add(txtCariKoleksi, BorderLayout.CENTER);

        JPanel btnGroup = new JPanel(new GridLayout(1, 2, 4, 0));
        btnGroup.setOpaque(false);
        btnGroup.add(btnCari);
        btnGroup.add(btnReset);
        searchBar.add(btnGroup, BorderLayout.EAST);

        lblTotalKoleksiArmada = new JLabel("Total: 0");
        lblTotalKoleksiArmada.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTotalKoleksiArmada.setForeground(T1);

        topBox.add(searchBar, BorderLayout.CENTER);
        topBox.add(lblTotalKoleksiArmada, BorderLayout.EAST);
        p.add(topBox, BorderLayout.NORTH);

        // 2. Table Kumpulan Kendaraan (List<Kendaraan>)
        String[] cols = {"Plat Nomor", "Kategori", "Merk / Brand", "Status Parkir"};
        tblModelKendaraan = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblKendaraanList = new JTable(tblModelKendaraan);
        tblKendaraanList.setBackground(FIELD_BG);
        tblKendaraanList.setForeground(T1);
        tblKendaraanList.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tblKendaraanList.setRowHeight(22);
        tblKendaraanList.setShowGrid(true);
        tblKendaraanList.setGridColor(BORDER);
        tblKendaraanList.setSelectionBackground(PRIMARY);
        tblKendaraanList.setSelectionForeground(WHITE);
        tblKendaraanList.getTableHeader().setBackground(new Color(235, 237, 241));
        tblKendaraanList.getTableHeader().setForeground(T1);
        tblKendaraanList.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 10));
        tblKendaraanList.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));

        JScrollPane sp = new JScrollPane(tblKendaraanList);
        sp.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        sp.getViewport().setBackground(FIELD_BG);
        p.add(sp, BorderLayout.CENTER);

        // 3. Form Pendaftaran Armada Baru & Aksi Hapus
        JPanel botBox = new JPanel(new BorderLayout(0, 6));
        botBox.setOpaque(false);

        JPanel formAdd = new JPanel(new GridLayout(3, 2, 6, 4));
        formAdd.setOpaque(false);

        formAdd.add(fieldLabel("Plat Baru:"));
        txtPlatBaru = field("");
        formAdd.add(txtPlatBaru);

        formAdd.add(fieldLabel("Kategori:"));
        cbTipeBaru = new JComboBox<>(new String[]{"MOBIL", "MOTOR", "MOBIL_LISTRIK"});
        styleCombo(cbTipeBaru);
        formAdd.add(cbTipeBaru);

        formAdd.add(fieldLabel("Merk / Model:"));
        txtMerkBaru = field("");
        formAdd.add(txtMerkBaru);

        botBox.add(formAdd, BorderLayout.CENTER);

        JPanel btnActions = new JPanel(new GridLayout(2, 2, 6, 4));
        btnActions.setOpaque(false);

        JButton btnDaftar = button("+ Daftarkan Armada", PRIMARY, WHITE);
        btnDaftar.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btnDaftar.addActionListener(e -> handleTambahKendaraanCollection());

        JButton btnHapus = button("Hapus Terpilih", RED, WHITE);
        btnHapus.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btnHapus.addActionListener(e -> handleHapusKendaraanCollection());

        JButton btnImpor = button("📥 Impor dari File", CARD_BG, T1);
        btnImpor.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btnImpor.setToolTipText("Muat armada dari berkas teks (Modul 12 File Persistence)");
        btnImpor.addActionListener(e -> handleImporArmadaFile());

        JButton btnEkspor = button("📤 Ekspor ke File", CARD_BG, T1);
        btnEkspor.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btnEkspor.setToolTipText("Simpan seluruh koleksi armada ke berkas teks (Modul 12 File Persistence)");
        btnEkspor.addActionListener(e -> handleEksporArmadaFile());

        btnActions.add(btnDaftar);
        btnActions.add(btnHapus);
        btnActions.add(btnImpor);
        btnActions.add(btnEkspor);
        botBox.add(btnActions, BorderLayout.SOUTH);

        p.add(botBox, BorderLayout.SOUTH);
        return p;
    }

    private void refreshTabelKendaraan(String query) {
        if (tblModelKendaraan == null) return;
        tblModelKendaraan.setRowCount(0);
        List<Kendaraan> list = gedung.getDaftarKendaraan();
        String q = (query != null && !query.trim().isEmpty()) ? query.trim().replaceAll("\\s+", "").toUpperCase() : null;

        for (Kendaraan k : list) {
            String pStr = k.getPlatNomor().replaceAll("\\s+", "").toUpperCase();
            if (q != null && !pStr.contains(q)) {
                continue;
            }
            SlotParkir slotAktif = gedung.cariSlotDenganPlat(k.getPlatNomor());
            String status = (slotAktif != null) ? "PARKIR (" + slotAktif.getIdSlot() + ")" : "BEBAS";
            tblModelKendaraan.addRow(new Object[]{
                k.getPlatNomor(),
                k.getTipe(),
                k.getMerk(),
                status
            });
        }
        if (lblTotalKoleksiArmada != null) {
            lblTotalKoleksiArmada.setText("Total: " + gedung.getTotalKendaraanTerdaftar() + " unit");
        }
    }

    private void handleTambahKendaraanCollection() {
        try {
            String plat = txtPlatBaru.getText().trim();
            String tipe = (String) cbTipeBaru.getSelectedItem();
            String merk = txtMerkBaru.getText().trim();

            if (plat.isEmpty()) { warn("Plat nomor tidak boleh kosong!"); return; }
            if (merk.isEmpty()) merk = "Umum";

            Kendaraan.validasiFormatPlat(plat);
            Kendaraan k = new Kendaraan(plat, tipe, merk);
            gedung.tambahKendaraan(k);

            txtPlatBaru.setText("");
            txtMerkBaru.setText("");
            refreshTabelKendaraan(null);
            JOptionPane.showMessageDialog(this,
                "Kendaraan " + k.getPlatNomor() + " (" + k.getMerk() + ") berhasil didaftarkan ke koleksi!",
                "Sukses Pendaftaran Koleksi", JOptionPane.INFORMATION_MESSAGE);
        } catch (FormatPlatTidakValidException ex) {
            warn("PLAT TIDAK VALID: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            warn("GAGAL DAFTAR: " + ex.getMessage());
        } catch (Exception ex) {
            warn("Kesalahan: " + ex.getMessage());
        }
    }

    private void handleHapusKendaraanCollection() {
        int row = tblKendaraanList.getSelectedRow();
        if (row < 0) {
            warn("Pilih kendaraan pada tabel koleksi terlebih dahulu!");
            return;
        }
        String plat = (String) tblModelKendaraan.getValueAt(row, 0);
        int konfirm = JOptionPane.showConfirmDialog(this,
            "Apakah Anda yakin ingin menghapus armada plat " + plat + " dari koleksi sistem?",
            "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
        if (konfirm != JOptionPane.YES_OPTION) return;

        try {
            boolean sukses = gedung.hapusKendaraan(plat);
            if (sukses) {
                refreshTabelKendaraan(null);
                JOptionPane.showMessageDialog(this,
                    "Kendaraan plat " + plat + " berhasil dihapus dari koleksi armada.",
                    "Sukses Dihapus", JOptionPane.INFORMATION_MESSAGE);
            } else {
                warn("Kendaraan plat " + plat + " tidak ditemukan dalam koleksi.");
            }
        } catch (IllegalStateException ex) {
            warn("PROTEKSI OPERASIONAL DITOLAK: " + ex.getMessage());
        } catch (Exception ex) {
            warn("Kesalahan: " + ex.getMessage());
        }
    }

    private void handleImporArmadaFile() {
        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setDialogTitle("Impor Koleksi Armada dari File (Modul 12)");
        chooser.setSelectedFile(new File("armada_kendaraan.txt"));
        chooser.setFileFilter(new FileNameExtensionFilter("Text File (*.txt)", "txt"));

        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (file == null) return;

            int konfirm = JOptionPane.showConfirmDialog(this,
                "Memuat armada dari file akan mengganti koleksi armada saat ini (" 
                + gedung.getTotalKendaraanTerdaftar() + " unit).\n"
                + "File target: " + file.getName() + "\n\nLanjutkan proses impor?",
                "Konfirmasi Impor Armada", JOptionPane.YES_NO_OPTION);
            if (konfirm != JOptionPane.YES_OPTION) return;

            try {
                int total = gedung.muatArmadaDariFile(file.toPath());
                refreshTabelKendaraan(null);
                JOptionPane.showMessageDialog(this,
                    "Berhasil mengimpor " + total + " unit armada dari berkas:\n" + file.getAbsolutePath(),
                    "Impor Berhasil", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                warn("GAGAL MEMUAT FILE (ATOMIC ROLLBACK AKTIF):\n" + ex.getMessage() 
                    + "\n\nState koleksi armada sebelumnya tetap terjaga utuh.");
            } catch (Exception ex) {
                warn("Kesalahan saat membaca file: " + ex.getMessage());
            }
        }
    }

    private void handleEksporArmadaFile() {
        if (gedung.getTotalKendaraanTerdaftar() == 0) {
            warn("Koleksi armada kosong! Tidak ada data untuk diekspor.");
            return;
        }

        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setDialogTitle("Ekspor Koleksi Armada ke File (Modul 12)");
        chooser.setSelectedFile(new File("armada_kendaraan.txt"));
        chooser.setFileFilter(new FileNameExtensionFilter("Text File (*.txt)", "txt"));

        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (file == null) return;
            if (!file.getName().toLowerCase().endsWith(".txt")) {
                file = new File(file.getAbsolutePath() + ".txt");
            }

            try {
                gedung.simpanArmadaKeFile(file.toPath());
                JOptionPane.showMessageDialog(this,
                    "Berhasil mengekspor " + gedung.getTotalKendaraanTerdaftar() + " unit armada ke berkas:\n" 
                    + file.getAbsolutePath(),
                    "Ekspor Berhasil", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                warn("GAGAL MENYIMPAN FILE:\n" + ex.getMessage());
            } catch (IllegalArgumentException ex) {
                warn("DATA TIDAK VALID UNTUK DISIMPAN:\n" + ex.getMessage());
            } catch (Exception ex) {
                warn("Kesalahan saat menyimpan file: " + ex.getMessage());
            }
        }
    }

    private JPanel buildLogCard() {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(8, 12, 10, 12)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        card.setPreferredSize(new Dimension(440, 210));

        card.add(sectionTitle("Riwayat Transaksi"), BorderLayout.NORTH);

        String[] cols = {"Tiket", "Plat", "Tipe", "Slot", "Waktu", "Status", "Biaya"};
        logModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tbl = new JTable(logModel);
        tbl.setBackground(FIELD_BG);
        tbl.setForeground(T1);
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tbl.setRowHeight(24);
        tbl.setShowGrid(true);
        tbl.setGridColor(BORDER);
        tbl.setSelectionBackground(PRIMARY);
        tbl.setSelectionForeground(WHITE);
        tbl.getTableHeader().setBackground(new Color(235, 237, 241));
        tbl.getTableHeader().setForeground(T1);
        tbl.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 10));
        tbl.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));

        DefaultTableCellRenderer ctr = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? FIELD_BG : new Color(246, 248, 250));
                    c.setForeground(T1);
                } else {
                    c.setBackground(PRIMARY);
                    c.setForeground(WHITE);
                }
                return c;
            }
        };
        ctr.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tbl.getColumnCount(); i++)
            tbl.getColumnModel().getColumn(i).setCellRenderer(ctr);

        tbl.getColumnModel().getColumn(0).setPreferredWidth(75);
        tbl.getColumnModel().getColumn(1).setPreferredWidth(85);
        tbl.getColumnModel().getColumn(2).setPreferredWidth(60);
        tbl.getColumnModel().getColumn(3).setPreferredWidth(45);
        tbl.getColumnModel().getColumn(4).setPreferredWidth(85);
        tbl.getColumnModel().getColumn(5).setPreferredWidth(60);
        tbl.getColumnModel().getColumn(6).setPreferredWidth(65);

        tbl.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = tbl.getSelectedRow();
                if (row >= 0) {
                    String tiket = (String) logModel.getValueAt(row, 0);
                    for (KarcisParkir t : gedung.getRiwayatTransaksi())
                        if (t.getNomorTiket().equalsIgnoreCase(tiket)) { onSlotClick(t.getSlot()); break; }
                }
            }
        });

        JScrollPane sp = new JScrollPane(tbl);
        sp.setPreferredSize(new Dimension(438, 150));
        sp.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        sp.getViewport().setBackground(FIELD_BG);
        sp.setBackground(CARD_BG);
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    private void openCameraScannerDialog() {
        JDialog dlg = new JDialog(this, "Pengaturan AI & Detail Scanner Gerbang", true);
        dlg.getContentPane().setBackground(CARD_BG);
        dlg.setLayout(new BorderLayout(12, 12));
        dlg.setSize(800, 520);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(CARD_BG);
        p.setBorder(new EmptyBorder(16, 20, 16, 20));

        p.add(sectionTitle("Konfigurasi Model AI Vision & Kredensial"));
        p.add(Box.createVerticalStrut(8));

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.setOpaque(false);

        form.add(fieldLabel("Endpoint API Vision"));
        JTextField txtEndpoint = field(savedEndpoint);
        form.add(txtEndpoint);

        form.add(fieldLabel("API Key"));
        JPasswordField txtKey = new JPasswordField(savedApiKey);
        txtKey.setBackground(FIELD_BG);
        txtKey.setForeground(T1);
        txtKey.setCaretColor(T1);
        txtKey.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1), new EmptyBorder(4, 7, 4, 7)));
        form.add(txtKey);

        form.add(fieldLabel("Model Vision"));
        JComboBox<String> cbModel = new JComboBox<>(new String[]{"orion", "llama3.2-vision", "llava:latest", "gpt-4o-mini"});
        cbModel.setEditable(true);
        cbModel.setSelectedItem(savedModel);
        styleCombo(cbModel);
        form.add(cbModel);
        p.add(form);

        p.add(Box.createVerticalStrut(12));
        JLabel hint = new JLabel(String.format("<html><span style='color:%s;font-size:10px;'>Kredensial disimpan aman di <b>ai_config.properties</b> (terproteksi .gitignore).</span></html>", hex(T2)));
        p.add(hint);

        p.add(Box.createVerticalStrut(16));
        JButton btnSave = button("Simpan Perubahan Kredensial", GREEN, WHITE);
        btnSave.addActionListener(e -> {
            savedEndpoint = txtEndpoint.getText().trim();
            savedApiKey   = new String(txtKey.getPassword()).trim();
            savedModel    = cbModel.getSelectedItem().toString().trim();

            java.util.Properties props = new java.util.Properties();
            props.setProperty("ai.endpoint", savedEndpoint);
            props.setProperty("ai.api_key", savedApiKey);
            props.setProperty("ai.model", savedModel);
            AIVisionService.saveConfig(props);

            JOptionPane.showMessageDialog(dlg, "Kredensial berhasil disimpan ke ai_config.properties!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            dlg.dispose();
        });
        p.add(btnSave);

        dlg.add(p, BorderLayout.CENTER);
        dlg.setVisible(true);
    }

    private void handleEmergencyQRCheckIn() {
        try {
            String tipe = (String) cbTipe.getSelectedItem();
            int jam = Integer.parseInt(txtJamMasuk.getText().trim());
            KarcisParkir tiket = gedung.prosesMasukEmergency(tipe, jam);
            if (tiket != null) {
                animGate(true, "TERBUKA MASUK (EMERGENCY)");
                selectedSlot = tiket.getSlot();
                selectedTiket = tiket;
                syncAll();
                showEmergencyTicketDialog(tiket);
            }
        } catch (NumberFormatException ex) {
            warn("Format jam masuk salah: Harap masukkan angka bulat (0-23)!");
        } catch (KapasitasPenuhException ex) {
            warn("KAPASITAS PENUH: " + ex.getMessage());
        } catch (Exception ex) {
            warn("Gagal membuat tiket darurat: " + ex.getMessage());
        }
    }

    private void showEmergencyTicketDialog(KarcisParkir tiket) {
        JDialog dlg = new JDialog(this, "Karcis Darurat Masuk (Fallback QR)", true);
        dlg.getContentPane().setBackground(CARD_BG);
        dlg.setLayout(new BorderLayout());

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AMBER, 1),
            new EmptyBorder(16, 20, 16, 20)));

        JLabel lblTitle = new JLabel("TIKET DARURAT SMARTPARKIR");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(AMBER);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(lblTitle);
        p.add(Box.createVerticalStrut(4));

        JLabel lblSub = new JLabel("Plat tidak terbaca / kotor di pintu masuk");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblSub.setForeground(T2);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(lblSub);
        p.add(Box.createVerticalStrut(12));

        BufferedImage qrImg = QRISService.generateQRCodeImage(tiket.getNomorTiket(), 180);
        JLabel lblQr = new JLabel(new ImageIcon(qrImg));
        lblQr.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(lblQr);
        p.add(Box.createVerticalStrut(10));

        JLabel lblCode = new JLabel(tiket.getNomorTiket());
        lblCode.setFont(new Font("Monospaced", Font.BOLD, 16));
        lblCode.setForeground(T1);
        lblCode.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(lblCode);
        p.add(Box.createVerticalStrut(6));

        JLabel lblHint = new JLabel("Simpan karcis ini. Tunjukkan ke scanner/kamera saat keluar.");
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        lblHint.setForeground(T2);
        lblHint.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(lblHint);
        p.add(Box.createVerticalStrut(14));

        JButton btnClose = button("Tutup & Buka Palang", AMBER, WHITE);
        btnClose.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnClose.addActionListener(e -> dlg.dispose());
        p.add(btnClose);

        dlg.add(p, BorderLayout.CENTER);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void handleCheckIn() {
        try {
            String plat = txtPlat.getText().trim();
            String merk = txtMerk.getText().trim();
            String tipe = (String) cbTipe.getSelectedItem();
            int jam = Integer.parseInt(txtJamMasuk.getText().trim());
            if (plat.isEmpty() || merk.isEmpty()) { warn("Plat dan merk tidak boleh kosong."); return; }
            Kendaraan.validasiFormatPlat(plat);

            KarcisParkir tiket = gedung.prosesMasuk(new Kendaraan(plat, tipe, merk), jam);
            if (tiket != null) {
                animGate(true, "TERBUKA MASUK");
                selectedSlot  = tiket.getSlot();
                selectedTiket = tiket;
                syncAll();
                onSlotClick(tiket.getSlot());
                JOptionPane.showMessageDialog(this,
                    "Check-In berhasil!\nTiket: " + tiket.getNomorTiket()
                    + "\nKendaraan: " + plat + " (" + merk + ")"
                    + "\nSlot: " + tiket.getSlot().getIdSlot(),
                    "Sukses Masuk", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            warn("Format jam masuk salah: Harap masukkan bilangan bulat (0-23)!");
        } catch (FormatPlatTidakValidException ex) {
            warn("PLAT TIDAK VALID: " + ex.getMessage());
        } catch (KendaraanSudahParkirException ex) {
            warn("DUPLIKASI DITOLAK: " + ex.getMessage());
        } catch (KapasitasPenuhException ex) {
            warn("PARKIR PENUH: " + ex.getMessage());
        } catch (Exception ex) {
            warn("Kesalahan input: " + ex.getMessage());
        }
    }

    private void handleCheckOut() {
        try {
            String noTiket = txtTiket.getText().trim();
            int jamKeluar  = Integer.parseInt(txtJamKeluar.getText().trim());
            String metode  = (String) cbMetode.getSelectedItem();
            double uang    = Double.parseDouble(txtUangBayar.getText().trim());

            KarcisParkir found = null;
            for (KarcisParkir t : gedung.getRiwayatTransaksi())
                if (t.getNomorTiket().equalsIgnoreCase(noTiket)) { found = t; break; }

            if (found == null) { warn("Tiket tidak ditemukan."); return; }
            if (found.isSudahDibayar()) { warn("Tiket ini sudah lunas."); return; }

            double tagihanHitung = hitungTotalTagihanTiket(found, jamKeluar);
            if ("QRIS".equalsIgnoreCase(metode)) {
                showQRISPaymentDialog(found, jamKeluar, tagihanHitung);
                return;
            }
            if ("VIRTUAL_ACCOUNT".equalsIgnoreCase(metode) || "VA".equalsIgnoreCase(metode)) {
                showVirtualAccountDialog(found, jamKeluar, tagihanHitung);
                return;
            }

            boolean ok = gedung.prosesKeluar(found, jamKeluar, metode, uang);
            if (ok) {
                animGate(true, "TERBUKA KELUAR");
                selectedSlot  = null;
                selectedTiket = null;
                lblNotice.setText("<html><span style='color:#16a34a;'>Check-out berhasil. Slot kosong kembali.</span></html>");
                lblTagihan.setText("Rp 0");
                syncAll();
                showReceiptDialog(found, uang);
            }
        } catch (NumberFormatException ex) {
            warn("Format angka salah: Harap masukkan jam keluar dan nominal bayar berupa angka bulat!");
        } catch (PembayaranKurangException ex) {
            warn("PEMBAYARAN DITOLAK: " + ex.getMessage() + "\nPortal keluar tetap terkunci!");
        } catch (TiketTidakDitemukanException ex) {
            warn("TIKET TIDAK DITEMUKAN: " + ex.getMessage());
        } catch (Exception ex) {
            warn("Kesalahan check-out: " + ex.getMessage());
        }
    }

    private void quickPay(String metode) {
        try {
            String noTiket = txtTiket.getText().trim();
            KarcisParkir found = null;
            for (KarcisParkir t : gedung.getRiwayatTransaksi()) {
                if (t.getNomorTiket().equalsIgnoreCase(noTiket) && !t.isSudahDibayar()) {
                    found = t;
                    break;
                }
            }
            if (found == null) {
                warn("Silakan pilih kendaraan yang sedang parkir terlebih dahulu.");
                return;
            }

            int jamKeluar = Integer.parseInt(txtJamKeluar.getText().trim());
            double total = hitungTotalTagihanTiket(found, jamKeluar);

            if ("QRIS".equalsIgnoreCase(metode)) {
                showQRISPaymentDialog(found, jamKeluar, total);
                return;
            }
            if ("VIRTUAL_ACCOUNT".equalsIgnoreCase(metode) || "VA".equalsIgnoreCase(metode)) {
                showVirtualAccountDialog(found, jamKeluar, total);
                return;
            }

            boolean ok = gedung.prosesKeluar(found, jamKeluar, metode, total);
            if (ok) {
                animGate(true, "TERBUKA KELUAR");
                selectedSlot  = null;
                selectedTiket = null;
                lblNotice.setText("<html><span style='color:#16a34a;'>Check-out sukses dengan " + metode + ". Gate terbuka!</span></html>");
                lblTagihan.setText("Rp 0");
                syncAll();
                showReceiptDialog(found, total);
            }
        } catch (NumberFormatException ex) {
            warn("Format jam keluar tidak valid!");
        } catch (PembayaranKurangException ex) {
            warn("PEMBAYARAN KURANG: " + ex.getMessage());
        } catch (TiketTidakDitemukanException ex) {
            warn("TIKET TIDAK DITEMUKAN: " + ex.getMessage());
        } catch (Exception ex) {
            warn("Gagal memproses pembayaran cepat: " + ex.getMessage());
        }
    }

    private double hitungTotalTagihanTiket(KarcisParkir t, int jamKeluar) {
        if (t == null) return 0.0;
        int durasi = t.hitungDurasi(jamKeluar);
        boolean motor = "MOTOR".equals(t.getKendaraan().getTipe());
        double p1 = motor ? gedung.getTarifJamPertamaMotor() : gedung.getTarifJamPertamaMobil();
        double pN = motor ? gedung.getTarifPerJamMotor()     : gedung.getTarifPerJamMobil();
        double total = p1 + (durasi > 1 ? (durasi - 1) * pN : 0);
        if (t.getKendaraan().isMobilListrik()) total *= 0.80;
        return total;
    }

    private JLabel createDetailLabel(String text, Color fg) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(fg);
        return l;
    }

    private void showQRISPaymentDialog(KarcisParkir tiket, int jamKeluar, double totalTagihan) {
        if (tiket == null) return;
        isPaymentDialogShowing = true;
        JDialog dlg = new JDialog(this, "Pembayaran QRIS Midtrans: Gerbang Keluar", true);
        dlg.getContentPane().setBackground(CARD_BG);
        dlg.setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(CARD_BG);
        root.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(18, 22, 18, 22)));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel(String.format("<html><b style='font-size:14px;color:%s;'>Pembayaran QRIS Gate Keluar (Midtrans Sandbox)</b><br/>"
            + "<span style='color:%s;font-size:10px;'>Pindai QR Code via GoPay, BCA, Livin, OVO, atau e-Wallet lainnya</span></html>", hex(T1), hex(T2)));
        header.add(title, BorderLayout.CENTER);
        root.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        // Inisialisasi Midtrans QRIS Transaction
        QRISService.MidtransQRISResult midtransRes = QRISService.createMidtransTransaction(tiket.getNomorTiket(), totalTagihan);
        BufferedImage qrImage = midtransRes.qrImage;
        if (qrImage == null) {
            qrImage = QRISService.generateQRCodeImage(midtransRes.qrString, 210);
        }
        final BufferedImage finalQrImg = qrImage;

        // QR Code Graphic Panel dengan Quiet Zone putih bersih & auto-scaling
        JPanel qrPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

                int w = getWidth();
                int h = getHeight();

                // Latar putih bersih (Quiet Zone standar ISO QRIS)
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(10, 6, w - 20, h - 12, 10, 10);

                if (finalQrImg != null) {
                    int targetW = w - 36;
                    int targetH = h - 28;
                    double scale = Math.min((double) targetW / finalQrImg.getWidth(), (double) targetH / finalQrImg.getHeight());
                    int dw = (int) (finalQrImg.getWidth() * scale);
                    int dh = (int) (finalQrImg.getHeight() * scale);
                    int ox = (w - dw) / 2;
                    int oy = (h - dh) / 2;
                    g2.drawImage(finalQrImg, ox, oy, dw, dh, null);
                }
                g2.dispose();
            }
        };
        qrPanel.setPreferredSize(new Dimension(250, 240));
        qrPanel.setMaximumSize(new Dimension(250, 240));
        qrPanel.setOpaque(false);
        center.add(qrPanel);
        center.add(Box.createVerticalStrut(6));

        // Tombol Bantuan Simulator & Salin Payload QRIS
        JPanel helperRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        helperRow.setOpaque(false);

        JButton btnOpenSim = button("Buka Midtrans Simulator", FIELD_BG, T1);
        btnOpenSim.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btnOpenSim.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(3, 8, 3, 8)));
        btnOpenSim.addActionListener(e -> {
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI("https://simulator.sandbox.midtrans.com/qris/index"));
                }
            } catch (Exception ignored) {}
        });

        JButton btnCopyStr = button("Salin String QR", FIELD_BG, T1);
        btnCopyStr.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btnCopyStr.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(3, 8, 3, 8)));
        btnCopyStr.addActionListener(e -> {
            if (midtransRes.qrString != null) {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new java.awt.datatransfer.StringSelection(midtransRes.qrString), null);
                btnCopyStr.setText("Tersalin!");
                Timer t = new Timer(2000, ev -> btnCopyStr.setText("Salin String QR"));
                t.setRepeats(false);
                t.start();
            }
        });

        helperRow.add(btnOpenSim);
        helperRow.add(btnCopyStr);
        center.add(helperRow);
        center.add(Box.createVerticalStrut(6));

        JLabel lblMidtransStatus = new JLabel(midtransRes.statusMessage, SwingConstants.CENTER);
        lblMidtransStatus.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        lblMidtransStatus.setForeground(midtransRes.isMockFallback ? AMBER : GREEN);
        lblMidtransStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(lblMidtransStatus);
        center.add(Box.createVerticalStrut(10));

        JPanel detail = new JPanel(new GridLayout(3, 2, 8, 4));
        detail.setOpaque(false);
        detail.add(createDetailLabel("No. Tiket", T2));
        detail.add(createDetailLabel(tiket.getNomorTiket(), T1));
        detail.add(createDetailLabel("Kendaraan", T2));
        detail.add(createDetailLabel(tiket.getKendaraan().getPlatNomor() + " (" + tiket.getKendaraan().getMerk() + ")", T1));
        detail.add(createDetailLabel("Total Tagihan", T2));
        JLabel lblTotal = new JLabel(rupiah.format(totalTagihan));
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTotal.setForeground(GREEN);
        detail.add(lblTotal);
        center.add(detail);

        root.add(center, BorderLayout.CENTER);

        // Polling Timer untuk mendeteksi settlement Midtrans otomatis
        Timer pollTimer = new Timer(1500, null);
        pollTimer.addActionListener(ev -> {
            String st = QRISService.checkTransactionStatus(tiket.getNomorTiket());
            if ("settlement".equalsIgnoreCase(st) || "capture".equalsIgnoreCase(st)) {
                pollTimer.stop();
                dlg.dispose();
                try {
                    boolean ok = gedung.prosesKeluar(tiket, jamKeluar, "QRIS_MIDTRANS", totalTagihan);
                    if (ok) {
                        DatabaseManager.updatePelunasanTiket(tiket.getNomorTiket(), jamKeluar, totalTagihan, "QRIS_MIDTRANS");
                        animGate(true, "TERBUKA KELUAR");
                        selectedSlot = null;
                        selectedTiket = null;
                        lblNotice.setText("<html><span style='color:#16a34a;'>Pembayaran QRIS Midtrans Terkonfirmasi LUNAS! Portal terbuka.</span></html>");
                        lblTagihan.setText("Rp 0");
                        syncAll();
                        showReceiptDialog(tiket, totalTagihan);
                    }
                } catch (Exception ex) {
                    warn("Gagal menyelesaikan transaksi: " + ex.getMessage());
                }
            }
        });
        pollTimer.start();

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        actions.setOpaque(false);

        JButton btnSwitchVA = button("Gunakan Virtual Account (Backup)", BLUE, WHITE);
        btnSwitchVA.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnSwitchVA.setToolTipText("Beralih ke transfer bank Virtual Account jika scanner QR bermasalah");
        btnSwitchVA.addActionListener(e -> {
            pollTimer.stop();
            dlg.dispose();
            showVirtualAccountDialog(tiket, jamKeluar, totalTagihan);
        });

        JButton btnCancel = button("Tutup / Batal", FIELD_BG, T1);
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnCancel.addActionListener(e -> {
            pollTimer.stop();
            dlg.dispose();
        });

        actions.add(btnSwitchVA);
        actions.add(btnCancel);
        root.add(actions, BorderLayout.SOUTH);

        dlg.addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) {
                pollTimer.stop();
                isPaymentDialogShowing = false;
                startCCTVCooldownCountdown(4);
            }
        });

        dlg.add(root);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void showVirtualAccountDialog(KarcisParkir tiket, int jamKeluar, double totalTagihan) {
        if (tiket == null) return;
        isPaymentDialogShowing = true;
        JDialog dlg = new JDialog(this, "Pembayaran Virtual Account: Gerbang Keluar", true);
        dlg.getContentPane().setBackground(CARD_BG);
        dlg.setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(CARD_BG);
        root.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(18, 22, 18, 22)));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel(String.format("<html><b style='font-size:14px;color:%s;'>Pembayaran Virtual Account Midtrans</b><br/>"
            + "<span style='color:%s;font-size:10px;'>Transfer via ATM, Mobile Banking, atau Internet Banking</span></html>", hex(T1), hex(T2)));
        header.add(title, BorderLayout.CENTER);
        root.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        // Selector Pilihan Bank
        JPanel bankChoicePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bankChoicePanel.setOpaque(false);
        bankChoicePanel.add(fieldLabel("Pilih Bank Transfer:"));

        String[] bankOptions = {"BCA", "BNI", "BRI", "Mandiri", "Permata"};
        JComboBox<String> cbBank = new JComboBox<>(bankOptions);
        styleCombo(cbBank);
        cbBank.setPreferredSize(new Dimension(110, 26));
        bankChoicePanel.add(cbBank);
        center.add(bankChoicePanel);
        center.add(Box.createVerticalStrut(10));

        // VA Display Card
        JPanel vaCard = new JPanel(new BorderLayout(0, 6));
        vaCard.setBackground(FIELD_BG);
        vaCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_DARK, 1),
            new EmptyBorder(12, 14, 12, 14)));

        JLabel lblBankName = new JLabel("BCA Virtual Account");
        lblBankName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblBankName.setForeground(BLUE);

        JLabel lblVaNumber = new JLabel("Memuat nomor VA...", SwingConstants.CENTER);
        lblVaNumber.setFont(new Font("Consolas", Font.BOLD, 16));
        lblVaNumber.setForeground(T1);

        JButton btnCopy = button("Salin", FIELD_BG, T1);
        btnCopy.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btnCopy.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(4, 8, 4, 8)));
        btnCopy.addActionListener(e -> {
            String vaText = lblVaNumber.getText().replaceAll("\\s+", "");
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new java.awt.datatransfer.StringSelection(vaText), null);
            btnCopy.setText("Tersalin!");
            Timer t = new Timer(2000, ev -> btnCopy.setText("Salin"));
            t.setRepeats(false);
            t.start();
        });

        JPanel vaNumBox = new JPanel(new BorderLayout(8, 0));
        vaNumBox.setOpaque(false);
        vaNumBox.add(lblVaNumber, BorderLayout.CENTER);
        vaNumBox.add(btnCopy, BorderLayout.EAST);

        vaCard.add(lblBankName, BorderLayout.NORTH);
        vaCard.add(vaNumBox, BorderLayout.CENTER);
        center.add(vaCard);
        center.add(Box.createVerticalStrut(10));

        // Detail Tagihan
        JPanel detail = new JPanel(new GridLayout(3, 2, 8, 4));
        detail.setOpaque(false);
        detail.add(createDetailLabel("No. Tiket", T2));
        detail.add(createDetailLabel(tiket.getNomorTiket(), T1));
        detail.add(createDetailLabel("Kendaraan", T2));
        detail.add(createDetailLabel(tiket.getKendaraan().getPlatNomor() + " (" + tiket.getKendaraan().getMerk() + ")", T1));
        detail.add(createDetailLabel("Total Tagihan", T2));
        JLabel lblTotal = new JLabel(rupiah.format(totalTagihan));
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTotal.setForeground(GREEN);
        detail.add(lblTotal);
        center.add(detail);
        center.add(Box.createVerticalStrut(8));

        JLabel lblStatusVA = new JLabel("Menghubungkan ke gateway Midtrans...", SwingConstants.CENTER);
        lblStatusVA.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        lblStatusVA.setForeground(T2);
        lblStatusVA.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(lblStatusVA);
        center.add(Box.createVerticalStrut(6));

        final String[] selectedBankName = new String[]{"BCA"};

        JPanel simRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        simRow.setOpaque(false);
        JButton btnOpenVASim = button("Buka Simulator Bank Midtrans", FIELD_BG, T1);
        btnOpenVASim.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btnOpenVASim.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(3, 8, 3, 8)));
        btnOpenVASim.addActionListener(e -> {
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    String b = selectedBankName[0].toLowerCase();
                    if ("permata".equals(b)) {
                        Desktop.getDesktop().browse(new URI("https://simulator.sandbox.midtrans.com/permata/va/index"));
                    } else if ("bri".equals(b)) {
                        Desktop.getDesktop().browse(new URI("https://simulator.sandbox.midtrans.com/bri/va/index"));
                    } else if ("bni".equals(b)) {
                        Desktop.getDesktop().browse(new URI("https://simulator.sandbox.midtrans.com/bni/va/index"));
                    } else if ("mandiri".equals(b)) {
                        Desktop.getDesktop().browse(new URI("https://simulator.sandbox.midtrans.com/mandiri/bill/index"));
                    } else {
                        Desktop.getDesktop().browse(new URI("https://simulator.sandbox.midtrans.com/bca/va/index"));
                    }
                }
            } catch (Exception ignored) {}
        });
        simRow.add(btnOpenVASim);
        center.add(simRow);

        root.add(center, BorderLayout.CENTER);

        final String[] currentVaOrderId = new String[]{""};

        // Polling Timer otomatis untuk mendeteksi transfer masuk
        Timer pollTimer = new Timer(1500, null);
        pollTimer.addActionListener(ev -> {
            if (currentVaOrderId[0].isEmpty()) return;
            String st = QRISService.checkTransactionStatus(currentVaOrderId[0]);
            if ("settlement".equalsIgnoreCase(st) || "capture".equalsIgnoreCase(st)) {
                pollTimer.stop();
                dlg.dispose();
                String paymentMethod = "VA_" + selectedBankName[0].toUpperCase();
                try {
                    boolean ok = gedung.prosesKeluar(tiket, jamKeluar, paymentMethod, totalTagihan);
                    if (ok) {
                        DatabaseManager.updatePelunasanTiket(tiket.getNomorTiket(), jamKeluar, totalTagihan, paymentMethod);
                        animGate(true, "TERBUKA KELUAR");
                        selectedSlot = null;
                        selectedTiket = null;
                        lblNotice.setText("<html><span style='color:#16a34a;'>Pembayaran Virtual Account Berhasil LUNAS! Portal terbuka.</span></html>");
                        lblTagihan.setText("Rp 0");
                        syncAll();
                        showReceiptDialog(tiket, totalTagihan);
                    }
                } catch (Exception ex) {
                    warn("Gagal menyelesaikan transaksi: " + ex.getMessage());
                }
            }
        });

        Runnable requestVA = () -> {
            String b = (String) cbBank.getSelectedItem();
            if (b == null) b = "BCA";
            selectedBankName[0] = b;
            lblBankName.setText(b + " Virtual Account");
            lblVaNumber.setText("Meminta nomor VA...");
            final String fBank = b.toLowerCase();

            aiExecutor.submit(() -> {
                QRISService.MidtransVAResult res = QRISService.createVirtualAccountTransaction(tiket.getNomorTiket(), totalTagihan, fBank);
                SwingUtilities.invokeLater(() -> {
                    currentVaOrderId[0] = res.orderId;
                    lblVaNumber.setText(res.vaNumber);
                    lblStatusVA.setText(res.statusMessage);
                    if (!pollTimer.isRunning()) pollTimer.start();
                });
            });
        };

        cbBank.addActionListener(e -> requestVA.run());
        requestVA.run();

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        actions.setOpaque(false);

        JButton btnSwitchQRIS = button("Beralih ke QRIS", FIELD_BG, BLUE);
        btnSwitchQRIS.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnSwitchQRIS.setToolTipText("Kembali ke pembayaran menggunakan QRIS");
        btnSwitchQRIS.addActionListener(e -> {
            pollTimer.stop();
            dlg.dispose();
            showQRISPaymentDialog(tiket, jamKeluar, totalTagihan);
        });

        JButton btnCancel = button("Tutup / Batal", FIELD_BG, T1);
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnCancel.addActionListener(e -> {
            pollTimer.stop();
            dlg.dispose();
        });
        actions.add(btnSwitchQRIS);
        actions.add(btnCancel);
        root.add(actions, BorderLayout.SOUTH);

        dlg.addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) {
                pollTimer.stop();
                isPaymentDialogShowing = false;
                startCCTVCooldownCountdown(4);
            }
        });

        dlg.add(root);
        dlg.pack();
        dlg.setSize(380, dlg.getPreferredSize().height);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void showReceiptDialog(KarcisParkir tiket, double uangBayar) {
        JDialog dlg = new JDialog(this, "Struk Kuitansi Parkir Digital", true);
        dlg.getContentPane().setBackground(CARD_BG);
        dlg.setLayout(new BorderLayout());

        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(16, 20, 16, 20)));

        Kendaraan k = tiket.getKendaraan();
        SlotParkir s = tiket.getSlot();
        int durasi = tiket.hitungDurasi(tiket.getJamKeluar());
        double kembalian = Math.max(0, uangBayar - tiket.getTotalBiaya());
        String diskonInfo = k.isMobilListrik() ? "\n [INSENTIF HIJAU: DISKON 20% DITERAPKAN]" : "";

        String receiptText = String.format(
            "=========================================\n" +
            "       MALL GRAND PENS PLAZA             \n" +
            "    Sistem Smart Parking & AI Gate       \n" +
            "=========================================\n" +
            " No. Tiket     : %s\n" +
            " Plat Nomor    : %s\n" +
            " Tipe Kendaraan: %s (%s)\n" +
            " Lokasi Slot   : %s (Lantai %d)\n" +
            " Waktu Masuk   : %02d:00\n" +
            " Waktu Keluar  : %02d:00\n" +
            " Durasi Parkir : %d Jam\n" +
            "-----------------------------------------\n" +
            " TOTAL TAGIHAN : %s%s\n" +
            " Metode Bayar  : %s\n" +
            " Uang Diterima : %s\n" +
            " Kembalian     : %s\n" +
            "-----------------------------------------\n" +
            " STATUS        : LUNAS (GATE TERBUKA)     \n" +
            " Terima kasih atas kunjungan Anda!       \n" +
            "=========================================\n",
            tiket.getNomorTiket(),
            k.getPlatNomor(),
            k.getTipe(), k.getMerk(),
            s.getIdSlot(), s.getLantai(),
            tiket.getJamMasuk(),
            tiket.getJamKeluar(),
            durasi,
            rupiah.format(tiket.getTotalBiaya()),
            diskonInfo,
            tiket.getMetodePembayaran(),
            rupiah.format(uangBayar),
            rupiah.format(kembalian)
        );

        JTextArea txt = new JTextArea(receiptText);
        txt.setEditable(false);
        txt.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txt.setBackground(FIELD_BG);
        txt.setForeground(T1);
        txt.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_DARK, 1),
            new EmptyBorder(10, 12, 10, 12)));

        p.add(txt, BorderLayout.CENTER);

        JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bot.setOpaque(false);
        JButton btnClose = button("Tutup & Selesai", BLUE, WHITE);
        btnClose.addActionListener(e -> dlg.dispose());
        bot.add(btnClose);
        p.add(bot, BorderLayout.SOUTH);

        dlg.add(p);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void executeVehicleSearch() {
        String q = txtSearchPlate.getText().trim();
        if (q.isEmpty() || q.startsWith("Ketik plat")) {
            lblSearchResult.setText("<html><span style='color:#4b5563;'>Ketik plat nomor atau merk kendaraan untuk mencari.</span></html>");
            return;
        }
        String cleanQ = q.replaceAll("\\s+", "").toUpperCase();
        SlotParkir found = null;
        for (SlotParkir s : gedung.getDaftarSlot()) {
            if (s.isTerisi() && s.getKendaraanParkir() != null) {
                Kendaraan k = s.getKendaraanParkir();
                String p = k.getPlatNomor().replaceAll("\\s+", "").toUpperCase();
                String m = k.getMerk().toUpperCase();
                if (p.contains(cleanQ) || m.contains(q.toUpperCase())) {
                    found = s;
                    break;
                }
            }
        }
        if (found != null) {
            searchedSlotId = found.getIdSlot();
            Kendaraan k = found.getKendaraanParkir();
            lblSearchResult.setText(String.format(
                "<html><b style='color:#16a34a;'>Ditemukan:</b> %s (%s) di <b>Slot %s (Lantai %d)</b></html>",
                k.getMerk(), k.getPlatNomor(), found.getIdSlot(), found.getLantai()));
            onSlotClick(found);
            renderBlueprint();
        } else {
            searchedSlotId = null;
            lblSearchResult.setText("<html><span style='color:#dc2626;'>Kendaraan \"" + q + "\" tidak ditemukan sedang parkir.</span></html>");
            renderBlueprint();
        }
    }

    private void onActiveVehicleComboSelected() {
        if (cbActiveVehicles == null || cbActiveVehicles.getSelectedIndex() <= 0) return;
        String sel = (String) cbActiveVehicles.getSelectedItem();
        if (sel == null) return;
        String noTiket = sel.split("\\|")[0].trim();
        KarcisParkir found = null;
        for (KarcisParkir t : gedung.getRiwayatTransaksi()) {
            if (t.getNomorTiket().equalsIgnoreCase(noTiket) && !t.isSudahDibayar()) {
                found = t;
                break;
            }
        }
        if (found != null) {
            selectedSlot = found.getSlot();
            selectedTiket = found;
            txtTiket.setText(found.getNomorTiket());
            Kendaraan k = found.getKendaraan();
            lblInfoKendaraan.setText(k.getPlatNomor() + "  " + k.getMerk()
                + "  |  Slot: " + found.getSlot().getIdSlot()
                + "  |  Masuk: " + found.getJamMasuk() + ":00");
            int jamOut = Math.max(java.time.LocalTime.now().getHour(), found.getJamMasuk() + 1);
            txtJamKeluar.setText(String.valueOf(jamOut));
            updateTagihan();
            renderBlueprint();
        }
    }

    private void refreshActiveVehiclesCombo() {
        if (cbActiveVehicles == null) return;
        ActionListener[] listeners = cbActiveVehicles.getActionListeners();
        for (ActionListener l : listeners) cbActiveVehicles.removeActionListener(l);

        cbActiveVehicles.removeAllItems();
        cbActiveVehicles.addItem("-- Pilih Kendaraan yang Sedang Parkir --");
        for (KarcisParkir t : gedung.getRiwayatTransaksi()) {
            if (!t.isSudahDibayar() && t.getKendaraan() != null) {
                cbActiveVehicles.addItem(String.format("%s | %s (%s) - Slot %s",
                    t.getNomorTiket(), t.getKendaraan().getPlatNomor(),
                    t.getKendaraan().getMerk(), t.getSlot().getIdSlot()));
            }
        }

        for (ActionListener l : listeners) cbActiveVehicles.addActionListener(l);
    }

    private void toggleGateMode() {
        isExitGateMode = !isExitGateMode;
        if (isExitGateMode) {
            btnGateModeToggle.setText("Mode: Keluar");
            btnGateModeToggle.setBackground(new Color(230, 233, 238));
            btnGateModeToggle.setForeground(T1);
            cctvHudText = "MEMANTAU GERBANG KELUAR...";
            lblCCTVStatus.setText("<html><span style='color:#4b5563;font-size:10px;'>Mode Gerbang KELUAR: Kamera memindai kendaraan yang hendak keluar.</span></html>");
        } else {
            btnGateModeToggle.setText("Mode: Masuk");
            btnGateModeToggle.setBackground(new Color(230, 233, 238));
            btnGateModeToggle.setForeground(T1);
            cctvHudText = "MEMANTAU GERBANG MASUK...";
            lblCCTVStatus.setText("<html><span style='color:#4b5563;font-size:10px;'>Mode Gerbang MASUK: Kamera memindai kendaraan masuk.</span></html>");
        }
        if (currentCCTVFrame != null) {
            drawCCTVScreen(currentCCTVFrame, false, cctvHudText);
        }
    }

    private void processExitAIDetection(String plat) {
        if (isPaymentDialogShowing) return;

        KarcisParkir aktif = gedung.cariTiketAktifDenganPlat(plat);
        if (aktif == null) {
            cctvVehicleDetected = false;
            cctvHudText = "KELUAR GAGAL: " + plat + " TIDAK DITEMUKAN";
            lblCCTVStatus.setText("<html><span style='color:#dc2626;font-size:10px;'>⚠️ Kendaraan plat <b>" + plat + "</b> tidak ditemukan sedang parkir di dalam gedung!</span></html>");
            lblNotice.setText("<html><span style='color:#dc2626;'>⚠️ Mobil plat <b>" + plat + "</b> tidak memiliki tiket parkir aktif.</span></html>");
            startCCTVCooldownCountdown(4);
            return;
        }

        selectedSlot = aktif.getSlot();
        selectedTiket = aktif;

        // Beralih ke tab Check-Out
        tabs.setSelectedIndex(0);
        txtTiket.setText(aktif.getNomorTiket());
        Kendaraan k = aktif.getKendaraan();
        lblInfoKendaraan.setText(k.getPlatNomor() + "  " + k.getMerk()
            + "  |  Slot: " + aktif.getSlot().getIdSlot()
            + "  |  Masuk: " + aktif.getJamMasuk() + ":00");

        int currentHour = java.time.LocalTime.now().getHour();
        int jamOut = Math.max(currentHour, aktif.getJamMasuk());
        txtJamKeluar.setText(String.valueOf(jamOut));
        updateTagihan();

        double total = hitungTotalTagihanTiket(aktif, jamOut);

        cctvVehicleDetected = false;
        cctvHudText = "KELUAR TERDETEKSI: " + plat + " (" + aktif.getNomorTiket() + ")";
        lblCCTVStatus.setText("<html><span style='color:#16a34a;font-size:10px;'><b>" + plat + "</b> di Gerbang Keluar. Tagihan siap dibayar.</span></html>");
        lblNotice.setText("<html><span style='color:#16a34a;'>Kendaraan <b>" + plat + "</b> siap check-out.</span></html>");
        startCCTVCooldownCountdown(8);

        // Munculkan popup QR Code pembayaran otomatis
        SwingUtilities.invokeLater(() -> showQRISPaymentDialog(aktif, jamOut, total));
    }

    private void updateTagihan() {
        try {
            String no = txtTiket.getText().trim();
            KarcisParkir t = null;
            for (KarcisParkir item : gedung.getRiwayatTransaksi())
                if (item.getNomorTiket().equalsIgnoreCase(no) && !item.isSudahDibayar()) { t = item; break; }
            if (t == null) { lblTagihan.setText("-"); return; }

            int durasi = t.hitungDurasi(Integer.parseInt(txtJamKeluar.getText().trim()));
            boolean motor = "MOTOR".equals(t.getKendaraan().getTipe());
            double p1 = motor ? gedung.getTarifJamPertamaMotor() : gedung.getTarifJamPertamaMobil();
            double pN = motor ? gedung.getTarifPerJamMotor()     : gedung.getTarifPerJamMobil();
            double total = p1 + (durasi > 1 ? (durasi - 1) * pN : 0);
            if (t.getKendaraan().isMobilListrik()) total *= 0.80;

            lblTagihan.setText(rupiah.format(total) + " (" + durasi + " jam)"
                + (t.getKendaraan().isMobilListrik() ? " [EV-20%]" : ""));
            txtUangBayar.setText(String.valueOf((long) total));
        } catch (Exception ignored) {}
    }

    private void updateAlokasiHint() {
        if (lblAlokasiHint == null || cbTipe == null) return;
        String t = (String) cbTipe.getSelectedItem();
        if ("MOTOR".equals(t))             { lblAlokasiHint.setText("Baris A (Zona Motor)");  lblAlokasiHint.setForeground(TEAL); }
        else if ("MOBIL_LISTRIK".equals(t)){ lblAlokasiHint.setText("EV1 / EV2 (Prioritas)"); lblAlokasiHint.setForeground(PURPLE); }
        else                               { lblAlokasiHint.setText("Baris B, C, atau D");    lblAlokasiHint.setForeground(BLUE); }
    }

    private void syncAll() {
        int total = gedung.getDaftarSlot().size(), terisi = 0;
        for (SlotParkir s : gedung.getDaftarSlot()) if (s.isTerisi()) terisi++;
        int tersedia = total - terisi;

        lblTersedia.setText(fmtPill("Tersedia", String.valueOf(tersedia), T1));
        lblTerisi.setText(fmtPill("Terisi", String.valueOf(terisi), RED));
        lblPendapatan.setText(fmtPill("Kas", rupiah.format(gedung.getTotalPendapatan()), T1));
        if (lblDBStatus != null) {
            lblDBStatus.setText(fmtPill("Database", dbConnected ? "Online" : "Offline", dbConnected ? T1 : RED));
        }
        if (lblQuotaLt1 != null) {
            lblQuotaLt1.setText(String.format("<html><span style='color:%s;font-size:10px;'>Lt. 1: </span><b style='color:%s;font-size:10px;'>%d Mobil</b></html>", hex(T2), hex(T1), gedung.getSisaKuotaLantai(1, "MOBIL")));
        }
        if (lblQuotaLt2 != null) {
            lblQuotaLt2.setText(String.format("<html><span style='color:%s;font-size:10px;'>Lt. 2: </span><b style='color:%s;font-size:10px;'>%d Mobil</b></html>", hex(T2), hex(T1), gedung.getSisaKuotaLantai(2, "MOBIL")));
        }
        if (lblQuotaEV != null) {
            lblQuotaEV.setText(String.format("<html><span style='color:%s;font-size:10px;'>EV: </span><b style='color:%s;font-size:10px;'>%d Slot</b></html>", hex(T2), hex(T1), gedung.getSisaKuota("MOBIL_LISTRIK")));
        }

        logModel.setRowCount(0);
        for (KarcisParkir t : gedung.getRiwayatTransaksi()) {
            String waktu  = t.isSudahDibayar()
                ? t.getJamMasuk() + ":00 - " + t.getJamKeluar() + ":00"
                : t.getJamMasuk() + ":00 Masuk";
            String status = t.isSudahDibayar() ? "LUNAS" : "PARKIR";
            String biaya  = t.isSudahDibayar() ? rupiah.format(t.getTotalBiaya()) : "-";
            String tipe   = "MOBIL_LISTRIK".equals(t.getKendaraan().getTipe()) ? "EV"
                          : "MOTOR".equals(t.getKendaraan().getTipe()) ? "Motor" : "Mobil";
            logModel.addRow(new Object[]{
                t.getNomorTiket(), t.getKendaraan().getPlatNomor(),
                tipe, t.getSlot().getIdSlot(),
                waktu, status, biaya
            });
        }

        renderBlueprint();
        refreshActiveVehiclesCombo();
        refreshTabelKendaraan(null);
    }

    private void animGate(boolean open, String label) {
        targetGate = open ? 70f : 0f;
        gateLabel  = label;
        gateColor  = open ? GREEN : RED;
        if (gateTimer != null && gateTimer.isRunning()) gateTimer.stop();
        gateTimer = new Timer(20, e -> {
            if (Math.abs(gateAngle - targetGate) > 1f) {
                gateAngle += (targetGate - gateAngle) * 0.2f;
                repaint();
            } else {
                gateAngle = targetGate; repaint();
                ((Timer) e.getSource()).stop();
            }
        });
        gateTimer.start();
        if (open) {
            Timer close = new Timer(3000, e -> {
                targetGate = 0f; gateLabel = "TERTUTUP"; gateColor = RED;
                if (gateTimer != null) gateTimer.start();
                ((Timer) e.getSource()).stop();
            });
            close.setRepeats(false); close.start();
        }
    }

    private SlotParkir findSlot(String id) {
        for (SlotParkir s : gedung.getDaftarSlot())
            if (s.getIdSlot().equalsIgnoreCase(id)) return s;
        return null;
    }

    private JLabel sectionTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(T1);
        l.setBorder(new EmptyBorder(0, 0, 4, 0));
        return l;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(T2);
        return l;
    }

    private JTextField field(String init) {
        JTextField tf = new JTextField(init);
        tf.setBackground(FIELD_BG);
        tf.setForeground(T1);
        tf.setCaretColor(T1);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_DARK, 1),
            new EmptyBorder(5, 8, 5, 8)));
        return tf;
    }

    private void styleCombo(JComboBox<String> cb) {
        cb.setBackground(FIELD_BG);
        cb.setForeground(T1);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        cb.setBorder(BorderFactory.createLineBorder(BORDER_DARK, 1));
        cb.setPreferredSize(new Dimension(cb.getPreferredSize().width, 28));
    }

    private JButton button(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        boolean isNeutral = (bg == CARD_BG || bg == FIELD_BG);
        Color actualBg = isNeutral ? new Color(238, 241, 245) : bg;
        Color actualFg = isNeutral ? T1 : fg;
        btn.setBackground(actualBg);
        btn.setForeground(actualFg);
        btn.setFocusPainted(false);
        Color bd = isNeutral ? BORDER_DARK : actualBg.darker();
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bd, 1),
            new EmptyBorder(6, 12, 6, 12)));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(actualBg.darker()); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(actualBg); }
        });
        return btn;
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Peringatan", JOptionPane.WARNING_MESSAGE);
    }

    private String hex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.put("TabbedPane.background", BG);
            UIManager.put("TabbedPane.foreground", T1);
            UIManager.put("TabbedPane.selected", CARD_BG);
            UIManager.put("TabbedPane.contentAreaColor", CARD_BG);
            UIManager.put("TabbedPane.borderHighlightColor", BORDER);
            UIManager.put("TabbedPane.darkShadow", BORDER);
            UIManager.put("TabbedPane.light", CARD_BG);
            UIManager.put("TabbedPane.focus", CARD_BG);
            UIManager.put("Panel.background", BG);
            UIManager.put("OptionPane.background", CARD_BG);
            UIManager.put("OptionPane.messageForeground", T1);
            UIManager.put("Table.background", Color.WHITE);
            UIManager.put("Table.foreground", T1);
            UIManager.put("Table.gridColor", BORDER);
            UIManager.put("TableHeader.background", new Color(235, 237, 241));
            UIManager.put("TableHeader.foreground", T1);
            UIManager.put("ScrollPane.background", CARD_BG);
            UIManager.put("Viewport.background", CARD_BG);
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new AdminGUI().setVisible(true));
    }
}

import model.AIVisionService;
import model.AIVisionService.DetectionResult;
import model.DatabaseManager;
import model.GedungParkir;
import model.KarcisParkir;
import model.Kendaraan;
import model.LiveCameraStreamer;
import model.QRISService;
import model.SlotParkir;

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
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class AdminGUI extends JFrame {

    // Palet Warna: Dark Workstation / Slate Terminal (Anti-Slop, Tenang & Berbobot)
    private static final Color BG        = new Color(11, 15, 25);      // #0b0f19 Deep Canvas
    private static final Color CARD_BG   = new Color(17, 24, 39);      // #111827 Surface Panel
    private static final Color FIELD_BG  = new Color(15, 23, 42);      // #0f172a Input Background
    private static final Color BORDER    = new Color(31, 41, 55);      // #1f2937 Quiet Border
    private static final Color BLUE      = new Color(37, 99, 235);     // #2563eb Primary Cobalt
    private static final Color BLUE_LT   = new Color(30, 58, 95);      // Container Blue
    private static final Color GREEN     = new Color(16, 185, 129);    // #10b981 Emerald
    private static final Color GREEN_LT  = new Color(6, 78, 59);       // Dark Emerald
    private static final Color RED       = new Color(239, 68, 68);     // #ef4444 Crimson
    private static final Color RED_LT    = new Color(69, 10, 10);      // Dark Crimson
    private static final Color PURPLE    = new Color(139, 92, 246);    // #8b5cf6 Violet
    private static final Color PURPLE_LT = new Color(46, 16, 101);     // Dark Violet
    private static final Color TEAL      = new Color(20, 184, 166);    // #14b8a6 Teal
    private static final Color TEAL_LT   = new Color(19, 78, 74);      // Dark Teal
    private static final Color AMBER     = new Color(245, 158, 11);    // #f59e0b Amber
    private static final Color T1        = new Color(249, 250, 251);   // #f9fafb Primary Text
    private static final Color T2        = new Color(156, 163, 175);   // #9ca3af Secondary Slate
    private static final Color T3        = new Color(107, 114, 128);   // #6b7280 Muted
    private static final Color AISLE     = new Color(15, 23, 42);      // Maneuver Lane
    private static final Color PILLAR    = new Color(55, 65, 81);      // Pillar
    private static final Color WHITE     = Color.WHITE;

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
                        lblDBStatus.setText(fmtPill("DATABASE", ok ? "MySQL ONLINE" : "OFFLINE", ok ? TEAL : RED));
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

        gedung.prosesMasuk(new Kendaraan("B 6789 MTR", "MOTOR",        "Honda Vario"), 9);
        KarcisParkir t2 = gedung.prosesMasuk(new Kendaraan("B 1234 ABC", "MOBIL",  "Toyota Avanza"), 8);
        gedung.prosesMasuk(new Kendaraan("L 9988 XYZ", "MOBIL",        "Honda HR-V"), 10);
        gedung.prosesMasuk(new Kendaraan("B 1010 EV",  "MOBIL_LISTRIK","Ioniq 5"), 9);

        if (t2 != null) { selectedSlot = t2.getSlot(); selectedTiket = t2; }
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
            new EmptyBorder(12, 20, 12, 20)));

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brand.setOpaque(false);

        JLabel icon = new JLabel("P", SwingConstants.CENTER);
        icon.setOpaque(true);
        icon.setBackground(BLUE);
        icon.setForeground(WHITE);
        icon.setFont(new Font("Segoe UI", Font.BOLD, 18));
        icon.setPreferredSize(new Dimension(36, 36));

        JLabel name = new JLabel(String.format(
            "<html><b style='font-size:15px;color:%s;'>SmartParkir</b>"
            + "<br/><span style='color:%s;font-size:11px;'>Grand PENS Plaza • 45 Slot • Live CCTV Gate</span></html>",
            hex(T1), hex(T2)));
        name.setForeground(T1);
        brand.add(icon);
        brand.add(name);

        JPanel pills = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pills.setOpaque(false);

        lblTersedia   = pill("TERSEDIA",  "-", GREEN);
        lblTerisi     = pill("TERISI",    "-", RED);
        lblPendapatan = pill("KAS MASUK", "-", AMBER);
        lblDBStatus   = pill("DATABASE",  "MySQL ONLINE", TEAL);

        JButton btnCameraHeader = button("Pengaturan AI", new Color(30, 41, 59), T1);
        btnCameraHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCameraHeader.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(51, 65, 85), 1),
            new EmptyBorder(7, 14, 7, 14)));
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
        lbl.setBackground(new Color(24, 32, 47));
        lbl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(45, 55, 75), 1),
            new EmptyBorder(6, 12, 6, 12)));
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
            "<html><center>"
            + "<span style='color:#94a3b8;font-size:10px;font-weight:600;'>%s</span><br/>"
            + "<b style='font-size:13px;color:%s;'>%s</b>"
            + "</center></html>",
            label, hex(c), val);
    }

    private JPanel buildBlueprint() {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(12, 14, 10, 14)));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel title = new JLabel(String.format(
            "<html><b style='color:%s;'>Denah Parkir Realtime (45 Slot)</b>"
            + " <span style='color:%s;font-weight:normal;font-size:10px;'>"
            + "- Klik kendaraan di denah untuk Check-Out</span></html>",
            hex(T1), hex(T2)));
        title.setFont(new Font("Segoe UI", Font.PLAIN, 13));
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
                g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(gateColor);
                g2.drawLine(cx, cy - 10, ex, ey);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                g2.setColor(gateColor);
                g2.drawString(gateLabel, cx + 36, getHeight() / 2 + 4);
                g2.dispose();
            }
        };
        gate.setPreferredSize(new Dimension(150, 28));
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

        // Pencarian Kendaraan (Find My Car)
        JPanel searchBar = new JPanel(new BorderLayout(6, 0));
        searchBar.setOpaque(false);
        searchBar.setBorder(new EmptyBorder(2, 0, 6, 0));

        txtSearchPlate = field("");
        txtSearchPlate.putClientProperty("JTextField.placeholderText", "Ketik plat atau merk (cth: Avanza / B 1234)...");
        txtSearchPlate.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        txtSearchPlate.addActionListener(e -> executeVehicleSearch());

        JButton btnSearch = button("🔍 Cari Mobil", BLUE, WHITE);
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btnSearch.setBorder(new EmptyBorder(5, 10, 5, 10));
        btnSearch.addActionListener(e -> executeVehicleSearch());

        JButton btnClearSearch = button("✕", CARD_BG, T2);
        btnClearSearch.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btnClearSearch.setBorder(new EmptyBorder(5, 8, 5, 8));
        btnClearSearch.setToolTipText("Reset Pencarian");
        btnClearSearch.addActionListener(e -> {
            txtSearchPlate.setText("");
            searchedSlotId = null;
            lblSearchResult.setText("");
            renderBlueprint();
        });

        JPanel searchBtnBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        searchBtnBox.setOpaque(false);
        searchBtnBox.add(btnSearch);
        searchBtnBox.add(btnClearSearch);

        searchBar.add(txtSearchPlate, BorderLayout.CENTER);
        searchBar.add(searchBtnBox, BorderLayout.EAST);

        lblSearchResult = new JLabel("");
        lblSearchResult.setFont(new Font("Segoe UI", Font.PLAIN, 10));

        JPanel searchBox = new JPanel(new BorderLayout(0, 3));
        searchBox.setOpaque(false);
        searchBox.add(searchBar, BorderLayout.NORTH);
        searchBox.add(lblSearchResult, BorderLayout.SOUTH);

        JPanel quotaBanner = new JPanel(new GridLayout(1, 4, 8, 0));
        quotaBanner.setOpaque(false);
        quotaBanner.setBorder(new EmptyBorder(0, 0, 6, 0));
        lblQuotaLt1 = new JLabel();
        lblQuotaLt2 = new JLabel();
        lblQuotaEV = new JLabel();
        JLabel lblManlessBadge = new JLabel(fmtPill("GATE", "MANLESS & QRIS", GREEN));
        quotaBanner.add(lblQuotaLt1);
        quotaBanner.add(lblQuotaLt2);
        quotaBanner.add(lblQuotaEV);
        quotaBanner.add(lblManlessBadge);

        JPanel topCenterBox = new JPanel(new BorderLayout(0, 4));
        topCenterBox.setOpaque(false);
        topCenterBox.add(quotaBanner, BorderLayout.NORTH);
        topCenterBox.add(searchBox, BorderLayout.SOUTH);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 4));
        centerPanel.setOpaque(false);
        centerPanel.add(topCenterBox, BorderLayout.NORTH);
        centerPanel.add(scroll, BorderLayout.CENTER);

        card.add(centerPanel, BorderLayout.CENTER);

        JPanel southBox = new JPanel(new BorderLayout(0, 4));
        southBox.setOpaque(false);

        JPanel leg = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 2));
        leg.setOpaque(false);
        leg.add(legend("Tersedia",    GREEN));
        leg.add(legend("Terisi",      RED));
        leg.add(legend("EV Charger",  PURPLE));
        leg.add(legend("Zona Motor",  TEAL));
        leg.add(legend("Zona Mobil",  BLUE));
        southBox.add(leg, BorderLayout.NORTH);

        southBox.add(buildTariffBar(), BorderLayout.SOUTH);
        card.add(southBox, BorderLayout.SOUTH);

        return card;
    }

    private JPanel buildTariffBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 2));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(4, 0, 0, 0));

        JLabel mtr = new JLabel("Motor: Rp 2.000 (+Rp 1.000/jam)");
        mtr.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        mtr.setForeground(T2);

        JLabel mbl = new JLabel("Mobil: Rp 5.000 (+Rp 3.000/jam)");
        mbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        mbl.setForeground(T2);

        JLabel ev = new JLabel("Mobil Listrik: Diskon 20% (EV Friendly)");
        ev.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        ev.setForeground(PURPLE);

        bar.add(mtr);
        bar.add(createNavDivider());
        bar.add(mbl);
        bar.add(createNavDivider());
        bar.add(ev);
        return bar;
    }

    private void renderBlueprint() {
        blueprintContainer.removeAll();

        addRow("A  MOTOR (9 slot)", TEAL,
            new String[]{"A01","A02","A03","A04","A05","A06","A07","A08","A09"},
            false);

        blueprintContainer.add(aisle("LAJUR MANUVER 1"));

        addRow("B  MOBIL (11 slot + tangga)", BLUE,
            new String[]{"B01","B02","B03","B04","TANGGA","B06","B07","B08","B09","B10","B11","B12"},
            true);

        addRow("C  MOBIL (11 slot + tangga)", BLUE,
            new String[]{"C01","C02","C03","C04","TANGGA","C06","C07","C08","C09","C10","C11","C12"},
            true);

        blueprintContainer.add(aisle("LAJUR MANUVER 2"));

        addRow("D  MOBIL (12 slot) + EV (2 slot)", T2,
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

        Color bgColor = isHighlight ? new Color(60, 40, 10)
            : isFull ? new Color(38, 16, 20)
            : isEV ? new Color(28, 20, 50)
            : new Color(18, 26, 40);

        Color bdColor = isHighlight ? AMBER
            : isSel ? BLUE
            : isFull ? new Color(153, 27, 27)
            : isEV ? new Color(88, 28, 135)
            : new Color(40, 52, 70);

        JPanel bay = new JPanel(new BorderLayout(0, 2)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.setColor(bdColor);
                g2.setStroke(new BasicStroke(isHighlight ? 2.5f : isSel ? 2f : 1f));
                int inset = isHighlight ? 2 : isSel ? 1 : 0;
                g2.drawRoundRect(inset, inset, getWidth()-1-inset, getHeight()-1-inset, 4, 4);
                g2.dispose();
            }
        };
        bay.setPreferredSize(new Dimension(62, 70));
        bay.setOpaque(false);
        bay.setBorder(new EmptyBorder(3, 3, 3, 3));
        bay.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel id = new JLabel(slot.getIdSlot(), SwingConstants.CENTER);
        id.setFont(new Font("Segoe UI", Font.BOLD, 9));
        id.setForeground(isEV ? PURPLE : isMotor ? TEAL : T1);
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
            JLabel p = new JLabel("<html><center style='color:#fca5a5;font-size:8px;font-weight:bold;line-height:95%;'>" + formattedPlat + "</center></html>",
                SwingConstants.CENTER);
            bay.add(p, BorderLayout.CENTER);
        } else {
            JLabel empty = new JLabel("<html><center style='color:#64748b;font-size:7px;'>TERSEDIA</center></html>", SwingConstants.CENTER);
            bay.add(empty, BorderLayout.CENTER);
        }

        Color typeCol = isEV ? PURPLE : isMotor ? TEAL : T3;
        String typeStr = isEV ? "EV" : isMotor ? "MTR" : "MBL";
        JLabel type = new JLabel(typeStr, SwingConstants.CENTER);
        type.setFont(new Font("Segoe UI", Font.PLAIN, 7));
        type.setForeground(typeCol);
        bay.add(type, BorderLayout.SOUTH);

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
                lblNotice.setText("<html><span style='color:#34d399;'>Kendaraan dipilih. Periksa tagihan lalu klik Bayar.</span></html>");
            }
        } else {
            tabs.setSelectedIndex(1);
            if ("MOTOR".equals(slot.getTipeSlot())) cbTipe.setSelectedItem("MOTOR");
            else if ("MOBIL_LISTRIK".equals(slot.getTipeSlot())) cbTipe.setSelectedItem("MOBIL_LISTRIK");
            else cbTipe.setSelectedItem("MOBIL");
            lblNotice.setText("<html><span style='color:#94a3b8;'>Slot <b>" + slot.getIdSlot() + "</b> kosong - isi form Check-In.</span></html>");
        }
        renderBlueprint();
    }

    private Component aisle(String label) {
        JPanel lane = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(AISLE); g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(BORDER);
                g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    10f, new float[]{6f, 5f}, 0f));
                g2.drawLine(10, getHeight()/2, getWidth()-10, getHeight()/2);
                g2.dispose();
            }
        };
        lane.setPreferredSize(new Dimension(800, 18));
        JLabel lbl = new JLabel(label, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        lbl.setForeground(T3);
        lane.add(lbl, BorderLayout.CENTER);
        return lane;
    }

    private JPanel stairBlock() {
        JPanel p = new JPanel(new BorderLayout());
        p.setPreferredSize(new Dimension(50, 72));
        p.setBackground(AISLE);
        p.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        JLabel l = new JLabel("<html><center style='font-size:8px;color:#64748b;'>TANGGA<br/>LIFT</center></html>",
            SwingConstants.CENTER);
        l.setForeground(T3);
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
        dot.setPreferredSize(new Dimension(8, 8));
        dot.setBackground(col);
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 10));
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
        JLabel title = sectionTitle("CCTV Gerbang (Live AI)");
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

        lblCCTVStatus = new JLabel("<html><span style='color:#94a3b8;font-size:10px;'>Sensor aktif: Memantau gerbang masuk...</span></html>");
        bot.add(lblCCTVStatus, BorderLayout.NORTH);

        JPanel actions = new JPanel(new GridLayout(1, 4, 4, 0));
        actions.setOpaque(false);

        btnGateModeToggle = button(isExitGateMode ? "Mode: Keluar" : "Mode: Masuk", CARD_BG, T1);
        btnGateModeToggle.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btnGateModeToggle.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(5, 4, 5, 4)));
        btnGateModeToggle.setToolTipText("Beralih antara Gerbang Masuk & Keluar");
        btnGateModeToggle.addActionListener(e -> toggleGateMode());

        JButton btnSimulate = button("Uji Sampel", CARD_BG, T1);
        btnSimulate.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btnSimulate.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(5, 4, 5, 4)));
        btnSimulate.setToolTipText("Uji pemindaian dengan file sample_car.jpg");
        btnSimulate.addActionListener(e -> simulateCarArrival());

        btnCCTVToggle = button("Jeda Video", CARD_BG, T1);
        btnCCTVToggle.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btnCCTVToggle.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(5, 4, 5, 4)));
        btnCCTVToggle.addActionListener(e -> toggleCCTVStreaming());

        JButton btnSettings = button("Layar Penuh", CARD_BG, T1);
        btnSettings.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btnSettings.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(5, 4, 5, 4)));
        btnSettings.addActionListener(e -> openCameraScannerDialog());

        actions.add(btnGateModeToggle);
        actions.add(btnSimulate);
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
                    lblCCTVStatus.setText("<html><span style='color:" + (isError ? "#ef4444" : "#38bdf8") + ";font-size:10px;'>" + status + "</span></html>");
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
            lblCCTVStatus.setText("<html><span style='color:#38bdf8;font-size:10px;'>Memindai plat nomor kendaraan...</span></html>");
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
                    lblCCTVStatus.setText("<html><span style='color:#ef4444;font-size:10px;'>Error AI: " + ex.getMessage() + "</span></html>");
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
            lblCCTVStatus.setText("<html><span style='color:#94a3b8;font-size:10px;'>👁️ Kamera aktif memantau " + (isExitGateMode ? "gerbang keluar..." : "gerbang masuk...") + "</span></html>");
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
            lblCCTVStatus.setText("<html><span style='color:#22c55e;font-size:10px;'>✅ <b>" + plat + "</b> (" + merk + ") Masuk -> Slot <b>" + tiket.getSlot().getIdSlot() + "</b> (Gate Terbuka)</span></html>");
            lblNotice.setText("<html><span style='color:#22c55e;'>✅ Mobil <b>" + plat + "</b> otomatis ter-check-in ke Slot <b>" + tiket.getSlot().getIdSlot() + "</b>. Tiket: <b>" + tiket.getNomorTiket() + "</b></span></html>");

            // Sinkronisasi denah, log table, pill statistik
            syncAll();

            // Isi form check-in juga untuk display
            txtPlat.setText(plat);
            txtMerk.setText(merk);
            cbTipe.setSelectedItem(tipe);
            updateAlokasiHint();

            // Hentikan kamera beberapa saat (countdown visual 8 detik anti-duplikasi)
            startCCTVCooldownCountdown(8);
        } else {
            cctvVehicleDetected = false;
            cctvHudText = "PARKIR PENUH UNTUK " + tipe;
            lblCCTVStatus.setText("<html><span style='color:#ef4444;font-size:10px;'>Kapasitas slot untuk " + tipe + " sudah penuh!</span></html>");
            startCCTVCooldownCountdown(5);
        }
    }

    private void startCCTVCooldownCountdown(int seconds) {
        cctvCooldownUntil = System.currentTimeMillis() + (seconds * 1000L);
        Timer timer = new Timer(1000, null);
        final int[] remaining = new int[]{seconds};
        timer.addActionListener(e -> {
            remaining[0]--;
            if (remaining[0] > 0) {
                lblCCTVLiveBadge.setText("⏸ JEDA SENSOR (" + remaining[0] + "s)");
                lblCCTVLiveBadge.setForeground(AMBER);
            } else {
                timer.stop();
                lblCCTVLiveBadge.setText("🔴 STREAMING AKTIF");
                lblCCTVLiveBadge.setForeground(GREEN);
                cctvHudText = "MEMANTAU GERBANG MASUK...";
                lblCCTVStatus.setText("<html><span style='color:#38bdf8;font-size:10px;'>🟢 Sensor aktif: Menunggu kendaraan di gerbang masuk...</span></html>");
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
            btnCCTVToggle.setText("▶ Mulai Video");
            lblCCTVLiveBadge.setText("⏹ VIDEO PAUSED");
            lblCCTVLiveBadge.setForeground(T3);
        } else {
            startDashboardCCTV();
            btnCCTVToggle.setText("⏹ Jeda Video");
            lblCCTVLiveBadge.setText("🔴 STREAMING AKTIF");
            lblCCTVLiveBadge.setForeground(GREEN);
        }
    }

    private JPanel buildOperationCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        card.setPreferredSize(new Dimension(440, 290));

        lblNotice = new JLabel("<html><span style='color:#94a3b8;'>Klik kendaraan di denah untuk check-out, atau gunakan tab Check-In.</span></html>");
        lblNotice.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblNotice.setForeground(T2);
        lblNotice.setBorder(new EmptyBorder(6, 12, 6, 12));
        card.add(lblNotice, BorderLayout.NORTH);

        tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 11));
        tabs.setBackground(CARD_BG);
        tabs.setForeground(T1);
        tabs.addTab("Check-Out / Kasir", buildCheckOutPanel());
        tabs.addTab("Check-In / Masuk",  buildCheckInPanel());
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

        lblInfoKendaraan = new JLabel("<html><i style='color:#94a3b8;'>Belum ada kendaraan dipilih.</i></html>");
        lblInfoKendaraan.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblInfoKendaraan.setOpaque(true);
        lblInfoKendaraan.setBackground(FIELD_BG);
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
        JPanel form = new JPanel(new GridLayout(4, 2, 8, 5));
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
        JButton btnNow = button("⏱️ Jam Sekarang", CARD_BG, T1);
        btnNow.setFont(new Font("Segoe UI", Font.BOLD, 9));
        btnNow.setBorder(new EmptyBorder(3, 6, 3, 6));
        btnNow.addActionListener(e -> {
            txtJamKeluar.setText(String.valueOf(java.time.LocalTime.now().getHour()));
            updateTagihan();
        });
        jamBox.add(txtJamKeluar, BorderLayout.CENTER);
        jamBox.add(btnNow, BorderLayout.EAST);
        form.add(jamBox);

        form.add(fieldLabel("Metode Bayar"));
        cbMetode = new JComboBox<>(new String[]{"E_MONEY", "QRIS", "CASH"});
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
        tagihanBar.setBackground(FIELD_BG);
        tagihanBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(6, 10, 6, 10)));
        JLabel tl = new JLabel("Total Tagihan:");
        tl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tl.setForeground(T2);
        lblTagihan = new JLabel("Rp 0");
        lblTagihan.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTagihan.setForeground(T1);
        tagihanBar.add(tl, BorderLayout.WEST);
        tagihanBar.add(lblTagihan, BorderLayout.EAST);
        bot.add(tagihanBar, BorderLayout.NORTH);

        JPanel actionBox = new JPanel(new GridLayout(2, 1, 0, 4));
        actionBox.setOpaque(false);

        JPanel instantRow = new JPanel(new GridLayout(1, 3, 4, 0));
        instantRow.setOpaque(false);

        JButton btnQuickEMoney = button("E-Money", CARD_BG, T1);
        btnQuickEMoney.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btnQuickEMoney.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(5, 3, 5, 3)));
        btnQuickEMoney.setToolTipText("Bayar lunas otomatis via Tap E-Money & buka gerbang");
        btnQuickEMoney.addActionListener(e -> quickPay("E_MONEY"));

        JButton btnQuickQRIS = button("QRIS", CARD_BG, T1);
        btnQuickQRIS.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btnQuickQRIS.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(5, 3, 5, 3)));
        btnQuickQRIS.setToolTipText("Bayar lunas otomatis via scan QRIS & buka gerbang");
        btnQuickQRIS.addActionListener(e -> quickPay("QRIS"));

        JButton btnQuickCashPas = button("Tunai Pas", CARD_BG, T1);
        btnQuickCashPas.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btnQuickCashPas.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(5, 3, 5, 3)));
        btnQuickCashPas.setToolTipText("Bayar uang pas tunai & buka gerbang");
        btnQuickCashPas.addActionListener(e -> quickPay("CASH"));

        instantRow.add(btnQuickEMoney);
        instantRow.add(btnQuickQRIS);
        instantRow.add(btnQuickCashPas);

        JButton btnBayarManual = button("Konfirmasi & Buka Portal", BLUE, WHITE);
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

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 6));
        form.setOpaque(false);

        form.add(fieldLabel("Nomor Plat"));     txtPlat = field("B 1234 XYZ"); form.add(txtPlat);
        form.add(fieldLabel("Tipe Kendaraan"));
        cbTipe = new JComboBox<>(new String[]{"MOBIL", "MOTOR", "MOBIL_LISTRIK"});
        styleCombo(cbTipe);
        cbTipe.addActionListener(e -> updateAlokasiHint());
        form.add(cbTipe);

        form.add(fieldLabel("Merk / Model"));  txtMerk = field("Toyota Avanza"); form.add(txtMerk);
        form.add(fieldLabel("Jam Masuk (0-23)")); txtJamMasuk = field("10"); form.add(txtJamMasuk);

        form.add(fieldLabel("Alokasi Sistem"));
        lblAlokasiHint = new JLabel("Baris B, C, atau D");
        lblAlokasiHint.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblAlokasiHint.setForeground(T1);
        form.add(lblAlokasiHint);

        p.add(form, BorderLayout.CENTER);

        JPanel botButtons = new JPanel(new GridLayout(1, 3, 6, 0));
        botButtons.setOpaque(false);

        JButton btnScanCam = button("Scan Kamera", CARD_BG, T1);
        btnScanCam.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btnScanCam.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(6, 4, 6, 4)));
        btnScanCam.addActionListener(e -> openCameraScannerDialog());

        JButton btnEmergency = button("🎫 Tiket Darurat QR", CARD_BG, AMBER);
        btnEmergency.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btnEmergency.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(6, 4, 6, 4)));
        btnEmergency.setToolTipText("Cetak karcis darurat jika plat nomor kotor/rusak");
        btnEmergency.addActionListener(e -> handleEmergencyQRCheckIn());

        JButton btnMasuk = button("Check-In ANPR", BLUE, WHITE);
        btnMasuk.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btnMasuk.setBorder(new EmptyBorder(6, 4, 6, 4));
        btnMasuk.addActionListener(e -> handleCheckIn());

        botButtons.add(btnScanCam);
        botButtons.add(btnEmergency);
        botButtons.add(btnMasuk);
        p.add(botButtons, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildLogCard() {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(8, 12, 10, 12)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        card.setPreferredSize(new Dimension(440, 190));

        card.add(sectionTitle("Riwayat Transaksi"), BorderLayout.NORTH);

        String[] cols = {"Tiket", "Plat", "Tipe", "Slot", "Waktu", "Status", "Biaya"};
        logModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tbl = new JTable(logModel);
        tbl.setBackground(FIELD_BG);
        tbl.setForeground(T1);
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tbl.setRowHeight(22);
        tbl.setShowGrid(false);
        tbl.setSelectionBackground(BLUE_LT);
        tbl.setSelectionForeground(WHITE);
        tbl.getTableHeader().setBackground(CARD_BG);
        tbl.getTableHeader().setForeground(T2);
        tbl.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 9));
        tbl.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));

        DefaultTableCellRenderer ctr = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(FIELD_BG);
                    c.setForeground(T1);
                } else {
                    c.setBackground(BLUE_LT);
                    c.setForeground(WHITE);
                }
                return c;
            }
        };
        ctr.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tbl.getColumnCount(); i++)
            tbl.getColumnModel().getColumn(i).setCellRenderer(ctr);

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
        dlg.getContentPane().setBackground(BG);
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
        JLabel hint = new JLabel("<html><span style='color:#94a3b8;font-size:10px;'>Kredensial disimpan aman di <b>ai_config.properties</b> (terproteksi .gitignore).</span></html>");
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
            } else {
                warn("Kapasitas untuk tiket darurat " + tipe + " sudah penuh.");
            }
        } catch (Exception ex) {
            warn("Input tidak valid: " + ex.getMessage());
        }
    }

    private void showEmergencyTicketDialog(KarcisParkir tiket) {
        JDialog dlg = new JDialog(this, "Karcis Darurat Masuk (Fallback QR)", true);
        dlg.getContentPane().setBackground(BG);
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

        JButton btnClose = button("Tutup & Buka Palang", AMBER, Color.BLACK);
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

            // Cek duplikasi di gedung
            SlotParkir sudahParkir = gedung.cariSlotDenganPlat(plat);
            if (sudahParkir != null) {
                warn("Kendaraan plat " + plat + " sudah terparkir di slot " + sudahParkir.getIdSlot() + "!\nDuplikasi dicegah.");
                return;
            }

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
            } else {
                warn("Kapasitas untuk " + tipe + " sudah penuh.");
            }
        } catch (Exception ex) {
            warn("Input tidak valid: " + ex.getMessage());
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

            boolean ok = gedung.prosesKeluar(found, jamKeluar, metode, uang);
            if (ok) {
                animGate(true, "TERBUKA KELUAR");
                selectedSlot  = null;
                selectedTiket = null;
                lblNotice.setText("<html><span style='color:#34d399;'>Check-out berhasil. Slot kosong kembali.</span></html>");
                lblTagihan.setText("Rp 0");
                syncAll();
                showReceiptDialog(found, uang);
            } else {
                warn("Pembayaran kurang dari tagihan.");
            }
        } catch (Exception ex) {
            warn("Input tidak valid: " + ex.getMessage());
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

            boolean ok = gedung.prosesKeluar(found, jamKeluar, metode, total);
            if (ok) {
                animGate(true, "TERBUKA KELUAR");
                selectedSlot  = null;
                selectedTiket = null;
                lblNotice.setText("<html><span style='color:#34d399;'>Check-out sukses dengan " + metode + ". Gate terbuka!</span></html>");
                lblTagihan.setText("Rp 0");
                syncAll();
                showReceiptDialog(found, total);
            } else {
                warn("Gagal memproses check-out keluar.");
            }
        } catch (Exception ex) {
            warn("Gagal proses keluar: " + ex.getMessage());
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
        dlg.getContentPane().setBackground(BG);
        dlg.setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(CARD_BG);
        root.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(18, 22, 18, 22)));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("<html><b style='font-size:14px;color:#f9fafb;'>Pembayaran QRIS Gate Keluar (Midtrans Sandbox)</b><br/>"
            + "<span style='color:#9ca3af;font-size:10px;'>Pindai QR Code via GoPay, BCA, Livin, OVO, atau e-Wallet lainnya</span></html>");
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

        // QR Code Graphic Panel
        JPanel qrPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                int ox = (getWidth() - finalQrImg.getWidth()) / 2;
                int oy = (getHeight() - finalQrImg.getHeight()) / 2;
                g2.drawImage(finalQrImg, ox, oy, null);
                g2.dispose();
            }
        };
        qrPanel.setPreferredSize(new Dimension(240, 220));
        qrPanel.setMaximumSize(new Dimension(240, 220));
        qrPanel.setOpaque(false);
        center.add(qrPanel);
        center.add(Box.createVerticalStrut(8));

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
                boolean ok = gedung.prosesKeluar(tiket, jamKeluar, "QRIS_MIDTRANS", totalTagihan);
                if (ok) {
                    DatabaseManager.updatePelunasanTiket(tiket.getNomorTiket(), jamKeluar, totalTagihan, "QRIS_MIDTRANS");
                    animGate(true, "TERBUKA KELUAR");
                    selectedSlot = null;
                    selectedTiket = null;
                    lblNotice.setText("<html><span style='color:#34d399;'>Pembayaran QRIS Midtrans Terkonfirmasi LUNAS! Portal terbuka.</span></html>");
                    lblTagihan.setText("Rp 0");
                    syncAll();
                    showReceiptDialog(tiket, totalTagihan);
                }
            }
        });
        pollTimer.start();

        JPanel actions = new JPanel(new GridLayout(1, 2, 8, 0));
        actions.setOpaque(false);

        JButton btnCancel = button("Batal / Bayar Tunai", FIELD_BG, T2);
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnCancel.addActionListener(e -> {
            pollTimer.stop();
            dlg.dispose();
        });

        JButton btnConfirm = button("⚡ Simulasi Bayar QRIS", BLUE, WHITE);
        btnConfirm.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnConfirm.setToolTipText("Simulasikan pengguna membayar lewat e-Wallet");
        btnConfirm.addActionListener(e -> {
            QRISService.simulasiBayar(tiket.getNomorTiket());
        });

        actions.add(btnCancel);
        actions.add(btnConfirm);
        root.add(actions, BorderLayout.SOUTH);

        dlg.addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) {
                pollTimer.stop();
                isPaymentDialogShowing = false;
                startCCTVCooldownCountdown(6);
            }
        });

        dlg.add(root);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void showReceiptDialog(KarcisParkir tiket, double uangBayar) {
        JDialog dlg = new JDialog(this, "Struk Kuitansi Parkir Digital", true);
        dlg.getContentPane().setBackground(BG);
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
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(10, 12, 10, 12)));

        p.add(txt, BorderLayout.CENTER);

        JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bot.setOpaque(false);
        JButton btnClose = button("Tutup & Siap Kendaraan Berikutnya", GREEN, WHITE);
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnClose.addActionListener(e -> dlg.dispose());
        bot.add(btnClose);
        p.add(bot, BorderLayout.SOUTH);

        dlg.add(p, BorderLayout.CENTER);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void executeVehicleSearch() {
        String q = txtSearchPlate.getText().trim();
        if (q.isEmpty() || q.startsWith("Ketik plat")) {
            lblSearchResult.setText("<html><span style='color:#94a3b8;'>Ketik plat nomor atau merk kendaraan untuk mencari.</span></html>");
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
                "<html><b style='color:#f59e0b;'>🎯 Ditemukan:</b> %s (%s) di <b style='color:#38bdf8;'>Slot %s (Lantai %d)</b></html>",
                k.getMerk(), k.getPlatNomor(), found.getIdSlot(), found.getLantai()));
            onSlotClick(found);
            renderBlueprint();
        } else {
            searchedSlotId = null;
            lblSearchResult.setText("<html><span style='color:#ef4444;'>❌ Kendaraan \"" + q + "\" tidak ditemukan sedang parkir.</span></html>");
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
            btnGateModeToggle.setText("🏁 KELUAR");
            btnGateModeToggle.setBackground(AMBER);
            cctvHudText = "MEMANTAU GERBANG KELUAR...";
            lblCCTVStatus.setText("<html><span style='color:#f59e0b;font-size:10px;'>🏁 <b>Mode Gerbang KELUAR:</b> Kamera memindai kendaraan yang hendak keluar.</span></html>");
        } else {
            btnGateModeToggle.setText("🚪 MASUK");
            btnGateModeToggle.setBackground(BLUE);
            cctvHudText = "MEMANTAU GERBANG MASUK...";
            lblCCTVStatus.setText("<html><span style='color:#38bdf8;font-size:10px;'>🚪 <b>Mode Gerbang MASUK:</b> Kamera memindai kendaraan masuk.</span></html>");
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
            lblCCTVStatus.setText("<html><span style='color:#ef4444;font-size:10px;'>⚠️ Kendaraan plat <b>" + plat + "</b> tidak ditemukan sedang parkir di dalam gedung!</span></html>");
            lblNotice.setText("<html><span style='color:#ef4444;'>⚠️ Mobil plat <b>" + plat + "</b> tidak memiliki tiket parkir aktif.</span></html>");
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
        lblCCTVStatus.setText("<html><span style='color:#f59e0b;font-size:10px;'>🏁 <b>" + plat + "</b> di Gerbang Keluar! Tagihan siap dibayar via QRIS / E-Money.</span></html>");
        lblNotice.setText("<html><span style='color:#f59e0b;'>🏁 Kendaraan <b>" + plat + "</b> siap check-out. QR Code pembayaran telah dibuka.</span></html>");
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
        String t = (String) cbTipe.getSelectedItem();
        if ("MOTOR".equals(t))             { lblAlokasiHint.setText("Baris A (Zona Motor)");  lblAlokasiHint.setForeground(TEAL); }
        else if ("MOBIL_LISTRIK".equals(t)){ lblAlokasiHint.setText("EV1 / EV2 (Prioritas)"); lblAlokasiHint.setForeground(PURPLE); }
        else                               { lblAlokasiHint.setText("Baris B, C, atau D");    lblAlokasiHint.setForeground(BLUE); }
    }

    private void syncAll() {
        int total = gedung.getDaftarSlot().size(), terisi = 0;
        for (SlotParkir s : gedung.getDaftarSlot()) if (s.isTerisi()) terisi++;
        int tersedia = total - terisi;

        lblTersedia.setText(fmtPill("TERSEDIA", String.valueOf(tersedia), GREEN));
        lblTerisi.setText(fmtPill("TERISI", String.valueOf(terisi), RED));
        lblPendapatan.setText(fmtPill("KAS MASUK", rupiah.format(gedung.getTotalPendapatan()), AMBER));
        if (lblDBStatus != null) {
            lblDBStatus.setText(fmtPill("DATABASE", dbConnected ? "MySQL ONLINE" : "OFFLINE", dbConnected ? TEAL : RED));
        }
        if (lblQuotaLt1 != null) {
            lblQuotaLt1.setText(fmtPill("LANTAI 1", gedung.getSisaKuotaLantai(1, "MOBIL") + " Mobil", BLUE));
        }
        if (lblQuotaLt2 != null) {
            lblQuotaLt2.setText(fmtPill("LANTAI 2", gedung.getSisaKuotaLantai(2, "MOBIL") + " Mobil", TEAL));
        }
        if (lblQuotaEV != null) {
            lblQuotaEV.setText(fmtPill("EV CHARGER", gedung.getSisaKuota("MOBIL_LISTRIK") + " Slot", PURPLE));
        }

        logModel.setRowCount(0);
        for (KarcisParkir t : gedung.getRiwayatTransaksi()) {
            String waktu  = t.getJamMasuk() + ":00 - " + (t.isSudahDibayar() ? t.getJamKeluar() + ":00" : "...");
            String status = t.isSudahDibayar() ? "LUNAS (" + t.getMetodePembayaran() + ")" : "PARKIR";
            String biaya  = t.isSudahDibayar() ? rupiah.format(t.getTotalBiaya()) : "-";
            logModel.addRow(new Object[]{
                t.getNomorTiket(), t.getKendaraan().getPlatNomor(),
                t.getKendaraan().getTipe(), t.getSlot().getIdSlot(),
                waktu, status, biaya
            });
        }

        renderBlueprint();
        refreshActiveVehiclesCombo();
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
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(T1);
        l.setBorder(new EmptyBorder(0, 0, 4, 0));
        return l;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
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
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(4, 7, 4, 7)));
        return tf;
    }

    private void styleCombo(JComboBox<String> cb) {
        cb.setBackground(FIELD_BG);
        cb.setForeground(T1);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 11));
    }

    private JButton button(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        Color orig = bg;
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(bg.darker()); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(orig); }
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
            UIManager.put("TabbedPane.background", CARD_BG);
            UIManager.put("TabbedPane.foreground", T1);
            UIManager.put("TabbedPane.selected", FIELD_BG);
            UIManager.put("TabbedPane.contentAreaColor", CARD_BG);
            UIManager.put("TabbedPane.borderHighlightColor", BORDER);
            UIManager.put("TabbedPane.darkShadow", BORDER);
            UIManager.put("TabbedPane.light", CARD_BG);
            UIManager.put("TabbedPane.focus", CARD_BG);
            UIManager.put("Panel.background", BG);
            UIManager.put("OptionPane.background", CARD_BG);
            UIManager.put("OptionPane.messageForeground", T1);
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new AdminGUI().setVisible(true));
    }
}

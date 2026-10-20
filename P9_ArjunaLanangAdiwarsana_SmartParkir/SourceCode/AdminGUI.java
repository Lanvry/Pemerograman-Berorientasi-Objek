import model.AIVisionService;
import model.AIVisionService.DetectionResult;
import model.GedungParkir;
import model.KarcisParkir;
import model.Kendaraan;
import model.LiveCameraStreamer;
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

public class AdminGUI extends JFrame {

    // Palet Warna: Dark Slate / Charcoal Modern (Elegan, tidak menyilaukan mata)
    private static final Color BG        = new Color(15, 23, 42);      // #0f172a Deep Background
    private static final Color CARD_BG   = new Color(30, 41, 59);      // #1e293b Slate Card
    private static final Color FIELD_BG  = new Color(15, 23, 42);      // #0f172a Inner / Input BG
    private static final Color BORDER    = new Color(51, 65, 85);      // #334155 Slate Border
    private static final Color BLUE      = new Color(59, 130, 246);    // #3b82f6 Accent Blue
    private static final Color BLUE_LT   = new Color(30, 58, 95);      // #1e3a5f Dark Blue Container
    private static final Color GREEN     = new Color(34, 197, 94);     // #22c55e Emerald
    private static final Color GREEN_LT  = new Color(16, 44, 33);      // Dark Emerald Surface
    private static final Color RED       = new Color(239, 68, 68);     // #ef4444 Red
    private static final Color RED_LT    = new Color(60, 20, 24);      // Dark Red Surface
    private static final Color PURPLE    = new Color(168, 85, 247);    // #a855f7 Purple
    private static final Color PURPLE_LT = new Color(48, 28, 70);      // Dark Purple Surface
    private static final Color TEAL      = new Color(20, 184, 166);    // #14b8a6 Teal
    private static final Color TEAL_LT   = new Color(17, 47, 45);      // Dark Teal Surface
    private static final Color AMBER     = new Color(245, 158, 11);    // #f59e0b Amber
    private static final Color T1        = new Color(241, 245, 249);   // #f1f5f9 Crisp Primary Text
    private static final Color T2        = new Color(148, 163, 184);   // #94a3b8 Secondary Slate
    private static final Color T3        = new Color(100, 116, 139);   // #64748b Muted Slate
    private static final Color AISLE     = new Color(20, 29, 44);      // Maneuver Lane
    private static final Color PILLAR    = new Color(71, 85, 105);     // Column/Pillar
    private static final Color WHITE     = Color.WHITE;

    private GedungParkir gedung;
    private NumberFormat rupiah;
    private SlotParkir selectedSlot;
    private KarcisParkir selectedTiket;

    private JPanel blueprintContainer;
    private JLabel lblTersedia, lblTerisi, lblPendapatan;
    private DefaultTableModel logModel;

    private JTextField txtTiket, txtJamKeluar, txtUangBayar;
    private JComboBox<String> cbMetode;
    private JLabel lblInfoKendaraan, lblTagihan, lblNotice;

    private JTextField txtPlat, txtMerk, txtJamMasuk;
    private JComboBox<String> cbTipe;
    private JLabel lblAlokasiHint;

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

    public AdminGUI() {
        super("SmartParkir — CCTV Live Monitoring & AI Auto-Gate");
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

        // Hentikan streaming kamera saat window ditutup
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                if (cctvStreamer != null) cctvStreamer.stopStream();
            }
        });

        // AUTO-START CCTV DAN AUTO-DETEKSI OTOMATIS SAAT PROGRAM DIJALANKAN
        SwingUtilities.invokeLater(() -> startDashboardCCTV());
    }

    private void initData() {
        gedung = new GedungParkir("Grand PENS Plaza", 5000, 3000, 2000, 1000);

        for (int i = 1; i <= 9; i++)
            gedung.tambahSlot(new SlotParkir(String.format("A%02d", i), 1, "MOTOR"));
        for (int i = 1; i <= 12; i++) {
            if (i == 5) continue;
            gedung.tambahSlot(new SlotParkir(String.format("B%02d", i), 1, "MOBIL"));
        }
        for (int i = 1; i <= 12; i++) {
            if (i == 5) continue;
            gedung.tambahSlot(new SlotParkir(String.format("C%02d", i), 1, "MOBIL"));
        }
        for (int i = 1; i <= 12; i++)
            gedung.tambahSlot(new SlotParkir(String.format("D%02d", i), 1, "MOBIL"));
        gedung.tambahSlot(new SlotParkir("EV1", 1, "MOBIL_LISTRIK"));
        gedung.tambahSlot(new SlotParkir("EV2", 1, "MOBIL_LISTRIK"));

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
            new EmptyBorder(10, 18, 10, 18)));

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brand.setOpaque(false);

        JLabel icon = new JLabel("P", SwingConstants.CENTER);
        icon.setOpaque(true);
        icon.setBackground(BLUE);
        icon.setForeground(WHITE);
        icon.setFont(new Font("Segoe UI", Font.BOLD, 17));
        icon.setPreferredSize(new Dimension(32, 32));

        JLabel name = new JLabel(String.format(
            "<html><b style='font-size:14px;color:%s;'>SmartParkir</b>"
            + "<br/><span style='color:%s;font-size:10px;'>Grand PENS Plaza &middot; 45 Slot &middot; CCTV Live Auto-Detect</span></html>",
            hex(T1), hex(T2)));
        name.setForeground(T1);
        brand.add(icon);
        brand.add(name);

        JPanel pills = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pills.setOpaque(false);

        JButton btnCameraHeader = button("⚙️ Pengaturan AI", PURPLE, WHITE);
        btnCameraHeader.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnCameraHeader.setBorder(new EmptyBorder(6, 12, 6, 12));
        btnCameraHeader.addActionListener(e -> openCameraScannerDialog());
        pills.add(btnCameraHeader);

        lblTersedia   = pill("TERSEDIA",  "-", GREEN);
        lblTerisi     = pill("TERISI",    "-", RED);
        lblPendapatan = pill("KAS MASUK", "-", AMBER);
        pills.add(lblTersedia);
        pills.add(lblTerisi);
        pills.add(lblPendapatan);

        h.add(brand, BorderLayout.WEST);
        h.add(pills,  BorderLayout.EAST);
        return h;
    }

    private JLabel pill(String label, String val, Color valColor) {
        JLabel lbl = new JLabel(fmtPill(label, val, valColor));
        lbl.setOpaque(true);
        lbl.setBackground(FIELD_BG);
        lbl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(4, 10, 4, 10)));
        return lbl;
    }

    private String fmtPill(String label, String val, Color c) {
        return String.format(
            "<html><div style='text-align:right'>"
            + "<span style='color:%s;font-size:9px;'>%s</span><br/>"
            + "<b style='font-size:13px;color:%s;'>%s</b></div></html>",
            hex(T2), label, hex(c), val);
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
        card.add(scroll, BorderLayout.CENTER);

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
        JPanel bar = new JPanel(new GridLayout(1, 3, 8, 0));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(4, 0, 0, 0));
        bar.add(tariffBadge("Motor", "Rp 2.000", "+ Rp 1.000/jam", TEAL, TEAL_LT));
        bar.add(tariffBadge("Mobil", "Rp 5.000", "+ Rp 3.000/jam", BLUE, BLUE_LT));
        bar.add(tariffBadge("EV", "Diskon 20%", "Harga Spesial", PURPLE, PURPLE_LT));
        return bar;
    }

    private JPanel tariffBadge(String name, String rate, String sub, Color accent, Color bg) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2));
        p.setBackground(bg);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(2, 6, 2, 6)));
        JLabel n = new JLabel(name); n.setFont(new Font("Segoe UI", Font.BOLD, 10)); n.setForeground(accent);
        JLabel r = new JLabel(rate); r.setFont(new Font("Segoe UI", Font.BOLD, 10)); r.setForeground(T1);
        JLabel s = new JLabel(sub);  s.setFont(new Font("Segoe UI", Font.PLAIN, 9)); s.setForeground(T2);
        p.add(n); p.add(r); p.add(s);
        return p;
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

        Color bgColor = isFull ? RED_LT : isEV ? PURPLE_LT : isMotor ? TEAL_LT : GREEN_LT;
        Color bdColor = isSel  ? BLUE   : isFull ? new Color(153, 27, 27) : BORDER;

        JPanel bay = new JPanel(new GridLayout(3, 1, 0, 1)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(bdColor);
                g2.setStroke(new BasicStroke(isSel ? 2f : 1f));
                int inset = isSel ? 1 : 0;
                g2.drawRoundRect(inset, inset, getWidth()-1-inset, getHeight()-1-inset, 6, 6);
                g2.dispose();
            }
        };
        bay.setPreferredSize(new Dimension(50, 72));
        bay.setOpaque(false);
        bay.setBorder(new EmptyBorder(3, 3, 3, 3));
        bay.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel id = new JLabel(slot.getIdSlot(), SwingConstants.CENTER);
        id.setFont(new Font("Segoe UI", Font.BOLD, 8));
        id.setForeground(isEV ? PURPLE : isMotor ? TEAL : T2);
        bay.add(id);

        if (isFull) {
            String plat = slot.getKendaraanParkir().getPlatNomor();
            JLabel p = new JLabel("<html><center style='color:#f87171;'>" + plat.replace(" ", "<br/>") + "</center></html>",
                SwingConstants.CENTER);
            p.setFont(new Font("Segoe UI", Font.BOLD, 8));
            p.setForeground(RED);
            bay.add(p);
        } else {
            JLabel empty = new JLabel("KOSONG", SwingConstants.CENTER);
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 8));
            empty.setForeground(T3);
            bay.add(empty);
        }

        Color typeCol = isEV ? PURPLE : isMotor ? TEAL : BLUE;
        String typeStr = isEV ? "EV" : isMotor ? "MTR" : "MBL";
        JLabel type = new JLabel(typeStr, SwingConstants.CENTER);
        type.setFont(new Font("Segoe UI", Font.PLAIN, 8));
        type.setForeground(typeCol);
        bay.add(type);

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
        
        // 1. CCTV CARD DI DASHBOARD UNTUK PEMANTAUAN LANGSUNG (KOMPAK)
        side.add(buildCCTVCard());
        side.add(Box.createVerticalStrut(8));

        // 2. FORM KASIR / OPERASI
        side.add(buildOperationCard());
        side.add(Box.createVerticalStrut(8));

        // 3. LOG TRANSAKSI
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
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(6, 10, 6, 10)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 205));
        card.setPreferredSize(new Dimension(438, 205));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = sectionTitle("CCTV Gerbang Masuk (Live AI)");
        lblCCTVLiveBadge = new JLabel("🔴 STREAMING AKTIF");
        lblCCTVLiveBadge.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lblCCTVLiveBadge.setForeground(GREEN);
        top.add(title, BorderLayout.WEST);
        top.add(lblCCTVLiveBadge, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        cctvScreen = new JLabel("", SwingConstants.CENTER);
        cctvScreen.setPreferredSize(new Dimension(420, 130));
        cctvScreen.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        cctvScreen.setMinimumSize(new Dimension(320, 130));
        cctvScreen.setOpaque(true);
        cctvScreen.setBackground(Color.BLACK);
        cctvScreen.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        card.add(cctvScreen, BorderLayout.CENTER);

        JPanel bot = new JPanel(new BorderLayout(0, 4));
        bot.setOpaque(false);

        lblCCTVStatus = new JLabel("<html><span style='color:#38bdf8;font-size:10px;'>🟢 Sensor aktif: Menunggu kendaraan di gerbang masuk...</span></html>");
        bot.add(lblCCTVStatus, BorderLayout.NORTH);

        JPanel actions = new JPanel(new GridLayout(1, 3, 4, 0));
        actions.setOpaque(false);

        JButton btnSimulate = button("🚗 Test Xenia", BLUE, WHITE);
        btnSimulate.setFont(new Font("Segoe UI", Font.BOLD, 9));
        btnSimulate.setBorder(new EmptyBorder(4, 4, 4, 4));
        btnSimulate.addActionListener(e -> simulateCarArrival());

        btnCCTVToggle = button("⏹ Jeda Video", CARD_BG, T1);
        btnCCTVToggle.setFont(new Font("Segoe UI", Font.BOLD, 9));
        btnCCTVToggle.setBorder(new EmptyBorder(4, 4, 4, 4));
        btnCCTVToggle.addActionListener(e -> toggleCCTVStreaming());

        JButton btnSettings = button("⚙️ Setting AI", CARD_BG, T1);
        btnSettings.setFont(new Font("Segoe UI", Font.BOLD, 9));
        btnSettings.setBorder(new EmptyBorder(4, 4, 4, 4));
        btnSettings.addActionListener(e -> openCameraScannerDialog());

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
        if (now < cctvCooldownUntil) return; // Dalam jeda/cooldown anti-duplikasi

        cctvCooldownUntil = now + 9000; // Pasang cooldown 9 detik agar kamera berhenti memindai sementara
        cctvVehicleDetected = true;
        cctvHudText = "MOBIL TERDETEKSI! MENGIRIM KE AI ORION...";

        SwingUtilities.invokeLater(() -> {
            lblCCTVStatus.setText("<html><span style='color:#a855f7;font-size:10px;'>🚗 Mobil terdeteksi! Memverifikasi plat ke AI Orion...</span></html>");
            drawCCTVScreen(frame, true, cctvHudText);
        });

        // Jalankan klasifikasi AI di thread latar belakang
        new Thread(() -> {
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
                });
            }
        }).start();
    }

    private void processAIDetectionResult(DetectionResult res) {
        String plat = res.plat.trim();
        String merk = res.merk.trim();
        String tipe = res.tipe.trim();

        // 0. CEK KEAKURATAN DETEKSI (Plat harus terdeteksi jelas dan valid)
        if (!AIVisionService.isValidPlate(plat)) {
            cctvVehicleDetected = false;
            cctvHudText = "STANDBY - KENDARAAN BELUM TERDETEKSI JELAS";
            lblCCTVStatus.setText("<html><span style='color:#94a3b8;font-size:10px;'>👁️ Kamera aktif memantau gerbang... (Menunggu kendaraan dengan plat jelas)</span></html>");
            startCCTVCooldownCountdown(4);
            return;
        }

        // 1. CEK DUPLIKASI KE GEDUNG PARKIR (Apakah mobil sedang parkir di dalam?)
        SlotParkir sudahParkir = gedung.cariSlotDenganPlat(plat);
        if (sudahParkir != null) {
            cctvVehicleDetected = false;
            cctvHudText = "DUPLIKAT DIABAIKAN: " + plat + " DI " + sudahParkir.getIdSlot();
            lblCCTVStatus.setText("<html><span style='color:#f59e0b;font-size:10px;'>⚠️ <b>" + plat + "</b> SUDAH ADA di Slot " + sudahParkir.getIdSlot() + ". Input ganda dicegah!</span></html>");
            lblNotice.setText("<html><span style='color:#f59e0b;'>⚠️ Mobil plat <b>" + plat + "</b> sudah terparkir di slot <b>" + sudahParkir.getIdSlot() + "</b>. Sistem menolak duplikasi.</span></html>");
            startCCTVCooldownCountdown(6);
            return;
        }

        // 2. CEK RECENT PLAT COOLDOWN (Mencegah input berulang dalam hitungan detik)
        long now = System.currentTimeMillis();
        if (plat.equalsIgnoreCase(lastProcessedPlate) && (now - lastProcessedTime < 20000)) {
            cctvVehicleDetected = false;
            cctvHudText = "DUPLIKAT BARU DIABAIKAN: " + plat;
            startCCTVCooldownCountdown(5);
            return;
        }

        // 3. MASUKKAN KE DATA OTOMATIS (AUTO CHECK-IN)
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
        int targetW = cctvScreen.getWidth() > 0 ? cctvScreen.getWidth() : 420;
        int targetH = 130; // Kunci tinggi kamera 130px agar presisi, tajam, dan tidak mendominasi layar

        BufferedImage combined = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = combined.createGraphics();
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

        // Bounding Scanner Reticle Kompak
        int rw = Math.min(targetW - 80, 220), rh = 76;
        int rx = (targetW - rw) / 2;
        int ry = (targetH - rh) / 2 - 2;

        Color hudColor = isDetected ? new Color(34, 197, 94) : new Color(59, 130, 246);
        g2.setColor(hudColor);
        g2.setStroke(new BasicStroke(1.8f));

        int cLen = 12;
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

        // Header Text
        g2.setFont(new Font("Segoe UI", Font.BOLD, 8));
        g2.setColor(Color.WHITE);
        g2.drawString("CAM-01 • GERBANG MASUK REALTIME", 8, 14);

        // Status Badge at Bottom
        g2.setColor(new Color(15, 23, 42, 210));
        g2.fillRoundRect(rx, ry + rh - 18, rw, 16, 4, 4);
        g2.setColor(hudColor);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 8));
        g2.drawString(hudStatus != null ? hudStatus : "MEMANTAU GERBANG MASUK...", rx + 6, ry + rh - 6);

        g2.dispose();
        cctvScreen.setIcon(new ImageIcon(combined));
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
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(CARD_BG);
        p.setBorder(new EmptyBorder(10, 12, 10, 12));

        lblInfoKendaraan = new JLabel("<html><i style='color:#94a3b8;'>Belum ada kendaraan dipilih.</i></html>");
        lblInfoKendaraan.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblInfoKendaraan.setOpaque(true);
        lblInfoKendaraan.setBackground(FIELD_BG);
        lblInfoKendaraan.setForeground(T1);
        lblInfoKendaraan.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1), new EmptyBorder(5, 8, 5, 8)));
        p.add(lblInfoKendaraan, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(4, 2, 8, 6));
        form.setOpaque(false);

        form.add(fieldLabel("No. Tiket"));
        txtTiket = field("TKT-0002");
        form.add(txtTiket);

        form.add(fieldLabel("Jam Keluar (0-23)"));
        txtJamKeluar = field("12");
        txtJamKeluar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateTagihan(); }
            public void removeUpdate(DocumentEvent e) { updateTagihan(); }
            public void changedUpdate(DocumentEvent e) { updateTagihan(); }
        });
        form.add(txtJamKeluar);

        form.add(fieldLabel("Metode Bayar"));
        cbMetode = new JComboBox<>(new String[]{"E_MONEY", "QRIS", "CASH"});
        styleCombo(cbMetode);
        form.add(cbMetode);

        form.add(fieldLabel("Nominal Bayar (Rp)"));
        txtUangBayar = field("0");
        form.add(txtUangBayar);

        p.add(form, BorderLayout.CENTER);

        JPanel bot = new JPanel(new BorderLayout(0, 6));
        bot.setOpaque(false);

        JPanel tagihanBar = new JPanel(new BorderLayout());
        tagihanBar.setBackground(BLUE_LT);
        tagihanBar.setBorder(new EmptyBorder(7, 10, 7, 10));
        JLabel tl = new JLabel("Total Tagihan:");
        tl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tl.setForeground(T2);
        lblTagihan = new JLabel("-");
        lblTagihan.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTagihan.setForeground(BLUE);
        tagihanBar.add(tl, BorderLayout.WEST);
        tagihanBar.add(lblTagihan, BorderLayout.EAST);
        bot.add(tagihanBar, BorderLayout.NORTH);

        JButton btnBayar = button("Bayar & Buka Portal", GREEN, WHITE);
        btnBayar.addActionListener(e -> handleCheckOut());
        bot.add(btnBayar, BorderLayout.SOUTH);
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
        lblAlokasiHint.setForeground(BLUE);
        form.add(lblAlokasiHint);

        p.add(form, BorderLayout.CENTER);

        JPanel botButtons = new JPanel(new GridLayout(1, 2, 8, 0));
        botButtons.setOpaque(false);

        JButton btnScanCam = button("📷 Scan AI Manual", PURPLE, WHITE);
        btnScanCam.addActionListener(e -> openCameraScannerDialog());

        JButton btnMasuk = button("Check-In & Terbitkan", BLUE, WHITE);
        btnMasuk.addActionListener(e -> handleCheckIn());

        botButtons.add(btnScanCam);
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

            boolean ok = gedung.prosesKeluar(found, jamKeluar, metode, uang);
            if (ok) {
                animGate(true, "TERBUKA KELUAR");
                selectedSlot  = null;
                selectedTiket = null;
                lblNotice.setText("<html><span style='color:#34d399;'>Check-out berhasil. Slot kosong kembali.</span></html>");
                lblTagihan.setText("Rp 0");
                syncAll();
                JOptionPane.showMessageDialog(this,
                    "Pembayaran LUNAS\nKendaraan: " + found.getKendaraan().getPlatNomor()
                    + "\nBiaya: " + rupiah.format(found.getTotalBiaya())
                    + "\nKembalian: " + rupiah.format(uang - found.getTotalBiaya()),
                    "Check-Out Sukses", JOptionPane.INFORMATION_MESSAGE);
            } else {
                warn("Pembayaran kurang dari tagihan.");
            }
        } catch (Exception ex) {
            warn("Input tidak valid: " + ex.getMessage());
        }
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

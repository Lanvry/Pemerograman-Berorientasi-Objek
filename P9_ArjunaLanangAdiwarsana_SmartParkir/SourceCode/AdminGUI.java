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
 * Modern Clean White & Aesthetic Admin GUI untuk Smart Parking (SmartParkir).
 * Didesain dengan estetika putih minimalis, interaksi klik langsung pada mobil
 * untuk check-out otomatis, dan animasi barrier gate 60 FPS yang mulus.
 */
public class AdminGUI extends JFrame {

    // Palet Warna Putih Bersih & Estetik (Clean Modern Slate & Soft Pastels)
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

    // Soft Badge Backgrounds
    private static final Color GREEN_SOFT = new Color(236, 253, 245);
    private static final Color RED_SOFT = new Color(255, 241, 242);
    private static final Color PURPLE_SOFT = new Color(245, 243, 255);
    private static final Color BLUE_SOFT = new Color(239, 246, 255);

    private GedungParkir gedung;
    private NumberFormat rupiahFormat;

    // State Interaksi
    private SlotParkir selectedSlot = null;
    private KarcisParkir selectedTiket = null;

    // Komponen UI
    private JPanel slotsGridPanel;
    private JLabel lblTotalSlot, lblTersedia, lblTerisi, lblPendapatan;
    private DefaultTableModel logTableModel;

    // Form Check-In
    private JTextField txtPlat, txtMerk, txtJamMasuk;
    private JComboBox<String> cbTipeKendaraan;

    // Form Check-Out
    private JTextField txtCariTiket, txtJamKeluar, txtUangBayar;
    private JComboBox<String> cbMetodeBayar;
    private JLabel lblCheckoutNotice;
    private JLabel lblBillingPreview;

    // Animasi State (60 FPS)
    private float pulseAlpha = 0.5f;
    private boolean pulseUp = true;
    private float gateAngle = 0.0f; // 0 = horizontal (tertutup), 75 = terbuka
    private float targetGateAngle = 0.0f;
    private String gateStatusText = "PORTAL TERTUTUP";
    private Color gateStatusColor = ACCENT_RED;

    public AdminGUI() {
        super("SmartParkir Command Center - Mall Grand PENS Plaza");
        this.rupiahFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

        initData();
        initUI();
        startAnimationEngine();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1340, 880);
        setMinimumSize(new Dimension(1120, 740));
        setLocationRelativeTo(null);
    }

    private void initData() {
        gedung = new GedungParkir(
                "Mall Grand PENS Plaza",
                5000.0, 3000.0, // Mobil: Rp 5.000 jam ke-1, Rp 3.000 jam berikutnya
                2000.0, 1000.0  // Motor: Rp 2.000 jam ke-1, Rp 1.000 jam berikutnya
        );

        // Tambah slot beragam lantai
        gedung.tambahSlot(new SlotParkir("MTR-L1-01", 1, "MOTOR"));
        gedung.tambahSlot(new SlotParkir("MTR-L1-02", 1, "MOTOR"));
        gedung.tambahSlot(new SlotParkir("MTR-L1-03", 1, "MOTOR"));
        gedung.tambahSlot(new SlotParkir("MBL-L1-01", 1, "MOBIL"));
        gedung.tambahSlot(new SlotParkir("MBL-L1-02", 1, "MOBIL"));
        gedung.tambahSlot(new SlotParkir("EV-L1-01", 1, "MOBIL_LISTRIK"));
        gedung.tambahSlot(new SlotParkir("MBL-L2-01", 2, "MOBIL"));
        gedung.tambahSlot(new SlotParkir("MBL-L2-02", 2, "MOBIL"));
        gedung.tambahSlot(new SlotParkir("EV-L2-01", 2, "MOBIL_LISTRIK"));

        // Contoh kendaraan awal yang sedang terparkir
        Kendaraan init1 = new Kendaraan("B 1234 ABC", "MOBIL", "Toyota Avanza");
        KarcisParkir t1 = gedung.prosesMasuk(init1, 8);

        // Otomatis pilih slot terisi pertama agar siap di-checkout
        if (t1 != null) {
            this.selectedSlot = t1.getSlot();
            this.selectedTiket = t1;
        }
    }

    private void initUI() {
        getContentPane().setBackground(BG_LIGHT);
        setLayout(new BorderLayout(16, 16));

        // 1. Top Navbar Header
        add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Main Content Split (Kiri: Matriks Slot, Kanan: Form Check-In & Check-Out Langsung)
        JPanel mainContent = new JPanel(new GridLayout(1, 2, 18, 18));
        mainContent.setBackground(BG_LIGHT);
        mainContent.setBorder(new EmptyBorder(0, 20, 20, 20));

        mainContent.add(createLeftSlotsPanel());
        mainContent.add(createRightControlsPanel());

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
                new EmptyBorder(16, 24, 16, 24)
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
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 19));
        lblTitle.setForeground(TEXT_DARK);

        JLabel lblSub = new JLabel("Mall Grand PENS Plaza • Sistem Otomasi Slot & Kasir Cerdas");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(TEXT_MUTED);

        textGroup.add(lblTitle);
        textGroup.add(lblSub);

        titleBox.add(logoBadge);
        titleBox.add(textGroup);

        // Stats Badges on Top
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        statsPanel.setOpaque(false);

        lblTotalSlot = createStatBadge("TOTAL SLOT", "9", TEXT_DARK, BG_LIGHT);
        lblTersedia = createStatBadge("TERSEDIA", "8", ACCENT_GREEN, GREEN_SOFT);
        lblTerisi = createStatBadge("TERISI", "1", ACCENT_RED, RED_SOFT);
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
                new EmptyBorder(6, 14, 6, 14)
        ));
        return l;
    }

    private JPanel createLeftSlotsPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1, true),
                new EmptyBorder(18, 18, 18, 18)
        ));

        // Header Section with Title, Gate Indicator, and Click Tip
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);

        JPanel titleGroup = new JPanel(new GridLayout(2, 1, 0, 2));
        titleGroup.setOpaque(false);
        JLabel lblSec = new JLabel("DENAH SLOT PARKIR REALTIME");
        lblSec.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblSec.setForeground(TEXT_DARK);

        JLabel lblTip = new JLabel("💡 Klik mobil terisi untuk langsung check-out otomatis");
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
                g2.fillRoundRect(0, 0, w, h, 10, 10);
                g2.setColor(CARD_BORDER);
                g2.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);

                // Pillar
                int pillarX = 20;
                int pillarY = h - 9;
                g2.setColor(new Color(100, 116, 139));
                g2.fillRect(pillarX - 4, pillarY - 14, 8, 14);

                // Barrier Arm with smooth rotation
                double rad = Math.toRadians(gateAngle);
                int armLen = 28;
                int armEndX = pillarX + (int) (armLen * Math.cos(rad));
                int armEndY = (pillarY - 10) - (int) (armLen * Math.sin(rad));

                g2.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(gateStatusColor);
                g2.drawLine(pillarX, pillarY - 10, armEndX, armEndY);

                // Status text
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                g2.drawString(gateStatusText, pillarX + 38, h / 2 + 4);

                g2.dispose();
            }
        };
        gateCanvas.setPreferredSize(new Dimension(180, 36));
        gateCanvas.setOpaque(false);

        topHeader.add(titleGroup, BorderLayout.WEST);
        topHeader.add(gateCanvas, BorderLayout.EAST);
        panel.add(topHeader, BorderLayout.NORTH);

        // Grid of slots (3x3)
        slotsGridPanel = new JPanel(new GridLayout(3, 3, 12, 12));
        slotsGridPanel.setBackground(CARD_BG);
        refreshSlotsGrid();

        panel.add(slotsGridPanel, BorderLayout.CENTER);

        // Legend at Bottom
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 4));
        legend.setOpaque(false);
        legend.add(createLegendItem("Tersedia (Kosong)", ACCENT_GREEN));
        legend.add(createLegendItem("Terisi (Klik utk Check-Out)", ACCENT_RED));
        legend.add(createLegendItem("Slot EV Charger", ACCENT_PURPLE));
        panel.add(legend, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createLegendItem(String label, Color col) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);
        JLabel dot = new JLabel("●");
        dot.setForeground(col);
        dot.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(TEXT_MUTED);
        p.add(dot);
        p.add(lbl);
        return p;
    }

    private void refreshSlotsGrid() {
        slotsGridPanel.removeAll();

        for (SlotParkir slot : gedung.getDaftarSlot()) {
            boolean isSelected = (selectedSlot != null && selectedSlot.getIdSlot().equals(slot.getIdSlot()));

            JPanel slotCard = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int w = getWidth();
                    int h = getHeight();

                    // Background color
                    Color bg = CARD_BG;
                    if (slot.isTerisi()) {
                        bg = RED_SOFT;
                    } else if ("MOBIL_LISTRIK".equals(slot.getTipeSlot())) {
                        bg = PURPLE_SOFT;
                    } else {
                        bg = GREEN_SOFT;
                    }
                    g2.setColor(bg);
                    g2.fillRoundRect(0, 0, w, h, 14, 14);

                    // Border styling
                    if (isSelected) {
                        g2.setColor(BORDER_ACTIVE);
                        g2.setStroke(new BasicStroke(2.5f));
                        g2.drawRoundRect(1, 1, w - 3, h - 3, 14, 14);
                    } else {
                        Color borderCol = slot.isTerisi() ? new Color(254, 205, 211) : CARD_BORDER;
                        g2.setColor(borderCol);
                        g2.setStroke(new BasicStroke(1.0f));
                        g2.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);
                    }

                    // Pulse indicator on available slots
                    if (!slot.isTerisi()) {
                        g2.setColor(new Color(16, 185, 129, (int) (pulseAlpha * 50)));
                        g2.setStroke(new BasicStroke(2.0f));
                        g2.drawRoundRect(2, 2, w - 5, h - 5, 12, 12);
                    }

                    g2.dispose();
                }
            };
            slotCard.setOpaque(false);
            slotCard.setBorder(new EmptyBorder(12, 14, 12, 14));
            slotCard.setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Header slot (ID & Type Badge)
            JPanel top = new JPanel(new BorderLayout());
            top.setOpaque(false);
            JLabel lblId = new JLabel(slot.getIdSlot());
            lblId.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblId.setForeground(TEXT_DARK);

            String typeBadgeText = slot.getTipeSlot().equals("MOBIL_LISTRIK") ? "⚡ EV" : slot.getTipeSlot();
            JLabel lblTipe = new JLabel("Lt." + slot.getLantai() + " • " + typeBadgeText);
            lblTipe.setFont(new Font("Segoe UI", Font.BOLD, 10));
            lblTipe.setForeground(TEXT_MUTED);

            top.add(lblId, BorderLayout.WEST);
            top.add(lblTipe, BorderLayout.EAST);
            slotCard.add(top, BorderLayout.NORTH);

            // Body slot (Vehicle Plate or Available text)
            JPanel center = new JPanel(new GridLayout(2, 1, 0, 0));
            center.setOpaque(false);

            if (slot.isTerisi()) {
                Kendaraan k = slot.getKendaraanParkir();
                JLabel lblPlat = new JLabel(k.getPlatNomor(), SwingConstants.CENTER);
                lblPlat.setFont(new Font("Segoe UI", Font.BOLD, 16));
                lblPlat.setForeground(ACCENT_RED);

                JLabel lblMerk = new JLabel(k.getMerk() + " (" + k.getTipe() + ")", SwingConstants.CENTER);
                lblMerk.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                lblMerk.setForeground(TEXT_MUTED);

                center.add(lblPlat);
                center.add(lblMerk);
            } else {
                JLabel lblStatus = new JLabel("TERSEDIA", SwingConstants.CENTER);
                lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lblStatus.setForeground(ACCENT_GREEN);

                String subtext = slot.getTipeSlot().equals("MOBIL_LISTRIK") ? "⚡ EV Charger Ready" : "Siap Parkir";
                JLabel lblReady = new JLabel(subtext, SwingConstants.CENTER);
                lblReady.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                lblReady.setForeground(TEXT_MUTED);

                center.add(lblStatus);
                center.add(lblReady);
            }
            slotCard.add(center, BorderLayout.CENTER);

            // KLIK INTERAKTIF PADA MOBIL / SLOT & SELURUH ELEMEN DI DALAMNYA!
            attachSlotClickListener(slotCard, slot);

            slotsGridPanel.add(slotCard);
        }

        slotsGridPanel.revalidate();
        slotsGridPanel.repaint();
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

                // Berikan jam keluar default yang logis (minimal jamMasuk + 2)
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
                        "<span style='color:#059669;font-size:10px;'>Siap Check-Out! Klik tombol hijau di bawah untuk proses.</span></html>",
                        k.getPlatNomor(), k.getMerk(), slot.getIdSlot()
                ));
            } else {
                lblCheckoutNotice.setText(String.format("<html><span style='color:#e11d48;'>Tiket aktif untuk slot %s tidak ditemukan.</span></html>", slot.getIdSlot()));
            }
        } else {
            this.selectedTiket = null;
            lblCheckoutNotice.setText(String.format("<html><span style='color:#64748b;'>Slot <b>%s</b> (Lantai %d) sedang kosong. Klik mobil merah untuk check-out.</span></html>", slot.getIdSlot(), slot.getLantai()));
            lblBillingPreview.setText("Pilih kendaraan terisi");
        }

        refreshSlotsGrid();
    }

    private JPanel createRightControlsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 14, 14));
        panel.setOpaque(false);

        // Bagian Atas: Check-In & Check-Out Boxes
        JPanel formsGrid = new JPanel(new GridLayout(1, 2, 12, 12));
        formsGrid.setOpaque(false);

        formsGrid.add(createCheckInBox());
        formsGrid.add(createCheckOutBox());
        panel.add(formsGrid);

        // Bagian Bawah: Tabel Riwayat Transaksi & Kuitansi
        panel.add(createLogTableBox());

        return panel;
    }

    private JPanel createCheckInBox() {
        JPanel box = createCardBox("CHECK-IN (GATE MASUK)");

        JPanel form = new JPanel(new GridLayout(4, 2, 8, 10));
        form.setOpaque(false);

        form.add(createFieldLabel("Nomor Plat:"));
        txtPlat = createStyledTextField("B 9988 PBO");
        form.add(txtPlat);

        form.add(createFieldLabel("Tipe Kendaraan:"));
        cbTipeKendaraan = new JComboBox<>(new String[]{"MOBIL", "MOBIL_LISTRIK", "MOTOR"});
        styleComboBox(cbTipeKendaraan);
        form.add(cbTipeKendaraan);

        form.add(createFieldLabel("Merk / Model:"));
        txtMerk = createStyledTextField("Honda HR-V");
        form.add(txtMerk);

        form.add(createFieldLabel("Jam Masuk (0-23):"));
        txtJamMasuk = createStyledTextField("10");
        form.add(txtJamMasuk);

        box.add(form, BorderLayout.CENTER);

        JButton btnMasuk = createStyledButton("🚗 TAP IN & CARI SLOT", ACCENT_BLUE, Color.WHITE);
        btnMasuk.addActionListener(e -> handleCheckIn());
        box.add(btnMasuk, BorderLayout.SOUTH);

        return box;
    }

    private JPanel createCheckOutBox() {
        JPanel box = createCardBox("CHECK-OUT & KASIR");

        JPanel formPanel = new JPanel(new BorderLayout(0, 8));
        formPanel.setOpaque(false);

        // Banner notifikasi kendaraan yang dipilih saat klik slot
        lblCheckoutNotice = new JLabel("<html><span style='color:#64748b;font-size:10px;'>💡 Klik mobil parkir pada denah untuk memilih otomatis</span></html>");
        lblCheckoutNotice.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1, true),
                new EmptyBorder(6, 8, 6, 8)
        ));
        formPanel.add(lblCheckoutNotice, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(4, 2, 8, 8));
        form.setOpaque(false);

        form.add(createFieldLabel("No. Tiket:"));
        txtCariTiket = createStyledTextField("TKT-0001");
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

        String[] cols = {"No. Tiket", "Plat Nomor", "Tipe", "Slot", "Jam", "Status", "Biaya"};
        logTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        JTable table = new JTable(logTableModel);
        table.setBackground(CARD_BG);
        table.setForeground(TEXT_BODY);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(26);
        table.setShowGrid(false);
        table.setSelectionBackground(BLUE_SOFT);
        table.setSelectionForeground(ACCENT_BLUE);

        table.getTableHeader().setBackground(BG_LIGHT);
        table.getTableHeader().setForeground(TEXT_MUTED);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(center);
        }

        // Klik baris pada tabel juga langsung memilih tiket untuk check-out!
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
                                    "<html><b style='color:#2563eb;'>✔ Terpilih: %s</b> di Slot %s<br/>" +
                                    "<span style='color:#059669;font-size:10px;'>Siap Check-Out! Klik tombol hijau di bawah.</span></html>",
                                    t.getKendaraan().getPlatNomor(), t.getSlot().getIdSlot()
                            ));
                            refreshSlotsGrid();
                            break;
                        }
                    }
                }
            }
        });

        JScrollPane sp = new JScrollPane(table);
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
            double tarifPertama = "MOTOR".equals(t.getKendaraan().getTipe()) ? 2000.0 : 5000.0;
            double tarifBerikutnya = "MOTOR".equals(t.getKendaraan().getTipe()) ? 1000.0 : 3000.0;
            double subtotal = tarifPertama + (durasi > 1 ? (durasi - 1) * tarifBerikutnya : 0);

            if (t.getKendaraan().isMobilListrik()) {
                subtotal = subtotal * 0.80; // Diskon 20%
            }

            lblBillingPreview.setText(String.format("%s (%d Jam)%s",
                    rupiahFormat.format(subtotal), durasi,
                    (t.getKendaraan().isMobilListrik() ? " [Diskon EV 20%]" : "")));

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

            Kendaraan k = new Kendaraan(plat, tipe, merk);
            KarcisParkir tiket = gedung.prosesMasuk(k, jam);

            if (tiket != null) {
                triggerGateAnimation(true, "GATE MASUK DIBUKA");
                selectedSlot = tiket.getSlot();
                selectedTiket = tiket;
                txtCariTiket.setText(tiket.getNomorTiket());

                refreshSlotsGrid();
                updateStats();
                updateLogTable();
                updateCheckoutPreview();

                JOptionPane.showMessageDialog(this,
                        String.format("Kendaraan %s Berhasil Masuk!\nTiket: %s\nAlokasi: Slot %s (Lantai %d)",
                                plat, tiket.getNomorTiket(), tiket.getSlot().getIdSlot(), tiket.getSlot().getLantai()),
                        "Check-In Berhasil", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Maaf, Parkir Penuh atau tidak ada slot yang cocok untuk " + tipe + "!",
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

                // Reset seleksi
                this.selectedSlot = null;
                this.selectedTiket = null;
                lblCheckoutNotice.setText("<html><span style='color:#059669;font-weight:600;'>✔ Check-out sukses! Slot telah kosong kembali.</span></html>");
                lblBillingPreview.setText("Rp 0");

                refreshSlotsGrid();
                updateStats();
                updateLogTable();

                String bonusMsg = tiketDitemukan.getKendaraan().isMobilListrik() ? "\n(Mendapatkan Diskon 20% Insentif Hijau EV)" : "";
                JOptionPane.showMessageDialog(this,
                        String.format("Pembayaran LUNAS!\nTotal Biaya: %s%s\nSlot %s sekarang kosong kembali.\nPortal keluar TERBUKA.",
                                rupiahFormat.format(tiketDitemukan.getTotalBiaya()), bonusMsg, tiketDitemukan.getSlot().getIdSlot()),
                        "Check-Out Selesai", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Pembayaran gagal! Nominal uang kurang dari total tagihan.", "Ditolak", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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

        // Tutup kembali portal otomatis setelah 3 detik
        if (open) {
            Timer closeTimer = new Timer(3000, new ActionListener() {
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
        // 60 FPS smooth interpolation timer
        Timer timer = new Timer(16, e -> {
            // Denyut halus pada slot tersedia
            if (pulseUp) {
                pulseAlpha += 0.02f;
                if (pulseAlpha >= 0.95f) pulseUp = false;
            } else {
                pulseAlpha -= 0.02f;
                if (pulseAlpha <= 0.35f) pulseUp = true;
            }

            // Interpolasi rotasi barrier gate
            if (Math.abs(gateAngle - targetGateAngle) > 0.5f) {
                gateAngle += (targetGateAngle - gateAngle) * 0.15f;
            } else {
                gateAngle = targetGateAngle;
            }

            repaint();
        });
        timer.start();
    }

    // Helper Styling
    private JPanel createCardBox(String title) {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(TEXT_DARK);
        p.add(lbl, BorderLayout.NORTH);
        return p;
    }

    private JLabel createFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(TEXT_MUTED);
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
                new EmptyBorder(5, 8, 5, 8)
        ));
        return tf;
    }

    private void styleComboBox(JComboBox<String> cb) {
        cb.setBackground(BG_LIGHT);
        cb.setForeground(TEXT_DARK);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    }

    private JButton createStyledButton(String text, Color bg, Color textCol) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(textCol);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 16, 10, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private String toHex(Color col) {
        return String.format("#%02x%02x%02x", col.getRed(), col.getGreen(), col.getBlue());
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            AdminGUI gui = new AdminGUI();
            gui.setVisible(true);
        });
    }
}

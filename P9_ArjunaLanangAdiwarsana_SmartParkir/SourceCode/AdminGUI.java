import model.GedungParkir;
import model.KarcisParkir;
import model.Kendaraan;
import model.SlotParkir;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Modern Animated Admin GUI untuk Smart Parking Gedung (SmartParkir).
 * Didesain dengan estetika dark-mode premium, animasi timer 60fps,
 * visualisasi matriks slot parkir interaktif, dan kontrol gerbang portal otomatis.
 */
public class AdminGUI extends JFrame {

    // Palet Warna Dark Mode Modern (Catppuccin Macchiato Inspired)
    private static final Color BG_DARK = new Color(24, 24, 37);         // #181825
    private static final Color CARD_BG = new Color(30, 30, 46);         // #1e1e2e
    private static final Color CARD_BORDER = new Color(49, 50, 68);     // #313244
    private static final Color ACCENT_BLUE = new Color(137, 180, 250);  // #89b4fa
    private static final Color ACCENT_GREEN = new Color(166, 227, 161); // #a6e3a1
    private static final Color ACCENT_RED = new Color(243, 139, 168);   // #f38ba8
    private static final Color ACCENT_YELLOW = new Color(249, 226, 175);// #f9e2af
    private static final Color ACCENT_PURPLE = new Color(203, 166, 247);// #cba6f7
    private static final Color TEXT_WHITE = new Color(205, 214, 244);   // #cdd6f4
    private static final Color TEXT_MUTED = new Color(147, 153, 178);   // #9399b2

    private GedungParkir gedung;
    private NumberFormat rupiahFormat;

    // Komponen UI
    private JPanel slotsGridPanel;
    private JLabel lblTotalSlot, lblTersedia, lblTerisi, lblPendapatan;
    private DefaultTableModel logTableModel;
    private JComboBox<String> cbTipeKendaraan;
    private JTextField txtPlat, txtMerk, txtJamMasuk;
    private JTextField txtCariTiket, txtJamKeluar, txtUangBayar;
    private JComboBox<String> cbMetodeBayar;

    // Animasi State
    private float pulseAlpha = 0.5f;
    private boolean pulseUp = true;
    private float gateAngle = 0.0f; // 0 = Tertutup (horizontal), 90 = Terbuka (vertikal)
    private float targetGateAngle = 0.0f;
    private String gateStatusText = "GATE CLOSED";
    private Color gateStatusColor = ACCENT_RED;

    public AdminGUI() {
        super("SmartParkir Admin Dashboard - PENS PSDKU");
        this.rupiahFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

        initData();
        initUI();
        startAnimationEngine();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1320, 860);
        setMinimumSize(new Dimension(1100, 720));
        setLocationRelativeTo(null);
    }

    private void initData() {
        gedung = new GedungParkir(
                "Mall Grand PENS Plaza",
                5000.0, 3000.0, // Mobil: Jam 1 Rp 5.000, berikutnya Rp 3.000
                2000.0, 1000.0  // Motor: Jam 1 Rp 2.000, berikutnya Rp 1.000
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

        // Contoh data terparkir awal
        Kendaraan init1 = new Kendaraan("B 1234 ABC", "MOBIL", "Toyota Avanza");
        gedung.prosesMasuk(init1, 8);
    }

    private void initUI() {
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(15, 15));

        // 1. Header Bar
        add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Main Content Split (Left: Slots Visualization, Right: Actions & Log)
        JPanel mainContent = new JPanel(new GridLayout(1, 2, 16, 16));
        mainContent.setBackground(BG_DARK);
        mainContent.setBorder(new EmptyBorder(0, 18, 18, 18));

        mainContent.add(createLeftSlotsPanel());
        mainContent.add(createRightControlsPanel());

        add(mainContent, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CARD_BG);
        header.setBorder(new EmptyBorder(16, 24, 16, 24));

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 2, 2));
        titleBox.setOpaque(false);

        JLabel lblTitle = new JLabel("SMARTPARKIR COMMAND CENTER");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT_WHITE);

        JLabel lblSub = new JLabel("Sistem Otomasi Alokasi Slot & Gate Gedung Bertingkat • PENS PSDKU");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(TEXT_MUTED);

        titleBox.add(lblTitle);
        titleBox.add(lblSub);

        // Stats Bar on Header
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        statsPanel.setOpaque(false);

        lblTotalSlot = createStatBadge("TOTAL SLOT", "9", ACCENT_BLUE);
        lblTersedia = createStatBadge("TERSEDIA", "8", ACCENT_GREEN);
        lblTerisi = createStatBadge("TERISI", "1", ACCENT_RED);
        lblPendapatan = createStatBadge("KAS PENDAPATAN", "Rp 0", ACCENT_YELLOW);

        statsPanel.add(lblTotalSlot);
        statsPanel.add(lblTersedia);
        statsPanel.add(lblTerisi);
        statsPanel.add(lblPendapatan);

        header.add(titleBox, BorderLayout.WEST);
        header.add(statsPanel, BorderLayout.EAST);
        return header;
    }

    private JLabel createStatBadge(String label, String val, Color color) {
        JLabel l = new JLabel(String.format("<html><div style='text-align:right;'><span style='color:#9399b2;font-size:9px;'>%s</span><br/><b style='font-size:14px;color:%s;'>%s</b></div></html>",
                label, toHex(color), val));
        l.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        return l;
    }

    private JPanel createLeftSlotsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));

        // Header Section with Floor Filter and Gate Status Indicator
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);

        JLabel lblSec = new JLabel("DENAH SLOT PARKIR REALTIME");
        lblSec.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblSec.setForeground(TEXT_WHITE);

        // Barrier Gate Indicator Box
        JPanel gateCanvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Background pill
                g2.setColor(new Color(24, 24, 37));
                g2.fillRoundRect(0, 0, w, h, 14, 14);
                g2.setColor(CARD_BORDER);
                g2.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);

                // Draw barrier gate icon (Pillar + Arm)
                int pillarX = 22;
                int pillarY = h - 10;
                g2.setColor(TEXT_MUTED);
                g2.fillRect(pillarX - 4, pillarY - 14, 8, 14);

                // Arm with dynamic angle animation
                double rad = Math.toRadians(gateAngle);
                int armLen = 28;
                int armEndX = pillarX + (int) (armLen * Math.cos(rad));
                int armEndY = (pillarY - 10) - (int) (armLen * Math.sin(rad));

                g2.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(gateStatusColor);
                g2.drawLine(pillarX, pillarY - 10, armEndX, armEndY);

                // Gate text
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                g2.drawString(gateStatusText, pillarX + 38, h / 2 + 4);

                g2.dispose();
            }
        };
        gateCanvas.setPreferredSize(new Dimension(160, 36));
        gateCanvas.setOpaque(false);

        topHeader.add(lblSec, BorderLayout.WEST);
        topHeader.add(gateCanvas, BorderLayout.EAST);
        panel.add(topHeader, BorderLayout.NORTH);

        // Slots Grid (Scrollable or Clean Grid)
        slotsGridPanel = new JPanel(new GridLayout(3, 3, 12, 12));
        slotsGridPanel.setBackground(CARD_BG);
        refreshSlotsGrid();

        panel.add(slotsGridPanel, BorderLayout.CENTER);

        // Legend at Bottom
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 4));
        legend.setOpaque(false);
        legend.add(createLegendItem("Tersedia (Kosong)", ACCENT_GREEN));
        legend.add(createLegendItem("Terisi (Mobil/Motor)", ACCENT_RED));
        legend.add(createLegendItem("Slot EV Charger", ACCENT_PURPLE));
        panel.add(legend, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createLegendItem(String label, Color col) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);
        JLabel dot = new JLabel("●");
        dot.setForeground(col);
        dot.setFont(new Font("Segoe UI", Font.PLAIN, 16));
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
            JPanel slotCard = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int w = getWidth();
                    int h = getHeight();

                    // Background card
                    Color baseCol = slot.isTerisi() ? new Color(45, 25, 35) : new Color(25, 40, 35);
                    if ("MOBIL_LISTRIK".equals(slot.getTipeSlot()) && !slot.isTerisi()) {
                        baseCol = new Color(35, 25, 45);
                    }
                    g2.setColor(baseCol);
                    g2.fillRoundRect(0, 0, w, h, 12, 12);

                    // Pulse glow on empty slots
                    if (!slot.isTerisi()) {
                        g2.setColor(new Color(166, 227, 161, (int) (pulseAlpha * 60)));
                        g2.setStroke(new BasicStroke(2.0f));
                        g2.drawRoundRect(1, 1, w - 3, h - 3, 12, 12);
                    } else {
                        g2.setColor(new Color(243, 139, 168, 140));
                        g2.setStroke(new BasicStroke(1.5f));
                        g2.drawRoundRect(1, 1, w - 3, h - 3, 12, 12);
                    }

                    g2.dispose();
                }
            };
            slotCard.setOpaque(false);
            slotCard.setBorder(new EmptyBorder(10, 12, 10, 12));

            // Top: Slot ID & Badge
            JPanel top = new JPanel(new BorderLayout());
            top.setOpaque(false);
            JLabel lblId = new JLabel(slot.getIdSlot());
            lblId.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblId.setForeground(TEXT_WHITE);

            JLabel lblTipe = new JLabel("Lt." + slot.getLantai() + " • " + slot.getTipeSlot());
            lblTipe.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            lblTipe.setForeground(TEXT_MUTED);

            top.add(lblId, BorderLayout.WEST);
            top.add(lblTipe, BorderLayout.EAST);
            slotCard.add(top, BorderLayout.NORTH);

            // Center: Status Icon / Plat
            JPanel center = new JPanel(new GridLayout(2, 1, 0, 0));
            center.setOpaque(false);
            if (slot.isTerisi()) {
                Kendaraan k = slot.getKendaraanParkir();
                JLabel lblPlat = new JLabel(k.getPlatNomor(), SwingConstants.CENTER);
                lblPlat.setFont(new Font("Segoe UI", Font.BOLD, 15));
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

                JLabel lblReady = new JLabel(slot.getTipeSlot().equals("MOBIL_LISTRIK") ? "⚡ EV Charger Ready" : "Siap Parkir", SwingConstants.CENTER);
                lblReady.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                lblReady.setForeground(TEXT_MUTED);

                center.add(lblStatus);
                center.add(lblReady);
            }
            slotCard.add(center, BorderLayout.CENTER);

            slotsGridPanel.add(slotCard);
        }

        slotsGridPanel.revalidate();
        slotsGridPanel.repaint();
    }

    private JPanel createRightControlsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 14, 14));
        panel.setOpaque(false);

        // Upper: Check-In & Check-Out Forms
        JPanel formsGrid = new JPanel(new GridLayout(1, 2, 12, 12));
        formsGrid.setOpaque(false);

        formsGrid.add(createCheckInBox());
        formsGrid.add(createCheckOutBox());
        panel.add(formsGrid);

        // Lower: Realtime Transaction Table
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

        JButton btnMasuk = createStyledButton("TAP IN & AMBIL KARCIS", ACCENT_BLUE);
        btnMasuk.addActionListener(e -> handleCheckIn());
        box.add(btnMasuk, BorderLayout.SOUTH);

        return box;
    }

    private JPanel createCheckOutBox() {
        JPanel box = createCardBox("CHECK-OUT & KASIR");

        JPanel form = new JPanel(new GridLayout(4, 2, 8, 10));
        form.setOpaque(false);

        form.add(createFieldLabel("No. Tiket:"));
        txtCariTiket = createStyledTextField("TKT-0001");
        form.add(txtCariTiket);

        form.add(createFieldLabel("Jam Keluar (0-23):"));
        txtJamKeluar = createStyledTextField("13");
        form.add(txtJamKeluar);

        form.add(createFieldLabel("Metode Bayar:"));
        cbMetodeBayar = new JComboBox<>(new String[]{"E_MONEY", "QRIS", "CASH"});
        styleComboBox(cbMetodeBayar);
        form.add(cbMetodeBayar);

        form.add(createFieldLabel("Nominal Bayar (Rp):"));
        txtUangBayar = createStyledTextField("50000");
        form.add(txtUangBayar);

        box.add(form, BorderLayout.CENTER);

        JButton btnKeluar = createStyledButton("BAYAR & BUKA GATE", ACCENT_GREEN);
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
        table.setBackground(new Color(24, 24, 37));
        table.setForeground(TEXT_WHITE);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(24);
        table.setShowGrid(false);
        table.getTableHeader().setBackground(new Color(36, 36, 56));
        table.getTableHeader().setForeground(TEXT_MUTED);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(center);
        }

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(new Color(24, 24, 37));
        box.add(sp, BorderLayout.CENTER);

        updateLogTable();
        return box;
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
                triggerGateAnimation(true, "GATE OPEN (IN)");
                refreshSlotsGrid();
                updateStats();
                updateLogTable();
                txtCariTiket.setText(tiket.getNomorTiket());
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
                JOptionPane.showMessageDialog(this, "Tiket " + noTiket + " tidak ditemukan!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (tiketDitemukan.isSudahDibayar()) {
                JOptionPane.showMessageDialog(this, "Tiket ini sudah pernah dibayar dan kendaraan telah keluar.", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean sukses = gedung.prosesKeluar(tiketDitemukan, jamKeluar, metode, uang);
            if (sukses) {
                triggerGateAnimation(true, "GATE OPEN (OUT)");
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

        lblTotalSlot.setText(String.format("<html><div style='text-align:right;'><span style='color:#9399b2;font-size:9px;'>TOTAL SLOT</span><br/><b style='font-size:14px;color:%s;'>%d</b></div></html>", toHex(ACCENT_BLUE), total));
        lblTersedia.setText(String.format("<html><div style='text-align:right;'><span style='color:#9399b2;font-size:9px;'>TERSEDIA</span><br/><b style='font-size:14px;color:%s;'>%d</b></div></html>", toHex(ACCENT_GREEN), tersedia));
        lblTerisi.setText(String.format("<html><div style='text-align:right;'><span style='color:#9399b2;font-size:9px;'>TERISI</span><br/><b style='font-size:14px;color:%s;'>%d</b></div></html>", toHex(ACCENT_RED), terisi));
        lblPendapatan.setText(String.format("<html><div style='text-align:right;'><span style='color:#9399b2;font-size:9px;'>KAS PENDAPATAN</span><br/><b style='font-size:14px;color:%s;'>%s</b></div></html>", toHex(ACCENT_YELLOW), rupiahFormat.format(gedung.getTotalPendapatan())));
    }

    private void updateLogTable() {
        logTableModel.setRowCount(0);
        for (KarcisParkir t : gedung.getRiwayatTransaksi()) {
            String jamKet = t.isSudahDibayar() ? (t.getJamMasuk() + ":00-" + t.getJamKeluar() + ":00") : (t.getJamMasuk() + ":00-...");
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

        // Auto close after 3 seconds
        if (open) {
            Timer closeTimer = new Timer(3000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    targetGateAngle = 0.0f;
                    gateStatusText = "GATE CLOSED";
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
            // 1. Pulse animation on empty slots
            if (pulseUp) {
                pulseAlpha += 0.02f;
                if (pulseAlpha >= 0.95f) pulseUp = false;
            } else {
                pulseAlpha -= 0.02f;
                if (pulseAlpha <= 0.35f) pulseUp = true;
            }

            // 2. Smooth Gate rotation interpolation
            if (Math.abs(gateAngle - targetGateAngle) > 0.5f) {
                gateAngle += (targetGateAngle - gateAngle) * 0.15f;
            } else {
                gateAngle = targetGateAngle;
            }

            repaint();
        });
        timer.start();
    }

    // Helper UI styling
    private JPanel createCardBox(String title) {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(TEXT_WHITE);
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
        tf.setBackground(new Color(24, 24, 37));
        tf.setForeground(TEXT_WHITE);
        tf.setCaretColor(TEXT_WHITE);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1, true),
                new EmptyBorder(4, 8, 4, 8)
        ));
        return tf;
    }

    private void styleComboBox(JComboBox<String> cb) {
        cb.setBackground(new Color(24, 24, 37));
        cb.setForeground(TEXT_WHITE);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(new Color(24, 24, 37));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private String toHex(Color col) {
        return String.format("#%02x%02x%02x", col.getRed(), col.getGreen(), col.getBlue());
    }

    public static void main(String[] args) {
        // Set Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            AdminGUI gui = new AdminGUI();
            gui.setVisible(true);
        });
    }
}

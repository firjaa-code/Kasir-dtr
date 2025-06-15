import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Main {
    public static void main(String[] args) {
        // Mengatur UIManager untuk mengubah warna tab
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.put("TabbedPane.selected", new Color(210, 230, 255));
            UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
            UIManager.put("TabbedPane.tabAreaInsets", new Insets(4, 4, 4, 4));
            UIManager.put("TabbedPane.selectedTabPadInsets", new Insets(2, 2, 2, 1));
            UIManager.put("TabbedPane.focus", new Color(0,0,0,0));
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            KasirFrame frame = new KasirFrame();
            frame.setVisible(true);
        });
    }

    // Inner class Menu (Tidak ada perubahan)
    static class Menu {
        private final String nama;
        private final double harga;
        public Menu(String nama, double harga) { this.nama = nama; this.harga = harga; }
        public String getNama() { return nama; }
        public double getHarga() { return harga; }
    }

    // Inner class Transaksi
    static class Transaksi {
        private Menu menuMakanan;
        private int jumlahMakanan;
        private final List<Menu> minuman;
        private final List<Integer> jumlahMinuman;
        private String jenisLayanan;
        private double biayaLayanan;

        public Transaksi() {
            minuman = new ArrayList<>();
            jumlahMinuman = new ArrayList<>();
        }
        public void setMenuMakanan(Menu menu, int jumlah) { this.menuMakanan = menu; this.jumlahMakanan = jumlah; }
        public void addMinuman(Menu minuman, int jumlah) { this.minuman.add(minuman); this.jumlahMinuman.add(jumlah); }
        public void setJenisLayanan(String jenisLayanan) { this.jenisLayanan = jenisLayanan; }

        public double getSubTotal() {
            double subTotal = 0;
            if (menuMakanan != null) {
                subTotal += menuMakanan.getHarga() * jumlahMakanan;
            }
            for (int i = 0; i < minuman.size(); i++) {
                subTotal += minuman.get(i).getHarga() * jumlahMinuman.get(i);
            }
            return subTotal;
        }

        public double getTotalBayar() {
            double subTotal = getSubTotal();
            biayaLayanan = 0;
            if ("delivery".equals(jenisLayanan)) {
                biayaLayanan = subTotal * 0.1;
            }
            return subTotal + biayaLayanan;
        }
        public void reset() {
            menuMakanan = null; jumlahMakanan = 0; minuman.clear(); jumlahMinuman.clear(); jenisLayanan = null; biayaLayanan = 0;
        }

        public Menu getMenuMakanan() { return menuMakanan; }
        public int getJumlahMakanan() { return jumlahMakanan; }
        public List<Menu> getMinuman() { return minuman; }
        public List<Integer> getJumlahMinuman() { return jumlahMinuman; }
        public double getBiayaLayanan() { return biayaLayanan; }
    }

    // Inner class GUI utama
    static class KasirFrame extends JFrame {
        private final Transaksi transaksi;
        private final Menu[] menuMakanan;
        private final Menu[] menuMinuman;
        private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

        // Komponen
        private JTabbedPane tabbedPane;
        private JComboBox<String> cbPaketMakanan;
        private JTextField tfNamaMenu, tfHargaMenu, tfSubtotalPesanan, tfSubTotal, tfJumlah;
        private JCheckBox[] cbMinuman;
        private JSpinner[] spJumlahMinuman;
        private JRadioButton rbDelivery, rbTakeAway;
        private ButtonGroup bgLayanan;
        private JTextField tfTotalBayar, tfUangBayar, tfKembalian;
        private JButton btnBersih, btnProses, btnKeluar, btnBuatStruk, btnNextTab, btnBackTab;
        private JTextArea taRingkasan;

        public KasirFrame() {
            // Inisialisasi data
            menuMakanan = new Menu[]{
                    new Menu("Paket Ayam Sambal Lalap", 20000), new Menu("Paket Nasi Ayam Geprek", 15000), new Menu("Paket Teriyaki Komplit", 25000)
            };
            menuMinuman = new Menu[]{
                    new Menu("Air Mineral", 5000), new Menu("Es Cincau Susu", 10000), new Menu("Es Teh Manis", 8000)
            };
            transaksi = new Transaksi();

            // Setup Frame
            setTitle("Dapur Tiga Rasa");
            setSize(550, 480);
            setResizable(false);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);

            try {
                ImageIcon frameIcon = new ImageIcon(getClass().getResource("/image/logo/Cooking Pot.png"));
                setIconImage(frameIcon.getImage());
            } catch (Exception e) {
                System.err.println("Window icon not found: /image/logo/Cooking Pot.png");
            }

            tabbedPane = new JTabbedPane();
            tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));

            tabbedPane.addTab("Pesanan", null, createPesananTab(), "Input pesanan makanan dan minuman");
            tabbedPane.addTab("Pembayaran", null, createPembayaranTab(), "Proses pembayaran dan aksi");

            getContentPane().add(tabbedPane, BorderLayout.CENTER);

            setupListeners();
            bersihkanForm();
        }

        private JPanel createPesananTab() {
            JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
            inputPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

            JPanel pesananWrapper = new JPanel();
            pesananWrapper.setLayout(new BoxLayout(pesananWrapper, BoxLayout.Y_AXIS));
            pesananWrapper.setBackground(Color.WHITE);

            JPanel makananPanel = new JPanel(new GridLayout(0, 2, 10, 10));
            makananPanel.setBorder(BorderFactory.createTitledBorder("Paket Makanan"));
            makananPanel.setBackground(Color.WHITE);
            cbPaketMakanan = new JComboBox<>();
            cbPaketMakanan.addItem("-- Pilih Paket --");
            for (Menu menu : menuMakanan) cbPaketMakanan.addItem(menu.getNama());
            tfNamaMenu = new JTextField(); tfNamaMenu.setEditable(false);
            tfHargaMenu = new JTextField(); tfHargaMenu.setEditable(false);
            tfJumlah = new JTextField();
            makananPanel.add(new JLabel("Paket Makanan:")); makananPanel.add(cbPaketMakanan);
            makananPanel.add(new JLabel("Nama Menu:")); makananPanel.add(tfNamaMenu);
            makananPanel.add(new JLabel("Harga:")); makananPanel.add(tfHargaMenu);
            makananPanel.add(new JLabel("Jumlah:")); makananPanel.add(tfJumlah);

            JPanel minumanPanel = new JPanel();
            minumanPanel.setLayout(new BoxLayout(minumanPanel, BoxLayout.Y_AXIS));
            minumanPanel.setBorder(BorderFactory.createTitledBorder("Minuman"));
            minumanPanel.setBackground(Color.WHITE);
            cbMinuman = new JCheckBox[menuMinuman.length];
            spJumlahMinuman = new JSpinner[menuMinuman.length];
            for (int i = 0; i < menuMinuman.length; i++) {
                JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
                rowPanel.setBackground(Color.WHITE);
                cbMinuman[i] = new JCheckBox(menuMinuman[i].getNama() + " (" + currencyFormatter.format(menuMinuman[i].getHarga()) + ")");
                spJumlahMinuman[i] = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
                spJumlahMinuman[i].setPreferredSize(new Dimension(60, 25));
                rowPanel.add(cbMinuman[i]); rowPanel.add(new JLabel("Jumlah:")); rowPanel.add(spJumlahMinuman[i]);
                minumanPanel.add(rowPanel);
            }

            pesananWrapper.add(makananPanel);
            pesananWrapper.add(Box.createVerticalStrut(15));
            pesananWrapper.add(minumanPanel);

            JPanel bottomPanel = new JPanel(new BorderLayout());
            bottomPanel.setBackground(Color.WHITE);
            JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            totalPanel.setBackground(Color.WHITE);
            totalPanel.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
            tfSubtotalPesanan = new JTextField(12);
            tfSubtotalPesanan.setEditable(false);
            tfSubtotalPesanan.setFont(new Font("Arial", Font.BOLD, 12));
            totalPanel.add(new JLabel("Subtotal:"));
            totalPanel.add(tfSubtotalPesanan);

            btnNextTab = new JButton();
            try {
                ImageIcon nextIcon = new ImageIcon(getClass().getResource("/image/next/Next Page_3.png"));
                btnNextTab.setIcon(nextIcon);
            } catch (Exception e) {
                btnNextTab.setText("Next >");
                System.err.println("Icon not found: /image/next/Next Page_3.png");
            }
            btnNextTab.setPreferredSize(new Dimension(40, 40));
            btnNextTab.setBorderPainted(false);
            btnNextTab.setFocusPainted(false);
            btnNextTab.setContentAreaFilled(false);

            JPanel nextButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            nextButtonPanel.setBackground(Color.WHITE);
            nextButtonPanel.add(btnNextTab);

            bottomPanel.add(totalPanel, BorderLayout.CENTER);
            bottomPanel.add(nextButtonPanel, BorderLayout.EAST);

            inputPanel.add(new JScrollPane(pesananWrapper), BorderLayout.CENTER);
            inputPanel.add(bottomPanel, BorderLayout.SOUTH);

            return inputPanel;
        }

        private JPanel createPembayaranTab() {
            JPanel pembayaranTabPanel = new JPanel(new BorderLayout(10, 10));
            pembayaranTabPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JPanel centerPanel = new JPanel();
            centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

            JPanel ringkasanPanel = new JPanel(new BorderLayout(5,5));
            ringkasanPanel.setBorder(BorderFactory.createTitledBorder("Ringkasan Pesanan"));
            taRingkasan = new JTextArea(3, 30); taRingkasan.setEditable(false);
            tfSubTotal = new JTextField(); tfSubTotal.setEditable(false);
            tfSubTotal.setFont(new Font("Arial", Font.BOLD, 12));
            JPanel subTotalWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            subTotalWrapper.add(new JLabel("Subtotal:")); subTotalWrapper.add(tfSubTotal);
            ringkasanPanel.add(new JScrollPane(taRingkasan), BorderLayout.CENTER);
            ringkasanPanel.add(subTotalWrapper, BorderLayout.SOUTH);

            JPanel layananPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
            layananPanel.setBorder(BorderFactory.createTitledBorder("Jenis Layanan (klik lagi untuk batal)"));
            bgLayanan = new ButtonGroup();
            rbDelivery = new JRadioButton("Delivery (+10%)");
            rbTakeAway = new JRadioButton("Take Away");
            bgLayanan.add(rbDelivery); bgLayanan.add(rbTakeAway);
            layananPanel.add(rbTakeAway); layananPanel.add(rbDelivery);

            JPanel paymentPanel = new JPanel(new GridLayout(0, 2, 10, 10));
            paymentPanel.setBorder(BorderFactory.createTitledBorder("Pembayaran"));
            tfTotalBayar = new JTextField(); tfTotalBayar.setEditable(false);
            tfTotalBayar.setFont(new Font("Arial", Font.BOLD, 12));
            tfUangBayar = new JTextField();
            tfKembalian = new JTextField(); tfKembalian.setEditable(false);
            paymentPanel.add(new JLabel("Total Bayar:")); paymentPanel.add(tfTotalBayar);
            paymentPanel.add(new JLabel("Uang Bayar:")); paymentPanel.add(tfUangBayar);
            paymentPanel.add(new JLabel("Kembalian:")); paymentPanel.add(tfKembalian);

            centerPanel.add(ringkasanPanel);
            centerPanel.add(Box.createVerticalStrut(5));
            centerPanel.add(layananPanel);
            centerPanel.add(Box.createVerticalStrut(5));
            centerPanel.add(paymentPanel);

            JPanel buttonPanel = new JPanel(new BorderLayout());

            btnBackTab = new JButton();
            try {
                ImageIcon backIcon = new ImageIcon(getClass().getResource("/image/back/Back To_1.png"));
                btnBackTab.setIcon(backIcon);
            } catch (Exception e) {
                btnBackTab.setText("< Back");
                System.err.println("Icon not found: /image/back/Back To_1.png");
            }
            btnBackTab.setPreferredSize(new Dimension(40,40));
            btnBackTab.setBorderPainted(false);
            btnBackTab.setFocusPainted(false);
            btnBackTab.setContentAreaFilled(false);

            JPanel backButtonWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
            backButtonWrapper.add(btnBackTab);

            JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
            btnBersih = new JButton("Bersih");
            btnProses = new JButton("Proses");
            btnKeluar = new JButton("Keluar");
            btnBuatStruk = new JButton("Buat Struk");

            btnProses.setBackground(new Color(60, 179, 113)); btnProses.setForeground(Color.WHITE);
            btnBuatStruk.setBackground(new Color(70, 130, 180)); btnBuatStruk.setForeground(Color.WHITE);
            btnKeluar.setBackground(new Color(205, 92, 92)); btnKeluar.setForeground(Color.WHITE);
            btnBersih.setBackground(new Color(240, 180, 50));
            btnBersih.setForeground(Color.WHITE);

            btnProses.setFocusPainted(false); btnProses.setBorderPainted(false);
            btnBuatStruk.setFocusPainted(false); btnBuatStruk.setBorderPainted(false);
            btnBersih.setFocusPainted(false); btnBersih.setBorderPainted(false);
            btnKeluar.setFocusPainted(false); btnKeluar.setBorderPainted(false);

            btnBuatStruk.setEnabled(false);

            actionButtonPanel.add(btnProses); actionButtonPanel.add(btnBuatStruk);
            actionButtonPanel.add(btnBersih); actionButtonPanel.add(btnKeluar);

            buttonPanel.add(backButtonWrapper, BorderLayout.WEST);
            buttonPanel.add(actionButtonPanel, BorderLayout.CENTER);

            pembayaranTabPanel.add(centerPanel, BorderLayout.CENTER);
            pembayaranTabPanel.add(buttonPanel, BorderLayout.SOUTH);
            return pembayaranTabPanel;
        }

        private void setupListeners() {
            ActionListener updateListener = e -> updateAllTotals();

            // === PERUBAHAN: Listener diubah dari KeyListener menjadi ActionListener ===
            // Subtotal hanya di-update setelah menekan Enter di field Jumlah
            tfJumlah.addActionListener(e -> updateAllTotals());
            cbPaketMakanan.addActionListener(updateListener);

            for (int i = 0; i < cbMinuman.length; i++) {
                final int index = i;
                cbMinuman[index].addActionListener(e -> {
                    if (cbMinuman[index].isSelected() && (Integer) spJumlahMinuman[index].getValue() == 0) {
                        spJumlahMinuman[index].setValue(1);
                    } else if (!cbMinuman[index].isSelected()) {
                        spJumlahMinuman[index].setValue(0);
                    }
                    updateAllTotals();
                });
                spJumlahMinuman[index].addChangeListener(e -> {
                    cbMinuman[index].setSelected((Integer) spJumlahMinuman[index].getValue() > 0);
                    updateAllTotals();
                });
            }

            MouseAdapter deselectListener = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    JRadioButton btn = (JRadioButton) e.getSource();
                    if (btn.isSelected()) {
                        SwingUtilities.invokeLater(() -> {
                            bgLayanan.clearSelection();
                            updateAllTotals();
                        });
                    }
                }
            };
            rbDelivery.addMouseListener(deselectListener);
            rbTakeAway.addMouseListener(deselectListener);
            rbDelivery.addActionListener(updateListener);
            rbTakeAway.addActionListener(updateListener);

            btnNextTab.addActionListener(e -> tabbedPane.setSelectedIndex(1));
            btnBackTab.addActionListener(e -> tabbedPane.setSelectedIndex(0));
            btnBersih.addActionListener(e -> bersihkanForm());
            btnProses.addActionListener(e -> performPayment());
            btnKeluar.addActionListener(e -> keluarAplikasi());
            btnBuatStruk.addActionListener(e -> tampilkanStruk());

            tfUangBayar.addActionListener(e -> performPayment());

            this.setFocusable(true);
            this.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    int currentIndex = tabbedPane.getSelectedIndex();
                    if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                        if (currentIndex < tabbedPane.getTabCount() - 1) {
                            tabbedPane.setSelectedIndex(currentIndex + 1);
                        }
                    } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                        if (currentIndex > 0) {
                            tabbedPane.setSelectedIndex(currentIndex - 1);
                        }
                    }
                }
            });
        }

        private void updateAllTotals() {
            transaksi.reset();
            StringBuilder ringkasan = new StringBuilder();

            int index = cbPaketMakanan.getSelectedIndex();
            if (index > 0) {
                Menu menu = menuMakanan[index - 1];
                int jumlah = 0;
                try {
                    String jumlahText = tfJumlah.getText();
                    if (!jumlahText.isEmpty()) {
                        jumlah = Integer.parseInt(jumlahText);
                    }
                } catch (NumberFormatException e) {
                    jumlah = 0; // Abaikan input non-numerik
                }

                if (jumlah > 0) {
                    transaksi.setMenuMakanan(menu, jumlah);
                    ringkasan.append(String.format("- %s x%d\n", menu.getNama(), jumlah));
                }
                tfNamaMenu.setText(menu.getNama());
                tfHargaMenu.setText(currencyFormatter.format(menu.getHarga()));
            } else {
                tfNamaMenu.setText(""); tfHargaMenu.setText("");
            }

            for (int i = 0; i < cbMinuman.length; i++) {
                if(cbMinuman[i].isSelected()) {
                    int jumlah = (Integer) spJumlahMinuman[i].getValue();
                    if (jumlah > 0) {
                        transaksi.addMinuman(menuMinuman[i], jumlah);
                        ringkasan.append(String.format("- %s x%d\n", menuMinuman[i].getNama(), jumlah));
                    }
                }
            }

            if (rbDelivery.isSelected()) {
                transaksi.setJenisLayanan("delivery");
            } else if (rbTakeAway.isSelected()) {
                transaksi.setJenisLayanan("take away");
            } else {
                transaksi.setJenisLayanan(null);
            }
            double subTotal = transaksi.getSubTotal();
            double totalFinal = transaksi.getTotalBayar();

            tfSubtotalPesanan.setText(currencyFormatter.format(subTotal));
            tfSubTotal.setText(currencyFormatter.format(subTotal));
            tfTotalBayar.setText(currencyFormatter.format(totalFinal));
            taRingkasan.setText(ringkasan.toString());

            tfKembalian.setText("");
            btnBuatStruk.setEnabled(false);
            btnProses.setEnabled(true);
        }

        private void performPayment() {
            double totalFinal;
            try {
                totalFinal = currencyFormatter.parse(tfTotalBayar.getText()).doubleValue();
            } catch (Exception e) {
                totalFinal = 0;
            }

            try {
                String uangBayarText = tfUangBayar.getText();
                if (uangBayarText.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Masukkan jumlah uang bayar!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                double uangBayar = Double.parseDouble(uangBayarText.replace(",", ""));
                if (uangBayar >= totalFinal) {
                    double kembalian = uangBayar - totalFinal;
                    tfKembalian.setText(currencyFormatter.format(kembalian));
                    btnBuatStruk.setEnabled(true);
                    btnProses.setEnabled(false);
                } else {
                    JOptionPane.showMessageDialog(this, "Uang bayar kurang!", "Error", JOptionPane.ERROR_MESSAGE);
                    btnBuatStruk.setEnabled(false);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Masukkan uang bayar yang valid.", "Error", JOptionPane.ERROR_MESSAGE);
                btnBuatStruk.setEnabled(false);
            }
        }

        private void bersihkanForm() {
            transaksi.reset();
            cbPaketMakanan.setSelectedIndex(0);
            tfJumlah.setText("");
            tfSubtotalPesanan.setText("");
            for (int i = 0; i < cbMinuman.length; i++) {
                cbMinuman[i].setSelected(false); spJumlahMinuman[i].setValue(0);
            }

            taRingkasan.setText("");
            tfSubTotal.setText("");
            bgLayanan.clearSelection();
            tfTotalBayar.setText("");
            tfUangBayar.setText("");
            tfKembalian.setText("");
            btnBuatStruk.setEnabled(false);
            btnProses.setEnabled(true);
            updateAllTotals();
            this.requestFocusInWindow();
        }

        private void tampilkanStruk() {
            StringBuilder strukText = new StringBuilder();
            strukText.append("<html><pre>");
            strukText.append("=====================================\n");
            strukText.append("           STRUK PEMBAYARAN\n");
            strukText.append("            Dapur Tiga Rasa\n");
            strukText.append("=====================================\n\n");
            strukText.append("Deskripsi Pesanan:\n");
            strukText.append(taRingkasan.getText());
            strukText.append("-------------------------------------\n");
            strukText.append(String.format("Subtotal: %26s\n", tfSubTotal.getText()));

            if (transaksi.getBiayaLayanan() > 0) {
                strukText.append(String.format("Biaya Layanan: %19s\n", currencyFormatter.format(transaksi.getBiayaLayanan())));
            }
            strukText.append("-------------------------------------\n");
            strukText.append(String.format("TOTAL BAYAR: %21s\n", tfTotalBayar.getText()));
            strukText.append(String.format("Uang Bayar: %22s\n", currencyFormatter.format(Double.parseDouble(tfUangBayar.getText()))));
            strukText.append(String.format("Kembalian: %23s\n", tfKembalian.getText()));

            strukText.append("\n=====================================\n");
            strukText.append("   Terima Kasih Atas Kunjungan Anda!   \n");
            strukText.append("=====================================\n");
            strukText.append("</pre></html>");

            JOptionPane.showMessageDialog(this, new JLabel(strukText.toString()), "Struk Pembayaran", JOptionPane.PLAIN_MESSAGE);
        }

        private void keluarAplikasi() {
            if (JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin menutup aplikasi?", "Konfirmasi", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        }
    }
}
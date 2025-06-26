import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            KasirFrame frame = new KasirFrame();
            frame.setVisible(true);
        });
    }

    // Inner class Menu
    static class Menu {
        private final String nama;
        private final double harga;
        public Menu(String nama, double harga) { this.nama = nama; this.harga = harga; }
        public String getNama() { return nama; }
        public double getHarga() { return harga; }
    }

    // Inner class Transaksi
    static class Transaksi {
        private final List<Menu> makanan;
        private final List<Integer> jumlahMakanan;
        private final List<Menu> minuman;
        private final List<Integer> jumlahMinuman;
        private String jenisLayanan;
        private double biayaLayanan;

        public Transaksi() {
            makanan = new ArrayList<>();
            jumlahMakanan = new ArrayList<>();
            minuman = new ArrayList<>();
            jumlahMinuman = new ArrayList<>();
        }
        public void addMakanan(Menu menu, int jumlah) { this.makanan.add(menu); this.jumlahMakanan.add(jumlah); }
        public void addMinuman(Menu minuman, int jumlah) { this.minuman.add(minuman); this.jumlahMinuman.add(jumlah); }
        public void setJenisLayanan(String jenisLayanan) { this.jenisLayanan = jenisLayanan; }
        public double getSubTotal() {
            double subTotal = 0;
            for (int i = 0; i < makanan.size(); i++) {
                subTotal += makanan.get(i).getHarga() * jumlahMakanan.get(i);
            }
            for (int i = 0; i < minuman.size(); i++) {
                subTotal += minuman.get(i).getHarga() * jumlahMinuman.get(i);
            }
            return subTotal;
        }
        public double getTotalBayar() {
            double subTotal = getSubTotal();
            biayaLayanan = 0;
            if ("delivery".equals(jenisLayanan)) { biayaLayanan = subTotal * 0.1; }
            return subTotal + biayaLayanan;
        }
        public void reset() {
            makanan.clear(); jumlahMakanan.clear(); minuman.clear(); jumlahMinuman.clear(); jenisLayanan = null; biayaLayanan = 0;
        }
        public List<Menu> getMakanan() { return makanan; }
        public List<Integer> getJumlahMakanan() { return jumlahMakanan; }
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

        private CardLayout cardLayout;
        private JPanel mainPanel;
        private JComboBox<String> cbPaketMakanan;
        private JTextField tfNamaMenu, tfHargaMenu, tfSubTotal, tfJumlah;
        private JButton btnAddMakanan;
        private JTable tblPesananMakanan;
        private DefaultTableModel tblModelMakanan;

        private JCheckBox[] cbMinuman;
        private JSpinner[] spJumlahMinuman;
        private JRadioButton rbDelivery, rbTakeAway;
        private ButtonGroup bgLayanan;
        private JTextField tfTotalBayar, tfUangBayar, tfKembalian;
        private JTextField tfBiayaLayanan;

        private JButton btnBack, btnProses, btnKeluar, btnBuatStruk, btnNext, btnClear;
        private JTextArea taRingkasan;

        private List<JButton> sidebarButtons;
        private final Color sidebarSelectedColor = new Color(25, 118, 210);
        private static final String STATE_FILE_PATH = "kasir_state.properties";

        // ### METHOD WITH CORRECTIONS ###
        public KasirFrame() {
            menuMakanan = new Menu[]{
                    new Menu("Paket Ayam Sambal Lalap", 20000), new Menu("Paket Nasi Ayam Geprek", 15000), new Menu("Paket Teriyaki Komplit", 25000)
            };
            menuMinuman = new Menu[]{
                    new Menu("Air Mineral", 5000), new Menu("Es Cincau Susu", 10000), new Menu("Es Teh Manis", 8000)
            };
            transaksi = new Transaksi();

            setTitle("Kasir");
            setSize(800, 650);
            setResizable(false);
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            setLocationRelativeTo(null);

            // MODIFIED: Menambahkan ikon aplikasi pada frame
            try {
                ImageIcon appIcon = new ImageIcon(getClass().getResource("/image/logoapp.png"));
                setIconImage(appIcon.getImage());
            } catch (Exception e) {
                System.err.println("Ikon aplikasi tidak ditemukan di /image/logoapp.png");
            }

            getContentPane().setLayout(new BorderLayout());

            JPanel sidebar = createSidebar();
            getContentPane().add(sidebar, BorderLayout.WEST);

            cardLayout = new CardLayout();
            mainPanel = new JPanel(cardLayout);
            mainPanel.add(createPesananPanel(), "PESANAN");
            mainPanel.add(createPembayaranPanel(), "PEMBAYARAN");

            getContentPane().add(mainPanel, BorderLayout.CENTER);

            setupListeners();
            bersihkanForm();

            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    keluarAplikasi();
                }
            });
            loadState();
        }

        private JPanel createSidebar() {
            JPanel sidebarPanel = new JPanel();
            sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
            sidebarPanel.setBackground(new Color(54, 54, 54));
            sidebarPanel.setPreferredSize(new Dimension(80, 0));
            sidebarPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

            sidebarButtons = new ArrayList<>();

            JButton btnHome = createSidebarButton("Home", "/image/home.png");
            JButton btnPayment = createSidebarButton("Payment", "/image/card.png");
            JButton btnExit = createSidebarButton("Exit", "/image/exit.png");

            sidebarButtons.add(btnHome);
            sidebarButtons.add(btnPayment);

            btnHome.addActionListener(e -> {
                cardLayout.show(mainPanel, "PESANAN");
                updateSelectedButtonUI(btnHome);
            });
            btnPayment.addActionListener(e -> {
                updateRingkasanAndTotals();
                cardLayout.show(mainPanel, "PEMBAYARAN");
                updateSelectedButtonUI(btnPayment);
            });
            btnExit.addActionListener(e -> keluarAplikasi());

            sidebarPanel.add(btnHome);
            sidebarPanel.add(Box.createVerticalStrut(15));
            sidebarPanel.add(btnPayment);
            sidebarPanel.add(Box.createVerticalGlue());
            sidebarPanel.add(btnExit);

            updateSelectedButtonUI(btnHome);

            return sidebarPanel;
        }

        private JButton createSidebarButton(String text, String iconPath) {
            JButton button = new JButton(text);
            String hoverIconPath = iconPath.replace(".png", "_hover.png");
            Icon originalIcon = loadSidebarIcon(iconPath);
            Icon hoverIcon = loadSidebarIcon(hoverIconPath);
            button.putClientProperty("originalIcon", originalIcon);
            button.putClientProperty("hoverIcon", hoverIcon);
            if (originalIcon != null) {
                button.setIcon(originalIcon);
                button.setText("");
            }
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.setVerticalTextPosition(SwingConstants.BOTTOM);
            button.setHorizontalTextPosition(SwingConstants.CENTER);
            button.setForeground(Color.WHITE);
            button.setFont(new Font("Arial", Font.BOLD, 12));
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setContentAreaFilled(false);
            button.setMargin(new Insets(10, 0, 10, 0));
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getPreferredSize().height + 10));
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    Icon icon = (Icon) button.getClientProperty("hoverIcon");
                    if (icon != null) button.setIcon(icon);
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    Icon icon = (Icon) button.getClientProperty("originalIcon");
                    if (icon != null) button.setIcon(icon);
                }
            });
            return button;
        }

        private Icon loadSidebarIcon(String path) {
            try {
                return new ImageIcon(new ImageIcon(getClass().getResource(path)).getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));
            } catch (Exception e) {
                System.err.println("Icon not found: " + path);
                return null;
            }
        }

        private void updateSelectedButtonUI(JButton selectedButton) {
            for (JButton button : sidebarButtons) {
                button.setOpaque(button == selectedButton);
                button.setBackground(button == selectedButton ? sidebarSelectedColor : null);
            }
        }

        private JPanel createPesananPanel() {
            JPanel pesananPanel = new JPanel(new BorderLayout(10, 10));
            pesananPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            JPanel makananSectionPanel = new JPanel(new GridLayout(1, 2, 15, 0));
            makananSectionPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Paket Makanan", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 14)));
            JPanel makananInputPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            cbPaketMakanan = new JComboBox<>();
            cbPaketMakanan.addItem("-- Pilih Paket Makanan --");
            for (Menu menu : menuMakanan) cbPaketMakanan.addItem(menu.getNama());
            tfNamaMenu = new JTextField(15); tfNamaMenu.setEditable(false);
            tfHargaMenu = new JTextField(15); tfHargaMenu.setEditable(false);
            tfJumlah = new JTextField(5);
            btnAddMakanan = new JButton("ADD");
            gbc.gridx = 0; gbc.gridy = 0; makananInputPanel.add(new JLabel("Pilih Paket Makanan:"), gbc);
            gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 2; makananInputPanel.add(cbPaketMakanan, gbc);
            gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; makananInputPanel.add(new JLabel("Nama Paket Makanan:"), gbc);
            gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 2; makananInputPanel.add(tfNamaMenu, gbc);
            gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1; makananInputPanel.add(new JLabel("Harga:"), gbc);
            gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 2; makananInputPanel.add(tfHargaMenu, gbc);
            gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1; makananInputPanel.add(new JLabel("Jumlah:"), gbc);
            gbc.gridx = 1; gbc.gridy = 3; gbc.gridwidth = 1; gbc.fill=GridBagConstraints.NONE; makananInputPanel.add(tfJumlah, gbc);
            gbc.gridx = 2; gbc.gridy = 3; makananInputPanel.add(btnAddMakanan, gbc);
            tblModelMakanan = new DefaultTableModel(new String[]{"Nama Paket", "Jumlah"}, 0){
                @Override public boolean isCellEditable(int row, int column) { return false; }
            };
            tblPesananMakanan = new JTable(tblModelMakanan);
            JScrollPane tableScrollPane = new JScrollPane(tblPesananMakanan);
            tableScrollPane.setPreferredSize(new Dimension(300, 150));
            makananSectionPanel.add(makananInputPanel);
            makananSectionPanel.add(tableScrollPane);
            JPanel minumanPanel = new JPanel();
            minumanPanel.setLayout(new BoxLayout(minumanPanel, BoxLayout.Y_AXIS));
            minumanPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Minuman", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 14)));
            cbMinuman = new JCheckBox[menuMinuman.length];
            spJumlahMinuman = new JSpinner[menuMinuman.length];
            for (int i = 0; i < menuMinuman.length; i++) {
                JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
                cbMinuman[i] = new JCheckBox(menuMinuman[i].getNama() + " (" + currencyFormatter.format(menuMinuman[i].getHarga()) + ")");
                spJumlahMinuman[i] = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
                spJumlahMinuman[i].setPreferredSize(new Dimension(60, 25));
                rowPanel.add(cbMinuman[i]); rowPanel.add(new JLabel("Jumlah:")); rowPanel.add(spJumlahMinuman[i]);
                rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, rowPanel.getPreferredSize().height));
                minumanPanel.add(rowPanel);
            }
            JPanel contentWrapper = new JPanel();
            contentWrapper.setLayout(new BoxLayout(contentWrapper, BoxLayout.Y_AXIS));
            contentWrapper.add(makananSectionPanel);
            contentWrapper.add(Box.createVerticalStrut(10));
            contentWrapper.add(minumanPanel);
            makananSectionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, makananSectionPanel.getPreferredSize().height));
            minumanPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, minumanPanel.getPreferredSize().height));
            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
            bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
            tfSubTotal = new JTextField(12); tfSubTotal.setEditable(false); tfSubTotal.setFont(new Font("Arial", Font.BOLD, 14));
            btnClear = new JButton("Clear"); btnNext = new JButton("Next");
            bottomPanel.add(new JLabel("Subtotal:")); bottomPanel.add(tfSubTotal); bottomPanel.add(btnClear); bottomPanel.add(btnNext);
            pesananPanel.add(contentWrapper, BorderLayout.NORTH);
            pesananPanel.add(bottomPanel, BorderLayout.SOUTH);
            return pesananPanel;
        }

        private JPanel createPembayaranPanel() {
            JPanel pembayaranTabPanel = new JPanel(new BorderLayout(10, 10));
            pembayaranTabPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            JPanel centerPanel = new JPanel();
            centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
            JPanel ringkasanPanel = new JPanel(new BorderLayout(5,5));
            ringkasanPanel.setBorder(BorderFactory.createTitledBorder("Ringkasan Pesanan"));
            taRingkasan = new JTextArea(5, 30); taRingkasan.setEditable(false);
            taRingkasan.setFont(new Font("Monospaced", Font.PLAIN, 12));
            ringkasanPanel.add(new JScrollPane(taRingkasan), BorderLayout.CENTER);
            JPanel layananPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
            layananPanel.setBorder(BorderFactory.createTitledBorder("Jenis Layanan (klik lagi untuk batal)"));
            bgLayanan = new ButtonGroup();
            rbDelivery = new JRadioButton("Delivery (+10%)"); rbTakeAway = new JRadioButton("Take Away");
            bgLayanan.add(rbDelivery); bgLayanan.add(rbTakeAway);
            tfBiayaLayanan = new JTextField(8); tfBiayaLayanan.setEditable(false); tfBiayaLayanan.setFont(new Font("Arial", Font.BOLD, 12));
            btnProses = new JButton("Proses");
            layananPanel.add(rbTakeAway); layananPanel.add(rbDelivery); layananPanel.add(Box.createHorizontalStrut(20));
            layananPanel.add(new JLabel("Biaya Layanan:")); layananPanel.add(tfBiayaLayanan); layananPanel.add(Box.createHorizontalStrut(5)); layananPanel.add(btnProses);
            JPanel paymentPanel = new JPanel(new GridLayout(0, 2, 10, 10));
            paymentPanel.setBorder(BorderFactory.createTitledBorder("Pembayaran"));
            tfTotalBayar = new JTextField(); tfTotalBayar.setEditable(false); tfTotalBayar.setFont(new Font("Arial", Font.BOLD, 12));
            tfUangBayar = new JTextField(); tfKembalian = new JTextField(); tfKembalian.setEditable(false);
            paymentPanel.add(new JLabel("Total Bayar:")); paymentPanel.add(tfTotalBayar);
            paymentPanel.add(new JLabel("Uang Bayar:")); paymentPanel.add(tfUangBayar);
            paymentPanel.add(new JLabel("Kembalian:")); paymentPanel.add(tfKembalian);
            centerPanel.add(ringkasanPanel); centerPanel.add(Box.createVerticalStrut(5)); centerPanel.add(layananPanel);
            centerPanel.add(Box.createVerticalStrut(5)); centerPanel.add(paymentPanel);
            JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
            btnBack = new JButton("Back"); btnKeluar = new JButton("Keluar"); btnBuatStruk = new JButton("Buat Struk");
            btnBuatStruk.setEnabled(false);
            actionButtonPanel.add(btnBack); actionButtonPanel.add(btnBuatStruk); actionButtonPanel.add(btnKeluar);
            pembayaranTabPanel.add(centerPanel, BorderLayout.CENTER);
            pembayaranTabPanel.add(actionButtonPanel, BorderLayout.SOUTH);
            return pembayaranTabPanel;
        }

        private void setupListeners() {
            cbPaketMakanan.addActionListener(e -> {
                int index = cbPaketMakanan.getSelectedIndex();
                if (index > 0) {
                    Menu menu = menuMakanan[index - 1];
                    tfNamaMenu.setText(menu.getNama());
                    tfHargaMenu.setText(currencyFormatter.format(menu.getHarga()));
                } else {
                    tfNamaMenu.setText("");
                    tfHargaMenu.setText("");
                }
            });
            btnAddMakanan.addActionListener(e -> addMakananToTable());
            tfJumlah.addActionListener(e -> addMakananToTable());
            for (int i = 0; i < cbMinuman.length; i++) {
                final int index = i;
                cbMinuman[index].addActionListener(e -> {
                    if (cbMinuman[index].isSelected() && (Integer) spJumlahMinuman[index].getValue() == 0) {
                        spJumlahMinuman[index].setValue(1);
                    } else if (!cbMinuman[index].isSelected()) {
                        spJumlahMinuman[index].setValue(0);
                    }
                    updateSubTotal();
                });
                spJumlahMinuman[index].addChangeListener(e -> {
                    cbMinuman[index].setSelected((Integer) spJumlahMinuman[index].getValue() > 0);
                    updateSubTotal();
                });
            }
            btnClear.addActionListener(e -> {
                bersihkanForm();
                File stateFile = new File(STATE_FILE_PATH);
                if (stateFile.exists()) {
                    stateFile.delete();
                }
            });
            btnNext.addActionListener(e -> {
                updateRingkasanAndTotals();
                cardLayout.show(mainPanel, "PEMBAYARAN");
                updateSelectedButtonUI(sidebarButtons.get(1));
            });
            MouseAdapter deselectListener = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    JRadioButton btn = (JRadioButton) e.getSource();
                    if (btn.isSelected()) {
                        SwingUtilities.invokeLater(() -> {
                            bgLayanan.clearSelection();
                            updateLayananDetails();
                        });
                    }
                }
            };
            ActionListener layananListener = e -> updateLayananDetails();
            rbDelivery.addMouseListener(deselectListener);
            rbTakeAway.addMouseListener(deselectListener);
            rbDelivery.addActionListener(layananListener);
            rbTakeAway.addActionListener(layananListener);
            btnProses.addActionListener(e -> updateTotalBayarDenganLayanan(true));
            btnBack.addActionListener(e -> {
                cardLayout.show(mainPanel, "PESANAN");
                updateSelectedButtonUI(sidebarButtons.get(0));
            });
            btnKeluar.addActionListener(e -> keluarAplikasi());
            btnBuatStruk.addActionListener(e -> tampilkanStruk());
            tfUangBayar.addActionListener(e -> performPayment());
        }

        private void addMakananToTable() {
            int index = cbPaketMakanan.getSelectedIndex();
            if (index <= 0) {
                JOptionPane.showMessageDialog(this, "Silakan pilih paket makanan terlebih dahulu.", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int jumlahBaru;
            try {
                jumlahBaru = Integer.parseInt(tfJumlah.getText());
                if (jumlahBaru <= 0) {
                    JOptionPane.showMessageDialog(this, "Jumlah harus lebih dari 0.", "Peringatan", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Masukkan jumlah yang valid.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Menu selectedMenu = menuMakanan[index - 1];
            String namaMenuYangDitambahkan = selectedMenu.getNama();
            for (int i = 0; i < tblModelMakanan.getRowCount(); i++) {
                String namaMenuDiTabel = (String) tblModelMakanan.getValueAt(i, 0);
                if (namaMenuDiTabel.equals(namaMenuYangDitambahkan)) {
                    int jumlahLama = (int) tblModelMakanan.getValueAt(i, 1);
                    int jumlahTotal = jumlahLama + jumlahBaru;
                    tblModelMakanan.setValueAt(jumlahTotal, i, 1);
                    resetInputMakanan();
                    updateSubTotal();
                    return;
                }
            }
            tblModelMakanan.addRow(new Object[]{namaMenuYangDitambahkan, jumlahBaru});
            resetInputMakanan();
            updateSubTotal();
        }

        private void resetInputMakanan() {
            cbPaketMakanan.setSelectedIndex(0);
            tfJumlah.setText("");
            tfNamaMenu.setText("");
            tfHargaMenu.setText("");
        }

        private void updateSubTotal() {
            double subTotal = 0;
            for (int i = 0; i < tblModelMakanan.getRowCount(); i++) {
                String namaMenu = (String) tblModelMakanan.getValueAt(i, 0);
                int jumlah = (int) tblModelMakanan.getValueAt(i, 1);
                for(Menu menu : menuMakanan) {
                    if(menu.getNama().equals(namaMenu)) {
                        subTotal += menu.getHarga() * jumlah;
                        break;
                    }
                }
            }
            for (int i = 0; i < cbMinuman.length; i++) {
                if(cbMinuman[i].isSelected()) {
                    int jumlah = (Integer) spJumlahMinuman[i].getValue();
                    if (jumlah > 0) {
                        subTotal += menuMinuman[i].getHarga() * jumlah;
                    }
                }
            }
            tfSubTotal.setText(currencyFormatter.format(subTotal));
            syncTransaksiObjectFromUI();
            updateLayananDetails();
        }

        private void syncTransaksiObjectFromUI() {
            transaksi.reset();
            StringBuilder ringkasan = new StringBuilder();

            ringkasan.append(String.format("%-24s %5s %12s\n", "Item", "Qty", "Total"));
            ringkasan.append("--------------------------------------------\n");

            for (int i = 0; i < tblModelMakanan.getRowCount(); i++) {
                String namaMenu = (String) tblModelMakanan.getValueAt(i, 0);
                int jumlah = (int) tblModelMakanan.getValueAt(i, 1);
                for (Menu menu : menuMakanan) {
                    if (menu.getNama().equals(namaMenu)) {
                        transaksi.addMakanan(menu, jumlah);
                        String totalItem = currencyFormatter.format(menu.getHarga() * jumlah);
                        ringkasan.append(String.format("%-24s %5d %12s\n", menu.getNama(), jumlah, totalItem));
                        break;
                    }
                }
            }
            for (int i = 0; i < cbMinuman.length; i++) {
                if (cbMinuman[i].isSelected()) {
                    int jumlah = (Integer) spJumlahMinuman[i].getValue();
                    if (jumlah > 0) {
                        transaksi.addMinuman(menuMinuman[i], jumlah);
                        String totalItem = currencyFormatter.format(menuMinuman[i].getHarga() * jumlah);
                        ringkasan.append(String.format("%-24s %5d %12s\n", menuMinuman[i].getNama(), jumlah, totalItem));
                    }
                }
            }

            ringkasan.append("--------------------------------------------\n");
            String formattedSubtotal = currencyFormatter.format(transaksi.getSubTotal());
            ringkasan.append(String.format("%-30s %12s\n", "Subtotal", formattedSubtotal));


            if (rbDelivery.isSelected()) {
                transaksi.setJenisLayanan("delivery");
            } else if (rbTakeAway.isSelected()) {
                transaksi.setJenisLayanan("take away");
            } else {
                transaksi.setJenisLayanan(null);
            }
            taRingkasan.setText(ringkasan.toString());
        }

        private void updateRingkasanAndTotals() {
            syncTransaksiObjectFromUI();
            tfTotalBayar.setText(currencyFormatter.format(transaksi.getSubTotal()));
            updateLayananDetails();
            tfKembalian.setText("");
            btnBuatStruk.setEnabled(false);
        }

        private void updateLayananDetails() {
            syncTransaksiObjectFromUI();
            double subTotal = transaksi.getSubTotal();
            double biaya = 0;
            if (rbDelivery.isSelected()) {
                biaya = subTotal * 0.1;
            }
            tfBiayaLayanan.setText(currencyFormatter.format(biaya));
        }

        private void updateTotalBayarDenganLayanan(boolean showDialog) {
            syncTransaksiObjectFromUI();
            double totalFinal = transaksi.getTotalBayar();
            tfTotalBayar.setText(currencyFormatter.format(totalFinal));
            if (showDialog) {
                JOptionPane.showMessageDialog(this, "Total bayar telah diperbarui.", "Informasi", JOptionPane.INFORMATION_MESSAGE);
            }
        }

        private void performPayment() {
            if (taRingkasan.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tidak ada item untuk diproses.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            double totalFinal;
            try {
                totalFinal = currencyFormatter.parse(tfTotalBayar.getText()).doubleValue();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Hitung total bayar terlebih dahulu dengan menekan tombol 'Proses'.", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                String uangBayarText = tfUangBayar.getText();
                if (uangBayarText.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Masukkan jumlah uang bayar!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                double uangBayar = Double.parseDouble(uangBayarText.replace(".", "").replace(",", "."));
                if (uangBayar >= totalFinal) {
                    double kembalian = uangBayar - totalFinal;
                    tfKembalian.setText(currencyFormatter.format(kembalian));
                    btnBuatStruk.setEnabled(true);
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
            tblModelMakanan.setRowCount(0);
            for (int i = 0; i < cbMinuman.length; i++) {
                cbMinuman[i].setSelected(false);
                spJumlahMinuman[i].setValue(0);
            }
            bgLayanan.clearSelection();
            tfJumlah.setText("");
            tfSubTotal.setText("");
            taRingkasan.setText("");
            tfBiayaLayanan.setText("");
            tfTotalBayar.setText("");
            tfUangBayar.setText("");
            tfKembalian.setText("");
            btnBuatStruk.setEnabled(false);
            cardLayout.show(mainPanel, "PESANAN");
            if (sidebarButtons != null && !sidebarButtons.isEmpty()) {
                updateSelectedButtonUI(sidebarButtons.get(0));
            }
            this.requestFocusInWindow();
        }

        private void tampilkanStruk() {
            if (!btnBuatStruk.isEnabled()) return;

            syncTransaksiObjectFromUI();

            StringBuilder strukText = new StringBuilder();
            String garis = "----------------------------------------\n";

            strukText.append("<html><pre>");
            strukText.append("        *** DAPUR TIGA RASA *** \n");
            strukText.append(garis);

            for (int i = 0; i < transaksi.getMakanan().size(); i++) {
                Menu menu = transaksi.getMakanan().get(i);
                int jumlah = transaksi.getJumlahMakanan().get(i);
                double totalItem = menu.getHarga() * jumlah;
                String hargaSatuanStr = currencyFormatter.format(menu.getHarga());
                String totalItemStr = currencyFormatter.format(totalItem);

                strukText.append(String.format("%-40s\n", menu.getNama()));
                strukText.append(String.format("  %d x @%-18s %15s\n", jumlah, hargaSatuanStr, totalItemStr));
            }
            for (int i = 0; i < transaksi.getMinuman().size(); i++) {
                Menu menu = transaksi.getMinuman().get(i);
                int jumlah = transaksi.getJumlahMinuman().get(i);
                double totalItem = menu.getHarga() * jumlah;
                String hargaSatuanStr = currencyFormatter.format(menu.getHarga());
                String totalItemStr = currencyFormatter.format(totalItem);

                strukText.append(String.format("%-40s\n", menu.getNama()));
                strukText.append(String.format("  %d x @%-18s %15s\n", jumlah, hargaSatuanStr, totalItemStr));
            }

            strukText.append(garis);
            strukText.append(String.format("%-24s: %15s\n", "Subtotal", currencyFormatter.format(transaksi.getSubTotal())));
            if (transaksi.getBiayaLayanan() > 0) {
                strukText.append(String.format("%-24s: %15s\n", "Biaya Layanan", currencyFormatter.format(transaksi.getBiayaLayanan())));
            }
            strukText.append(garis);

            try {
                double uangBayarDouble = Double.parseDouble(tfUangBayar.getText().replace(".", "").replace(",", "."));
                strukText.append(String.format("%-24s: %15s\n", "TOTAL", tfTotalBayar.getText()));
                strukText.append(String.format("%-24s: %15s\n", "Uang Bayar", currencyFormatter.format(uangBayarDouble)));
                strukText.append(String.format("%-24s: %15s\n", "Kembalian", tfKembalian.getText()));
            } catch (NumberFormatException e) {
                strukText.append("Error parsing payment amount.\n");
            }

            strukText.append(garis);
            strukText.append("\n");
            strukText.append("      Terima Kasih Atas Kunjungan Anda!   \n");
            strukText.append(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()) + "\n");
            strukText.append("</pre></html>");

            JEditorPane editorPane = new JEditorPane("text/html", strukText.toString());
            editorPane.setEditable(false);

            JOptionPane.showMessageDialog(this, editorPane, "Struk Pembayaran", JOptionPane.PLAIN_MESSAGE);
        }

        private void keluarAplikasi() {
            if (JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin menutup aplikasi?", "Konfirmasi", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                saveState();
                System.exit(0);
            }
        }

        private void saveState() {
            Properties props = new Properties();
            props.setProperty("makanan.count", String.valueOf(tblModelMakanan.getRowCount()));
            for (int i = 0; i < tblModelMakanan.getRowCount(); i++) {
                props.setProperty("makanan." + i + ".nama", (String) tblModelMakanan.getValueAt(i, 0));
                props.setProperty("makanan." + i + ".jumlah", tblModelMakanan.getValueAt(i, 1).toString());
            }
            int minumanCount = 0;
            for (int i = 0; i < cbMinuman.length; i++) {
                if (cbMinuman[i].isSelected()) {
                    props.setProperty("minuman." + minumanCount + ".nama", menuMinuman[i].getNama());
                    props.setProperty("minuman." + minumanCount + ".jumlah", spJumlahMinuman[i].getValue().toString());
                    minumanCount++;
                }
            }
            props.setProperty("minuman.count", String.valueOf(minumanCount));
            if (rbDelivery.isSelected()) props.setProperty("layanan", "delivery");
            else if (rbTakeAway.isSelected()) props.setProperty("layanan", "take_away");
            props.setProperty("subTotal", tfSubTotal.getText());
            props.setProperty("biayaLayanan", tfBiayaLayanan.getText());
            props.setProperty("totalBayar", tfTotalBayar.getText());
            props.setProperty("uangBayar", tfUangBayar.getText());
            props.setProperty("kembalian", tfKembalian.getText());
            if(sidebarButtons.get(0).isOpaque()) props.setProperty("active.panel", "pesanan");
            else if(sidebarButtons.get(1).isOpaque()) props.setProperty("active.panel", "pembayaran");
            try (FileOutputStream out = new FileOutputStream(STATE_FILE_PATH)) {
                props.store(out, "Kasir Application State");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void loadState() {
            File stateFile = new File(STATE_FILE_PATH);
            if (!stateFile.exists()) return;
            Properties props = new Properties();
            try (FileInputStream in = new FileInputStream(stateFile)) {
                props.load(in);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            tblModelMakanan.setRowCount(0);
            int makananCount = Integer.parseInt(props.getProperty("makanan.count", "0"));
            for (int i = 0; i < makananCount; i++) {
                String nama = props.getProperty("makanan." + i + ".nama");
                int jumlah = Integer.parseInt(props.getProperty("makanan." + i + ".jumlah", "0"));
                if (nama != null) tblModelMakanan.addRow(new Object[]{nama, jumlah});
            }
            int minumanCount = Integer.parseInt(props.getProperty("minuman.count", "0"));
            for (int i = 0; i < minumanCount; i++) {
                String nama = props.getProperty("minuman." + i + ".nama");
                int jumlah = Integer.parseInt(props.getProperty("minuman." + i + ".jumlah", "0"));
                for (int j = 0; j < menuMinuman.length; j++) {
                    if (menuMinuman[j].getNama().equals(nama)) {
                        cbMinuman[j].setSelected(true);
                        spJumlahMinuman[j].setValue(jumlah);
                        break;
                    }
                }
            }
            String layanan = props.getProperty("layanan");
            if ("delivery".equals(layanan)) rbDelivery.setSelected(true);
            else if ("take_away".equals(layanan)) rbTakeAway.setSelected(true);
            tfSubTotal.setText(props.getProperty("subTotal", ""));
            tfBiayaLayanan.setText(props.getProperty("biayaLayanan", ""));
            tfTotalBayar.setText(props.getProperty("totalBayar", ""));
            tfUangBayar.setText(props.getProperty("uangBayar", ""));
            tfKembalian.setText(props.getProperty("kembalian", ""));
            syncTransaksiObjectFromUI();
            String activePanel = props.getProperty("active.panel", "pesanan");
            if("pembayaran".equals(activePanel)) {
                cardLayout.show(mainPanel, "PEMBAYARAN");
                updateSelectedButtonUI(sidebarButtons.get(1));
            } else {
                cardLayout.show(mainPanel, "PESANAN");
                updateSelectedButtonUI(sidebarButtons.get(0));
            }
            if(!tfKembalian.getText().isEmpty()) {
                btnBuatStruk.setEnabled(true);
            }
        }
    }
}

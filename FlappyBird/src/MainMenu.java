import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URL;

/**
 * Kelas MainMenu merepresentasikan tampilan Menu Utama di awal program.
 * Sesuai dengan Spesifikasi Wajib 5 pada Modul 7:
 * "Buat Main Menu yang ditampilkan di awal program yang terdiri dari 2 tombol,
 * untuk memulai permainan dan untuk keluar dari permainan."
 */
public class MainMenu extends JPanel {
    // Aset visual menu utama
    private Image backgroundImage;
    private Image birdImage;

    // Dua tombol wajib sesuai Modul 7
    private JButton startButton;
    private JButton exitButton;

    // Komponen visual pendukung
    private JLabel highScoreLabel;
    private double bobAngle = 0;
    private Timer bobTimer;

    /**
     * Konstruktor MainMenu: Menyiapkan layout, tombol aksi, serta animasi burung melayang.
     *
     * @param onStart ActionListener untuk memulai permainan
     * @param onExit  ActionListener untuk keluar dari permainan
     */
    public MainMenu(ActionListener onStart, ActionListener onExit) {
        setPreferredSize(new Dimension(360, 640));
        setLayout(new BorderLayout());

        // Memuat aset gambar
        backgroundImage = loadImage("assets/background.png");
        birdImage = loadImage("assets/bird.png");

        // Timer untuk animasi mengambang burung di menu utama
        bobTimer = new Timer(35, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bobAngle += 0.08;
                repaint();
            }
        });
        bobTimer.start();

        // ==========================================
        // PANEL TENGAH MENU UTAMA
        // ==========================================
        JPanel centerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int cardW = 300;
                int cardH = 410;
                int cardX = (getWidth() - cardW) / 2;
                int cardY = (getHeight() - cardH) / 2;

                // Bayangan kartu
                g2.setColor(new Color(0, 0, 0, 60));
                g2.fillRoundRect(cardX + 4, cardY + 6, cardW, cardH, 30, 30);

                // Latar belakang kartu putih bersih
                g2.setColor(new Color(255, 255, 255, 235));
                g2.fillRoundRect(cardX, cardY, cardW, cardH, 30, 30);

                // Border kartu berkilau
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(cardX, cardY, cardW, cardH, 30, 30);

                g2.dispose();
            }
        };
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 16, 4, 16);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Preview ikon burung dengan animasi melayang lembut
        JLabel iconLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (birdImage != null) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int bw = 48;
                    int bh = 34;
                    int bx = (getWidth() - bw) / 2;
                    int by = (getHeight() - bh) / 2 + (int) (Math.sin(bobAngle) * 5);
                    g2.drawImage(birdImage, bx, by, bw, bh, null);
                    g2.dispose();
                }
            }
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(60, 48);
            }
        };
        gbc.gridy = 0;
        centerPanel.add(iconLabel, gbc);

        // Judul Game
        JLabel titleLabel = new JLabel("FLAPPY BIRD", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(30, 41, 59));
        gbc.gridy = 1;
        centerPanel.add(titleLabel, gbc);

        // Subtitle Menu Utama
        JLabel subLabel = new JLabel("Menu Utama", SwingConstants.CENTER);
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subLabel.setForeground(new Color(100, 116, 139));
        gbc.gridy = 2;
        centerPanel.add(subLabel, gbc);

        // Label Rekor Terbaik
        highScoreLabel = new JLabel("Rekor Terbaik: 0", SwingConstants.CENTER);
        highScoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        highScoreLabel.setForeground(new Color(16, 185, 129));
        gbc.gridy = 3;
        gbc.insets = new Insets(2, 16, 16, 16);
        centerPanel.add(highScoreLabel, gbc);

        // ==========================================
        // DUA TOMBOL WAJIB (MODUL 7 SPESIFIKASI 5)
        // ==========================================

        // Tombol 1: Untuk Memulai Permainan
        startButton = new ModernButton("Memulai Permainan", new Color(16, 185, 129), new Color(5, 150, 105));
        startButton.addActionListener(onStart);
        gbc.gridy = 4;
        gbc.insets = new Insets(6, 26, 6, 26);
        centerPanel.add(startButton, gbc);

        // Tombol 2: Untuk Keluar dari Permainan
        exitButton = new ModernButton("Keluar dari Permainan", new Color(239, 68, 68), new Color(220, 38, 38));
        exitButton.addActionListener(onExit);
        gbc.gridy = 5;
        centerPanel.add(exitButton, gbc);

        // Teks instruksi kontrol permainan
        JLabel tipLabel = new JLabel("<html><center>Tekan <b>SPASI</b> untuk melompat<br>Tekan <b>R</b> untuk restart saat kalah</center></html>", SwingConstants.CENTER);
        tipLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tipLabel.setForeground(new Color(100, 116, 139));
        gbc.gridy = 6;
        gbc.insets = new Insets(16, 18, 6, 18);
        centerPanel.add(tipLabel, gbc);

        add(centerPanel, BorderLayout.CENTER);
    }

    /**
     * Memperbarui angka rekor terbaik pada menu utama.
     */
    public void updateHighScore(int highScore) {
        if (highScoreLabel != null) {
            highScoreLabel.setText("Rekor Terbaik: " + highScore);
        }
    }

    /**
     * Helper untuk memuat gambar latar dan sprite burung.
     */
    private Image loadImage(String path) {
        try {
            URL url = getClass().getResource(path);
            if (url == null) {
                url = getClass().getResource("/" + path);
            }
            if (url != null) {
                return new ImageIcon(url).getImage();
            }
            File f = new File("src/" + path);
            if (f.exists()) {
                return new ImageIcon(f.getAbsolutePath()).getImage();
            }
        } catch (Exception ignored) {}
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (backgroundImage != null) {
            g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
        } else {
            g2.setColor(new Color(113, 197, 207));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        // Vignette lembut untuk menambah kedalaman visual
        GradientPaint vignette = new GradientPaint(
                0, 0, new Color(15, 23, 42, 70),
                0, getHeight(), new Color(15, 23, 42, 130)
        );
        g2.setPaint(vignette);
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.dispose();
    }

    /**
     * Komponen tombol kustom dengan efek hover dan rounded corners.
     */
    public static class ModernButton extends JButton {
        private Color normalColor;
        private Color hoverColor;
        private boolean isHovered = false;

        public ModernButton(String text, Color normalColor, Color hoverColor) {
            super(text);
            this.normalColor = normalColor;
            this.hoverColor = hoverColor;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(210, 42));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Bayangan tombol
            g2.setColor(new Color(0, 0, 0, 35));
            g2.fillRoundRect(2, 3, getWidth() - 4, getHeight() - 4, 22, 22);

            // Warna tombol (aktif/hover)
            Color fill = isHovered ? hoverColor : normalColor;
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);

            g2.dispose();
            super.paintComponent(g);
        }
    }
}

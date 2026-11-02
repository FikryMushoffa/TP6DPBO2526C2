import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * Kelas View bertanggung jawab untuk me-render grafis permainan Flappy Bird:
 * - Latar belakang atmosferik dengan ambient sun glow, siluet perbukitan, dan awan melayang.
 * - Rintangan pipa dengan drop shadow realistis.
 * - Efek partikel hembusan angin dinamis saat melompat.
 * - Karakter burung dengan rotasi sudut aerodinamis dan efek squash & stretch.
 * - Scrolling tanah tak berujung (infinite ground).
 * - HUD skor elegan di bagian atas layar.
 * - Layar Game Over yang bersih tanpa medali.
 */
public class View extends JPanel {

    int width = 360;   // Lebar kanvas tampilan
    int height = 640;  // Tinggi kanvas tampilan

    private Logic logic;
    private JLabel scoreLabel;

    /**
     * Konstruktor View: Mengatur layout, inisialisasi HUD skor, serta menghubungkan listener.
     */
    public View(Logic logic) {
        this.logic = logic;
        setPreferredSize(new Dimension(width, height));
        setBackground(Color.cyan);
        setLayout(new BorderLayout());

        // ==========================================
        // 1. HUD SCORE BADGE
        // ==========================================
        JPanel topContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 16));
        topContainer.setOpaque(false);

        // Desain badge skor bergaya modern frosted glass
        JPanel scoreBadge = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Bayangan lembut badge skor
                g2.setColor(new Color(0, 0, 0, 50));
                g2.fillRoundRect(2, 3, getWidth() - 4, getHeight() - 4, 22, 22);

                // Latar belakang badge gelap elegan
                g2.setColor(new Color(15, 23, 42, 200));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);

                // Border tipis berkilau
                g2.setColor(new Color(255, 255, 255, 100));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);

                g2.dispose();
            }
        };
        scoreBadge.setOpaque(false);
        scoreBadge.setBorder(BorderFactory.createEmptyBorder(6, 24, 6, 24));

        // JLabel untuk menampilkan skor real-time
        scoreLabel = new JLabel("Score: 0", SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        scoreLabel.setForeground(Color.WHITE);
        scoreBadge.add(scoreLabel, BorderLayout.CENTER);

        topContainer.add(scoreBadge);
        add(topContainer, BorderLayout.NORTH);

        // ==========================================
        // 2. FOKUS & KEY LISTENER INPUT
        // ==========================================
        setFocusable(true);
        addKeyListener(logic);

        // Memastikan kanvas selalu mendapatkan fokus keyboard saat diklik
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
            }
        });
    }

    public JLabel getScoreLabel() {
        return scoreLabel;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    /**
     * Menggambar seluruh lapisan elemen game secara berurutan:
     * 1. Latar Belakang & Efek Pencahayaan Langit
     * 2. Siluet Perbukitan & Awan Melayang
     * 3. Rintangan Pipa (Atas dan Bawah)
     * 4. Tanah Bawah (Infinite Scrolling)
     * 5. Partikel Hembusan Angin Kepakan Sayap
     * 6. Karakter Burung (Dengan Squash-Stretch & Rotasi)
     * 7. Layar Game Over (Jika permainan berakhir)
     */
    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        // Penerapan Anti-Aliasing untuk kualitas grafis yang halus
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Efek getar layar lembut saat benturan
        int shake = (logic != null) ? logic.getShakeOffset() : 0;
        g2.translate(0, shake);

        // ==========================================
        // 1. RENDER BACKGROUND & CAHAYA AMBIENT
        // ==========================================
        int bgX = (logic != null) ? logic.getBgX() : 0;
        Image bgImg = (logic != null) ? logic.getBackgroundImage() : null;

        if (bgImg != null) {
            // Digambar dua segmen bersebelahan agar latar belakang bergerak menyambung tanpa jeda
            g2.drawImage(bgImg, bgX, 0, width, height, null);
            g2.drawImage(bgImg, bgX + width, 0, width, height, null);
            if (bgX > 0) {
                g2.drawImage(bgImg, bgX - width, 0, width, height, null);
            }
        } else {
            // Fallback gradien langit biru jika gambar aset tidak ditemukan
            GradientPaint skyGradient = new GradientPaint(0, 0, new Color(56, 189, 248), 0, height, new Color(186, 230, 253));
            g2.setPaint(skyGradient);
            g2.fillRect(0, 0, width, height);
        }

        // Efek cahaya matahari lembut di sudut kiri atas (Sun Glow)
        RadialGradientPaint sunGlow = new RadialGradientPaint(
                new Point(65, 75), 110f,
                new float[]{0.0f, 0.45f, 1.0f},
                new Color[]{new Color(255, 255, 255, 125), new Color(254, 240, 138, 60), new Color(254, 240, 138, 0)}
        );
        g2.setPaint(sunGlow);
        g2.fillOval(-35, -25, 200, 200);

        // ==========================================
        // 2. RENDER AWAN-AWAN MELAYANG
        // ==========================================
        if (logic != null) {
            for (Logic.Cloud cloud : logic.getClouds()) {
                // Lapisan bayangan awan
                g2.setColor(new Color(255, 255, 255, 115));
                g2.fillRoundRect((int) cloud.x, (int) cloud.y, cloud.width, cloud.height, 22, 22);
                // Inti awan putih lembut
                g2.setColor(new Color(255, 255, 255, 185));
                g2.fillRoundRect((int) cloud.x + 6, (int) cloud.y + 4, cloud.width - 12, cloud.height - 8, 18, 18);
            }
        }

        // ==========================================
        // 3. RENDER RINTANGAN PIPA
        // ==========================================
        ArrayList<Pipe> pipes = (logic != null) ? logic.getPipes() : null;
        if (pipes != null) {
            for (int i = 0; i < pipes.size(); i++) {
                Pipe pipe = pipes.get(i);
                // Drop shadow lembut di belakang pipa
                g2.setColor(new Color(0, 0, 0, 32));
                g2.fillRect(pipe.getPosX() + 4, pipe.getPosY() + 4, pipe.getWidth(), pipe.getHeight());
                // Sprite pipa asli
                g2.drawImage(pipe.getImage(), pipe.getPosX(), pipe.getPosY(), pipe.getWidth(), pipe.getHeight(), null);
                // Kilau vertikal pipa
                g2.setColor(new Color(255, 255, 255, 35));
                g2.fillRect(pipe.getPosX() + 5, pipe.getPosY(), 5, pipe.getHeight());
            }
        }

        // ==========================================
        // 4. RENDER TANAH BAWAH (INFINITE SCROLLING)
        // ==========================================
        if (logic != null) {
            int gy = logic.getGroundY();
            int gh = height - gy;
            int gX = logic.getGroundX();

            // Lapisan tanah pasir utama
            g2.setColor(new Color(222, 215, 152));
            g2.fillRect(0, gy, width, gh);

            // Garis rumput hijau bagian atas tanah
            g2.setColor(new Color(115, 191, 46));
            g2.fillRect(0, gy, width, 14);

            g2.setColor(new Color(85, 145, 30));
            g2.fillRect(0, gy + 12, width, 3);

            // Pola garis diagonal tanah yang bergerak
            g2.setColor(new Color(200, 190, 130));
            for (int x = -30 + (gX % 24); x < width + 30; x += 20) {
                g2.drawLine(x, gy + 16, x - 10, gy + gh);
            }
        }

        // ==========================================
        // 5. RENDER PARTIKEL HEMBUSAN LOMPAT
        // ==========================================
        if (logic != null) {
            for (Logic.Particle p : logic.getParticles()) {
                int alpha = Math.max(0, Math.min(255, (int) (p.alpha * 200)));
                g2.setColor(new Color(255, 255, 255, alpha));
                g2.fillOval((int) p.x, (int) p.y, p.size, p.size);
            }
        }

        // ==========================================
        // 6. RENDER PEMAIN / BURUNG (SQUASH-STRETCH & ROTASI)
        // ==========================================
        Player player = (logic != null) ? logic.getPlayer() : null;
        if (player != null) {
            Graphics2D g2Bird = (Graphics2D) g2.create();
            int cx = player.getPosX() + player.getWidth() / 2;
            int cy = player.getPosY() + player.getHeight() / 2;

            // Translasi ke titik pusat burung
            g2Bird.translate(cx, cy);
            // Rotasi aerodinamis sesuai lintasan terbang
            g2Bird.rotate(player.getRotation());
            // Efek elastisitas squash & stretch saat melompat
            g2Bird.scale(player.getScaleX(), player.getScaleY());

            g2Bird.drawImage(player.getImage(), -player.getWidth() / 2, -player.getHeight() / 2, player.getWidth(), player.getHeight(), null);
            g2Bird.dispose();
        }

        // ==========================================
        // 7. RENDER LAYAR GAME OVER (BERSIH TANPA MEDALI)
        // ==========================================
        if (logic != null && logic.isGameOver()) {
            // Overlay latar belakang gelap transparan
            g2.setColor(new Color(15, 23, 42, 180));
            g2.fillRect(0, 0, width, height);

            int cardW = 290;
            int cardH = 240;
            int cardX = (width - cardW) / 2;
            int cardY = (height - cardH) / 2 - 10;

            // Bayangan kartu Game Over
            g2.setColor(new Color(0, 0, 0, 70));
            g2.fillRoundRect(cardX + 4, cardY + 6, cardW, cardH, 26, 26);

            // Bodi kartu putih bersih
            g2.setColor(new Color(255, 255, 255, 245));
            g2.fillRoundRect(cardX, cardY, cardW, cardH, 26, 26);

            // Border kartu
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(cardX, cardY, cardW, cardH, 26, 26);

            // Judul "GAME OVER"
            g2.setFont(new Font("Segoe UI", Font.BOLD, 26));
            FontMetrics fm = g2.getFontMetrics();
            String title = "GAME OVER";
            int titleX = cardX + (cardW - fm.stringWidth(title)) / 2;
            g2.setColor(new Color(239, 68, 68)); // Warna merah crimson
            g2.drawString(title, titleX, cardY + 42);

            // Kotak ringkasan skor (Scoreboard)
            int statBoxX = cardX + 18;
            int statBoxY = cardY + 58;
            int statBoxW = cardW - 36;
            int statBoxH = 105;

            g2.setColor(new Color(241, 245, 249));
            g2.fillRoundRect(statBoxX, statBoxY, statBoxW, statBoxH, 16, 16);
            g2.setColor(new Color(226, 232, 240));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(statBoxX, statBoxY, statBoxW, statBoxH, 16, 16);

            int currentScore = (int) logic.getScore();
            int bestScore = logic.getHighScore();

            // Kolom Kiri: Skor Akhir
            int col1X = statBoxX + 22;
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.setColor(new Color(100, 116, 139));
            g2.drawString("SKOR AKHIR", col1X, statBoxY + 34);

            g2.setFont(new Font("Segoe UI", Font.BOLD, 32));
            g2.setColor(new Color(30, 41, 59));
            g2.drawString(String.valueOf(currentScore), col1X, statBoxY + 76);

            // Garis pemisah vertikal di tengah scoreboard
            g2.setColor(new Color(226, 232, 240));
            g2.drawLine(statBoxX + statBoxW / 2, statBoxY + 16, statBoxX + statBoxW / 2, statBoxY + statBoxH - 16);

            // Kolom Kanan: Skor Terbaik (High Score)
            int col2X = statBoxX + statBoxW / 2 + 20;
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.setColor(new Color(100, 116, 139));
            g2.drawString("TERBAIK", col2X, statBoxY + 34);

            g2.setFont(new Font("Segoe UI", Font.BOLD, 32));
            g2.setColor(new Color(16, 185, 129)); // Warna hijau emerald
            g2.drawString(String.valueOf(bestScore), col2X, statBoxY + 76);

            // ==========================================
            // INSTRUKSI RESTART (TOMBOL 'R')
            // ==========================================
            g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
            g2.setColor(new Color(71, 85, 105));
            fm = g2.getFontMetrics();
            String restartHint = "Tekan \"R\" untuk Restart";
            int hintX = cardX + (cardW - fm.stringWidth(restartHint)) / 2;
            g2.drawString(restartHint, hintX, cardY + 200);
        }

        // Kembalikan pergeseran translasi getar layar
        g2.translate(0, -shake);
    }
}

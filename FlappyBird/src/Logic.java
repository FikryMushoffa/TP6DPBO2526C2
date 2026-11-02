import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * Kelas Logic mengelola seluruh logika inti permainan Flappy Bird:
 * - Mengatur siklus game loop (60 FPS) dan cooldown pemunculan pipa.
 * - Memproses simulasi gravitasi, pergerakan pipa, dan deteksi tabrakan.
 * - Mengatur penambahan skor dan pemutaran audio (BGM & SFX).
 * - Efek animasi lompat dinamis (squash-stretch, rotasi aerodinamis, partikel kepakan sayap).
 * - Menangani input keyboard (Spasi untuk terbang, 'R' untuk restart).
 */
public class Logic implements ActionListener, KeyListener {
    // ==========================================
    // 1. DIMENSI FRAME & PLAYER
    // ==========================================
    int frameWidth = 360;   // Lebar window
    int frameHeight = 640;  // Tinggi window

    // Posisi awal burung tepat di tengah layar
    int playerWidth = 34;
    int playerHeight = 24;
    int playerStartPosX = (frameWidth / 2) - (playerWidth / 2);
    int playerStartPosY = (frameHeight / 2) - (playerHeight / 2);

    // ==========================================
    // 2. ATRIBUT PIPA & RINTANGAN
    // ==========================================
    int pipeStartPosX = frameWidth;
    int pipeStartPosY = 0;
    int pipeWidth = 64;
    int pipeHeight = 512;
    int pipeVelocityX = -2; // Kecepatan pipa bergerak ke kiri

    // ==========================================
    // 3. TIMER & FISIKA GAMEPLAY
    // ==========================================
    Timer gameLoop;        // Timer utama penggerak animasi (~60 FPS)
    Timer pipesCooldown;   // Timer interval pemunculan pipa baru
    int gravity = 1;       // Besaran akselerasi gravitasi
    int gravityTick = 0;   // Penghitung tick gravitasi halus

    // ==========================================
    // 4. ENTITAS OBJEK & ASET GAMBAR
    // ==========================================
    View view;
    Player player;
    ArrayList<Pipe> pipes;

    Image birdImage;
    Image lowerPipeImage;
    Image upperPipeImage;
    Image backgroundImage;

    // ==========================================
    // 5. STATUS PERMAINAN, SKOR, & AUDIO
    // ==========================================
    boolean gameOver = false;
    double score = 0;
    int highScore = 0;
    boolean isNewHighScore = false;
    Sound sound = new Sound(); // Pengelola audio BGM & SFX

    // ==========================================
    // 6. SCROLLING LATAR BELAKANG & EFEK VISUAL
    // ==========================================
    private int bgX = 0;
    private int groundX = 0;
    private int groundY = frameHeight - 56; // Batas atas tanah
    private int shakeFrames = 0;            // Efek getar layar saat benturan
    private Random random = new Random();

    // ==========================================
    // 7. SISTEM PARTIKEL & AWAN ATMOSFERIK
    // ==========================================

    /**
     * Kelas Particle merepresentasikan hembusan angin lembut saat burung mengepakkan sayap.
     */
    public static class Particle {
        public double x, y;
        public double vx, vy;
        public float alpha = 1.0f;
        public int size;

        public Particle(double x, double y, double vx, double vy, int size) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.size = size;
        }

        public boolean update() {
            x += vx;
            y += vy;
            alpha -= 0.045f; // Pudar perlahan
            return alpha <= 0;
        }
    }

    /**
     * Kelas Cloud untuk awan atmosferik berlapis yang melayang di langit.
     */
    public static class Cloud {
        public double x, y, speed;
        public int width, height;

        public Cloud(double x, double y, int width, int height, double speed) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.speed = speed;
        }

        public void update(int frameW) {
            x -= speed;
            if (x + width < 0) {
                x = frameW + 20;
            }
        }
    }

    private ArrayList<Particle> particles = new ArrayList<>();
    private ArrayList<Cloud> clouds = new ArrayList<>();

    /**
     * Konstruktor Logic: Memuat semua aset gambar, menginisialisasi objek player,
     * awan latar belakang, serta mengkonfigurasi Timer gameLoop dan pipesCooldown.
     */
    public Logic() {
        // Memuat seluruh aset gambar yang diperlukan
        birdImage = loadImage("assets/bird.png");
        lowerPipeImage = loadImage("assets/lowerPipe.png");
        upperPipeImage = loadImage("assets/upperPipe.png");
        backgroundImage = loadImage("assets/background.png");

        // Inisialisasi pemain di posisi awal
        player = new Player(playerStartPosX, playerStartPosY, playerWidth, playerHeight, birdImage);
        pipes = new ArrayList<Pipe>();

        // Inisialisasi awan-awan melayang di langit
        clouds.add(new Cloud(25, 40, 95, 34, 0.30));
        clouds.add(new Cloud(175, 75, 120, 42, 0.45));
        clouds.add(new Cloud(305, 50, 85, 30, 0.25));

        // Timer kemunculan rintangan pipa setiap 1.7 detik
        pipesCooldown = new Timer(1700, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });

        // Timer utama pergerakan game 60 FPS
        gameLoop = new Timer(1000 / 60, this);
    }

    // ==========================================
    // SETTER & GETTER
    // ==========================================

    public void setView(View view) {
        this.view = view;
    }

    public Player getPlayer() {
        return player;
    }

    public ArrayList<Pipe> getPipes() {
        return pipes;
    }

    public Image getBackgroundImage() {
        return backgroundImage;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public double getScore() {
        return score;
    }

    public int getHighScore() {
        return highScore;
    }

    public boolean isNewHighScore() {
        return isNewHighScore;
    }

    public Sound getSound() {
        return sound;
    }

    public int getBgX() {
        return bgX;
    }

    public int getGroundX() {
        return groundX;
    }

    public int getGroundY() {
        return groundY;
    }

    public int getShakeOffset() {
        if (shakeFrames > 0) {
            return (int) (Math.sin(shakeFrames * 1.5) * (shakeFrames / 3.0));
        }
        return 0;
    }

    public ArrayList<Particle> getParticles() {
        return particles;
    }

    public ArrayList<Cloud> getClouds() {
        return clouds;
    }

    // ==========================================
    // METHOD GAMEPLAY UTAMA
    // ==========================================

    /**
     * Memulai permainan baru dari awal saat tombol "Memulai Permainan" pada Main Menu ditekan.
     */
    public void startGame() {
        restartGame();
    }

    /**
     * Membuat sepasang rintangan pipa (atas dan bawah) dengan posisi celah vertikal acak.
     */
    public void placePipes() {
        if (gameOver) return;

        int openingSpace = 175; // Lebar celah vertikal yang nyaman untuk burung lewat
        int maxVariance = pipeHeight / 2 - 40;
        int randomPosY = (int) (pipeStartPosY - (pipeHeight / 4) - (random.nextDouble() * maxVariance));

        // Pipa atas
        Pipe upperPipe = new Pipe(pipeStartPosX, randomPosY, pipeWidth, pipeHeight, upperPipeImage);
        pipes.add(upperPipe);

        // Pipa bawah
        Pipe lowerPipe = new Pipe(pipeStartPosX, randomPosY + openingSpace + pipeHeight, pipeWidth, pipeHeight, lowerPipeImage);
        pipes.add(lowerPipe);
    }

    /**
     * Memperbarui posisi seluruh objek setiap frame:
     * - Scrolling background & tanah tak terbatas.
     * - Animasi pergerakan awan atmosferik di langit.
     * - Gravitasi dan pergerakan jatuh pemain.
     * - Pemulihan bentuk burung (squash & stretch recovery).
     * - Pergeseran pipa ke kiri dan deteksi skor saat melewati pipa.
     * - Deteksi tabrakan dengan pipa atau batas bawah frame (Game Over).
     */
    public void move() {
        // Efek getar layar benturan berkurang per frame
        if (shakeFrames > 0) shakeFrames--;

        // 1. Pergerakan awan-awan di langit
        for (Cloud cloud : clouds) {
            cloud.update(frameWidth);
        }

        // 2. Infinite scrolling background dan tanah
        bgX = (bgX - 1) % frameWidth;
        groundX = (groundX + pipeVelocityX) % frameWidth;

        // 3. Efek pemulihan elastisitas bentuk burung (squash & stretch recovery)
        player.setScaleX(player.getScaleX() + (1.0 - player.getScaleX()) * 0.16);
        player.setScaleY(player.getScaleY() + (1.0 - player.getScaleY()) * 0.16);

        // 4. Update partikel hembusan kepakan sayap
        Iterator<Particle> pIt = particles.iterator();
        while (pIt.hasNext()) {
            if (pIt.next().update()) pIt.remove();
        }

        // Jika permainan sedang Game Over, hentikan pergerakan karakter dan pipa
        if (gameOver) return;

        // 5. Terapkan akselerasi gravitasi secara bertahap
        gravityTick++;
        if (gravityTick % 2 == 0) {
            // Batasi terminal velocity jatuh agar burung tidak menukik terlalu cepat
            player.setVelocityY(Math.min(player.getVelocityY() + gravity, 6));
        }

        // Update posisi Y pemain
        player.setPosY(player.getPosY() + player.getVelocityY());
        player.setPosY(Math.max(player.getPosY(), 0)); // Batasi agar tidak menembus batas atas langit

        // Rotasi sudut aerodinamis burung (menengadah saat lompat, menukik saat jatuh)
        double targetAngle = Math.toRadians(Math.max(-28.0, Math.min(player.getVelocityY() * 5.2, 70.0)));
        player.setRotation(player.getRotation() + (targetAngle - player.getRotation()) * 0.20);

        // 6. Game Over jika player menyentuh batas bawah / tanah
        if (player.getPosY() + player.getHeight() >= groundY) {
            player.setPosY(groundY - player.getHeight());
            triggerGameOver();
            return;
        }

        // 7. Pergerakan pipa ke kiri dan pengecekan tabrakan serta skor
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            pipe.setPosX(pipe.getPosX() + pipeVelocityX);

            // Tambah skor (+1) setiap kali player melewati sepasang pipa
            if (!pipe.isPassed() && player.getPosX() > pipe.getPosX() + pipe.getWidth()) {
                pipe.setPassed(true);
                // Dihitung tepat satu kali per pasang pipa (pada pipa atas)
                if (pipe.getImage() == upperPipeImage) {
                    score += 1.0;
                    if ((int) score > highScore) {
                        highScore = (int) score;
                        isNewHighScore = true;
                    }
                    sound.playSound("point.wav"); // SFX saat berhasil menambah skor
                    updateScoreLabel();
                }
            }

            // Hentikan permainan jika player menabrak pipa
            if (collision(player, pipe)) {
                triggerGameOver();
                return;
            }
        }

        // Hapus pipa yang sudah keluar dari batas kiri layar
        while (!pipes.isEmpty() && pipes.get(0).getPosX() + pipeWidth < 0) {
            pipes.remove(0);
        }
    }

    /**
     * Deteksi tabrakan AABB antara objek Player dan Pipe.
     */
    public boolean collision(Player a, Pipe b) {
        int padX = 4; // Padding hitbox agar deteksi benturan presisi dan adil
        int padY = 3;
        return (a.getPosX() + padX) < (b.getPosX() + b.getWidth()) &&
               (a.getPosX() + a.getWidth() - padX) > b.getPosX() &&
               (a.getPosY() + padY) < (b.getPosY() + b.getHeight()) &&
               (a.getPosY() + a.getHeight() - padY) > b.getPosY();
    }

    /**
     * Menghentikan permainan (Game Over) saat terjadi benturan atau jatuh.
     */
    private void triggerGameOver() {
        gameOver = true;
        shakeFrames = 8;
        gameLoop.stop();
        pipesCooldown.stop();

        if ((int) score > highScore) {
            highScore = (int) score;
            isNewHighScore = true;
        }
        updateScoreLabel();

        sound.playSound("hit.wav"); // SFX benturan
        sound.stopBGM();            // Hentikan BGM

        if (view != null) {
            view.repaint();
        }
    }

    /**
     * Merestart permainan ke status awal melalui penekanan tombol 'R' pada keyboard.
     */
    public void restartGame() {
        player.setPosX(playerStartPosX);
        player.setPosY(playerStartPosY);
        player.setVelocityY(0);
        player.setRotation(0);
        player.setScaleX(1.0);
        player.setScaleY(1.0);
        pipes.clear();
        particles.clear();
        score = 0;
        gameOver = false;
        isNewHighScore = false;
        shakeFrames = 0;
        updateScoreLabel();

        // Jalankan kembali timer permainan
        gameLoop.start();
        pipesCooldown.start();

        // Putar kembali musik latar BGM
        sound.playBGM("bgm.wav");
    }

    /**
     * Aksi melompat/terbang ke atas saat pemain menekan tombol SPASI:
     * - Memberikan kecepatan vertikal ke atas (-7).
     * - Memicu efek squash & stretch dinamis pada burung.
     * - Menghasilkan partikel hembusan kepakan sayap lembut.
     * - Memutar efek suara kepakan sayap (wing.wav).
     */
    public void jump() {
        if (!gameOver) {
            player.setVelocityY(-7);      // Dorongan vertikal ke atas
            player.setScaleX(0.80);       // Efek squash horizontal
            player.setScaleY(1.25);       // Efek stretch vertikal
            sound.playSound("wing.wav");  // SFX kepakan sayap

            // Spawn partikel hembusan angin kepakan sayap di belakang burung
            int px = player.getPosX() + 4;
            int py = player.getPosY() + player.getHeight() / 2;
            for (int i = 0; i < 4; i++) {
                double vx = -1.5 - random.nextDouble() * 1.5;
                double vy = (random.nextDouble() - 0.5) * 1.6;
                particles.add(new Particle(px, py, vx, vy, 4 + random.nextInt(4)));
            }
        }
    }

    /**
     * Memperbarui teks pada komponen JLabel skor di bagian atas layar.
     */
    private void updateScoreLabel() {
        if (view != null && view.getScoreLabel() != null) {
            view.getScoreLabel().setText("Score: " + (int) score);
        }
    }

    // ==========================================
    // IMPLEMENTASI LISTENER
    // ==========================================

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        if (view != null) {
            view.repaint();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    /**
     * Penanganan input keyboard:
     * - Tombol SPASI: Membuat burung melompat ke atas.
     * - Tombol 'R': Merestart permainan hanya saat Game Over.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            jump();
        } else if (e.getKeyCode() == KeyEvent.VK_R) {
            if (gameOver) {
                restartGame();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    /**
     * Helper untuk memuat gambar dari classpath maupun direktori src lokal.
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
            File file = new File("src/" + path);
            if (file.exists()) {
                return new ImageIcon(file.getAbsolutePath()).getImage();
            }
        } catch (Exception ignored) {}
        return null;
    }
}

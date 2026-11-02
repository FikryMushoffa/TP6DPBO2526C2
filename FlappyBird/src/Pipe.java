import java.awt.*;

/**
 * Kelas Pipe merepresentasikan rintangan pipa dalam permainan Flappy Bird.
 * Menyimpan data koordinat, ukuran, gambar (sprite atas atau bawah), serta status passed.
 * Sesuai dengan spesifikasi Modul 7 (Bagian 2 - Menambah Obstacle).
 */
public class Pipe {
    // Atribut posisi dan dimensi rintangan pipa
    private int posX;
    private int posY;
    private int width;
    private int height;
    private Image image;

    // Atribut kecepatan pergerakan horizontal pipa
    private int velocityX;

    // Penanda apakah pipa ini sudah berhasil dilewati oleh burung untuk perhitungan skor
    private boolean passed;

    /**
     * Konstruktor untuk membuat objek Pipe baru dengan posisi, ukuran, dan gambar tertentu.
     */
    public Pipe(int posX, int posY, int width, int height, Image image) {
        this.posX = posX;
        this.posY = posY;
        this.width = width;
        this.height = height;
        this.image = image;
        this.velocityX = 0;
        this.passed = false;
    }

    // ==========================================
    // GETTER & SETTER ATRIBUT PIPA
    // ==========================================

    public int getPosX() {
        return posX;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public int getPosY() {
        return posY;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public int getVelocityX() {
        return velocityX;
    }

    public void setVelocityX(int velocityX) {
        this.velocityX = velocityX;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }
}

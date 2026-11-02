import java.awt.*;

/**
 * Kelas Player merepresentasikan karakter burung yang dikendalikan oleh pemain.
 * Menyimpan informasi posisi, ukuran, gambar (sprite), kecepatan, dan sudut rotasi.
 * Sesuai dengan spesifikasi Modul 6 (Pertemuan 1).
 */
public class Player {
    // Atribut posisi dan dimensi pemain
    private int posX;
    private int posY;
    private int width;
    private int height;
    private Image image;

    // Atribut fisika gerakan
    private int velocityY = 0;  // Kecepatan vertikal (ke bawah positif karena gravitasi, ke atas negatif saat melompat)
    private int velocityX = 0;  // Kecepatan horizontal (default 0)

    // Atribut efek visual modern
    private double rotation = 0; // Sudut rotasi burung saat melompat atau menukik
    private double scaleX = 1.0;  // Skala horizontal untuk efek animasi squash & stretch
    private double scaleY = 1.0;  // Skala vertikal untuk efek animasi squash & stretch

    /**
     * Konstruktor untuk inisialisasi objek Player dengan posisi, ukuran, dan gambar awal.
     */
    public Player(int posX, int posY, int width, int height, Image image) {
        this.posX = posX;
        this.posY = posY;
        this.width = width;
        this.height = height;
        this.image = image;
        this.velocityY = 0;
        this.velocityX = 0;
        this.rotation = 0;
        this.scaleX = 1.0;
        this.scaleY = 1.0;
    }

    // ==========================================
    // GETTER & SETTER POSISI DAN UKURAN
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

    // ==========================================
    // GETTER & SETTER FISIKA & VISUAL
    // ==========================================

    public int getVelocityY() {
        return velocityY;
    }

    public void setVelocityY(int velocityY) {
        this.velocityY = velocityY;
    }

    public int getVelocityX() {
        return velocityX;
    }

    public void setVelocityX(int velocityX) {
        this.velocityX = velocityX;
    }

    public double getRotation() {
        return rotation;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    public double getScaleX() {
        return scaleX;
    }

    public void setScaleX(double scaleX) {
        this.scaleX = scaleX;
    }

    public double getScaleY() {
        return scaleY;
    }

    public void setScaleY(double scaleY) {
        this.scaleY = scaleY;
    }
}

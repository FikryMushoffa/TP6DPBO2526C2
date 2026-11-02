import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Kelas App merupakan titik masuk utama (entry point) aplikasi game Flappy Bird.
 * Menginisialisasi window utama (JFrame), mengatur ukuran kanvas (360x640),
 * dan mengelola perpindahan layar antara Main Menu dan Game View menggunakan CardLayout.
 * Sesuai dengan spesifikasi Modul 6 dan Modul 7 DPBO.
 */
public class App {
    public static void main(String[] args) {
        // ==========================================
        // 1. INISIASI JFRAME WINDOW (MODUL 6 LANGKAH 1)
        // ==========================================
        JFrame frame = new JFrame("FlappyBird");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Menutup aplikasi saat window di-close
        frame.setSize(360, 640);                              // Resolusi standar 360 x 640 piksel
        frame.setLocationRelativeTo(null);                    // Menempatkan window tepat di tengah layar monitor
        frame.setResizable(false);                            // Ukuran window dibuat tetap (tidak dapat diubah)

        // ==========================================
        // 2. INISIASI LOGIC & VIEW (MODUL 6 & 7)
        // ==========================================
        Logic logika = new Logic();
        View tampilan = new View(logika);
        logika.setView(tampilan);

        // ==========================================
        // 3. PENGATURAN CARDLAYOUT UNTUK NAVIGASI (MODUL 7 SPESIFIKASI 5)
        // ==========================================
        CardLayout cardLayout = new CardLayout();
        JPanel rootPanel = new JPanel(cardLayout);

        // ==========================================
        // 4. MEMBUAT MAIN MENU DENGAN 2 TOMBOL (MODUL 7 SPESIFIKASI 5)
        // ==========================================
        MainMenu mainMenu = new MainMenu(
                // Aksi Tombol 1: "Memulai Permainan"
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        cardLayout.show(rootPanel, "GAME"); // Beralih ke layar gameplay
                        tampilan.requestFocus();            // Memastikan game view menerima input keyboard
                        logika.startGame();                 // Memulai jalannya permainan
                    }
                },
                // Aksi Tombol 2: "Keluar dari Permainan"
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        System.exit(0);                     // Menutup seluruh aplikasi
                    }
                }
        );

        // Menambahkan panel Main Menu dan Game View ke dalam CardLayout
        rootPanel.add(mainMenu, "MENU");
        rootPanel.add(tampilan, "GAME");

        // Menambahkan rootPanel ke dalam frame utama
        frame.add(rootPanel);
        frame.pack();
        frame.setVisible(true); // Menampilkan window ke layar pengguna
    }
}

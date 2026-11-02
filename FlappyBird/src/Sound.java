import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Kelas Sound bertanggung jawab untuk mengelola seluruh audio dalam permainan,
 * mencakup pemutaran Background Music (BGM) secara berulang (loop) dan efek suara (SFX).
 * Memenuhi Spesifikasi Bonus (+20) pada Modul 7.
 */
public class Sound {
    // Clip aktif untuk Background Music (BGM)
    private Clip bgmClip;

    // Cache memori byte data audio agar suara SFX dapat diputar instan tanpa jeda I/O
    private Map<String, byte[]> audioCache = new HashMap<>();

    /**
     * Konstruktor: Melakukan pre-load file audio ke memori (audio caching)
     * agar saat game berjalan efek suara langsung terdengar responsif.
     */
    public Sound() {
        preloadAudio("wing.wav");
        preloadAudio("point.wav");
        preloadAudio("hit.wav");
    }

    /**
     * Membaca dan menyimpan file audio ke dalam buffer byte memori.
     */
    private void preloadAudio(String fileName) {
        try {
            byte[] data = loadAudioBytes(fileName);
            if (data != null) {
                audioCache.put(fileName, data);
            }
        } catch (Exception ignored) {}
    }

    /**
     * Memutar efek suara (SFX) sekali putar secara asynchronous.
     * Digunakan untuk:
     * - wing.wav  : Saat burung mengepakkan sayap / melompat
     * - point.wav : Saat burung berhasil melewati rintangan pipa
     * - hit.wav   : Saat burung menabrak rintangan atau terjatuh ke tanah
     *
     * @param fileName Nama file audio di folder assets (misal: "wing.wav")
     */
    public void playSound(String fileName) {
        try {
            byte[] data = audioCache.get(fileName);
            AudioInputStream audioStream;

            if (data != null) {
                // Gunakan cache memori untuk performa tinggi tanpa delay
                audioStream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(data));
            } else {
                audioStream = getAudioStream(fileName);
            }

            if (audioStream != null) {
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                clip.start();

                // Bersihkan sumber daya clip secara otomatis setelah selesai diputar
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
            }
        } catch (Exception e) {
            // Error audio tidak boleh menghentikan jalannya loop game
        }
    }

    /**
     * Memutar Background Music (BGM) secara terus menerus (looping).
     *
     * @param fileName Nama file musik latar di folder assets (misal: "bgm.wav")
     */
    public void playBGM(String fileName) {
        try {
            stopBGM(); // Hentikan BGM sebelumnya jika ada yang sedang aktif

            AudioInputStream audioStream = getAudioStream(fileName);
            if (audioStream != null) {
                bgmClip = AudioSystem.getClip();
                bgmClip.open(audioStream);
                bgmClip.loop(Clip.LOOP_CONTINUOUSLY); // Ulang terus menerus
                bgmClip.start();
            }
        } catch (Exception e) {
            // Fallback aman jika sistem audio tidak tersedia
        }
    }

    /**
     * Menghentikan Background Music (BGM) yang sedang berjalan dan membebaskan sumber daya.
     */
    public void stopBGM() {
        try {
            if (bgmClip != null && bgmClip.isOpen()) {
                bgmClip.stop();
                bgmClip.close();
            }
        } catch (Exception ignored) {}
    }

    /**
     * Helper untuk membaca stream audio dari classpath (JAR) maupun direktori lokal (IDE).
     */
    private AudioInputStream getAudioStream(String fileName) {
        try {
            URL url = getClass().getResource("assets/" + fileName);
            if (url == null) {
                url = getClass().getResource("/assets/" + fileName);
            }
            if (url != null) {
                return AudioSystem.getAudioInputStream(url);
            }
            File file = new File("src/assets/" + fileName);
            if (file.exists()) {
                return AudioSystem.getAudioInputStream(file);
            }
        } catch (Exception ignored) {}
        return null;
    }

    /**
     * Helper untuk membaca seluruh byte data dari file audio.
     */
    private byte[] loadAudioBytes(String fileName) {
        try {
            InputStream is = getClass().getResourceAsStream("assets/" + fileName);
            if (is == null) {
                is = getClass().getResourceAsStream("/assets/" + fileName);
            }
            if (is == null) {
                File file = new File("src/assets/" + fileName);
                if (file.exists()) {
                    is = new java.io.FileInputStream(file);
                }
            }
            if (is != null) {
                return is.readAllBytes();
            }
        } catch (Exception ignored) {}
        return null;
    }
}

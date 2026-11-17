package model;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * InMemoryKendaraanRepository adalah implementasi in-memory dari IKendaraanRepository.
 * 
 * Sesuai DIP: Memungkinkan pengujian unit (unit testing) dan operasi cepat tanpa terikat
 * pada berkas fisik di disk ataupun database MySQL.
 * 
 * Penulis: Arjuna Lanang Adiwarsana (3125522010)
 */
public class InMemoryKendaraanRepository implements IKendaraanRepository {

    private final List<Kendaraan> storage = new ArrayList<>();

    @Override
    public void simpan(Path lokasi) throws IOException {
        // Simulasi in-memory persistensi tanpa overhead I/O disk
    }

    @Override
    public List<Kendaraan> muat(Path lokasi) throws IOException {
        return new ArrayList<>(storage);
    }

    @Override
    public void tambah(Kendaraan k) throws FormatPlatTidakValidException {
        if (k == null) {
            throw new IllegalArgumentException("Kendaraan tidak boleh null.");
        }
        Kendaraan.validasiFormatPlat(k.getPlatNomor());
        if (cari(k.getPlatNomor()) != null) {
            throw new IllegalArgumentException("Kendaraan dengan plat '" + k.getPlatNomor() + "' sudah terdaftar.");
        }
        storage.add(k);
    }

    @Override
    public boolean hapus(String plat) {
        Kendaraan k = cari(plat);
        if (k == null) return false;
        return storage.remove(k);
    }

    @Override
    public Kendaraan cari(String plat) {
        if (plat == null || plat.trim().isEmpty()) return null;
        String target = plat.replaceAll("\\s+", "").toUpperCase();
        for (Kendaraan k : storage) {
            if (k.getPlatNomor().replaceAll("\\s+", "").toUpperCase().equals(target)) {
                return k;
            }
        }
        return null;
    }

    @Override
    public List<Kendaraan> getSemua() {
        return Collections.unmodifiableList(new ArrayList<>(storage));
    }
}

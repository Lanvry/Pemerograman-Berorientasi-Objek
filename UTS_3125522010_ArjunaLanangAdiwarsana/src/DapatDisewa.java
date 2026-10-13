public interface DapatDisewa {
    boolean isTersedia();
    void sewa(String idTransaksi);
    void kembalikan();
}

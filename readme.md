# ReZy

ReZy adalah game multiplayer 2D bertema aksi dengan fitur skill unik, power-up, dan sistem waktu dinamis. Dua pemain akan bertarung dalam arena penuh tantangan.

---

## 🕹️ Kontrol Pemain

### Player 1
- Gerak:
  - W = Atas
  - A = Kiri
  - S = Bawah
  - D = Kanan
- Skill:
  - E (default, dapat diubah di Settings)

### Player 2
- Gerak:
  - Arrow Up = Atas
  - Arrow Left = Kiri
  - Arrow Down = Bawah
  - Arrow Right = Kanan
- Skill:
  - END (default, dapat diubah di Settings)

---

## ✨ Daftar Skill

| Nama Skill       | Efek                                                                                              |
|------------------|---------------------------------------------------------------------------------------------------|
| The World        | Menghentikan waktu untuk sementara. Pergerakan dan skor lawan terhenti, pemain tetap normal.     |
| Star Platinum    | Menghapus peluru di sekitar karakter.                                                             |
| Made in Heaven   | Membalikkan waktu: pergerakan lawan terhenti, skor mereka perlahan berkurang.                    |
| Crazy Diamond    | Menambah nyawa.                                                                                   |
| The Hand         | Menembakkan laser ke atas, menghapus peluru di jalurnya.                                         |
| King Crimson     | Meningkatkan kecepatan gerak secara drastis selama 10 detik.                                     |
| Silver Chariot   | Memanggil Spade Silver yang mengejar lawan dan bisa mengurangi nyawa mereka.                     |
| Gold Experience  | Mengaktifkan death loop: lawan terkena damage terus-menerus, lalu darahnya tersisa 1 saat selesai.|

---

## ⚡ Power-Up

| Icon | Nama   | Efek / Keterangan                        |
|------|--------|------------------------------------------|
|  +   | Heart  | Menambah nyawa                           |
|  ⛨   | Shield | Kebal sementara dari peluru              |
|  ⇶   | Speed  | Meningkatkan kecepatan gerak sementara   |
|  ⇄   | Swap   | Tukar nyawa lawan dan tambah nyawa       |
|  ✖   | Lock   | Mengunci skill lawan sementara           |

---

## ⚙️ Setup Game

1. Nyalakan MySQL dengan port `3306`.
2. Jalankan file `run.bat`.
3. Selesai! Game siap dimainkan.

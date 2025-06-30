# ReZy

## Kontrol Pemain

### **Player 1**
- **Gerak:**  
  - **W** = Atas  
  - **A** = Kiri  
  - **S** = Bawah  
  - **D** = Kanan  
- **Tombol Skill:**  
  - **E** (default, bisa diubah di Settings)

### **Player 2**
- **Gerak:**  
  - **Arrow Up** = Atas  
  - **Arrow Left** = Kiri  
  - **Arrow Down** = Bawah  
  - **Arrow Right** = Kanan  
- **Tombol Skill:**  
  - **END** (default, bisa diubah di Settings)

---

## Daftar Skill

| Nama Skill         | Efek                                                                                                                                                                                                 |
|--------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| The World          | Menghentikan pergerakan peluru untuk sementara waktu.<br>Cooldown lawan akan terhenti (pause) selama efek aktif, sedangkan cooldown pengguna tetap berjalan normal.                                 |
| Star Platinum      | Menghapus peluru di sekitar karakter.                                                                                                                        |
| Made in Heaven     | Membalikkan skor lawan secara perlahan.<br>Cooldown lawan akan terhenti (pause) selama efek aktif, sedangkan cooldown pengguna tetap berjalan normal.                                              |
| Crazy Diamond      | Menambah nyawa.                                                                                                                                              |
| The Hand           | Menembakkan laser ke atas, menghapus peluru di jalur laser. Cooldown 30 detik.                                                                              |
| King Crimson       | Meningkatkan kecepatan gerak secara drastis selama 10 detik. Cooldown 25 detik.                                                                             |
| Silver Chariot     | Memanggil summon (Spade Silver) yang mengejar lawan dan bisa mengurangi nyawa lawan. Cooldown sesuai database.                                              |
| Gold Experience    | Mengaktifkan efek visual nyawa lawan naik-turun (bertambah-kurang) selama 10 detik, lalu mengurangi nyawa lawan menjadi 1.<br>Cooldown lawan terhenti (pause) selama efek aktif.                   |

---

## Power-Up

| Icon | Nama      | Efek/Keterangan                       |
|------|-----------|---------------------------------------|
|  +   | Heart     | Menambah nyawa                        |
|  ⛨   | Shield    | Kebal sementara dari peluru           |
|  ⇶   | Speed     | Gerak cepat sementara                 |
|  ⇄   | Swap      | Tukar nyawa lawan & tambah nyawa      |
|  ✖   | Lock      | Mengunci skill lawan sementara        |

---

## Mekanisme Cooldown Skill

- **Cooldown skill lawan akan terhenti (pause) selama efek Time Stop, Time Reverse, atau Gold Experience aktif.**
- **Cooldown pengguna skill tetap berjalan normal.**
- Tampilan sisa cooldown di layar juga sudah sesuai dengan mekanisme ini.

---

## Setup Database

1. **Buat database baru** di MySQL, misal:  
   ```sql
   CREATE DATABASE game_db;
   ```
2. **Import File Database**  
3. **Setelah Semua Selesai, Klik File run.bat**  
4. **Selesai!
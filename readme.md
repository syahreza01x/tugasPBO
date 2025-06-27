# ReZy Retro Game MVC

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

| Nama Skill    | Efek                                                                                   |
|---------------|----------------------------------------------------------------------------------------|
| The World     | Menghentikan pergerakan peluru untuk sementara waktu. <br>Cooldown lawan akan terhenti (pause) selama efek aktif, sedangkan cooldown pengguna tetap berjalan normal. |
| Star Platinum    | Menghapus peluru di sekitar karakter.                                                  |
| Made in Heaven  | Membalikkan skor lawan secara perlahan. <br>Cooldown lawan akan terhenti (pause) selama efek aktif, sedangkan cooldown pengguna tetap berjalan normal. |
| Crazy Diamond  | Menambah nyawa.                                                                        |

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

- **Cooldown skill lawan akan terhenti (pause) selama efek Time Stop atau Time Reverse aktif.**
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
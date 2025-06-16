# ReZy Retro Game MVC

## Username Player 1 : Reza  
## Password Player 1 : 123  
## Username Player 2 : Ferdi  
## Password Player 2 : 789  
## Username Player 3 : Puci  
## Password Player 3 : 123  
## Username Player 4 : Fadilah  
## Password Player 4 : 789  

---

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
| Time Stop     | Menghentikan pergerakan peluru untuk sementara waktu. <br>Cooldown lawan akan terhenti (pause) selama efek aktif, sedangkan cooldown pengguna tetap berjalan normal. |
| Area Clear    | Menghapus peluru di sekitar karakter.                                                  |
| Time Reverse  | Membalikkan skor lawan secara perlahan. <br>Cooldown lawan akan terhenti (pause) selama efek aktif, sedangkan cooldown pengguna tetap berjalan normal. |
| Extra Health  | Menambah nyawa.                                                                        |

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
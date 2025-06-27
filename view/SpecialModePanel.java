package view;

import model.DatabaseManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.*;
import java.io.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class SpecialModePanel extends JPanel implements ActionListener, KeyListener {
    private final javax.swing.Timer timer = new javax.swing.Timer(20, this);
    private final int ROWS = 20, COLS = 60;
    private char[][] arena = new char[ROWS][COLS];
    private int playerX = ROWS - 2, playerY = COLS / 2;
    private int bossHP = 100, bossHPMax = 100;
    private boolean bossPhase = false, bossDead = false, blackScreen = false, mirrorPhase = false, endPhase = false;
    private boolean playerTurn = false, attackBarActive = false;
    private boolean showChat = false, showEnd = false;
    private int bossX = 2, bossY = COLS / 2 - 6;
    private int bossMoveTick = 0;
    private int bossAttackTick = 0, bossAttackPattern = 0, bossAttackTargetX = 0, bossAttackTargetY = 0;
    private int surviveTime = 30_000; // 30 detik
    private long startTime;
    private List<Bullet> bullets = new ArrayList<>();
    private List<Bullet> bossBullets = new ArrayList<>();
    private Random rand = new Random();
    private JFrame frame;
    private HomePage homePage;
    private String username;
    private int playerId;
    private Clip finalMusic;
    private Clip bossMusic;
    private Clip bgmMusic;
    private Clip laughMusic;
    private Clip attackMusic;
    private Clip hitMusic;
    private Clip dialogMusic;
    private Clip laughBadMusic;
    private DatabaseManager db;
    private int mirrorX = 2, mirrorY = COLS - 5;
    private int chatStep = 0;
    private int bulletTick = 0;
    private int attackBarPos = 0;
    private int attackBarDir = 1;
    private int attackBarTimer = 0;
    private String attackResult = "";

    // Mirror phase player
    private int mirrorPlayerX = ROWS - 2, mirrorPlayerY = COLS / 2;
    private boolean mirrorShowPlayer = false;

    // Dialog gambar cermin
    private int mirrorDialogStep = 0;
    private boolean showMirrorDialog = false;
    private BufferedImage[] mirrorDialogImgs = new BufferedImage[9];

    // Boss images
    private BufferedImage bossImg1, bossImg2, bossImg3, bossScareImg, bossBadImg, bossGoodImg;
    private BufferedImage bossAfter1Img, bossAfter2Img, bossAfter3Img, bossAfter4Img;

    // Attack effect
    private ImageIcon attackEffectImg;
    private boolean showAttackEffect = false;
    private int attackEffectTick = 0;
    private final int attackEffectDuration = 24; // sekitar 0.5 detik (24*20ms)
    private boolean pendingHitEffect = false;
    private int pendingHitDamage = 0;

    // Boss merah effect
    private boolean bossRedEffect = false;
    private int bossRedEffectTick = 0;
    private final int bossRedEffectDuration = 15; // sekitar 0.3 detik (15*20ms)

    // Final boss phase
    private boolean bossIntro = false;
    private boolean bossIntroDone = false;
    private long bossIntroStart = 0;

    // After boss death
    private boolean bossAfterDialog = false;
    private int bossAfterDialogStep = 0;
    private int bossAfterDialogMax = 1;
    private boolean bossChoice = false;
    private boolean bossKilled = false;
    private boolean bossSpared = false;
    private boolean bossChoiceSelectedKill = true; // true: KILL, false: SPARE
    private boolean bossBranchDialog = false;
    private int bossBranchDialogStep = 0;
    private int bossBranchDialogMax = 1;
    private boolean bossBranchDialogDone = false;
    private boolean bossBranchBlack = false;
    private boolean bossBranchBlackDone = false;

    public SpecialModePanel(int playerId, String username, DatabaseManager db, JFrame frame, HomePage homePage) {
        this.playerId = playerId;
        this.username = username;
        this.db = db;
        this.frame = frame;
        this.homePage = homePage;
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        timer.start();
        startTime = System.currentTimeMillis();
        playBGM();

        // Load boss images from assets/
        try {
            bossImg1 = ImageIO.read(new File("assets/boss1.png"));
            bossImg2 = ImageIO.read(new File("assets/boss2.png"));
            bossImg3 = ImageIO.read(new File("assets/boss3.png"));
            bossScareImg = ImageIO.read(new File("assets/bossScare.png"));
            bossBadImg = ImageIO.read(new File("assets/bossBad.png"));
            bossGoodImg = ImageIO.read(new File("assets/bossGood.png"));
            bossAfter1Img = ImageIO.read(new File("assets/bossAfter1.png"));
            bossAfter2Img = ImageIO.read(new File("assets/bossAfter2.png"));
            bossAfter3Img = ImageIO.read(new File("assets/bossAfter3.png"));
            bossAfter4Img = ImageIO.read(new File("assets/bossAfter4.png"));
        } catch (Exception ex) {
            bossImg1 = bossImg2 = bossImg3 = bossScareImg = bossBadImg = bossGoodImg = null;
            bossAfter1Img = bossAfter2Img = bossAfter3Img = bossAfter4Img = null;
        }

        // Load dialog images 1.png - 9.png
        for (int i = 0; i < 9; i++) {
            try {
                mirrorDialogImgs[i] = ImageIO.read(new File("assets/" + (i+1) + ".png"));
            } catch (Exception ex) {
                mirrorDialogImgs[i] = null;
            }
        }

        // Load attack effect gif
        try {
            attackEffectImg = new ImageIcon("assets/attackPlayer.gif");
        } catch (Exception ex) {
            attackEffectImg = null;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int cw = getWidth() / COLS;
        int ch = getHeight() / ROWS;
        int fontSize = Math.min(cw, ch) - 2;
        g.setFont(new Font("Monospaced", Font.BOLD, fontSize));

        if (blackScreen || bossBranchBlack) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
            if (bossBranchBlackDone) {
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 80));
                g.drawString("END", getWidth()/2-110, getHeight()/2+30);
            }
            return;
        }

        // Mirror phase: player, cermin, dialog, END
        if (mirrorPhase) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
            if (mirrorShowPlayer) {
                g.setColor(Color.PINK);
                g.drawString("♥", mirrorPlayerY * cw + cw / 4, mirrorPlayerX * ch + (3 * ch / 4));
            }
            g.setColor(Color.CYAN);
            int mirrorY = COLS / 2;
            g.drawString("▮", mirrorY * cw + cw / 4, 1 * ch + (3 * ch / 4));
            if (showMirrorDialog && mirrorDialogStep < 9 && mirrorDialogImgs[mirrorDialogStep] != null) {
                int imgW = 400, imgH = 120;
                int imgX = getWidth()/2 - imgW/2, imgY = getHeight() - imgH - 40;
                g.drawImage(mirrorDialogImgs[mirrorDialogStep], imgX, imgY, imgW, imgH, null);
            }
            if (endPhase) {
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 80));
                g.drawString("END", getWidth()/2-110, getHeight()/2+30);
                stopDialogMusic();
            }
            return;
        }

        // Boss intro laugh
        if (bossPhase && !bossIntroDone && bossIntro) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
            BufferedImage bossImg = bossImg1;
            int bossW = cw * 10, bossH = ch * 10;
            int px = bossY * cw, py = bossX * ch;
            if (bossImg != null)
                g.drawImage(bossImg, px, py, bossW, bossH, null);
            else {
                g.setColor(Color.WHITE);
                g.fillRect(px, py, bossW, bossH);
            }
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("...", getWidth()/2-30, getHeight()/2+30);
            return;
        }

        // Boss after death dialog
        if (bossAfterDialog) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
            if (bossScareImg != null)
                g.drawImage(bossScareImg, bossY * cw, bossX * ch, cw * 10, ch * 10, null);
            if (bossAfter1Img != null && bossAfterDialogStep == 0)
                g.drawImage(bossAfter1Img, getWidth()/2-200, getHeight()-160, 400, 120, null);
            // Pilihan KILL/SPARE
            if (bossChoice) {
                g.setFont(new Font("Arial", Font.BOLD, 32));
                int y = getHeight()-60;
                g.setColor(bossChoiceSelectedKill ? Color.YELLOW : Color.WHITE);
                g.drawString("KILL", getWidth()/2-120, y);
                g.setColor(!bossChoiceSelectedKill ? Color.YELLOW : Color.WHITE);
                g.drawString("SPARE", getWidth()/2+40, y);
            }
            return;
        }

        // Boss branch dialog (setelah KILL/SPARE)
        if (bossBranchDialog) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
            if (bossKilled && bossBadImg != null)
                g.drawImage(bossBadImg, bossY * cw, bossX * ch, cw * 10, ch * 10, null);
            if (bossSpared && bossGoodImg != null)
                g.drawImage(bossGoodImg, bossY * cw, bossX * ch, cw * 10, ch * 10, null);
            if (bossKilled) {
                if (bossAfter3Img != null && bossBranchDialogStep == 0)
                    g.drawImage(bossAfter3Img, getWidth()/2-200, getHeight()-160, 400, 120, null);
                if (bossAfter4Img != null && bossBranchDialogStep == 1)
                    g.drawImage(bossAfter4Img, getWidth()/2-200, getHeight()-160, 400, 120, null);
            }
            if (bossSpared) {
                if (bossAfter2Img != null && bossBranchDialogStep == 0)
                    g.drawImage(bossAfter2Img, getWidth()/2-200, getHeight()-160, 400, 120, null);
            }
            return;
        }

        // Arena dan player
        if (!mirrorPhase) {
            for (int i = 0; i < ROWS; i++) {
                for (int j = 0; j < COLS; j++) {
                    char c = arena[i][j];
                    g.setColor(Color.WHITE);
                    g.drawString(String.valueOf(c == '\0' ? ' ' : c), j * cw + cw / 4, i * ch + (3 * ch / 4));
                }
            }
            g.setColor(Color.PINK);
            g.drawString("♥", playerY * cw + cw / 4, playerX * ch + (3 * ch / 4));
        }

        // Peluru time rush (kuning)
        if (!bossPhase && !bossDead) {
            for (Bullet b : bullets) {
                g.setColor(Color.YELLOW);
                g.drawString("*", b.y * cw + cw / 4, b.x * ch + (3 * ch / 4));
            }
        }

        // Peluru boss (putih/merah)
        if (bossPhase && !bossDead) {
            for (Bullet b : bossBullets) {
                g.setColor(b.red ? Color.RED : Color.WHITE);
                g.drawString("*", b.y * cw + cw / 4, b.x * ch + (3 * ch / 4));
            }
        }

        // Boss pakai gambar
        if (bossPhase && !bossDead) {
            BufferedImage bossImg = bossImg1;
            if (bossHP <= 10) bossImg = bossImg3;
            else if (bossHP <= 30) bossImg = bossImg2;
            int bossW = cw * 10, bossH = ch * 10;
            int px = bossY * cw, py = bossX * ch;

            if (bossRedEffect) {
                BufferedImage redBoss = new BufferedImage(bossW, bossH, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = redBoss.createGraphics();
                if (bossImg != null)
                    g2.drawImage(bossImg, 0, 0, bossW, bossH, null);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.5f));
                g2.setColor(Color.RED);
                g2.fillRect(0, 0, bossW, bossH);
                g2.dispose();
                g.drawImage(redBoss, px, py, null);
            } else {
                if (bossImg != null)
                    g.drawImage(bossImg, px, py, bossW, bossH, null);
                else {
                    g.setColor(Color.WHITE);
                    g.fillRect(px, py, bossW, bossH);
                }
            }

            // Efek serangan (GIF) kecil di tengah bos
            if (showAttackEffect && attackEffectImg != null) {
                int effW = 120, effH = 120;
                int effX = px + bossW/2 - effW/2;
                int effY = py + bossH/2 - effH/2;
                g.drawImage(attackEffectImg.getImage(), effX, effY, effW, effH, null);
            }

            int barW = 400, barH = 36;
            int barX = getWidth()/2 - barW/2, barY = 20;
            g.setColor(Color.DARK_GRAY);
            g.fillRect(barX, barY, barW, barH);
            g.setColor(Color.RED);
            int hpW = (int)((bossHP/(double)bossHPMax)*barW);
            g.fillRect(barX, barY, hpW, barH);
            g.setColor(Color.WHITE);
            g.drawRect(barX, barY, barW, barH);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("BOSS HP: " + bossHP + " / " + bossHPMax, barX + 80, barY + 30);
        }

        // Timer
        if (!bossPhase && !bossDead) {
            int sisa = Math.max(0, (int)((surviveTime - (System.currentTimeMillis() - startTime))/1000));
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 22));
            g.drawString("Survive: " + sisa + "s", getWidth()/2-60, 40);
        }

        // Attack bar (player turn)
        if (bossPhase && playerTurn && attackBarActive) {
            int barW = 320, barH = 30;
            int barX = getWidth()/2 - barW/2, barY = getHeight()/2 + 120;
            g.setColor(Color.GRAY);
            g.fillRect(barX, barY, barW, barH);
            g.setColor(Color.DARK_GRAY);
            g.fillRect(barX, barY, 40, barH);
            g.setColor(Color.RED);
            g.fillRect(barX + 40, barY, 60, barH);
            g.setColor(Color.YELLOW);
            g.fillRect(barX + 100, barY, 40, barH);
            g.setColor(Color.GREEN);
            g.fillRect(barX + 140, barY, 20, barH);
            g.setColor(Color.YELLOW);
            g.fillRect(barX + 160, barY, 40, barH);
            g.setColor(Color.RED);
            g.fillRect(barX + 200, barY, 60, barH);
            g.setColor(Color.DARK_GRAY);
            g.fillRect(barX + 260, barY, 60, barH);
            g.setColor(Color.WHITE);
            g.drawRect(barX, barY, barW, barH);
            int pos = barX + attackBarPos;
            g.setColor(Color.BLACK);
            g.fillRect(pos-2, barY-4, 4, barH+8);
            g.setFont(new Font("Arial", Font.BOLD, 22));
            g.setColor(Color.WHITE);
            g.drawString("Press E to Attack!", barX, barY-10);
            if (!attackResult.isEmpty()) {
                g.drawString(attackResult, barX + barW + 20, barY + 24);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (endPhase) return;
        if (blackScreen || bossBranchBlack) return;
        if (mirrorPhase) {
            repaint();
            return;
        }
        // Boss intro laugh
        if (bossPhase && !bossIntroDone) {
            if (!bossIntro) {
                bossIntro = true;
                bossIntroStart = System.currentTimeMillis();
                playLaughMusic();
            } else {
                if (System.currentTimeMillis() - bossIntroStart >= 5000) {
                    bossIntroDone = true;
                    stopLaughMusic();
                }
            }
            repaint();
            return;
        }
        if (!bossPhase) {
            long now = System.currentTimeMillis();
            if (now - startTime >= surviveTime) {
                blackScreen = true;
                timer.stop();
                new java.util.Timer().schedule(new java.util.TimerTask() {
                    public void run() {
                        bossPhase = true;
                        blackScreen = false;
                        timer.start();
                        stopBGM();
                        playBossMusic();
                        bossIntro = false;
                        bossIntroDone = false;
                        bossIntroStart = 0;
                    }
                }, 1500);
                return;
            }
            if (bulletTick % 5 == 0) {
                int peluruCount = 2 + rand.nextInt(2);
                for (int i = 0; i < peluruCount; i++) {
                    int y = rand.nextInt(COLS);
                    bullets.add(new Bullet(0, y, 1, 0, false));
                }
            }
            bulletTick++;
            if (bulletTick % 2 == 0) {
                List<Bullet> toRemove = new ArrayList<>();
                for (Bullet b : bullets) {
                    b.x += b.dx;
                    b.y += b.dy;
                    if (b.x == playerX && b.y == playerY) {
                        timer.stop();
                        stopBGM();
                        JOptionPane.showMessageDialog(this, "Kamu terkena peluru! Coba lagi.");
                        backToHome();
                        return;
                    }
                    if (b.x >= ROWS) toRemove.add(b);
                }
                bullets.removeAll(toRemove);
            }
        } else if (!bossDead) {
            bossMoveTick++;
            if (bossMoveTick % 15 == 0) {
                int dir = rand.nextInt(4);
                if (dir == 0 && bossX > 0) bossX--;
                if (dir == 1 && bossX < ROWS / 2 - 10) bossX++;
                if (dir == 2 && bossY > 0) bossY--;
                if (dir == 3 && bossY < COLS - 10) bossY++;
            }
            if (!playerTurn) {
                bossAttackTick++;
                if (bossAttackTick == 1) {
                    bossAttackPattern = rand.nextInt(5);
                    bossBullets.clear();
                    bossAttackTargetX = playerX;
                    bossAttackTargetY = playerY;
                }
                if (bossAttackTick % 18 == 0 && bossAttackTick <= 72) {
                    spawnBossPattern(bossAttackPattern, bossAttackTargetX, bossAttackTargetY);
                }
                if (bossAttackTick % 2 == 0) {
                    List<Bullet> toRemove = new ArrayList<>();
                    for (Bullet b : bossBullets) {
                        if (b.bounce) {
                            b.x += b.dx;
                            b.y += b.dy;
                            if (b.x <= 0 || b.x >= ROWS - 1) {
                                b.dx = -b.dx;
                                b.x += b.dx;
                            }
                        } else {
                            if (b.dx == 1 && b.dy == 0) {
                                b.tick++;
                                if (b.tick % 2 == 0) {
                                    b.x += b.dx;
                                }
                            }
                            else if (b.dx == 0) {
                                b.y += b.dy;
                                b.tick++;
                            }
                            else {
                                b.x += b.dx;
                                b.y += b.dy;
                            }
                        }
                        if (b.x == playerX && b.y == playerY) {
                            timer.stop();
                            stopBossMusic();
                            int opt = JOptionPane.showOptionDialog(
                                this,
                                "Kamu terkena peluru! Coba lagi?",
                                "Game Over",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.INFORMATION_MESSAGE,
                                null,
                                new Object[]{"Coba Lagi", "Keluar"},
                                "Coba Lagi"
                            );
                            if (opt == JOptionPane.YES_OPTION) {
                                playerX = ROWS - 2;
                                playerY = COLS / 2;
                                bossBullets.clear();
                                playerTurn = false;
                                attackBarActive = false;
                                bossAttackTick = 0;
                                attackBarTimer = 0;
                                attackBarPos = 0;
                                attackBarDir = 1;
                                attackResult = "";
                                bossHP = bossHPMax;
                                stopBossMusic();
                                playBossMusic();
                                timer.start();
                            } else {
                                backToHome();
                            }
                            return;
                        }
                        if (b.x < 0 || b.x >= ROWS || b.y < 0 || b.y >= COLS) toRemove.add(b);
                    }
                    bossBullets.removeAll(toRemove);
                }
                if (bossAttackTick > 90 && bossBullets.isEmpty()) {
                    bossAttackTick = 0;
                    bossBullets.clear();
                    playerTurn = true;
                    attackBarActive = true;
                    attackBarPos = 0;
                    attackBarDir = 1;
                    attackResult = "";
                }
            } else if (attackBarActive) {
                attackBarTimer++;
                if (attackBarTimer % 1 == 0) {
                    attackBarPos += attackBarDir * 12;
                    if (attackBarPos <= 0) {
                        attackBarPos = 0;
                        attackBarDir = 1;
                    } else if (attackBarPos >= 320) {
                        attackBarPos = 320;
                        attackBarDir = -1;
                    }
                }
            }
        }

        // Update efek serangan dan efek merah bos
        if (showAttackEffect) {
            attackEffectTick++;
            if (attackEffectTick > attackEffectDuration) {
                showAttackEffect = false;
                attackEffectTick = 0;
                // Setelah animasi serangan selesai, baru trigger efek hit
                if (pendingHitEffect) {
                    bossRedEffect = true;
                    bossRedEffectTick = 0;
                    playHitMusic();
                    bossHP -= pendingHitDamage;
                    if (bossHP <= 0) {
                        bossHP = 0;
                        bossDead = true;
                        stopBossMusic();
                        blackScreen = true;
                        attackResult = "";
                        timer.stop();
                        new java.util.Timer().schedule(new java.util.TimerTask() {
                            public void run() {
                                blackScreen = false;
                                bossAfterDialog = true;
                                bossAfterDialogStep = 0;
                                bossAfterDialogMax = 1;
                                bossChoice = false;
                                bossKilled = false;
                                bossSpared = false;
                                bossBranchDialog = false;
                                bossBranchDialogStep = 0;
                                bossBranchDialogMax = 1;
                                bossBranchDialogDone = false;
                                bossBranchBlack = false;
                                bossBranchBlackDone = false;
                                stopHitMusic();
                                playDialogMusic();
                                repaint();
                            }
                        }, 1500);
                        pendingHitEffect = false;
                        pendingHitDamage = 0;
                        return;
                    }
                    pendingHitEffect = false;
                    pendingHitDamage = 0;
                }
            }
        }
        if (bossRedEffect) {
            bossRedEffectTick++;
            if (bossRedEffectTick > bossRedEffectDuration) {
                bossRedEffect = false;
            }
        }

        repaint();
    }

    // Pola serangan boss
    private void spawnBossPattern(int pattern, int targetX, int targetY) {
        switch (pattern) {
            case 0: // Vertikal lebih banyak
                for (int i = 0; i < 12; i++) {
                    int col = rand.nextInt(COLS);
                    bossBullets.add(new Bullet(0, col, 1, 0, false));
                }
                break;
            case 1: // Diagonal kiri bawah ke kanan atas (memantul)
                for (int offset = -4; offset <= 4; offset++) {
                    int startX = ROWS - 1;
                    int startY = 0 + offset;
                    if (startY >= 0 && startY < COLS)
                        bossBullets.add(new Bullet(startX, startY, -1, 1, false, true));
                }
                break;
            case 2: // Diagonal kanan atas ke kiri bawah (memantul)
                for (int offset = -4; offset <= 4; offset++) {
                    int startX = 0;
                    int startY = COLS - 1 - offset;
                    if (startY >= 0 && startY < COLS)
                        bossBullets.add(new Bullet(startX, startY, 1, -1, false, true));
                }
                break;
            case 3: // Horizontal kiri ke kanan (lebih banyak baris)
                for (int bx = Math.max(0, targetX - 2); bx <= Math.min(ROWS - 1, targetX + 2); bx++) {
                    int skipRow = rand.nextInt(5);
                    if (bx - Math.max(0, targetX - 2) == skipRow) continue;
                    bossBullets.add(new Bullet(bx, 0, 0, 1, false));
                }
                break;
            case 4: // Horizontal kanan ke kiri (lebih banyak baris)
                for (int bx = Math.max(0, targetX - 2); bx <= Math.min(ROWS - 1, targetX + 2); bx++) {
                    int skipRow = rand.nextInt(5);
                    if (bx - Math.max(0, targetX - 2) == skipRow) continue;
                    bossBullets.add(new Bullet(bx, COLS - 1, 0, -1, false));
                }
                break;
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (endPhase) return;

        // Boss after death dialog
        if (bossAfterDialog) {
            if (bossChoice) {
                if (e.getKeyCode() == KeyEvent.VK_A || e.getKeyCode() == KeyEvent.VK_LEFT) {
                    bossChoiceSelectedKill = true;
                    repaint();
                } else if (e.getKeyCode() == KeyEvent.VK_D || e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    bossChoiceSelectedKill = false;
                    repaint();
                } else if (e.getKeyCode() == KeyEvent.VK_E || e.getKeyCode() == KeyEvent.VK_ENTER) {
                    bossChoice = false;
                    bossAfterDialog = false;
                    bossBranchDialog = true;
                    bossBranchDialogStep = 0;
                    bossKilled = bossChoiceSelectedKill;
                    bossSpared = !bossChoiceSelectedKill;
                    bossBranchDialogMax = bossKilled ? 2 : 1;
                    playDialogMusic();
                    repaint();
                }
                return;
            }
            if (e.getKeyCode() == KeyEvent.VK_E || e.getKeyCode() == KeyEvent.VK_ENTER) {
                stopDialogMusic();
                playDialogMusic();
                bossAfterDialogStep++;
                if (bossAfterDialogStep >= bossAfterDialogMax) {
                    bossAfterDialogStep = bossAfterDialogMax - 1;
                    bossChoice = true;
                }
                repaint();
            }
            return;
        }

        // Boss branch dialog (setelah KILL/SPARE)
        if (bossBranchDialog) {
            if (e.getKeyCode() == KeyEvent.VK_E || e.getKeyCode() == KeyEvent.VK_ENTER) {
                stopDialogMusic();
                bossBranchDialogStep++;
                if (bossKilled && bossBranchDialogStep >= bossBranchDialogMax) {
                    bossBranchDialog = false;
                    bossBranchBlack = true;
                    stopDialogMusic();
                    try {
                        AudioInputStream audioIn = AudioSystem.getAudioInputStream(new File("sounds/laughBad.wav"));
                        laughBadMusic = AudioSystem.getClip();
                        laughBadMusic.open(audioIn);
                        laughBadMusic.addLineListener(event -> {
                            if (event.getType() == LineEvent.Type.STOP) {
                                laughBadMusic.close();
                                System.exit(0);
                            }
                        });
                        laughBadMusic.start();
                    } catch (Exception ex) {
                        System.exit(0);
                    }
                    repaint();
                    return;
                } else if (bossSpared && bossBranchDialogStep >= bossBranchDialogMax) {
                    bossBranchDialog = false;
                    bossBranchBlack = false;
                    bossBranchBlackDone = false;
                    bossPhase = false;
                    bossDead = true;
                    blackScreen = false;
                    mirrorPhase = true;
                    mirrorShowPlayer = true;
                    showChat = false;
                    showEnd = false;
                    stopBossMusic();
                    playFinalMusic();
                    timer.start();
                } else {
                    playDialogMusic();
                }
                repaint();
            }
            return;
        }

        // Mirror phase movement & dialog
        if (mirrorPhase && mirrorShowPlayer) {
            if (showMirrorDialog) {
                if (e.getKeyCode() == KeyEvent.VK_E) {
                    stopDialogMusic();
                    playDialogMusic();
                    if (mirrorDialogStep < 8) {
                        mirrorDialogStep++;
                    } else {
                        showMirrorDialog = false;
                        endPhase = true;
                    }
                }
                repaint();
                return;
            }
            if (e.getKeyCode() == KeyEvent.VK_W && mirrorPlayerX > 0) mirrorPlayerX--;
            else if (e.getKeyCode() == KeyEvent.VK_S && mirrorPlayerX < ROWS - 2) mirrorPlayerX++;
            else if (e.getKeyCode() == KeyEvent.VK_A && mirrorPlayerY > 0) mirrorPlayerY--;
            else if (e.getKeyCode() == KeyEvent.VK_D && mirrorPlayerY < COLS - 1) mirrorPlayerY++;
            if (Math.abs(mirrorPlayerX - 1) <= 1 && Math.abs(mirrorPlayerY - COLS/2) <= 1 && e.getKeyCode() == KeyEvent.VK_E) {
                showMirrorDialog = true;
                mirrorDialogStep = 0;
                stopDialogMusic();
                playDialogMusic();
            }
            repaint();
            return;
        }
        if (mirrorPhase) {
            repaint();
            return;
        }

        // Boss phase: attack bar
        if (bossPhase && playerTurn && attackBarActive && e.getKeyCode() == KeyEvent.VK_E) {
            stopAttackMusic();
            int pos = attackBarPos;
            final String color;
            final int dmg;
            String tempColor = "MISS";
            int tempDmg = 0;
            if (pos >= 40 && pos < 100) { tempColor = "RED"; tempDmg = 1 + rand.nextInt(2); }
            else if (pos >= 100 && pos < 140) { tempColor = "YELLOW"; tempDmg = 3 + rand.nextInt(2); }
            else if (pos >= 140 && pos < 160) { tempColor = "GREEN"; tempDmg = 5; }
            else if (pos >= 160 && pos < 200) { tempColor = "YELLOW"; tempDmg = 3 + rand.nextInt(2); }
            else if (pos >= 200 && pos < 260) { tempColor = "RED"; tempDmg = 1 + rand.nextInt(2); }
            color = tempColor;
            dmg = tempDmg;
            if (color.equals("MISS")) attackResult = "MISS!";
            else attackResult = "HIT! (" + color + ") -" + dmg;
            attackBarActive = false;
            playerTurn = false;
            bossAttackTick = 0;
            attackBarTimer = 0;
            attackBarPos = 0;
            attackBarDir = 1;

            // Efek serangan dan pending hit
            if (!color.equals("MISS")) {
                showAttackEffect = true;
                attackEffectTick = 0;
                pendingHitEffect = true;
                pendingHitDamage = dmg;
            }

            // Putar attack.wav bersamaan dengan efek
            playAttackMusic();

            // Damage dan hit.wav diberikan setelah animasi selesai (lihat actionPerformed)
            attackResult = "";
            repaint();
            return;
        }
        // Movement
        if (!bossPhase || (bossPhase && !playerTurn)) {
            if (e.getKeyCode() == KeyEvent.VK_W && playerX > 0) playerX--;
            else if (e.getKeyCode() == KeyEvent.VK_S && playerX < ROWS-1) playerX++;
            else if (e.getKeyCode() == KeyEvent.VK_A && playerY > 0) playerY--;
            else if (e.getKeyCode() == KeyEvent.VK_D && playerY < COLS-1) playerY++;
            repaint();
        }
    }
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    private void backToHome() {
        timer.stop();
        stopBossMusic();
        stopFinalMusic();
        stopBGM();
        stopLaughMusic();
        stopAttackMusic();
        stopHitMusic();
        stopDialogMusic();
        stopLaughBadMusic();
        frame.setContentPane(homePage);
        frame.revalidate();
        homePage.refreshHighScore();
    }

    private void playBossMusic() {
        try {
            stopBossMusic();
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(new File("sounds/boss.wav"));
            bossMusic = AudioSystem.getClip();
            bossMusic.open(audioIn);
            bossMusic.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception ex) {}
    }
    private void stopBossMusic() {
        if (bossMusic != null && bossMusic.isRunning()) {
            bossMusic.stop();
            bossMusic.close();
        }
    }

    private void playFinalMusic() {
        try {
            stopFinalMusic();
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(new File("sounds/final.wav"));
            finalMusic = AudioSystem.getClip();
            finalMusic.open(audioIn);
            finalMusic.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception ex) {}
    }
    private void stopFinalMusic() {
        if (finalMusic != null && finalMusic.isRunning()) {
            finalMusic.stop();
            finalMusic.close();
        }
    }
    private void playBGM() {
        try {
            stopBGM();
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(new File("sounds/bgm.wav"));
            bgmMusic = AudioSystem.getClip();
            bgmMusic.open(audioIn);
            bgmMusic.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception ex) {}
    }
    private void stopBGM() {
        if (bgmMusic != null && bgmMusic.isRunning()) {
            bgmMusic.stop();
            bgmMusic.close();
        }
    }
    private void playLaughMusic() {
        try {
            stopLaughMusic();
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(new File("sounds/laugh.wav"));
            laughMusic = AudioSystem.getClip();
            laughMusic.open(audioIn);
            laughMusic.start();
        } catch (Exception ex) {}
    }
    private void stopLaughMusic() {
        if (laughMusic != null && laughMusic.isRunning()) {
            laughMusic.stop();
            laughMusic.close();
        }
    }
    private void playAttackMusic() {
        try {
            stopAttackMusic();
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(new File("sounds/attack.wav"));
            attackMusic = AudioSystem.getClip();
            attackMusic.open(audioIn);
            attackMusic.start();
        } catch (Exception ex) {}
    }
    private void stopAttackMusic() {
        if (attackMusic != null && attackMusic.isRunning()) {
            attackMusic.stop();
            attackMusic.close();
        }
    }
    private void playHitMusic() {
        try {
            stopHitMusic();
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(new File("sounds/hit.wav"));
            hitMusic = AudioSystem.getClip();
            hitMusic.open(audioIn);
            hitMusic.start();
        } catch (Exception ex) {}
    }
    private void stopHitMusic() {
        if (hitMusic != null && hitMusic.isRunning()) {
            hitMusic.stop();
            hitMusic.close();
        }
    }
    private void playDialogMusic() {
        try {
            stopDialogMusic();
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(new File("sounds/dialog.wav"));
            dialogMusic = AudioSystem.getClip();
            dialogMusic.open(audioIn);
            dialogMusic.start();
        } catch (Exception ex) {}
    }
    private void stopDialogMusic() {
        if (dialogMusic != null && dialogMusic.isRunning()) {
            dialogMusic.stop();
            dialogMusic.close();
        }
    }
    private void playLaughBadMusic() {
        try {
            stopLaughBadMusic();
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(new File("sounds/laughBad.wav"));
            laughBadMusic = AudioSystem.getClip();
            laughBadMusic.open(audioIn);
            laughBadMusic.start();
        } catch (Exception ex) {}
    }
    private void stopLaughBadMusic() {
        if (laughBadMusic != null && laughBadMusic.isRunning()) {
            laughBadMusic.stop();
            laughBadMusic.close();
        }
    }

    static class Bullet {
        int x, y, dx, dy;
        boolean red;
        int tick = 0;
        boolean bounce = false;
        Bullet(int x, int y, int dx, int dy, boolean red) {
            this.x = x; this.y = y; this.dx = dx; this.dy = dy; this.red = red;
        }
        Bullet(int x, int y, int dx, int dy, boolean red, boolean bounce) {
            this.x = x; this.y = y; this.dx = dx; this.dy = dy; this.red = red; this.bounce = bounce;
        }
    }
}
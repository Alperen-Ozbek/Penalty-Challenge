package penaltyChallenge;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class game extends JPanel implements ActionListener {
	private BufferedImage image;
	private BufferedImage baslangic;
	private BufferedImage kaleci;
	private BufferedImage top;
	private BufferedImage kalp;
	private BufferedImage warning;
	private int topcapi = 26;
	private static int konumx;
	private static int konumy;
	private static int topX;
	private static int topY;
	private boolean durma = false; // topun durdurdugu durum icin bayrak
	private boolean dragging = false;
	private int lastMouseX, lastMouseY; // fare konumlari
	private double velocityX = 0, velocityY = 0; // Fırlatma hızları
	private int hareket = 3; // Başlangıç hızı
	private int can = 3; // can sayaci
	private int golSayaci = 0; // Gol sayacı
	private static final double MAX_SPEED = 30.0; // topun maximum hizi

	private boolean canplus = false;
	private boolean hasCollidedWithGoalkeeper = false; // kaleci carpisma durumu icin bayrak ekledik
	private boolean beskati = true;// her 5 golde hizi arttirmak icin bayrak ekledik
	private boolean pole = false; // direk icin bayrak ekledik
	private boolean firstplay = true;
	private boolean secondplay = false;
	private boolean uyari = false;
	private int startX = 0, startY = 0; // Çizginin başlangıç noktası
	private int endX = 0, endY = 0; // Çizginin bitiş noktası

	public game() {

		try {// burda dosyadan görselleri cekiyoruz
			baslangic = ImageIO.read(new FileImageInputStream(
					new File("C:\\Users\\ozbek\\eclipse-workspace\\penaltyChallenge\\baslangic.png")));
			image = ImageIO.read(new FileImageInputStream(
					new File("C:\\Users\\ozbek\\eclipse-workspace\\penaltyChallenge\\messi.png")));
			kaleci = ImageIO.read(new FileImageInputStream(
					new File("C:\\Users\\ozbek\\eclipse-workspace\\penaltyChallenge\\kaleci.png")));
			top = ImageIO.read(new FileImageInputStream(
					new File("C:\\Users\\ozbek\\eclipse-workspace\\penaltyChallenge\\top.png")));
			kalp = ImageIO.read(new FileImageInputStream(
					new File("C:\\Users\\ozbek\\eclipse-workspace\\penaltyChallenge\\can.png")));
			warning = ImageIO.read(new FileImageInputStream(
					new File("C:\\Users\\ozbek\\eclipse-workspace\\penaltyChallenge\\warning.png")));
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}

		Timer timer = new Timer(5, this);// her 5 milisaniyede bir eylem gerceklesecek
		timer.start();
		if (firstplay) {// ----------------------------------giris
						// ekrani---------------------------------------
			// Panel oluşturma ve düzenleme
			JPanel panel = new JPanel();

			// "Oyun" butonu
			JButton startButton = new JButton("Oyun");
			startButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {

					secondplay = true; // Saha sınıfındaki oyunu başlatan metot

					remove(panel); // paneli kaldır
					revalidate();
					repaint();
				}
			});
			// "Nasıl Oynanır" butonu
			JButton howToPlayButton = new JButton("Nasıl Oynanır");
			howToPlayButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JOptionPane.showMessageDialog(null,
							"Oyun talimatları:\n1. 3 can hakkınız var. \n2. Sut çekmek için mouseden yardim alın. "
									+ "\n3. Her 5 golde kalecinin hızı 1.5 kat artıyor.\n4. Canın 3 ten az ise her 5 golde 1 canın artar. "
									+ "\n5. Sut atarken çok hızlı vurmayın, yoksa kontrolü kaybedebilirsiniz. Dikkatli olun! "
									+ "\n6. Direğe, kaleciye yada dışarıya attığınız durumlarda canınız 1 eksiliyor. "
									+ "\n7. Tek bir amacınız var oda gol atmak :)\n     İYİ OYUNLAR!!!");
				}
			});

			// "Çıkış" butonu
			JButton exitButton = new JButton("Çıkış");
			exitButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					System.exit(0); // Oyunu sonlandır
				}
			});

			// Butonları ekle
			panel.add(startButton);
			panel.add(howToPlayButton);
			panel.add(exitButton);

			// Paneli çerçeveye ekle
			add(panel);

			repaint();
		} // ----------------------------------------giris ekrani
			// bitis----------------------------------------------------------------

		if (!secondplay) { // -------------------------------oyun
							// ekrani---------------------------------------------------------------
			addMouseListener(new MouseAdapter() {

				@Override
				public void mousePressed(MouseEvent e) {
					if (isInsideTop(e.getX(), e.getY())) {
						dragging = true;

						lastMouseX = e.getX();
						lastMouseY = e.getY();
						velocityX = 0;
						velocityY = 0;

					}
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					if (dragging) {
						dragging = false;

						// Şut hızını belirle (fare hareketinin ters yönünde)
						velocityX = (lastMouseX - e.getX()) * 0.5; // Hız faktörü
						velocityY = (lastMouseY - e.getY()) * 0.5;

					}

					// Eğer şut tamamlandıysa, topu başlangıç pozisyonuna döndür
					if (velocityX == 0 && velocityY == 0) {
						topX = getWidth() / 2 - (topcapi / 2) + 5;
						topY = ((getHeight() / 10) * 6) + 19;
					}

				}

			});
			addMouseMotionListener(new MouseMotionAdapter() {
				@Override
				public void mouseDragged(MouseEvent e) {
					if (dragging) {
						// Fare hareketine göre ters yön çizgisini hesapla
						int dx = lastMouseX - e.getX();
						int dy = lastMouseY - e.getY();

						// Çekilen mesafeyi hesapla
						double distance = Math.sqrt(dx * dx + dy * dy); // Mesafe

						if (distance >= 100) {
							distance = 100;
							uyari = true;
						} else {
							uyari = false;
						}
						// Çizginin uzunluğunu, çekilen mesafeye göre belirle
						double length = distance; // Çekilen mesafe kadar uzunluk
						double magnitude = Math.sqrt(dx * dx + dy * dy);

						// Ters yön çizgisini hesapla
						endX = (int) (lastMouseX + (dx / magnitude * length)); // Ters x
						endY = (int) (lastMouseY + (dy / magnitude * length)); // Ters y
						startX = (int) (lastMouseX - (dx / magnitude * length)); // düz x
						startY = (int) (lastMouseY - (dy / magnitude * length)); // düz y

						repaint(); // Çizimi sürekli güncelle
					}
				}
			});

		} // ----------------------------------------- oyun ekrani
			// bitis------------------------------------
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("Penalty Challenge");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1500, 1000); // Pencere boyutunu ayarla
		frame.setLocationRelativeTo(null); // Pencereyi ortala
		game field = new game();
		frame.add(field);

		frame.setVisible(true);
		konumx = frame.getWidth() / 2;
		konumy = frame.getHeight() / 40;
		topX = (frame.getWidth() / 2) - 14;
		topY = ((frame.getHeight() / 10) * 6);

	}

	private int readHighScore() {
		File file = new File("highscore.txt");
		if (file.exists()) {
			try (BufferedReader br = new BufferedReader(new FileReader(file))) {
				String line = br.readLine();
				if (line != null) {
					return Integer.parseInt(line); // Dosyadaki değeri integer'a dönüştür
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return 0; // Dosya yoksa sıfırdan başla
	}

	// En yüksek gol sayısını dosyaya yazma
	private void writeHighScore(int score) {
		File file = new File("highscore.txt");
		try (FileWriter fw = new FileWriter(file)) {
			fw.write(String.valueOf(score)); // Yüksek skoru dosyaya yaz
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void kalpçizdir(Graphics g, int a, int b) {
		if (kalp != null) {
			// Görüntünün genişlik ve yüksekliğini alıyoruz
			int width = kalp.getWidth();
			int height = kalp.getHeight();

			// Görüntüyü bit bit çizmek için iç içe iki döngü kullanıyoruz
			for (int y = 0; y < height; y += 30) {
				for (int x = 0; x < width; x += 30) {
					// Her bir pikselin rengini alıyoruz
					int pixelColor = kalp.getRGB(x, y);

					// Graphics objesiyle pikseli çiziyoruz
					g.setColor(new Color(pixelColor, true));
					g.fillRect((x / 30) + a, (y / 30) + b, 1, 1); // Tek bir
					// piksel
					// çiziyoruz
				}
			}
		}
	}

	private void topcizdir(Graphics g, int a, int b) {
		if (top != null) {
			// Görüntünün genişlik ve yüksekliğini alıyoruz
			int width = top.getWidth();
			int height = top.getHeight();

			// Görüntüyü bit bit çizmek için iç içe iki döngü kullanıyoruz
			for (int y = 0; y < height; y += 40) {
				for (int x = 0; x < width; x += 40) {
					// Her bir pikselin rengini alıyoruz
					int pixelColor = top.getRGB(x, y);

					// Graphics objesiyle pikseli çiziyoruz
					g.setColor(new Color(pixelColor, true));
					g.fillRect((x / 40) + a, (y / 40) + b, 1, 1); // Tek bir
																	// piksel
																	// çiziyoruz
				}
			}
		}

	}

	private void kalecizdir(Graphics g, int a, int b) {
		if (kaleci != null) {
			// Görüntünün genişlik ve yüksekliğini alıyoruz
			int width = kaleci.getWidth();
			int height = kaleci.getHeight();

			// Görüntüyü bit bit çizmek için iç içe iki döngü kullanıyoruz
			for (int y = 0; y < height; y += 10) {
				for (int x = 0; x < width; x += 10) {
					// Her bir pikselin rengini alıyoruz
					int pixelColor = kaleci.getRGB(x, y);

					// Graphics objesiyle pikseli çiziyoruz
					g.setColor(new Color(pixelColor, true));
					g.fillRect((x / 10) + a, (y / 10) + b, 1, 1); // Tek bir piksel çiziyoruz
				}
			}
		}

	}

	private void playercizdir(Graphics g, int a, int b) {
		if (image != null) {
			// Görüntünün genişlik ve yüksekliğini alıyoruz
			int width = image.getWidth();
			int height = image.getHeight();

			// Görüntüyü bit bit çizmek için iç içe iki döngü kullanıyoruz
			for (int y = 0; y < height; y += 10) {
				for (int x = 0; x < width; x += 10) {
					// Her bir pikselin rengini alıyoruz
					int pixelColor = image.getRGB(x, y);

					// Graphics objesiyle pikseli çiziyoruz
					g.setColor(new Color(pixelColor, true));
					g.fillRect((x / 10) + a, (y / 10) + b, 1, 1); // Tek bir
																	// piksel
																	// çiziyoruz
				}
			}
		}

	}

	private void warningcizdir(Graphics g, int a, int b) {
		if (warning != null) {
			// Dikdörtgenin sınırlarını belirliyoruz

			int rectWidth = warning.getWidth(); // Dikdörtgenin genişliği
			int rectHeight = warning.getHeight(); // Dikdörtgenin yüksekliği

			// Görüntünün genişlik ve yüksekliği
			int width = warning.getWidth();
			int height = warning.getHeight();

			// İç içe döngü ile dikdörtgen alanı tarıyoruz
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					// Her pikselin koordinatlarını gerçek konuma göre ayarla
					int px = x + a;
					int py = y + b;

					// Dikdörtgenin sınırları içinde kalıp kalmadığını kontrol et
					if (px >= a && px < a + rectWidth && py >= b && py < b + rectHeight) {
						// Pikselin rengini al ve çiz
						int pixelColor = warning.getRGB(x, y);
						g.setColor(new Color(pixelColor, true));
						g.fillRect(px, py, 1, 1); // Piksel çizimi
					}
				}
			}
		}

	}

	@Override
	public void paintComponent(Graphics g) {
		if (firstplay) {// ------------------------------baslangic
						// ekrani------------------------------------
			if (baslangic != null) {
				int x = (getWidth() - baslangic.getWidth()) / 2;
				int y = (getHeight() - baslangic.getHeight()) / 2;
				g.drawImage(baslangic, x, y, null); // Resmi çizme
			}
			if (secondplay) {
				firstplay = false;

			}
		} // -------------------------------------------baslangic ekrani
			// bitis---------------------------------

		if (secondplay) {// -----------------------------oyun
							// ekrani-------------------------------------------

			super.paintComponent(g);

			// Arka planı yeşil yapma
			g.setColor(new Color(34, 139, 34)); // Çimen yeşili bir renk kodu
			g.fillRect(0, 0, getWidth(), getHeight());

			// Çizgi rengini beyaz yapma
			g.setColor(Color.WHITE);
			Graphics2D g2d = (Graphics2D) g;
			g2d.setStroke(new BasicStroke(5)); // 5 piksel kalınlığında saha çizgisi

			// Kale çizgisi
			g.drawLine(0, (getHeight() / 80) * 5, getWidth(), (getHeight() / 80) * 5);
			g.drawLine(getWidth() / 3, 0, getWidth() / 3, (getHeight() / 80) * 5);
			g.drawLine(getWidth() / 3 * 2, 0, getWidth() / 3 * 2, (getHeight() / 80) * 5);

			// Ceza sahası
			g.drawRect(getWidth() / 12, (getHeight() / 80) * 5, (getWidth() / 6) * 5, (getHeight() / 80) * 60);

			// Kale alanı
			g.drawRect(getWidth() / 4, (getHeight() / 80) * 5, getWidth() / 2, (getHeight() / 80) * 30);

			// Penaltı noktası
			g.fillOval(getWidth() / 2, (getHeight() / 8) * 5, 10, 10);

			g2d.setStroke(new BasicStroke(1)); // 1 piksel kalınlığında file çizgisi

			// file cizgileri
			g.drawRect(getWidth() / 3, 0, getWidth() / 3, (getHeight() / 80) * 5);
			g.drawLine(getWidth() / 3, getHeight() / 20, getWidth() / 3 * 2, (getHeight() / 80) * 4);
			g.drawLine(getWidth() / 3, (getHeight() / 80) * 3, getWidth() / 3 * 2, (getHeight() / 80) * 3);
			g.drawLine(getWidth() / 3, (getHeight() / 80) * 2, getWidth() / 3 * 2, (getHeight() / 80) * 2);
			g.drawLine(getWidth() / 3, (getHeight() / 80), getWidth() / 3 * 2, (getHeight() / 80));
			for (int i = 0; i <= getWidth() / 3; i += 5) {// dikine file çizgileri
				g.drawLine((getWidth() / 3) + i, 0, (getWidth() / 3) + i, (getHeight() / 80) * 5);
			}
			// Hız sayacını ekranda göstermek
			g.setColor(Color.BLACK); // Yazı rengi siyah
			g.setFont(new Font("Arial", Font.BOLD, 20)); // Yazı tipi ve boyutu
			String speedText = String.format("Hız: %.2f", Math.sqrt(velocityX * velocityX + velocityY * velocityY)); // Hız
																														// değeri
			g.drawString(speedText, 20, 25); // Ekranın sol üst köşesinde göster

			// Ekranda en yüksek gol sayısını göster
			g.setColor(Color.BLACK); // Yazı rengi siyah
			g.setFont(new Font("Arial", Font.BOLD, 20)); // Yazı tipi ve boyutu
			String highScoreText = "Rekor: " + readHighScore(); // Dosyadaki en yüksek gol
			g.drawString(highScoreText, 20, 45); // Ekranın sol üst köşesinde göster

			// Gol sayacını sol üst köşede yazdırmak g.setColor(Color.BLACK); // Yazı
			g.setFont(new Font("Arial", Font.BOLD, 20)); // Yazı tipi ve
			String golMetni = "Gol: " + golSayaci;
			g.drawString(golMetni, 1350, 750);
			// sağ alt köşe

			topcizdir(g2d, topX, topY);

			kalecizdir(g2d, konumx, konumy);

			if (can == 3) {
				kalpçizdir(g2d, getWidth() - 100, 20);
				kalpçizdir(g2d, getWidth() - 130, 20);
				kalpçizdir(g2d, getWidth() - 160, 20);

			}

			if (can == 2) {
				kalpçizdir(g2d, getWidth() - 100, 20);
				kalpçizdir(g2d, getWidth() - 130, 20);
			}
			if (can == 1) {
				kalpçizdir(g2d, getWidth() - 100, 20);
			}

			for (int i = 0; i < golSayaci * 20; i += 20) {
				topcizdir(g2d, i, 770);
			}

			if (!beskati) {
				g.setColor(Color.BLACK); // Yazı rengi siyah
				g.setFont(new Font("Arial", Font.BOLD, 30)); // Yazı tipi ve boyutu
				String kalecinfo = "kaleci hızı 1.5 kat hızlandırıldı "; // kalecinin hizlandigini bildiren yazi
				g.drawString(kalecinfo, 0, 680); // sool alt

				String golsayisi = "gol sayısı =" + golSayaci;
				g.drawString(golsayisi, 0, 710); // sol alt

				if (canplus) {

					String cansayisi = "can sayınız 1 arttırıldı";
					g.drawString(cansayisi, 0, 770); // sol alt

				}

				int temp;
				temp = hareket;
				if (temp < 0) {
					temp = -temp;
				}

				String kalecihizi = "kaleci hızı =" + temp;
				g.drawString(kalecihizi, 0, 740); // sol alt

				repaint();
			}
			if (beskati && canplus) {
				canplus = false;
			}

			// Ters yön çizgisini çiz
			if (dragging) {
				g2d.setStroke(new BasicStroke(3));
				g.setColor(Color.BLACK);
				g.drawLine(lastMouseX, lastMouseY, endX, endY);
				if (uyari) {
					warningcizdir(g2d, 620, 650);
				}
				playercizdir(g2d, startX, startY);

			}
			if (!dragging) {
				playercizdir(g2d, getWidth() / 2, ((getHeight() / 8) * 5) + 50);
			}

		} // ----------------------------------------oyun ekrani
			// bitis-------------------------------------------------------------
	}

	private boolean isInsideTop(int x, int y) {// topun icinde olup olmadigini belirledigimiz fonksiyon
		int centerX = topX + topcapi / 2;
		int centerY = topY + topcapi / 2;
		int distSquared = (x - centerX) * (x - centerX) + (y - centerY) * (y - centerY);
		return distSquared <= (topcapi / 2) * (topcapi / 2);
	}

	private boolean isCollisionWithGoalkeeper() {// kaleci kurtaris durumu
		// Kalecinin dikdörtgeninin sınırları
		int kaleciLeft = konumx;
		int kaleciTop = konumy + 10;
		int kaleciRight = konumx + kaleci.getWidth() / 10;
		int kaleciBottom = konumy + 10 + kaleci.getHeight() / 10;

		// Topun dikdörtgeninin sınırları
		int topLeft = topX;
		int topTop = topY;
		int topRight = topX + topcapi;
		int topBottom = topY + topcapi;

		// çarpisip carpışmadiginin kontrolü
		return !(topRight < kaleciLeft || // Top kalecinin solunda
				topLeft > kaleciRight || // Top kalecinin sağında
				topBottom < kaleciTop || // Top kalecinin üstünde
				topTop > kaleciBottom); // Top kalecinin altında
	}

	private boolean hitpole() {// topun direge carpma durumu
		// Direk sınırları
		int leftpole = getWidth() / 3;
		int rightpole = (getWidth() / 3) * 2;
		int poleY = (getHeight() / 80) * 5;

		// Topun dikdörtgeninin sınırları
		int topLeft = topX;
		int topTop = topY;
		int topRight = topX + topcapi;
		int topBottom = topY + topcapi;

		// Direk çarpışması kontrolü
		boolean hitsLeftPole = topRight >= leftpole && topLeft <= leftpole && topBottom >= poleY && topTop <= poleY;
		boolean hitsRightPole = topLeft <= rightpole && topRight >= rightpole && topBottom >= poleY && topTop <= poleY;

		return hitsLeftPole || hitsRightPole;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (secondplay) {// --------------------------------------oyun
							// ekrani------------------------------------------------------------
			// Gol kontrolü
			if (topY <= (getHeight() / 80) * 5 - 5 && topX > getWidth() / 3 && topX <= (getWidth() / 3) * 2) {
				golSayaci++; // Gol sayacını artır
				topX = getWidth() / 2 - (topcapi / 2) + 5;
				topY = ((getHeight() / 10) * 6) + 19;
				velocityX = 0;
				velocityY = 0;
			} else if (topY <= (getHeight() / 80) * 5 && topX < getWidth() / 3 && topX > (getWidth() / 3) * 2) {
				can--;
				topX = getWidth() / 2 - (topcapi / 2) + 5;
				topY = ((getHeight() / 10) * 6) + 19;
				velocityX = 0;
				velocityY = 0;
			}
			// En yüksek gol sayısı kontrolü
			int highScore = readHighScore();
			if (golSayaci > highScore) {
				writeHighScore(golSayaci); // Yeni en yüksek gol sayısını dosyaya yaz
			}

			// Kalecinin hareket yönünü kontrol etme
			if (konumx >= (getWidth() / 3) * 2 - kaleci.getWidth() / 10) {
				hareket = -hareket; // Yön değiştir
			}
			if (konumx <= getWidth() / 3) {
				hareket = -hareket; // Yön değiştir
			}
			// x konumunu güncelle
			konumx += hareket;

			if (!dragging) {

				// Topun hareketini hız vektörlerine göre güncelle
				topX += velocityX;
				topY += velocityY;

				// Sürtünme etkisi
				velocityX *= 0.95;
				velocityY *= 0.95;

				// Çok düşük hızda durdur
				if (Math.abs(velocityX) < 0.1 && Math.abs(velocityY) < 0.1) {
					velocityX = 0;
					velocityY = 0;
					durma = true;
				}
				if (velocityX == 0 && velocityY == 0 && durma) {
					topX = getWidth() / 2 - (topcapi / 2) + 5;
					topY = ((getHeight() / 10) * 6) + 19;
				}

				// Sınır kontrolü
				if (topX < 0) {
					topX = 0;
					velocityX = -velocityX;
				}
				if (topX > getWidth() - topcapi) {
					topX = getWidth() - topcapi;
					velocityX = -velocityX;
				}

				if (topY > getHeight() - topcapi) {
					topY = getHeight() - topcapi;
					velocityY = -velocityY;
				}
				if (topY <= 0) {
					can--;
					topX = getWidth() / 2 - (topcapi / 2) + 5;
					topY = ((getHeight() / 10) * 6) + 19;
					velocityX = 0;
					velocityY = 0;

				}
				// Kaleciyle çarpışma kontrolü
				if (isCollisionWithGoalkeeper() && !hasCollidedWithGoalkeeper) {
					// Top kaleciye ilk çarptığında
					velocityY = -velocityY; // Top geri dönsün
					velocityX *= 0.5; // Çarpışma sonrası yatay hareketi azalt
					can--;
					hasCollidedWithGoalkeeper = true; // Çarpışmayı işaretle

				}
				if (!isCollisionWithGoalkeeper()) {
					hasCollidedWithGoalkeeper = false; // Çarpışma bayrağını sıfırla
				}
				if (hitpole() && !pole) {

					double angle = Math.atan2(velocityY, velocityX);
					if (topX + topcapi / 2 <= getWidth() / 2) {
						// Sol direk
						if (angle < 0) {
							velocityY = Math.abs(velocityY); // Dışarı
							can--;
						} else {
							velocityY = -Math.abs(velocityY); // Gol
						}
						velocityX = -Math.abs(velocityX);
					} else {
						// Sağ direk
						if (angle < 0) {
							velocityY = Math.abs(velocityY); // Dışarı
							can--;
						} else {
							velocityY = -Math.abs(velocityY); // Gol
						}
						velocityX = Math.abs(velocityX);
					}
					pole = true;
				}
				if (!hitpole()) {// carpisma bayragini sifirla
					pole = false;
				}

				if (can <= 0) {
					JOptionPane.showMessageDialog(this, "Oyun Bitti! Canlarınız tükendi.");
					System.exit(0); // Oyunu sonlandır

				}

				// her 5 golde hizi 1.5 kat arttir.
				if (golSayaci % 5 == 0 && golSayaci != 0 && beskati) {
					hareket *= 1.5;
					beskati = false;
					if (can < 3) {
						can++;
						canplus = true;
					}

					repaint();
				}
				if (!((golSayaci % 5) == 0)) {
					beskati = true;
					repaint();
				}

				// Hızı limitlendir
				if (Math.abs(velocityX) > MAX_SPEED) {
					velocityX = Math.signum(velocityX) * MAX_SPEED;
				}
				if (Math.abs(velocityY) > MAX_SPEED) {
					velocityY = Math.signum(velocityY) * MAX_SPEED;
				}

			}

			repaint();
		} // -------------------------------------oyun ekrani
			// bitis--------------------------------------------------
	}

}
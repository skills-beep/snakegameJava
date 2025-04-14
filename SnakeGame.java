import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class SnakeGame extends JFrame {
    public SnakeGame() {
        setTitle("🐍 Snake Game - Java Project");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        add(new GamePanel());
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        new SnakeGame();
    }
}

class GamePanel extends JPanel implements ActionListener, KeyListener {
    private final int WIDTH = 600;
    private final int HEIGHT = 600;
    private final int UNIT_SIZE = 20;
    private final int INITIAL_DELAY = 120;
    private final String HIGH_SCORE_FILE = "highscore.txt";

    private ArrayList<Point> snake = new ArrayList<>();
    private ArrayList<Point> grassPatches = new ArrayList<>();
    private final int GRASS_COUNT = 80;

    private Point food;
    private char direction = 'R';
    private boolean running = false;
    private boolean paused = false;
    private Timer timer;
    private Random random;
    private int score = 0;
    private int highScore = 0;
    private int delay = INITIAL_DELAY;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addKeyListener(this);
        loadHighScore();
        startGame();
    }

    private void startGame() {
        snake.clear();
        snake.add(new Point(100, 100));
        direction = 'R';
        score = 0;
        delay = INITIAL_DELAY;
        random = new Random();
        running = true;
        paused = false;
        spawnFood();
        generateGrass();
        if (timer != null) timer.stop();
        timer = new Timer(delay, this);
        timer.start();
    }

    private void spawnFood() {
        int x = random.nextInt(WIDTH / UNIT_SIZE) * UNIT_SIZE;
        int y = random.nextInt(HEIGHT / UNIT_SIZE) * UNIT_SIZE;
        food = new Point(x, y);
    }

    private void generateGrass() {
        grassPatches.clear();
        for (int i = 0; i < GRASS_COUNT; i++) {
            int x = random.nextInt(WIDTH / UNIT_SIZE) * UNIT_SIZE;
            int y = random.nextInt(HEIGHT / UNIT_SIZE) * UNIT_SIZE;
            grassPatches.add(new Point(x, y));
        }
    }

    private void move() {
        Point head = new Point(snake.get(0));
        switch (direction) {
            case 'U' -> head.y -= UNIT_SIZE;
            case 'D' -> head.y += UNIT_SIZE;
            case 'L' -> head.x -= UNIT_SIZE;
            case 'R' -> head.x += UNIT_SIZE;
        }
        snake.add(0, head);

        if (head.equals(food)) {
            score++;
            if (score % 5 == 0 && delay > 50) {
                delay -= 10;
                timer.setDelay(delay);
            }
            spawnFood();
        } else {
            snake.remove(snake.size() - 1);
        }
    }

    private void checkCollision() {
        Point head = snake.get(0);

        if (head.x < 0 || head.x >= WIDTH || head.y < 0 || head.y >= HEIGHT) {
            running = false;
        }

        for (int i = 1; i < snake.size(); i++) {
            if (head.equals(snake.get(i))) {
                running = false;
                break;
            }
        }

        if (!running) {
            timer.stop();
            if (score > highScore) {
                highScore = score;
                saveHighScore();
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBackground(g);
        if (running) {
            drawGame(g);
            if (paused) drawPaused(g);
        } else {
            drawGame(g);
            drawGameOver(g);
        }
    }

    private void drawBackground(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Color top = new Color(100, 180, 100);
        Color bottom = new Color(70, 130, 70);
        GradientPaint gp = new GradientPaint(0, 0, top, 0, HEIGHT, bottom);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        g.setColor(new Color(0, 100, 0, 40));
        for (int x = 0; x < WIDTH; x += UNIT_SIZE) {
            g.drawLine(x, 0, x, HEIGHT);
        }
        for (int y = 0; y < HEIGHT; y += UNIT_SIZE) {
            g.drawLine(0, y, WIDTH, y);
        }

        g.setColor(new Color(255, 255, 255, 30));
        for (Point p : grassPatches) {
            g.fillOval(p.x, p.y, UNIT_SIZE / 2, UNIT_SIZE / 2);
        }
    }

    private void drawGame(Graphics g) {
        g.setColor(Color.RED);
        g.fillOval(food.x, food.y, UNIT_SIZE, UNIT_SIZE);

        for (int i = 0; i < snake.size(); i++) {
            Point p = snake.get(i);
            if (i == 0) {
                g.setColor(new Color(144, 238, 144));
            } else {
                g.setColor(new Color(34, 139, 34));
            }
            g.fillRoundRect(p.x, p.y, UNIT_SIZE, UNIT_SIZE, 8, 8);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Segoe UI", Font.BOLD, 18));
        g.drawString("Score: " + score, 15, 25);
        g.drawString("High Score: " + highScore, 15, 45);
    }

    private void drawGameOver(Graphics g) {
        String msg = "GAME OVER";
        String restart = "Press ENTER to restart";

        g.setColor(Color.RED);
        g.setFont(new Font("Verdana", Font.BOLD, 38));
        FontMetrics fm = getFontMetrics(g.getFont());
        g.drawString(msg, (WIDTH - fm.stringWidth(msg)) / 2, HEIGHT / 2 - 40);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.drawString(restart, (WIDTH - 200) / 2, HEIGHT / 2);
    }

    private void drawPaused(Graphics g) {
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Consolas", Font.BOLD, 32));
        g.drawString("PAUSED", WIDTH / 2 - 70, HEIGHT / 2);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running && !paused) {
            move();
            checkCollision();
        }
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT -> { if (direction != 'R') direction = 'L'; }
            case KeyEvent.VK_RIGHT -> { if (direction != 'L') direction = 'R'; }
            case KeyEvent.VK_UP -> { if (direction != 'D') direction = 'U'; }
            case KeyEvent.VK_DOWN -> { if (direction != 'U') direction = 'D'; }
            case KeyEvent.VK_ENTER -> {
                if (!running) startGame();
            }
            case KeyEvent.VK_P -> {
                paused = !paused;
            }
        }
    }

    private void loadHighScore() {
        try (BufferedReader br = new BufferedReader(new FileReader(HIGH_SCORE_FILE))) {
            highScore = Integer.parseInt(br.readLine().trim());
        } catch (Exception e) {
            highScore = 0;
        }
    }

    private void saveHighScore() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(HIGH_SCORE_FILE))) {
            bw.write(String.valueOf(highScore));
        } catch (IOException e) {
            System.out.println("Failed to save high score.");
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}

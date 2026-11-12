import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class MiniMazeChallenge extends JFrame {

    
    String url = "jdbc:mysql://localhost:3306/maze_db";
    String username = "root";
    String password = "";

    Connection con;

    public void connectDatabase() {
        try {
            con = DriverManager.getConnection(url, username, password);
            System.out.println("MySQL Connected Successfully!");
        } catch (Exception e) {
            System.out.println("Connection Failed: " + e.getMessage());
        }
    }

    private BufferedImage playerSprite;
    private Clip moveSound;

    private int directionRow = 0;
    private int animationFrame = 0;
    private boolean walking = false;
    private Timer animationTimer;
    private Timer gameTimer;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);

    private final JLabel levelLabel = new JLabel();
    private final JLabel movesLabel = new JLabel();
    private final JLabel timeLabel = new JLabel();
    private final JLabel messageLabel = new JLabel();

    private final JTextField nicknameField = new JTextField(18);

    private MazePanel mazePanel;

    private int seconds;
    private int moves;
    private int level;
    
    private int totalMoves;
    private int totalSeconds;

    private String playerNickname = "";
    private boolean gameRunning;
    private final Random random = new Random();

    private static final int TOTAL_LEVELS = 5;
    private static final int TIME_LIMIT = 120;

    private static final Color BG = new Color(11, 16, 32);
    private static final Color PANEL = new Color(17, 27, 51);
    private static final Color PANEL_LIGHT = new Color(25, 38, 67);
    private static final Color CYAN = new Color(34, 211, 238);
    private static final Color MAGENTA = new Color(244, 114, 182);
    private static final Color GOLD = new Color(251, 191, 36);
    private static final Color WHITE = new Color(241, 245, 249);
    private static final Color MUTED = new Color(148, 163, 184);
    private static final Color GREEN = new Color(74, 222, 128);
    private static final Color RED = new Color(248, 113, 113);

    public MiniMazeChallenge() {
        super("Mini Maze Challenge");

        connectDatabase();

        try {
        playerSprite = ImageIO.read(new File("player3s.png"));

            System.out.println("Sprite loaded successfully!");
            
        } catch (IOException e) {
            System.out.println("Sprite loading failed: " + e.getMessage());
        }

        loadMoveSound();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 750);
        setMinimumSize(new Dimension(720, 650));
        setLocationRelativeTo(null);

        mainPanel.setBackground(BG);

        mainPanel.add(createHomePanel(), "HOME");
        mainPanel.add(createLoginPanel(), "LOGIN");
        mainPanel.add(createGamePanel(), "GAME");
        mainPanel.add(createEndPanel(false), "WIN");
        mainPanel.add(createEndPanel(true), "LOSE");

        setContentPane(mainPanel);

        gameTimer = new Timer(1000, e -> {
            if (gameRunning) {
                seconds++;
                totalSeconds++;
                updateStats();

                if (seconds >= TIME_LIMIT) {
                    finishGame(false);
                }
            }
        });

        animationTimer = new Timer(120, e -> {
            if (walking && gameRunning) {
                animationFrame = (animationFrame + 1) % 4;

                if (mazePanel != null) {
                    mazePanel.repaint();
                }
            }
        });

        animationTimer.start();
        cardLayout.show(mainPanel, "HOME");
    }

    private Font pixelFont(int size) {
        return new Font("Monospaced", Font.BOLD, size);
    }

    private javax.swing.border.Border pixelBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CYAN, 3),
            BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(4, 4, 4, 4),
                BorderFactory.createLineBorder(MAGENTA, 2)
            )
        );
    }

    private JPanel createBackground() {
        JPanel panel = new JPanel();
        panel.setBackground(BG);
        return panel;
    }

    private JPanel createHomePanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(BG);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 38, 30, 38));

        JPanel frame = new JPanel(new BorderLayout(15, 20));
        frame.setBackground(BG);
        frame.setBorder(pixelBorder());

        JPanel heading = new JPanel(new GridLayout(2, 1, 4, 4));
        heading.setBackground(BG);
        heading.setBorder(BorderFactory.createEmptyBorder(28, 12, 10, 12));

        JLabel logo = new JLabel("MINI MAZE", SwingConstants.CENTER);
        logo.setFont(pixelFont(43));
        logo.setForeground(CYAN);

        JLabel subtitle = new JLabel("CHALLENGE", SwingConstants.CENTER);
        subtitle.setFont(pixelFont(27));
        subtitle.setForeground(MAGENTA);

        heading.add(logo);
        heading.add(subtitle);

        JPanel center = new JPanel(new GridLayout(3, 1, 8, 8));
        center.setBackground(BG);
        center.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel description = new JLabel(
            "<html><div style='text-align:center;'>"
            + "FIND THE EXIT. BEAT THE CLOCK.<br><br>"
            + "CHOOSE THE SHORT OR LONG PATH!"
            + "</div></html>",
            SwingConstants.CENTER
        );
        description.setForeground(WHITE);
        description.setFont(pixelFont(15));

        JLabel controls = new JLabel(
            "ARROWS / WASD  |  120 SECONDS",
            SwingConstants.CENTER
        );
        controls.setForeground(GOLD);
        controls.setFont(pixelFont(12));

        JLabel decoration = new JLabel(
            "*  FIVE LEVELS. TWO ROUTES. ONE EXIT.  *",
            SwingConstants.CENTER
        );
        decoration.setForeground(CYAN);
        decoration.setFont(pixelFont(11));

        center.add(description);
        center.add(controls);
        center.add(decoration);

        JPanel buttons = new JPanel(new GridLayout(2, 1, 0, 12));
        buttons.setBackground(BG);
        buttons.setBorder(BorderFactory.createEmptyBorder(12, 100, 28, 100));

        JButton play = makeButton("▶ PLAY GAME");
        JButton howTo = makeButton("HOW TO PLAY");

        play.addActionListener(e -> {
            nicknameField.setText("");
            cardLayout.show(mainPanel, "LOGIN");
            SwingUtilities.invokeLater(
                () -> nicknameField.requestFocusInWindow()
            );
        });

        howTo.addActionListener(e -> {
            JTextArea instructions = new JTextArea(
                "HOW TO PLAY\n\n"
                + "> Enter your player nickname.\n"
                + "> Use ARROW KEYS or W, A, S, D to move.\n"
                + "> Reach the green E tile to finish a level.\n"
                + "> Walls block your movement.\n"
                + "> Complete each level within 120 seconds.\n"
                + "> Every level has a short and a long route.\n"
                + "> The short route needs fewer moves.\n"
                + "> The long route winds through the maze.\n"
                + "> Complete all five levels to save your score."
            );

            instructions.setEditable(false);
            instructions.setFocusable(false);
            instructions.setFont(pixelFont(13));
            instructions.setForeground(WHITE);
            instructions.setBackground(BG);
            instructions.setMargin(new Insets(18, 18, 18, 18));

            JOptionPane.showMessageDialog(
                this,
                instructions,
                "GAME INSTRUCTIONS",
                JOptionPane.PLAIN_MESSAGE
            );
        });


        buttons.add(play);
        buttons.add(howTo);

        frame.add(heading, BorderLayout.NORTH);
        frame.add(center, BorderLayout.CENTER);
        frame.add(buttons, BorderLayout.SOUTH);

        panel.add(frame, BorderLayout.CENTER);

        JLabel footer = new JLabel(
            "★  PREPARE TO BE AMAZE  ★",
            SwingConstants.CENTER
        );
        footer.setForeground(GOLD);
        footer.setFont(pixelFont(12));
        panel.add(footer, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(PANEL);
        card.setBorder(pixelBorder());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(11, 22, 11, 22);

        JLabel decoration = new JLabel(
            "★ Mini Maze Challenge ★",
            SwingConstants.CENTER
        );
        decoration.setFont(pixelFont(12));
        decoration.setForeground(GOLD);

        JLabel title = new JLabel(
            "PLAYER LOGIN",
            SwingConstants.CENTER
        );
        title.setFont(pixelFont(27));
        title.setForeground(CYAN);

        JLabel instruction = new JLabel(
            "ENTER YOUR NICKNAME",
            SwingConstants.CENTER
        );
        instruction.setForeground(MAGENTA);
        instruction.setFont(pixelFont(13));

        nicknameField.setFont(pixelFont(17));
        nicknameField.setHorizontalAlignment(SwingConstants.CENTER);
        nicknameField.setBackground(new Color(7, 12, 26));
        nicknameField.setForeground(GOLD);
        nicknameField.setCaretColor(CYAN);
        nicknameField.setSelectionColor(new Color(49, 78, 110));
        nicknameField.setSelectedTextColor(WHITE);
        nicknameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MAGENTA, 2),
            BorderFactory.createEmptyBorder(12, 10, 12, 10)
        ));

        JButton start = makeButton("START");
        JButton back = makeButton("BACK TO MENU");

        start.addActionListener(e -> loginAndStart());
        nicknameField.addActionListener(e -> loginAndStart());

        back.addActionListener(e ->
            cardLayout.show(mainPanel, "HOME")
        );

        c.gridy = 0;
        card.add(decoration, c);

        c.gridy = 1;
        card.add(title, c);

        c.gridy = 2;
        card.add(instruction, c);

        c.gridy = 3;
        card.add(nicknameField, c);

        c.gridy = 4;
        card.add(start, c);

        c.gridy = 5;
        card.add(back, c);

        JLabel note = new JLabel(
            "TIPS: FIND A SHORT PATH",
            SwingConstants.CENTER
        );
        note.setFont(pixelFont(10));
        note.setForeground(GOLD);

        c.gridy = 6;
        card.add(note, c);

        panel.add(card);
        return panel;
    }

    private void loginAndStart() {
        String nickname = nicknameField.getText().trim();

        if (nickname.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Please enter your nickname.",
                "Nickname Required",
                JOptionPane.WARNING_MESSAGE
            );
            nicknameField.requestFocusInWindow();
            return;
        }

        if (nickname.contains(",")
                || nickname.contains("\n")
                || nickname.contains("\r")) {
            JOptionPane.showMessageDialog(
                this,
                "Please use a nickname without commas or line breaks.",
                "Invalid Nickname",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        playerNickname = nickname;
        totalMoves = 0;
        totalSeconds = 0;

        startLevel(0);
        cardLayout.show(mainPanel, "GAME");

        SwingUtilities.invokeLater(
            () -> mazePanel.requestFocusInWindow()
        );
    }

    private JPanel createGamePanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(BG);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel stats = new JPanel(new GridLayout(1, 3, 10, 0));
        stats.setBackground(BG);

        styleStat(levelLabel);
        styleStat(movesLabel);
        styleStat(timeLabel);

        stats.add(levelLabel);
        stats.add(movesLabel);
        stats.add(timeLabel);

        mazePanel = new MazePanel();

        messageLabel.setForeground(CYAN);
        messageLabel.setFont(pixelFont(12));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton restart = makeButton("NEW MAZE");
        JButton home = makeButton("MAIN MENU");

        restart.addActionListener(e -> startLevel(level));

        home.addActionListener(e -> {
            stopTimer();
            cardLayout.show(mainPanel, "HOME");
        });

        JPanel bottom = new JPanel(new BorderLayout(10, 10));
        bottom.setBackground(BG);
        bottom.add(messageLabel, BorderLayout.CENTER);

        JPanel controls = new JPanel(
            new FlowLayout(FlowLayout.CENTER, 10, 0)
        );
        controls.setBackground(BG);
        controls.add(restart);
        controls.add(home);

        bottom.add(controls, BorderLayout.SOUTH);

        panel.add(stats, BorderLayout.NORTH);
        panel.add(mazePanel, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createEndPanel(boolean lost) {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(BG);
        panel.setBorder(BorderFactory.createEmptyBorder(35, 38, 35, 38));

        JPanel frame = new JPanel(new BorderLayout(15, 20));
        frame.setBackground(PANEL);
        frame.setBorder(pixelBorder());

        JLabel banner = new JLabel(
            lost ? "GAME OVER" : "VICTORY!",
            SwingConstants.CENTER
        );
        banner.setFont(pixelFont(13));
        banner.setForeground(GOLD);
        banner.setBorder(BorderFactory.createEmptyBorder(20, 10, 0, 10));

        JLabel title = new JLabel(
            lost ? "TIME'S UP!" : "LEVEL COMPLETE!",
            SwingConstants.CENTER
        );
        title.setFont(pixelFont(lost ? 27 : 25));
        title.setForeground(lost ? RED : CYAN);

        JLabel details = new JLabel("", SwingConstants.CENTER);
        details.setName(lost ? "loseDetails" : "winDetails");
        details.setForeground(WHITE);
        details.setFont(pixelFont(14));
        details.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel heading = new JPanel(new GridLayout(2, 1, 6, 6));
        heading.setBackground(PANEL);
        heading.add(banner);
        heading.add(title);

        JButton next = makeButton(
            lost ? "TRY AGAIN" : "CONTINUE"
        );
        next.setName(lost ? "loseAgain" : "winAgain");

        next.addActionListener(e -> {
            if (lost) {
                startLevel(level);
                cardLayout.show(mainPanel, "GAME");
            } else if (level < TOTAL_LEVELS - 1) {
                startLevel(level + 1);
                cardLayout.show(mainPanel, "GAME");
            } else {
                startGameAgain();
            }
        });

        JButton menu = makeButton("MAIN MENU");
        menu.addActionListener(e -> {
            stopTimer();
            cardLayout.show(mainPanel, "HOME");
        });

        JPanel buttons = new JPanel(
            new FlowLayout(FlowLayout.CENTER, 12, 0)
        );
        buttons.setBackground(PANEL);
        buttons.setBorder(BorderFactory.createEmptyBorder(5, 10, 25, 10));
        buttons.add(next);
        buttons.add(menu);

        frame.add(heading, BorderLayout.NORTH);
        frame.add(details, BorderLayout.CENTER);
        frame.add(buttons, BorderLayout.SOUTH);

        panel.add(frame, BorderLayout.CENTER);

        return panel;
    }

    private void startGameAgain() {
        nicknameField.setText("");
        cardLayout.show(mainPanel, "LOGIN");
        SwingUtilities.invokeLater(
            () -> nicknameField.requestFocusInWindow()
        );
    }

    private void styleStat(JLabel label) {
        label.setOpaque(true);
        label.setBackground(PANEL);
        label.setForeground(CYAN);
        label.setFont(pixelFont(13));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CYAN, 2),
            BorderFactory.createEmptyBorder(10, 5, 10, 5)
        ));
    }

    private JButton makeButton(String text) {
        JButton button = new JButton(text);
        button.setFont(pixelFont(12));
        button.setForeground(CYAN);
        button.setBackground(PANEL_LIGHT);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MAGENTA, 2),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CYAN, 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
            )
        ));
        button.setCursor(
            Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(35, 54, 85));
                button.setForeground(GOLD);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(PANEL_LIGHT);
                button.setForeground(CYAN);
            }
        });

        return button;
    }

    private void startLevel(int newLevel) {
        level = newLevel;
        seconds = 0;
        moves = 0;
        gameRunning = true;
        walking = false;
        animationFrame = 0;
        directionRow = 0;

        messageLabel.setText(
            "Two routes available! Find the green exit."
        );

        mazePanel.loadMaze(generateMaze(level));

        updateStats();
        gameTimer.restart();

        cardLayout.show(mainPanel, "GAME");

        SwingUtilities.invokeLater(
            () -> mazePanel.requestFocusInWindow()
        );
    }

    private String[] generateMaze(int currentLevel) {
        int size = 15 + currentLevel * 2;
        char[][] maze = new char[size][size];

        for (int r = 0; r < size; r++) {
            Arrays.fill(maze[r], '#');
        }

        carveMaze(maze, 1, 1);

        List<Point> possibleShortcuts = new ArrayList<>();

        for (int r = 1; r < size - 1; r++) {
            for (int c = 1; c < size - 1; c++) {
                if (maze[r][c] != '#') {
                    continue;
                }

                boolean horizontal =
                    maze[r][c - 1] == '.'
                    && maze[r][c + 1] == '.';

                boolean vertical =
                    maze[r - 1][c] == '.'
                    && maze[r + 1][c] == '.';

                if (horizontal || vertical) {
                    possibleShortcuts.add(new Point(c, r));
                }
            }
        }

        Collections.shuffle(possibleShortcuts, random);

        int extraOpenings = 3 + currentLevel * 2;
        int opened = 0;

        for (Point point : possibleShortcuts) {
            if (opened >= extraOpenings) {
                break;
            }

            maze[point.y][point.x] = '.';
            opened++;
        }

        int exit = size - 2;

        maze[1][1] = 'S';
        maze[exit][exit] = 'E';

        String[] result = new String[size];

        for (int r = 0; r < size; r++) {
            result[r] = new String(maze[r]);
        }

        return result;
    }

    private void carveMaze(char[][] maze, int row, int col) {
        maze[row][col] = '.';

        int[][] directions = {
            {-2, 0},
            {2, 0},
            {0, -2},
            {0, 2}
        };

        List<int[]> options = new ArrayList<>(
            Arrays.asList(directions)
        );
        Collections.shuffle(options, random);

        for (int[] direction : options) {
            int nextRow = row + direction[0];
            int nextCol = col + direction[1];

            if (nextRow <= 0 || nextRow >= maze.length - 1
                    || nextCol <= 0
                    || nextCol >= maze[0].length - 1) {
                continue;
            }

            if (maze[nextRow][nextCol] == '#') {
                maze[row + direction[0] / 2]
                    [col + direction[1] / 2] = '.';

                carveMaze(maze, nextRow, nextCol);
            }
        }
    }

    private void updateStats() {
        levelLabel.setText(
            "LEVEL: " + (level + 1) + " / " + TOTAL_LEVELS
        );
        movesLabel.setText("MOVES: " + moves);
        timeLabel.setText(
            "TIME: " + seconds + "s / " + TIME_LIMIT + "s"
        );
    }

    private void stopTimer() {
        gameRunning = false;
        walking = false;

        if (gameTimer != null) {
            gameTimer.stop();
        }

        if (mazePanel != null) {
            mazePanel.repaint();
        }
    }

    private void finishGame(boolean won) {
        if (!gameRunning) {
            return;
        }

        stopTimer();

        if (won) {
            saveScore();
        }

        String target = won ? "WIN" : "LOSE";

        JPanel endPanel = (JPanel) mainPanel.getComponent(
            won ? 3 : 4
        );

        JLabel details = findLabel(
            endPanel,
            won ? "winDetails" : "loseDetails"
        );

        JButton next = findButton(
            endPanel,
            won ? "winAgain" : "loseAgain"
        );

        if (won && level == TOTAL_LEVELS - 1) {
            details.setText(
                "<html><div style='text-align:center;'>"
                + "<font color='#FBBF24'>"
                + "CONGRATULATIONS, " + escapeHtml(playerNickname) + "!"
                + "</font><br><br>"
                + "YOU COMPLETED ALL FIVE LEVELS!<br><br>"
                + "TOTAL MOVES: " + totalMoves + "<br>"
                + "TOTAL TIME: " + totalSeconds + " SECONDS<br><br>"
                + "YOUR SCORE HAS BEEN SAVED."
                + "</div></html>"
            );
            next.setText("PLAY AGAIN");

        } else if (won) {
            details.setText(
                "<html><div style='text-align:center;'>"
                + "<font color='#FBBF24'>LEVEL "
                + (level + 1) + " CLEARED!</font><br><br>"
                + "MOVES: " + moves + "<br>"
                + "TIME: " + seconds + " SECONDS<br><br>"
                + "GET READY FOR THE NEXT MAZE!"
                + "</div></html>"
            );
            next.setText("NEXT LEVEL");

        } else {
            details.setText(
                "<html><div style='text-align:center;'>"
                + "<font color='#F87171'>TIME RAN OUT!</font><br><br>"
                + "The clock reached zero.<br><br>"
                + "TRY AGAIN AND FIND THE EXIT FASTER!"
                + "</div></html>"
            );
            next.setText("TRY AGAIN");
        }

        cardLayout.show(mainPanel, target);
    }

    private String escapeHtml(String value) {
        return value.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;");
    }

    
    private void saveScore() {
        String sql = "INSERT INTO leaderboards "
                + "(nickname, level_number, total_moves, total_seconds) "
                + "VALUES (?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE "
                + "level_number = VALUES(level_number), "
                + "total_moves = VALUES(total_moves), "
                + "total_seconds = VALUES(total_seconds)";

        try {
            if (con == null || con.isClosed()) {
                connectDatabase();
            }

            if (con == null || con.isClosed()) {
                throw new Exception("Database connection is unavailable.");
            }

            try (java.sql.PreparedStatement pst = con.prepareStatement(sql)) {

                pst.setString(1, playerNickname);
                pst.setInt(2, level + 1);
                pst.setInt(3, totalMoves);
                pst.setInt(4, totalSeconds);

                pst.executeUpdate();

                System.out.println(
                    "Leaderboard updated for " + playerNickname
                    + " | Level: " + (level + 1)
                );
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Could not save score: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );

            e.printStackTrace();
        }
    }
    
    private JLabel findLabel(Container parent, String name) {
        for (Component component : parent.getComponents()) {
            if (component instanceof JLabel
                    && name.equals(component.getName())) {
                return (JLabel) component;
            }

            if (component instanceof Container) {
                JLabel found = findLabel(
                    (Container) component, name
                );

                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }

    private JButton findButton(Container parent, String name) {
        for (Component component : parent.getComponents()) {
            if (component instanceof JButton
                    && name.equals(component.getName())) {
                return (JButton) component;
            }

            if (component instanceof Container) {
                JButton found = findButton(
                    (Container) component, name
                );

                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }

    private void loadMoveSound() {
        try {
            File soundFile = new File("player_move.wav");

            if (soundFile.exists()) {
                AudioInputStream stream =
                    AudioSystem.getAudioInputStream(soundFile);

                moveSound = AudioSystem.getClip();
                moveSound.open(stream);
                stream.close();
            } else {
                createRetroSound();
            }
        } catch (Exception e) {
            System.out.println(
                "Movement sound unavailable: " + e.getMessage()
            );
        }
    }

    private void createRetroSound() throws Exception {
        float sampleRate = 8000f;
        int durationMs = 110;
        int samples = (int) (sampleRate * durationMs / 1000);
        byte[] audio = new byte[samples];

        for (int i = 0; i < samples; i++) {
            double progress = (double) i / samples;
            double frequency = 520 + 600 * progress;
            double envelope = 1.0 - progress;
            double wave = Math.sin(
                2.0 * Math.PI * frequency * i / sampleRate
            );

            audio[i] = (byte) (wave * envelope * 100);
        }

        AudioFormat format = new AudioFormat(
            sampleRate, 8, 1, true, false
        );

        moveSound = AudioSystem.getClip();
        moveSound.open(format, audio, 0, audio.length);
    }

    private void playMoveSound() {
        if (moveSound == null) {
            return;
        }

        if (moveSound.isRunning()) {
            moveSound.stop();
        }

        moveSound.setFramePosition(0);
        moveSound.start();
    }

    private class MazePanel extends JPanel {

        private char[][] grid;
        private int rows;
        private int cols;

        private int playerRow;
        private int playerCol;
        private int exitRow;
        private int exitCol;

        private final Color wallColor = new Color(51, 65, 85);
        private final Color pathColor = new Color(226, 232, 240);
        private final Color exitColor = new Color(34, 197, 94);

        MazePanel() {
            setBackground(new Color(15, 23, 42));
            setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            setFocusable(true);

            addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    walking = false;
                    animationFrame = 0;
                    repaint();
                }

                @Override
                public void keyPressed(KeyEvent e) {
                    if (!gameRunning) {
                        return;
                    }

                    int dr = 0;
                    int dc = 0;

                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_UP:
                        case KeyEvent.VK_W:
                            dr = -1;
                            directionRow = 3;
                            break;

                        case KeyEvent.VK_DOWN:
                        case KeyEvent.VK_S:
                            dr = 1;
                            directionRow = 0;
                            break;

                        case KeyEvent.VK_LEFT:
                        case KeyEvent.VK_A:
                            dc = -1;
                            directionRow = 1;
                            break;

                        case KeyEvent.VK_RIGHT:
                        case KeyEvent.VK_D:
                            dc = 1;
                            directionRow = 2;
                            break;

                        default:
                            return;
                    }

                    walking = true;
                    movePlayer(dr, dc);
                }
            });
        }

        void loadMaze(String[] source) {
            rows = source.length;
            cols = source[0].length();
            grid = new char[rows][cols];

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    char cell = source[r].charAt(c);
                    grid[r][c] = cell;

                    if (cell == 'S') {
                        playerRow = r;
                        playerCol = c;
                        grid[r][c] = '.';
                    } else if (cell == 'E') {
                        exitRow = r;
                        exitCol = c;
                        grid[r][c] = '.';
                    }
                }
            }

            repaint();
        }

        private void movePlayer(int dr, int dc) {
            int nextRow = playerRow + dr;
            int nextCol = playerCol + dc;

            if (nextRow < 0 || nextRow >= rows
                    || nextCol < 0 || nextCol >= cols) {
                return;
            }

            if (grid[nextRow][nextCol] == '#') {
                messageLabel.setText(
                    "Wall blocked! Find another path."
                );
                return;
            }

            playerRow = nextRow;
            playerCol = nextCol;

            moves++;
            totalMoves++;

            playMoveSound();

            messageLabel.setText(
                "Keep going, " + playerNickname + "!"
            );

            updateStats();
            repaint();

            if (playerRow == exitRow && playerCol == exitCol) {
                finishGame(true);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (grid == null) {
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
            );

            int availableWidth = getWidth() - 24;
            int availableHeight = getHeight() - 24;

            int cellSize = Math.min(
                availableWidth / cols,
                availableHeight / rows
            );

            cellSize = Math.max(10, cellSize);

            int mazeWidth = cols * cellSize;
            int mazeHeight = rows * cellSize;

            int startX = (getWidth() - mazeWidth) / 2;
            int startY = (getHeight() - mazeHeight) / 2;

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    int x = startX + c * cellSize;
                    int y = startY + r * cellSize;

                    g2.setColor(
                        grid[r][c] == '#'
                            ? wallColor
                            : pathColor
                    );

                    g2.fillRect(
                        x, y, cellSize - 1, cellSize - 1
                    );

                    if (r == exitRow && c == exitCol) {
                        g2.setColor(exitColor);
                        g2.fillRect(
                            x + 1, y + 1,
                            cellSize - 3, cellSize - 3
                        );

                        g2.setColor(new Color(20, 83, 45));
                        g2.setFont(new Font(
                            "Arial",
                            Font.BOLD,
                            Math.max(9, cellSize / 2)
                        ));

                        g2.drawString(
                            "E",
                            x + cellSize / 3,
                            y + (2 * cellSize) / 3
                        );
                    }
                }
            }

            int px = startX + playerCol * cellSize;
            int py = startY + playerRow * cellSize;

            int padding = Math.max(1, cellSize / 10);
            int spriteSize = cellSize - padding * 2;

            if (playerSprite != null
                    && playerSprite.getWidth() >= 23
                    && playerSprite.getHeight() >= 23) {

                int sourceX = animationFrame * 23;
                int sourceY = directionRow * 23;

                if (sourceX + 23 <= playerSprite.getWidth()
                        && sourceY + 23 <= playerSprite.getHeight()) {

                    g2.drawImage(
                        playerSprite,
                        px + padding,
                        py + padding,
                        px + padding + spriteSize,
                        py + padding + spriteSize,
                        sourceX,
                        sourceY,
                        sourceX + 23,
                        sourceY + 23,
                        null
                    );
                } else {
                    drawFallbackPlayer(g2, px, py, cellSize);
                }
            } else {
                drawFallbackPlayer(g2, px, py, cellSize);
            }

            g2.dispose();
        }

        private void drawFallbackPlayer(
                Graphics2D g2,
                int px,
                int py,
                int cellSize) {

            int size = Math.max(6, cellSize - 6);
            int inset = (cellSize - size) / 2;

            g2.setColor(new Color(37, 99, 235));
            g2.fillRect(
                px + inset, py + inset, size, size
            );

            g2.setColor(Color.WHITE);
            g2.drawRect(
                px + inset, py + inset, size, size
            );
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MiniMazeChallenge game = new MiniMazeChallenge();
            game.setVisible(true);
        });
    }
}

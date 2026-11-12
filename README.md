# 🎮 Mini Maze Challenge

**A 2D Maze Adventure Game Developed Using Java Swing and MySQL**

## 📌 Project Overview

Mini Maze Challenge is a 2D maze adventure game developed using Java and Java Swing. The game challenges players to navigate through randomly generated mazes, find the exit, and complete five progressively challenging levels within a 120-second time limit per level.

Players can explore two possible routes in each maze: a short route and a long route. The game encourages logical thinking, problem-solving, decision-making, and strategic navigation.

It also integrates MySQL through JDBC to store player leaderboard records, including nicknames, completed levels, total moves, and total playing time.

## 🎯 Project Objectives

- Develop an interactive 2D maze game using Java Swing.
- Improve players' problem-solving and logical thinking skills.
- Implement randomly generated mazes with increasing difficulty.
- Apply Object-Oriented Programming (OOP) principles.
- Utilize data structures and algorithms in maze generation.
- Integrate MySQL for storing and managing player scores.

## ✨ Game Features

- **Random Maze Generation** – Creates different maze layouts using a randomized algorithm.
- **Five Game Levels** – Progressively increases maze size and difficulty.
- **Two Possible Routes** – Provides short and long paths for players to explore.
- **Animated Pixel Character** – Includes directional sprite animations.
- **Keyboard Controls** – Supports Arrow Keys and WASD.
- **Countdown Timer** – Gives players 120 seconds to complete each level.
- **Movement Sound Effects** – Provides retro-style movement audio.
- **Player Login** – Allows players to enter their nicknames before starting.
- **MySQL Leaderboard** – Stores player records using JDBC.
- **Retro Pixel Interface** – Features a pixel-inspired design with cyan, magenta, and dark-themed colors.

## 🕹️ Game Controls

| Key | Action |
|---|---|
| ↑ / W | Move Up |
| ↓ / S | Move Down |
| ← / A | Move Left |
| → / D | Move Right |

**Objective:** Find the green exit tile (`E`) before the 120-second timer runs out.

## 🛠️ Technologies Used

| Technology | Purpose |
|---|---|
| Java | Main programming language |
| Java Swing | Graphical User Interface |
| Java AWT | Graphics, layouts, colors, and events |
| MySQL | Database management |
| JDBC | Java-MySQL connectivity |
| XAMPP | Local database server |
| phpMyAdmin | Database administration |
| VS Code / JDK | Development environment |

## 🧠 OOP and DSA Concepts

### Object-Oriented Programming (OOP)

- **Encapsulation** – Organizes game data and methods within the `MiniMazeChallenge` class and its inner `MazePanel` class.
- **Abstraction** – Uses methods to hide implementation details, such as `generateMaze()`, `movePlayer()`, and `saveScore()`.
- **Inheritance** – The main game class extends `JFrame`, while the maze panel extends `JPanel`.
- **Polymorphism** – Uses method overriding, such as `paintComponent()`, `keyPressed()`, and mouse event methods.

### Data Structures and Algorithms (DSA)

- **2D Arrays** – Represent the maze using rows and columns.
- **ArrayList** – Stores possible shortcut positions and movement directions.
- **Stack-Based Recursion** – Uses recursive Depth-First Search (DFS) to generate maze pathways.
- **Randomization** – Shuffles movement directions and shortcut positions to create different maze layouts.
- **Searching and Traversal** – Explores available maze cells while generating pathways.

## 🗄️ Database Information

**Database Name:** `maze_db`

**Table Name:** `leaderboards`

| Column | Data Type | Description |
|---|---|---|
| leaderboard_id | INT | Primary key with auto-increment |
| nickname | VARCHAR(50) | Player's nickname |
| level_number | INT | Latest completed level |
| total_moves | INT | Total player movements |
| total_seconds | INT | Total playing time |
| date_created | TIMESTAMP / DATETIME | Record creation date |

**Database Connection:**

The project uses JDBC to connect Java to MySQL.

```java
String url = "jdbc:mysql://localhost:3306/maze_db";
String username = "root";
String password = "";

Connection con = DriverManager.getConnection(
    url, username, password
);
```

## 🚀 How to Run the Project

1. Install the Java Development Kit (JDK).
2. Install XAMPP and start Apache (optional for phpMyAdmin) and MySQL.
3. Open phpMyAdmin.
4. Create a database named `maze_db`.
5. Create the required `leaderboards` table.
6. Add the MySQL Connector/J library to your Java project.
7. Place the required game assets in the correct project directory:
   - `player3s.png`
   - `player_move.wav` (optional; the game generates a retro movement sound if unavailable)
8. Open the project in VS Code or another Java IDE.
9. Run `MiniMazeChallenge.java`.

## 📂 Project Structure

```text
MiniMazeChallenge/
│
├── MiniMazeChallenge.java
├── player3s.png
├── player_move.wav
└── README.md
```

## 🎓 Project Information

**Project Name:** Mini Maze Challenge  
**Project Type:** 2D Desktop Game  
**Programming Language:** Java  
**Framework:** Java Swing  
**Database:** MySQL  
**Academic Subjects:** Object-Oriented Programming (OOP) and Data Structures and Algorithms (DSA).

## 👨‍💻 Developer

Developed as an academic project to demonstrate the application of Java programming, Object-Oriented Programming, Data Structures and Algorithms, GUI development, and database connectivity.

---

⭐ **Thank you for checking out Mini Maze Challenge!**

*Find the exit. Beat the clock. Challenge your mind.*

### 2D Mini Maze Challenge presented by: Revster B. Regacho

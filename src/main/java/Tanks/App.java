package Tanks;

import org.checkerframework.checker.units.qual.A;
import processing.core.PApplet;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import java.io.*;
import java.util.*;
import java.util.List;

public class App extends PApplet {

    public static final int CELLSIZE = 32; //8;
    public static final int CELLHEIGHT = 32;

    public static final int CELLAVG = 32;
    public static final int TOPBAR = 0;
    public static int WIDTH = 864; //CELLSIZE*BOARD_WIDTH;
    public static int HEIGHT = 640; //BOARD_HEIGHT*CELLSIZE+TOPBAR;
    public static final int BOARD_WIDTH = WIDTH/CELLSIZE;
    public static final int BOARD_HEIGHT = 20;

    public static final int INITIAL_PARACHUTES = 1;

    public static final int FPS = 30;
    public boolean movingLeft = false;
    public boolean movingRight = false;
    public boolean rotatingLeft = false;
    public boolean rotatingRight = false;
    public boolean fireProjectile = false;
    public boolean increasePower = false;
    public boolean decreasePower = false;
    public String configPath;


    public static Random random = new Random();
	
	// Feel free to add any additional methods or attributes you want. Please put classes in different files.
    private Level level;
    private ArrayList<JSONObject> levels;
    private HashMap<String, int[]> playerColours;
    private ArrayList<Tank> tanks = new ArrayList<>();
    private ArrayList<Player> players = new ArrayList<>();
    private int currentPlayerIndex = 0;  // 追踪当前轮到的坦克索引
    private Wind wind;
    private static ArrayList<Tank> allTanks = new ArrayList<>();
    private HashMap<String, Integer> playerScores = new HashMap<>();
    private int currentLevelIndex = 0;
    private long loadingFrame = 0;
    private boolean isNextLevel = false;
    private boolean isOver = false;
    

    Tank tank;

    public App() {
        this.configPath = "config.json";
    }


    /**
     * Initialise the setting of the window size.
     */
	@Override
    public void settings() {
        size(WIDTH, HEIGHT);
    }

    /**
     * Load all resources such as images. Initialise the elements such as the player and map elements.
     */
	@Override
    public void setup() {
        frameRate(FPS);
        
        wind = new Wind();
        manageConfig();
        if (!levels.isEmpty()) {
            loadLevel(0);  // Load the first level
        } else {
            println("No levels found in the configuration file.");
        }
        initialisePlayers(); 

		//See PApplet javadoc:
		//loadJSONObject(configPath)
    }

    /*
     * Write a method to store the config file
     */

    public void manageConfig(){
        JSONObject config = loadJSONObject(configPath);
        JSONArray levelsArray = config.getJSONArray("levels");
        JSONObject colours = config.getJSONObject("player_colours");

        levels = new ArrayList<>();
        if (levelsArray != null) {
            for (int i = 0; i < levelsArray.size(); i++) {  
                JSONObject levelConfig = levelsArray.getJSONObject(i);
                levels.add(levelConfig);
            }
            playerColours = new HashMap<>();
            if (colours != null) {
                    for (Object objKey : colours.keys()) {  
                    String key = (String) objKey;  
                    String colourStr = colours.getString(key);  
                    int[] colour = parseRGB(colourStr);
                    playerColours.put(key, colour);
                }
            }
        }
    }

    /*
     * Loadlevl
     */
    private void loadLevel(int levelIndex) {
        println("Loading level: " + (levelIndex + 1));
        if (levelIndex < levels.size()) {
            JSONObject levelConfig = levels.get(levelIndex);
            level = new Level(levelConfig, this);
            currentLevelIndex = levelIndex; 
            // Backup existing scores
            HashMap<String, Integer>backupScores = new HashMap<>(playerScores);

            // Re-initialize players and tanks
            initialisePlayers();

            // Restore scores
            restoreScores(backupScores);

            println("Level " + (levelIndex + 1) + " loaded.");
        } else {
            println("No more levels to load.");
            endGame();
        }
    }

    private int[] parseRGB(String rgbString) {
        if (rgbString.equals("random")) {
            return new int[] {
                (int) (Math.random() * 256),
                (int) (Math.random() * 256),
                (int) (Math.random() * 256)
            };
        } else {
            String[] rgbParts = rgbString.split(",");
            return new int[] {
                Integer.parseInt(rgbParts[0].trim()),
                Integer.parseInt(rgbParts[1].trim()),
                Integer.parseInt(rgbParts[2].trim())
            };
        }
    }
    

    private void initialisePlayers(){
        tanks.clear();
        players.clear();
        allTanks.clear();
        playerScores.clear(); 

        HashMap<String, float[]> tankPositions = level.getTankPositions();
        float[] terrainHeights = level.getSmoothedHeights();
        //System.out.println(Arrays.toString(terrainHeights));

        

        for (String playerId : tankPositions.keySet()) {
            float[] position = tankPositions.get(playerId);
            int[] color = playerColours.get(playerId); 
        
            if (position != null && color != null) {
                Tank newTank = new Tank(this, playerId,position[0], position[1], color, terrainHeights); // Create a new tank with position and color
                Player newPlayer = new Player(playerId, newTank, color); // Create a new player with the tank
                players.add(newPlayer); // Add the player to the list
                tanks.add(newTank);
                allTanks.add(newTank);
                playerScores.put(playerId, 0);
                //System.out.println("Stored player: " + playerId + " with tank at position: " + Arrays.toString(position));
                //System.out.println("Color: " + Arrays.toString(color));
            }
        }
    }

    public void updateScore(String playerId, int score) {
        if (playerScores.containsKey(playerId)) {
            playerScores.put(playerId, playerScores.getOrDefault(playerId, 0) + score);
        }
    }
     
    private void restoreScores(HashMap<String,Integer>backupScores) {
        for (Player player : players) {
            String playerId = player.getName();
            if (backupScores.containsKey(playerId)) {
                playerScores.put(playerId, backupScores.get(playerId));
            } else {
                playerScores.put(playerId, 0);  // Set default score if new player
            }
        }
    }
    /**
     * Receive key pressed signal from the keyboard.
     */
	@Override
    public void keyPressed(KeyEvent event){
        //System.out.println("Key pressed: " + event.getKeyCode());
        int keyCode = event.getKeyCode();

            switch (keyCode) {
                case LEFT:
                    movingLeft = true;
                    break;
                case RIGHT:
                    movingRight = true;
                    break;
                case DOWN:
                    rotatingLeft = true;
                    break;
                case UP:
                    rotatingRight = true;
                    break;
                case ' ':
                    fireProjectile = true;
                    break;
                case 'W':
                    increasePower = true; 
                    break;
                case 'S':
                    decreasePower = true;
                    break;
                case 'R':
                    reset();
                    break;

            }
        //}
    }

    /**
     * Receive key released signal from the keyboard.
     */
	@Override
    public void keyReleased(KeyEvent event){
        //System.out.println("Key released: " + event.getKeyCode()); 
        int keyCode = event.getKeyCode();
            switch (keyCode) {
                case LEFT:
                    movingLeft = false;
                    break;
                case RIGHT:
                    movingRight = false;
                    break;
                case DOWN:
                    rotatingLeft = false;
                    break;
                case UP:
                    rotatingRight = false;
                    break;
                case ' ':
                    switchTurn(); 
                    fireProjectile = false;
                    break;
                case 'W':
                    increasePower = false; 
                    break;
                case 'S':
                    decreasePower = false; 
                    break;
                
            }
        //}
    }

    @Override
    public void mousePressed(MouseEvent e) {
        //TODO - powerups, like repair and extra fuel and teleport


    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    private void switchTurn() {
        movingLeft = false;
        movingRight = false;
        wind.updateWind();
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        //System.out.println("The turn of the" + currentPlayerIndex);
        tanks.get(currentPlayerIndex).clearProjectiles(); 
    }


    public void displayPlayerInfo(){
        Player currentPlayer = players.get(currentPlayerIndex);
        Tank currentTank = currentPlayer.getTank();
        int[] colour = currentPlayer.getColour();
        

        textSize(16);
        fill(0); // Black color for text
        textAlign(LEFT, TOP);

        String turnText = "Player "+currentPlayer.getName() + "'s turn";
        text(turnText, 20, 10); // Position at 10, 10
        
        PImage fuelLogo = loadImage("src/main/resources/Tanks/fuel.png");
        image(fuelLogo, 155,8,25,25);

        String fuelText = Integer.toString(currentTank.getFuel());
        text(fuelText, 182,10 );

        PImage parachuteLogo = loadImage("src/main/resources/Tanks/parachute.png");
        image(parachuteLogo,155,40,25,25);
        String parachuteText = Integer.toString(currentTank.getNumberofParachutes());
        text(parachuteText,182,42);
        
        String healthText = "Health: " ;
        String healthNum = Integer.toString(currentTank.getHealth());
        fill(0);
        text(healthText, 380, 10);
        text(healthNum,570,10);
        //Draw a health bar, the colour is the same as the player
        healthBar(currentTank.getHealth(), currentTank.getPower(), colour);

        String powerText = "Power: "+ Integer.toString(currentTank.getPower());
        fill(0);
        text(powerText, 380,42);

        PImage toRightWind = loadImage("src/main/resources/Tanks/wind.png"); //>0
        PImage toLeftWind = loadImage("src/main/resources/Tanks/wind-1.png");//<0
        if(wind.isWindToTheRight()){
            image(toRightWind,760,4,52,52);
        } else if (wind.isWindToTheLeft()){
            image(toLeftWind,760,4,52,52);
        }
        String windText = Integer.toString(wind.getWindSpeed());
        text(windText,820,20);
    }


    public void displayScores() {
        textSize(16);
        noFill(); 
        stroke(0); 
        strokeWeight(3); 
    
        int scoreWidth = 140; 
        int scoreHeight = 100; 
    
        rectMode(CENTER);
        rect(780, 110, scoreWidth, scoreHeight);
    
        fill(0); 
        String title = "Scores";
        text(title,715,60);

        strokeWeight(3);
        line(710, 80, 850,80);

        int yP = 80; 
    
        ArrayList<String> sortedScore = new ArrayList<>(playerScores.keySet());
        Collections.sort(sortedScore);

        for (String playerId : sortedScore) {
            int score = playerScores.get(playerId);
            String playerName = "Player " + playerId;
            int[] color = playerColours.get(playerId);

            fill(color[0], color[1], color[2]);
            text(playerName, 715, yP);
            fill(0);
            text(String.valueOf(score), 820, yP);
            yP += 20;
        }
    
        noStroke(); 
    }


    

    public void healthBar(int health, int power, int[] colour){
        
        fill(255);
        stroke(0);
        strokeWeight(3);
        rect(500,20,120,20);

        float HP =(float) ((health/100.0) *120);

        rectMode(CORNER);
        
        noStroke();
        fill(colour[0],colour[1],colour[2]);
        rect(440,10,HP,20);

        float p = (float)(power/100.0)*120;
        stroke(100);
        strokeWeight(3);
        rect(440,10,p,20);

        float P = (float) (500 +(power/100.0)*120 - 60);
        
        noStroke();
        fill(255,0,0);
        rect(P,7,1,30);

    }

    public static ArrayList<Tank> getAllTanks() {
        return allTanks;
    }

    public void removeDeadTanks() {
        for (int i = tanks.size() - 1; i >= 0; i--) {
            if (!tanks.get(i).alive()) {
                tanks.remove(i);
                players.remove(i);
            }
        }
        if (currentPlayerIndex >= players.size()) {
            currentPlayerIndex = 0;
        }
    }

    /**
     * Check the level
     */
    
    public void checkLevelEnd() {
        if (tanks.size() == 1) {
            levelEnd(); 
        }
    }

    public void levelEnd() {
        if (!isNextLevel) { 
            isNextLevel = true;
            loadingFrame = 0; 
        }
    }

    public void loadNextLevel() {
        currentLevelIndex++; 
        if (currentLevelIndex <levels.size()) {
            JSONObject nextLevelConfig = levels.get(currentLevelIndex);
            level = new Level(nextLevelConfig, this);
            initialisePlayers(); 
        } else{
            endGame();
        }
    }
    
    public void endGame() {
        isOver = true;
        System.out.println("Game over logic executed.");
    }

    
    

    public String getWinner() {
        return playerScores.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("No Winner");
    }

    public void reset(){
        currentPlayerIndex = 0;
        currentLevelIndex = 0;
        isNextLevel = false;
        isOver = false;
        playerScores.clear();
        tanks.clear();
        players.clear();

        loadLevel(0); 
        initialisePlayers();
    }    


    /**
     * Draw all elements in the game by current frame.
     */
	@Override
    public void draw() {
        float frameTime = 1.0f / FPS; // public static final int FPS = 30;

        if (!isNextLevel) {
            checkLevelEnd();
        }
        if (isNextLevel && loadingFrame > 30) { 
            if (currentLevelIndex + 1 < levels.size()) {
                loadLevel(currentLevelIndex + 1);  
                isNextLevel = false; 
            } else {
                endGame();
                isNextLevel = false;  
            }
        }

        if (isNextLevel) {
            loadingFrame++;
        }
        
        if (isOver) {
            // Only draw the game over screen

            background(255, 182, 193);
            textSize(32);
            fill(255);
            textAlign(CENTER, CENTER);
            text("Game Over!", width / 2, height / 2 - 50);
            String winner = getWinner();
            text(winner + " wins!", width / 2, height / 2);

            int y = height / 2 + 50;
            textSize(24);
            String[] players = playerScores.keySet().toArray(new String[0]);
            Integer[] scores = playerScores.values().toArray(new Integer[0]);


            for (int i = 0; i < scores.length; i++) {
                for (int j = 1; j < scores.length - i; j++) {
                    if (scores[j - 1] < scores[j]) {
    
                        int tempScore = scores[j - 1];
                        scores[j - 1] = scores[j];
                        scores[j] = tempScore;

                        String tempPlayer = players[j - 1];
                        players[j - 1] = players[j];
                        players[j] = tempPlayer;
                    }
                }
            }

    
            playerScores.clear();
            int[] color = new int[3];  
        for (int i = 0; i < players.length; i++) {
        if (playerColours.containsKey(players[i])) {
            color = playerColours.get(players[i]);  
        }
        playerScores.put(players[i], scores[i]);  

        
        fill(color[0], color[1], color[2], 150);
        text("Player " + players[i] + " got " + scores[i], width / 2, y);
        y += 40;  // Increase y for the next score
        //System.out.println("Final score - Player " + players[i] + ": " + scores[i]);
    }
            return;
        }
    
        //----------------------------------
        //display HUD:
        //----------------------------------
        //TODO
        //System.out.println("Drawing frame");
        //----------------------------------
        //display scoreboard:
        //----------------------------------
        //TODO
        
		//----------------------------------
        //----------------------------------

        //TODO: Check user action
        level.drawAll();
    
        displayPlayerInfo();
        displayScores();
        for (Tank tank : tanks) {
            if (tanks.indexOf(tank) == currentPlayerIndex) {
                if (movingLeft) tank.moveLeft(frameTime);
                if (movingRight) tank.moveRight(frameTime);
                if (rotatingLeft) tank.rotateTurretLeft(frameTime);
                if (rotatingRight) tank.rotateTurretRight(frameTime);
                if (increasePower) tank.increasePower();
                if (decreasePower) tank.decreasePower();
                if (fireProjectile) tank.fire((float)wind.getWindForce());
            }
            tank.drawProjectile();
            if(tank.isAlive)tank.drawTank();
        }
        removeDeadTanks();
        
    }


    public static void main(String[] args) {
        PApplet.main("Tanks.App");
    }

}

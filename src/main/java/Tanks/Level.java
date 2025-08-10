package Tanks;

import processing.core.PImage;
import processing.core.PApplet;
import processing.data.JSONObject;
import java.util.*;

public class Level {
    private PApplet app; 
    private char[][] terrainLayout;
    private int[] heights;
    private float[] initialHeights_total_pixel;
    private float[] smoothedHeights;
    private PImage backgroundImage;
    private int foregroundColour;
    private PImage treeImage;
    private int[][] treePositions; 
    private HashMap<Character, int[]> tankPositions;
    private HashMap<String, float[]> tankPixelPositions;


    //Constructor
    public Level(JSONObject levelConfig, PApplet app){
        this.app = app;

        // Import Background Image
        String backgroundPath = levelConfig.getString("background");
        backgroundImage = app.loadImage(backgroundPath);
        

        //Import Foreground Colour
        String[] rgb = levelConfig.getString("foreground-colour").split(",");
        foregroundColour = app.color(Integer.parseInt(rgb[0]),Integer.parseInt(rgb[1]),Integer.parseInt(rgb[2]));

        //Import Tree Image
        if(levelConfig.hasKey("trees")){
            String treePath = levelConfig.getString("trees");
            treeImage = app.loadImage(treePath);
        }

        //Import Layout
        String layoutPath = levelConfig.getString("layout");
        String[] lines = app.loadStrings(layoutPath);

        //Create the correct array with correct capacity
        terrainLayout = new char[20][28];
        //Create to check the tree position
        treePositions = new int[20][28];

        //Fill in the terrain layout

        for (int i = 0; i < lines.length && i < 20; i++) {
            for (int j = 0; j < lines[i].length() && j < 28; j++) {
                terrainLayout[i][j] = lines[i].charAt(j);
                if (lines[i].charAt(j) == 'T') {
                    treePositions[i][j] = 1;  // Use 1 to represent the tree position
                }
            }
        }
        //System.out.println(Arrays.deepToString(terrainLayout));

        //Store the position of the tank
        tankPositions = new HashMap<>();
        tankPixelPositions = new HashMap<>();
        loadTankPositions();
        getTankPositions();
        
    }

    public PImage getBackgroundImage() {
        return backgroundImage;
    }

    public PImage getTreeImage() {
        return treeImage;
    }

    public int getForegroundColour() {
        return foregroundColour;
    }

    public char[][] getTerrainLayout() {
        return terrainLayout;
    }

    /*
     * This method is used to count how many cells of the terrrain's height
     */

    public int[] heightsOfTerrain(){
        heights = new int[28]; 
        Arrays.fill(heights, 20); 
        for (int j = 0; j < 28; j++) { // Columns: 28
            for (int i = 19; i >= 0; i--) { // Lines:20
                if (terrainLayout[i][j] == 'X') {
                    heights[j] = 20 - i; 
                    break;
                }
            }
        }
        return heights;
    }

    /*8
     * This method is used to store the height of the terrain (in pixel)
    */
    public float[] createInitialHeights() {
        // To store the original height values for each column (in pixels).
        int[] initialHeights = new int[28];
        int[] rawheight = new int[28 * App.CELLSIZE];
        initialHeights_total_pixel = floatHeights(rawheight);
    
        for (int i = 0; i < 28; i++) {
            initialHeights[i] = heights[i] * App.CELLHEIGHT;
        }

        for (int i = 0; i < initialHeights.length; i++) {
            for (int j = 0; j < 32; j++) {
                initialHeights_total_pixel[i * 32 + j] = initialHeights[i];
            }
        }

        return initialHeights_total_pixel;
    }

    /**
     * Write a method to calculate the moving average
     * Need moving average calculation to smooth the curve of terrain
     */

    public float[] movingAverage(float[] heights) {
        int n = 32;
        float[] movingAverageHeights = new float[heights.length];
    
        // Calculation
        for (int i = 0; i < heights.length; i++) {
            int sum = 0;
            int count = 0;

            for (int j = i; j < i + n && j < heights.length; j++) {
                sum += heights[j];
                count++;
            }
    
            movingAverageHeights[i] = 0; 
            if (count > 0) {
                double average = (double) sum / count;
                movingAverageHeights[i] = (float) Math.round(average);
            }
        }
        
        return movingAverageHeights;
    }
    
    public float[] getSmoothedHeights(){

        smoothedHeights = movingAverage(movingAverage(initialHeights_total_pixel));
        //System.out.println("The first one is "+a);
        //System.out.println(Arrays.toString(smoothedHeights));
        return smoothedHeights;
    }

    public float[] floatHeights(int[] heights){
        float[] floatHeights = new float[heights.length];
        for(int i = 0; i < heights.length; i ++){
            floatHeights[i] = heights[i];
        }
        return floatHeights;
    }
    
    /**
     * 
     * @param terrainHeights
     * try to use method to draw the correct terrain layout
     */
    public void drawTerrain(float[] terrainHeights) {
        //getSmoothedHeights();
        app.fill(foregroundColour); 
        app.noStroke(); 
    
        app.beginShape();
        for (int i = 0; i < terrainHeights.length; i++) {
            app.vertex(i, App.HEIGHT-terrainHeights[i]);
        }

        app.vertex(terrainHeights.length - 1, App.HEIGHT); // End at the bottom-right corner
        app.vertex(0, App.HEIGHT);
        app.endShape(PApplet.CLOSE); 
    
    }

    public void drawBackground(){
        app.image(backgroundImage, 0, 0, App.WIDTH, App.HEIGHT); // Set the background image
    }

    public void drawTrees(){
        
        for (int i = 0; i < treePositions.length; i++) {
            //app.stroke(255, 0, 0);  // Set the point color to red for visibility
            //app.strokeWeight(2); 
            for (int j = 0; j < treePositions[i].length; j++) {
                if (treePositions[i][j] == 1) {
                    int x = j * App.CELLSIZE;
                    int x2 = x-16;
                    float y = App.HEIGHT - smoothedHeights[x] - App.CELLSIZE;
                    //System.out.println("Drawing tree at: (" + x+ ", " + y + ")"）
                    app.image(treeImage, x2, y, App.CELLSIZE, App.CELLHEIGHT); 
                    //app.point(x,y);
                }
            }
        }
        
    }

    /**
     * 
     * This method is used to transfer the info of the tank
     */
    public void loadTankPositions(){
        heightsOfTerrain();
        createInitialHeights();
        getSmoothedHeights();
        if (smoothedHeights == null) {
            System.out.println("smoothedHeights array is null.");
            return; 
        }
        for (int i = 0; i < terrainLayout.length; i++) {
            for (int j = 0; j < terrainLayout[i].length; j++) {
                char currentChar = terrainLayout[i][j];
                if (currentChar == 'A' || currentChar == 'B' || currentChar == 'C' || currentChar == 'D') {
                    tankPositions.put(currentChar, new int[]{j,i});
                    
                    int pixelIndex = j * App.CELLSIZE;
                    if (pixelIndex > 0 && pixelIndex < smoothedHeights.length) {
                        float pixelHeight = smoothedHeights[pixelIndex - 1];
                        tankPixelPositions.put(String.valueOf(currentChar), new float[]{pixelIndex, pixelHeight});
                    } else {
                        System.out.println("Index out of bounds for smoothedHeights: " + pixelIndex);
                    }
                }
            }
        }
        for (Character tankId : tankPositions.keySet()) {
            int[] position = tankPositions.get(tankId);
            //System.out.println("Tank " + tankId + " is at position: [" + position[0] + ", " + position[1] + "]");
        
        }
        

    }

    
    public HashMap<String, float[]> getTankPositions(){
        loadTankPositions();
        
        //for (String tankId : tankPixelPositions.keySet()) {
            //float[] position = tankPixelPositions.get(tankId);
            //System.out.println("Tank " + tankId + " is at position: [" + position[0] + ", " + position[1] + "]");
        //}
        
        return tankPixelPositions;
    }


    public void drawAll(){
        drawBackground();
        drawTerrain(smoothedHeights);
        drawTrees();
    }
    
    
}

package Tanks;

import processing.core.PImage;
import processing.core.PApplet;
import processing.data.JSONObject;
import java.util.*;

public class Projectile{
    private App app;
    private float x,y,vx,vy;
    private final float gravity = -0.2f;
    private float windForce;
    private int[] colour;
    private boolean isProjectileDisplayed = true;
    private boolean isCollided = false;
    private String tankId;

    public Projectile(App app, float x, float y, float angle, int power, float windForce, int[] colour,String tankId){
        this.app = app;
        this.x = x;
        this.y = y;
        float initialVelocity = calculateInitialVelocity(power);
        this.vx = initialVelocity * (float)Math.sin(angle);
        this.vy = initialVelocity * (float)Math.cos(angle);
        this.windForce = windForce;
        this.colour = colour;
        this.tankId = tankId;
        //System.out.println("Wind" + windForce);
        //System.out.println("The power is" + power);
        //System.out.println("The InitialVelocity is" + initialVelocity);
        //System.out.println("Intial vx: " +vx);
        //System.out.println("Inital vy: " + vy);
    }

    private float calculateInitialVelocity(int power) {
        //System.out.println("Received power: " + power); 
        float v = 2 + (power/100.0f) * 16;
        return v; 
    }
    public void update() {
        vx += windForce; 
        vy += gravity; 
        x += vx;
        y -= vy;
    }
        /**
         * Wriite a mwthod to check the collision of projectile and terrain
        */
    public void collision(float[] terrain){
            //Check if the projectile is disaplayed
        if(isProjectileDisplayed == false){
            return;
        }
        //boolean damageApplied = false;
            //Check the collision
        int index = Math.round(x);
        int indexF = Math.round(Math.abs(vx)); 

        ArrayList<Integer> indexList = new ArrayList<>();
        for (int i = index - indexF; i <= index + indexF; i++) {
            indexList.add(i);
        }

        int[] indicesToCheck = indexList.stream().mapToInt(i -> i).toArray();

        
        for (int i : indicesToCheck) {
        if (i >= 0 && i < terrain.length) {
            float terrainHeight = App.HEIGHT - terrain[i];
            if (Math.abs(terrainHeight - y) < 5) {  
                isProjectileDisplayed = false;
                isCollided = true;
                //System.out.println("Collision is at x: " + x + ", index: " + i + ", terrain height: " + terrain[i]);

                //Terrain destroyed
                for(int j = 0; j < terrain.length; j ++){
                    //The distance of the terrain's point ad exlpored point
                    //System.out.println("Original terrain Height is" + terrain[j]);
                    double d = Math.sqrt((j-x)*(j-x) + (App.HEIGHT - terrain[j]-y)*(App.HEIGHT - terrain[j]-y));
                    if (d <= 30){
                        //System.out.println("Original terrain Height is" + terrain[j]);
                        //double radian = Math.abs(x-j) / 30 * Math.PI / 2;
                        double radian = Math.toRadians((Math.abs(x-j)/30)*90);
                        float newChange = (float) (30 * Math.cos(radian));
                        terrain[j] -= newChange;
                        /**
                         * 
                         * double Px = x;
                         * double Py = y;
                         * double x0 = j;
                         * double y0 = App.HEIGHT-terrain[j];
                         * double y1 = Py - Math.sqrt(Math.pow(30, 2) - Math.pow(x0 - Px, 2)); 
                         * double newChange = (float)Math.abs(y1-y0);
                         * System.out.println("Original:" + y0);
                         * terrain[j] = terrain[j]-newChange;
                         * terrain[j] = (float)(App.HEIGHT-(y0+newChange));
                         * System.out.println("recent: "+terrain[j]+" changed: "+ newChange);
                         * terrain[j] = (float)(App.HEIGHT-(y0+newChange));
                         * I used this method to calculate the terrain before, however, the display actually looks bad than the current 
                         * one.I use the sin and cos to represent the fistance between explosion point and the terrain point which is on
                         * the circlr
                         */

                    }
                    getSmoothedHeights(terrain);
                }
                //Check if there any tank nearby
                ArrayList<Tank> tanks = App.getAllTanks();
                for (Tank t : tanks) {
                    float Tx = t.getPosition()[0];
                    float Ty = t.getPosition()[1];
                    double d2 = Math.sqrt((Tx - x)*(Tx - x)+(Ty - y)*(Ty - y));
                    if(t.alive() && d2<=30){
                        //System.out.println("Damage is " + d2);
                        double damage = 60 - 2*d2 ;// 60 - d/30*60;
                        t.decreaseHealth(damage);
                        if (!t.getTankId().equals(this.tankId)) { // Avoid count the score which hurt itself
                            app.updateScore(this.tankId, (int) damage);
                        }
                        break; 
                    }
                }
                //Check the tank if have parachute
                for (Tank t :tanks){
                    if(t.isFlying(terrain)){
                        if(t.parachutes > 0){
                            t.parachutes -= 1;
                            t.paraFlying = true;
                        } else {
                            t.noParaFlying = true;
                        }
                    }
                }
                break;
            }
                //break;
            }
        }
    }

        

    public void draw(){
            if (isCollided){
                app.noStroke();
                //Red
                app.fill(255,0,0); 
                app.ellipse(x,y,60,60);
                //Orange
                app.fill(255,97,0);
                app.ellipse(x,y,30,30);
                //Yellow
                app.fill(255,255,0);
                app.ellipse(x,y,12,12);
                isCollided = false;
                //System.out.println("Projectile at x: " + x + " y: " + y + " is not displayed due to collision.");
            }
            if (isProjectileDisplayed){
                app.fill(app.color(colour[0], colour[1], colour[2]));
                app.ellipse(x, y, 10, 10); 
                //System.out.println("Drawing projectile at x: " + x + " y: " + y);
            }

    }
        

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

    public float[] getSmoothedHeights(float[] heights){
        heights = movingAverage(movingAverage(heights));
        return heights;
    }




}

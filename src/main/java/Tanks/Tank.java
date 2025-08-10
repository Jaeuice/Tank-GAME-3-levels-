package Tanks;

import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.radians;

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


public class Tank {
    private App app; 
    private String tankId;
    private float turretAngle;    // Angle of the turret
    private int health;           // Health of the tank
    private int fuel;             // Fuel level for movement
    private int power;            // Power of the projectile
    private float x;                // The Xposition of the tank
    private float y;                // The Yposition of the tank
    private int[] colour;          // The colour of the tank
    private float[] path;
    private int turretColour;
    private ArrayList<Projectile> projectiles; 
    public int parachutes;
    public boolean noParaFlying = false;
    public boolean paraFlying = false;
    public boolean isAlive = true;
    
    


    private static final int TURRET_LENGTH = 15;
    private static final float MOVE_SPEED = 60; // Pixels per second for tank movement
    private static final float ROTATE_SPEED = 3; // Radians per second for turret rotation

    public Tank(App app, String tankId, float position, float position2, int[] colour, float[] terrainHeights){
        this.tankId = tankId;
        this.app = app;
        this.turretAngle = 0; // default turret angle facing upwards
        this.health = 100;    // default health
        this.fuel = 250;      // default fuel
        this.power = 50;      // default power
        this.x = position;
        this.y = App.HEIGHT - position2;
        this.colour = colour;
        this.path = terrainHeights;
        this.turretColour = app.color(0,0,0);
        this.projectiles = new ArrayList<>();
        this.parachutes =3;

    }

    
    public int getHealth(){
        return health;
    }

    public int setHealth(int m){
        return m;
    }

    public int getFuel(){
        return fuel;
    }

    public int getPower(){
        return power;
    }

    public String getTankId(){
        return tankId;
    }

    public float getTurretAngle(){
        return turretAngle;
    }

    public float[] getPosition(){
        float[] position = new float[2];
        position[0] = x;  
        position[1] = y; 
        return position;
    }

    public float getX() {
        return x;
    }

    public float setY(float m){
        return m;
    }
    public float getY() {
        return y;
    }

    public int[] getColour() {
        return colour;
    }

    public ArrayList<Projectile>getProjectiles() {
        return projectiles;
    }

    public void decreaseHealth(double damage) {
        this.health -= damage;
        if (this.health < 0) this.health = 0;
    }

    public void updateYPosition(float m){
        this.y = m;
    }

    public void drawTank(){
        //Draw the body of the tank
        if(alive()){
        float turretEndX = x + TURRET_LENGTH * (float)Math.sin(turretAngle);
        float turretEndY = (y-5) - TURRET_LENGTH * (float)Math.cos(turretAngle);

        app.stroke(turretColour);  // Set the turret color
        app.strokeWeight(4);        // Set the thickness of the turret line
        app.line(x, y-5, turretEndX, turretEndY);  // Draw the line representing the turret

        app.noStroke();  // Reset stroke settings
        app.rectMode(app.CENTER);
        app.fill(app.color(colour[0], colour[1], colour[2])); // Set color for tank body
        app.rect(x,y, 20, 5); // Main body
        app.rect(x,y-5,14, 5); // Upper body

        //Draw the tank with parachute
        if(paraFlying){
            PImage parachuteLogo = app.loadImage("src/main/resources/Tanks/parachute.png");
            this.y += 2;
            if(this.y >= (App.HEIGHT -path[(int)this.x])){
                paraFlying = false;
            }
            app.image(parachuteLogo,x-16,y-32,32,32);
        }
        if(noParaFlying){
            this.y += 4;
            this.health -= 4;
            if(this.y >= (App.HEIGHT - path[(int)this.x])){
                noParaFlying = false;
            }
        }
        }
        else{
            //Draw explosion
            app.noStroke();
            //Red circle
            app.fill(255,0,0); 
            app.ellipse(x,y,60,60);
            //Orange circle
            app.fill(255,97,0);
            app.ellipse(x,y,30,30);
            //Yellow circle
            app.fill(255,255,0);
            app.ellipse(x,y,12,12);
        }
    }

    public int getNumberofParachutes(){
        return parachutes;
    }


    /**
     * 
     * @param time
     * Use the delta time between pressing and releasing, to calculate the movement
     */
    public void moveLeft(float time) {
        float movement = MOVE_SPEED * time;
        if (fuel >= movement) {
            x -= movement;
            fuel -= movement; 
        } else {
            x -= fuel; 
            fuel = 0; 
        }
        updateTankYaxisPosition(x);
    }

    public void moveRight(float time){
        float movement = MOVE_SPEED * time;
        if (fuel >= movement) {
            x += movement;
            fuel -= movement; 
        } else {
            x -= fuel; 
            fuel = 0; 
        }
        updateTankYaxisPosition(x);
    }

    public void updateTankYaxisPosition(float x2){
        int index = (int)x2 - 1;
        if(index >= 0 && index < path.length){
            y = App.HEIGHT-path[index];
        }
    }

    public void rotateTurretLeft(float time) {
        turretAngle += ROTATE_SPEED * time;  // Increase the angle
        if (turretAngle > Math.PI / 2) {
            turretAngle = (float) (Math.PI / 2);
        }
    }
    
    public void rotateTurretRight(float time) {
        turretAngle -= ROTATE_SPEED * time; // Decrease the angle
        if (turretAngle < -Math.PI / 2) {
           turretAngle = (float) (-Math.PI / 2);
        }
    }


    public void increasePower() {
        if (power + 1.2 > getHealth()) {
            power = getHealth(); 
        } else {
            power += 1.2;
        }

    }
    

    public void decreasePower() {
        if (power - 1.2 < 0) {
            power = 0; 
        } else {
            power -= 1.2;
        }
    }


    public boolean alive(){
        if(health > 0){
            isAlive = true;
            return true;
        } else {
            isAlive = false;
            return false;
        }
        
    }


    public void fire(float wind){
        if (projectiles.isEmpty()) { 
            Projectile newProjectile = new Projectile(this.app, x, y-5, turretAngle, power, wind, colour,this.tankId);
            projectiles.add(newProjectile);  

        }
    }

    public void drawProjectile() {
        for (Projectile projectile : projectiles) {
            projectile.update();  
            projectile.draw();
            projectile.collision(path);
        }
    }

    public void clearProjectiles() {
        projectiles.clear();  
    }

    public static void getTankPositions() {
        throw new UnsupportedOperationException("error on 'getTankPositions'");
    }

    public boolean isFlying(float[] terrain){
        if(this.y < ( App.HEIGHT-terrain[(int)this.x])){
            return true;
        }
        return false;
    }
    
}

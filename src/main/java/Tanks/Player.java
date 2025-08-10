package Tanks;

public class Player {
    private String name;
    private Tank tank;
    private int score;
    private int parachutes;
    private int[] colour; 
    
    public Player(String name, Tank tank, int[] colour){
        this.name = name;
        this.tank = tank;
        this.score = 0;
        this.colour = colour;
        this.parachutes = 3;
    }

    public void updateScore(int points){
        score += points;
    }

    public int getScore(){
        return score;
    }

    public int getNumberofParachutes(){
        return parachutes;
    }
    public Tank getTank(){
        return tank;
    }
    public String getName(){
        return name;
    }

    public int[] getColour(){
        return colour;
    }

    /* 
    public boolean affordPara(){
        final int paraCost = 15;
        if(score >= paraCost){
            score -= paraCost;
            parachutes ++;
            return true;
        }
        return false;
    }
    */
    
    

}

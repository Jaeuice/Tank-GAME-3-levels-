package Tanks;

public class Wind {
    private int windSpeed;
    private int toLeftWindSpeed = -35;
    private int toRightWindSpeed = 35;

    public Wind(){
        initializeWind();
    }

    private void initializeWind(){
        windSpeed = (int) (Math.random()*(toRightWindSpeed - toLeftWindSpeed +1)) - 35;
    }
    
    public void updateWind() {
        windSpeed += (int) (Math.random() * 11) - 5; 
        windSpeed = Math.max(toLeftWindSpeed, Math.min(toRightWindSpeed, windSpeed)); 
    }

    public int getWindSpeed() {
        return Math.abs(windSpeed);
    }

    public int setWindSpeed(int m){
        return m;
    }

    public boolean isWindToTheLeft() {
        return windSpeed < 0;
    }

    public boolean isWindToTheRight() {
        return windSpeed > 0;
    }

    public float getWindForce() {
        float wF = windSpeed*0.001f;
        return wF;
    }
}

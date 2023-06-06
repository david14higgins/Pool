public class Ball {
    public Vector2D position;
    public Vector2D velocity = new Vector2D(0, 0);
    public Vector2D acceleration = new Vector2D(0, 0);
    public boolean selected = false;
    public double mass;
    public int radius;
    public BallColours colour; //0=White, 1=Black, 2=Red, 3=Yellow




    public Ball(int radius, BallColours colour) {
        this.radius = radius;
        this.mass = 10 * radius;
        this.colour = colour;
    }


    //Temp test function - gives balls random velocity
    public void hit(double power) {
        velocity.x = -power/2 + Math.random() * power;
        velocity.y = -power/2 + Math.random() * power;
    }

    public boolean isStationary() {
        return !((velocity.x * velocity.x + velocity.y * velocity.y) > 5);
    }

    public enum BallColours {
        Red,
        Yellow,
        White,
        Black
    }
}

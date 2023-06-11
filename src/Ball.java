public class Ball {
    public Vector2D position;
    public Vector2D velocity = new Vector2D(0, 0);
    public Vector2D acceleration = new Vector2D(0, 0);
    public boolean selected = false;
    public double mass;
    public int radius;
    public BallColours colour; 

    
    public Ball(int radius, Vector2D position,  BallColours colour) {
        this.radius = radius;
        this.mass = 10 * radius;
        this.colour = colour;
        this.position = position; 
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

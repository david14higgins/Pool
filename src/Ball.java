public class Ball {
    public Vector2D position;
    public Vector2D velocity = new Vector2D(0, 0);
    public Vector2D acceleration = new Vector2D(0, 0);
    public boolean selected = false;
    public double mass;
    public double radius;
    public BallColours colour; 


    public Ball(double radius, Vector2D position,  BallColours colour) {
        this.radius = radius;
        this.mass = 10 * radius;
        this.colour = colour;
        this.position = position; 
    }

    public boolean isStationary() {
        return !((velocity.x * velocity.x + velocity.y * velocity.y) > 5);
    }

    public boolean clicked(double mouseX, double mouseY) {
        return Math.sqrt(Math.pow(position.x - mouseX, 2) + Math.pow(position.y - mouseY, 2)) <= radius;
    }

    public enum BallColours {
        Red,
        Yellow,
        White,
        Black
    }
}

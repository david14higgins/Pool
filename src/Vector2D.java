public class Vector2D {
    public double x,y;
    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public static Vector2D add(Vector2D vec1, Vector2D vec2) {
        return new Vector2D(vec1.x + vec2.x, vec1.y + vec2.y);
    }

    public static Vector2D subtract(Vector2D vec1, Vector2D vec2) {
        return new Vector2D(vec1.x - vec2.x, vec1.y - vec2.y);
    }

    public static double dotProduct(Vector2D vec1, Vector2D vec2) {
        return vec1.x * vec2.x + vec1.y * vec2.y;
    }

    public static Vector2D scalar(double scalar, Vector2D vec) {
        return new Vector2D(vec.x * scalar, vec.y * scalar);
    }

    public static double magnitude(Vector2D vec) {
        return Math.sqrt(Math.pow(vec.x, 2) + Math.pow(vec.y, 2));
    }


}
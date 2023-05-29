public class Compartment {
    public final int x;
    public final int y;
    public final int width;
    public final int height;


    public Compartment(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }


    public boolean containsMouse(int x, int y) {
        return (x >= x && x <= x + width && y >= y && y <= y + height);
    }


}

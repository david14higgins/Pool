public class Compartment {
    private final int compartmentX;
    private final int compartmentY;
    private final int compartmentWidth;
    private final int compartmentHeight;


    public Compartment(int x, int y, int width, int height) {
        this.compartmentX = x;
        this.compartmentY = y;
        this.compartmentWidth = width;
        this.compartmentHeight = height;
    }

    public int getCompartmentX() {
        return compartmentX;
    }

    public int getCompartmentY() {
        return compartmentY;
    }

    public int getCompartmentWidth() {return compartmentWidth;}

    public int getCompartmentHeight() {
        return compartmentHeight;
    }

    public boolean containsMouse(int x, int y) {
        return (x >= compartmentX && x <= compartmentX + compartmentWidth && y >= compartmentY && y <= compartmentY + compartmentHeight);
    }


}

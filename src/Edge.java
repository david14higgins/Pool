import javax.swing.*;

public class Edge {
    private Vector2D start;
    private Vector2D startNormalOne;
    private Vector2D startNormalTwo;

    private Vector2D end;
    private Vector2D endNormalOne;
    private Vector2D endNormalTwo;

    public int radius;
    public boolean startSelected = false;
    public boolean endSelected = false;

    public Edge(double startX, double startY, double endX, double endY, int radius) {
        this.start = new Vector2D(startX, startY);
        this.end = new Vector2D(endX, endY);
        this.radius = radius;
        updateNormals();
    }

    public void checkClicked(double mouseX, double mouseY) {
        //Need to check if either start or end selected
        if (Math.pow((start.x - mouseX), 2) + Math.pow((start.y - mouseY), 2) <= Math.pow(radius, 2)) {
            startSelected = true;
        } else if (Math.pow((end.x - mouseX), 2) + Math.pow((end.y - mouseY), 2) <= Math.pow(radius, 2)) {
            endSelected = true;
        }
    }

    private void updateNormals() {
        //Get normal unit vectors
        double nx = -1 * (end.y - start.y);
        double ny = (end.x - start.x);
        double d = Math.sqrt(nx * nx + ny * ny);
        nx = nx / d;
        ny = ny / d;

        startNormalOne = new Vector2D(start.x + nx * radius, start.y + ny * radius);
        endNormalOne = new Vector2D(end.x + nx * radius, end.y + ny * radius);

        startNormalTwo = new Vector2D(start.x - nx * radius, start.y - ny * radius);
        endNormalTwo = new Vector2D(end.x - nx * radius, end.y - ny * radius);
    }


    //Getters and setters used so that normal points can be updated when start and end is updated

    public Vector2D getStart() {
        return start;
    }

    public void setStart(Vector2D start) {
        this.start = start;
        updateNormals();
    }

    public Vector2D getEnd() {
        return end;
    }

    public void setEnd(Vector2D end) {
        this.end = end;
        updateNormals();
    }

    //Normal getters (never need setters)

    public Vector2D getStartNormalOne() {
        return startNormalOne;
    }

    public Vector2D getStartNormalTwo() {
        return startNormalTwo;
    }

    public Vector2D getEndNormalOne() {
        return endNormalOne;
    }

    public Vector2D getEndNormalTwo() {
        return endNormalTwo;
    }
}

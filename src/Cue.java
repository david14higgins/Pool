import java.awt.Polygon;

public class Cue {
    //Game information
    private final int gameWidth;
    private final int gameHeight;
    private Vector2D mousePos; 
    private Vector2D cueStart; 
    private Vector2D cueStartNormalOne; 
    private Vector2D cueStartNormalTwo; 
    private Vector2D cueEnd; 
    private Vector2D cueEndNormalOne; 
    private Vector2D cueEndNormalTwo; 
    private Vector2D whiteBallPos;
    private Vector2D cueHitBoxVertexOne, cueHitBoxVertexTwo, cueHitBoxVertexThree, cueHitBoxVertexFour; 
    private static final int length = 300; 
    private static final int cueTipWidth = 4; 
    private static final int cueEndWidth = 6;
    private static final int hitBoxWidth = 20; 
    public boolean selected = false;


    public Cue(int gameWidth, int gameHeight) {
        this.gameWidth = gameWidth;
        this.gameHeight = gameHeight;
    }

    //Cue always sits horizontally to the right of white ball before it has been moved by player
    public void initializeCue(Vector2D whiteBallPos) {
        this.whiteBallPos = whiteBallPos; 
        cueStart = new Vector2D(whiteBallPos.x + 20, whiteBallPos.y);
        cueEnd = new Vector2D(cueStart.x + length, cueStart.y); 
        updateNormals();
    }

    //Taken from the edge functionality 
    private void updateNormals() {
        //Get normal unit vectors
        double nx = -1 * (cueEnd.y - cueStart.y);
        double ny = (cueEnd.x - cueStart.x);
        double d = Math.sqrt(nx * nx + ny * ny);
        nx = nx / d;
        ny = ny / d;

        //Calculate pool cue vertices 
        cueStartNormalOne = new Vector2D(cueStart.x + nx * (cueTipWidth / 2), cueStart.y + ny * (cueTipWidth / 2));
        cueStartNormalTwo = new Vector2D(cueStart.x - nx * (cueTipWidth / 2), cueStart.y - ny * (cueTipWidth / 2));
        
        cueEndNormalOne = new Vector2D(cueEnd.x + nx * (cueEndWidth / 2), cueEnd.y + ny * (cueEndWidth / 2));
        cueEndNormalTwo = new Vector2D(cueEnd.x - nx * (cueEndWidth / 2), cueEnd.y - ny * (cueEndWidth / 2));
        
        //Calculate pool cue hitbox Vertexs
        cueHitBoxVertexOne = new Vector2D(cueStart.x + nx * (hitBoxWidth / 2), cueStart.y + ny * (hitBoxWidth / 2));
        cueHitBoxVertexTwo = new Vector2D(cueStart.x - nx * (hitBoxWidth / 2), cueStart.y - ny * (hitBoxWidth / 2));
        cueHitBoxVertexThree = new Vector2D(cueEnd.x + nx * (hitBoxWidth / 2), cueEnd.y + ny * (hitBoxWidth / 2));
        cueHitBoxVertexFour = new Vector2D(cueEnd.x - nx * (hitBoxWidth / 2), cueEnd.y - ny * (hitBoxWidth / 2));
    }

    //Updates selected
    public void checkClicked(int mouseX, int mouseY) {
        if (this.getCueHitboxVertices().contains(mouseX, mouseY)) {
            selected = true; 
            System.out.println("Cue selected");
        } else {
            selected = false; 
            System.out.println("Cue deselected");
        }
    }


    //Return a polygon of the vertices going clockwise from the top right corner 
    public Polygon getCueVertices() {
        Vector2D[] cueVertices = new Vector2D[] {cueStartNormalOne, cueEndNormalOne, cueEndNormalTwo, cueStartNormalTwo};
        int[] xPoints = new int[4];  
        int[] yPoints = new int[4];  
        for (int i = 0; i < 4; i++) {
            xPoints[i] = (int) cueVertices[i].x;
            yPoints[i] = (int) cueVertices[i].y;
        }
        return new Polygon(xPoints, yPoints, 4);
    }

    //Return an array of the vertices going clockwise from the top right hitbox corner 
    public Polygon getCueHitboxVertices() {
        Vector2D[] cueHitboxVertices = new Vector2D[] {cueHitBoxVertexOne, cueHitBoxVertexThree, cueHitBoxVertexFour, cueHitBoxVertexTwo};
        int[] xPoints = new int[4];  
        int[] yPoints = new int[4];  
        for (int i = 0; i < 4; i++) {
            xPoints[i] = (int) cueHitboxVertices[i].x;
            yPoints[i] = (int) cueHitboxVertices[i].y;
        }
        return new Polygon(xPoints, yPoints, 4);
    }
}

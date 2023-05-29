import java.awt.Polygon;

public abstract class Cue {
    //Game information
    protected Vector2D cueStart; 
    protected Vector2D cueStartNormalOne; 
    protected Vector2D cueStartNormalTwo; 
    protected Vector2D cueEnd; 
    protected Vector2D cueEndNormalOne; 
    protected Vector2D cueEndNormalTwo; 
    protected Vector2D cueHitBoxVertexOne, cueHitBoxVertexTwo, cueHitBoxVertexThree, cueHitBoxVertexFour; 
    protected static final int length = 300; 
    protected static final int cueTipWidth = 4; 
    protected static final int cueEndWidth = 6;
    protected static final int hitBoxWidth = 20; 
    protected double directionX; 
    protected double directionY; 
    public boolean selected = false;


    public Cue() {
        
    }

    //Repositions cue based on its current direction
    public abstract void repositionCue();

    //Taken from the edge functionality 
    protected void updateVerticesAndHitbox() {
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
        selected = this.getCueHitboxVertices().contains(mouseX, mouseY) ?  true : false;
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

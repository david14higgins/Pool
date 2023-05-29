import java.awt.Polygon;

public abstract class Cue {
    //Game information

    //Cue start and end coordinates
    protected Vector2D cueStart, cueEnd; 
    //Cue vertices coordinates
    protected Vector2D cueStartNormalOne, cueStartNormalTwo, cueEndNormalOne, cueEndNormalTwo; 
    //Cue hitbox vertices coordinates
    protected Vector2D cueHitBoxVertexOne, cueHitBoxVertexTwo, cueHitBoxVertexThree, cueHitBoxVertexFour; 
    //Cue settings 
    protected final int length; 
    protected final int cueTipWidth; 
    protected final int cueEndWidth;
    protected final int hitBoxWidth = 20; 
    //If cue has been clicked (default - not)
    public boolean selected = false;


    public Cue(int length, int cueTipWidth, int cueEndWidth) {
        this.length = length;
        this.cueTipWidth = cueTipWidth; 
        this.cueEndWidth = cueEndWidth; 
    }

    //Repositions cue based on its current direction - implemented differently by aiming cue and power cue
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

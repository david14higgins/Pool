import java.awt.Polygon;

public class CushionsInfo {
    private final int cornerPocketRadius;
    private final int middlePocketRadius;
    private final int edgeRadius;
    private final int gameWidth;
    private final int gameHeight;

    private Polygon polyTopLeft;
    private Polygon polyTopRight;
    private Polygon polyRightSide; 
    private Polygon polyBottomRight; 
    private Polygon polyBottomLeft; 
    private Polygon polyLeftSide;

    //distance from corner pocket to corner edge
    private int dist1;
    //distance from corner edge to other edge
    private int dist2;
    //distance from middle pocket to middle edge
    private int dist3;
    //distance from middle edge to other edge
    private int dist4;

    Vector2D[] cushionCoords = new Vector2D[29];


    public CushionsInfo(int cornerPocketRadius, int middlePocketRadius, int edgeRadius, int gameWidth, int gameHeight) {
        this.cornerPocketRadius = cornerPocketRadius;
        this.middlePocketRadius = middlePocketRadius;
        this.edgeRadius = edgeRadius;
        this.gameWidth = gameWidth;
        this.gameHeight = gameHeight;
        calculateKeyValues();
        calculateCushionCoords();
        assignCushionPolygons();
    }

    private void calculateKeyValues() {
        dist1 = (int) Math.sqrt(Math.pow(cornerPocketRadius + edgeRadius, 2) - Math.pow(cornerPocketRadius - edgeRadius, 2));
        dist2 = 2 * middlePocketRadius - edgeRadius;
        dist3 = (int) Math.sqrt(Math.pow(middlePocketRadius + edgeRadius, 2) - Math.pow(middlePocketRadius - edgeRadius, 2));
        dist4 = (int) (2 * middlePocketRadius / (2 + Math.sqrt(3)));
    }

    private void calculateCushionCoords() {
        //Top Left Corner
        cushionCoords[0] = new Vector2D(edgeRadius, edgeRadius);

        //Top Left Cushion
        cushionCoords[1] = new Vector2D(cornerPocketRadius + dist1, edgeRadius);
        cushionCoords[2] = new Vector2D(cornerPocketRadius + dist1 + (dist2 - edgeRadius), dist2);
        cushionCoords[3] = new Vector2D(gameWidth / 2 - dist3 - dist4, dist2);
        cushionCoords[4] = new Vector2D(gameWidth / 2 - dist3, edgeRadius);

        //Top Rigth Cushion
        cushionCoords[5] = new Vector2D(gameWidth / 2 + dist3, edgeRadius);
        cushionCoords[6] = new Vector2D(gameWidth / 2 + dist3 + dist4, dist2);
        cushionCoords[7] = new Vector2D(gameWidth - cornerPocketRadius - dist1 - (dist2 - edgeRadius), dist2);
        cushionCoords[8] = new Vector2D(gameWidth - cornerPocketRadius - dist1, edgeRadius);

        //Top Right Corner
        cushionCoords[9] = new Vector2D(gameWidth - edgeRadius, edgeRadius);

        //Right Side Cushion
        cushionCoords[10] = new Vector2D(gameWidth - edgeRadius, cornerPocketRadius + dist1);
        cushionCoords[11] = new Vector2D(gameWidth - dist2, cornerPocketRadius + dist1 + (dist2 - edgeRadius));
        cushionCoords[12] = new Vector2D(gameWidth - dist2, gameHeight - cornerPocketRadius - dist1 - (dist2 - edgeRadius));
        cushionCoords[13] = new Vector2D(gameWidth - edgeRadius, gameHeight - cornerPocketRadius - dist1);

        //Bottom Right Corner 
        cushionCoords[14] = new Vector2D(gameWidth - edgeRadius, gameHeight - edgeRadius);

        //Bottom Right Cushion 
        cushionCoords[15] = new Vector2D(gameWidth - cornerPocketRadius - dist1, gameHeight - edgeRadius);
        cushionCoords[16] = new Vector2D(gameWidth - cornerPocketRadius - dist1 - (dist2 - edgeRadius), gameHeight - dist2);
        cushionCoords[17] = new Vector2D(gameWidth / 2 + dist3 + dist4, gameHeight - dist2);
        cushionCoords[18] = new Vector2D(gameWidth / 2 + dist3, gameHeight - edgeRadius);
        
        //Bottom Left Cushion 
        cushionCoords[19] = new Vector2D(gameWidth / 2 - dist3, gameHeight - edgeRadius);
        cushionCoords[20] = new Vector2D(gameWidth / 2 - dist3 - dist4, gameHeight - dist2);
        cushionCoords[21] = new Vector2D(cornerPocketRadius + dist1 + (dist2 - edgeRadius), gameHeight - dist2);
        cushionCoords[22] = new Vector2D(cornerPocketRadius + dist1, gameHeight - edgeRadius);

        //Bottom Left Corner 
        cushionCoords[23] = new Vector2D(edgeRadius, gameHeight - edgeRadius);

        //Left Side Cushion
        cushionCoords[24] = new Vector2D(edgeRadius, gameHeight - cornerPocketRadius - dist1);
        cushionCoords[25] = new Vector2D(dist2, gameHeight - cornerPocketRadius - dist1 - (dist2 - edgeRadius));
        cushionCoords[26] = new Vector2D(dist2, cornerPocketRadius + dist1 + (dist2 - edgeRadius));
        cushionCoords[27] = new Vector2D(edgeRadius, cornerPocketRadius + dist1);

        //Top Left Corner (repeated to create a cyclical DS and make cushion creation a simple iteration)
        cushionCoords[28] = new Vector2D(edgeRadius, edgeRadius);
    }

    public Vector2D[] getCushionCoords() {
        return cushionCoords;
    }

    private void assignCushionPolygons() {
        //Top Left 
        int[] xPoints = new int[] {(int) cushionCoords[1].x, (int) cushionCoords[2].x, (int) cushionCoords[3].x, (int) cushionCoords[4].x, (int) cushionCoords[1].x};
        int[] yPoints = new int[] {(int) cushionCoords[1].y, (int) cushionCoords[2].y, (int) cushionCoords[3].y, (int) cushionCoords[4].y, (int) cushionCoords[1].y};
        polyTopLeft = new Polygon(xPoints, yPoints, 5);

        //Top Right 
        xPoints = new int[] {(int) cushionCoords[5].x, (int) cushionCoords[6].x, (int) cushionCoords[7].x, (int) cushionCoords[8].x, (int) cushionCoords[5].x};
        yPoints = new int[] {(int) cushionCoords[5].y, (int) cushionCoords[6].y, (int) cushionCoords[7].y, (int) cushionCoords[8].y, (int) cushionCoords[5].y};
        polyTopRight = new Polygon(xPoints, yPoints, 5);

        //Right Side
        xPoints = new int[] {(int) cushionCoords[10].x, (int) cushionCoords[11].x, (int) cushionCoords[12].x, (int) cushionCoords[13].x, (int) cushionCoords[10].x};
        yPoints = new int[] {(int) cushionCoords[10].y, (int) cushionCoords[11].y, (int) cushionCoords[12].y, (int) cushionCoords[13].y, (int) cushionCoords[10].y};
        polyRightSide = new Polygon(xPoints, yPoints, 5);

        //Bottom Right
        xPoints = new int[] {(int) cushionCoords[15].x, (int) cushionCoords[16].x, (int) cushionCoords[17].x, (int) cushionCoords[18].x, (int) cushionCoords[15].x};
        yPoints = new int[] {(int) cushionCoords[15].y, (int) cushionCoords[16].y, (int) cushionCoords[17].y, (int) cushionCoords[18].y, (int) cushionCoords[15].y};
        polyBottomRight = new Polygon(xPoints, yPoints, 5);

        //Bottom Left 
        xPoints = new int[] {(int) cushionCoords[19].x, (int) cushionCoords[20].x, (int) cushionCoords[21].x, (int) cushionCoords[22].x, (int) cushionCoords[19].x};
        yPoints = new int[] {(int) cushionCoords[19].y, (int) cushionCoords[20].y, (int) cushionCoords[21].y, (int) cushionCoords[22].y, (int) cushionCoords[19].y};
        polyBottomLeft = new Polygon(xPoints, yPoints, 5);

        //Left Side  
        xPoints = new int[] {(int) cushionCoords[24].x, (int) cushionCoords[25].x, (int) cushionCoords[26].x, (int) cushionCoords[27].x, (int) cushionCoords[24].x};
        yPoints = new int[] {(int) cushionCoords[24].y, (int) cushionCoords[25].y, (int) cushionCoords[26].y, (int) cushionCoords[27].y, (int) cushionCoords[24].y};
        polyLeftSide = new Polygon(xPoints, yPoints, 5);
    }
    
    public Polygon[] getCushionPolygons() {
        return new Polygon[] {polyTopLeft, polyTopRight, polyRightSide, polyBottomRight, polyBottomLeft, polyLeftSide};
    }


    public enum Cushion{
        UpperLeft,
        UpperRight,
        LeftSide,
        RightSide,
        BottomLeft,
        BottomRight
    }
}

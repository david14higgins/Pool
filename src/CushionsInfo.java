public class CushionsInfo {
    private final int cornerPocketRadius;
    private final int middlePocketRadius;
    private final int edgeRadius;
    private final int gameWidth;
    private final int gameHeight;

    //distance from corner pocket to corner edge
    private int dist1;
    //distance from corner edge to other edge
    private int dist2;
    //distance from middle pocket to middle edge
    private int dist3;
    //distance from middle edge to other edge
    private int dist4;

    Vector2D[] cushionCoords = new Vector2D[28];


    public CushionsInfo(int cornerPocketRadius, int middlePocketRadius, int edgeRadius, int gameWidth, int gameHeight) {
        this.cornerPocketRadius = cornerPocketRadius;
        this.middlePocketRadius = middlePocketRadius;
        this.edgeRadius = edgeRadius;
        this.gameWidth = gameWidth;
        this.gameHeight = gameHeight;
        calculateKeyValues();
        calculateCushionCoords();
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
    }

    public Vector2D[] getCushionCoords() {
        return cushionCoords;
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

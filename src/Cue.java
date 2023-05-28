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
    private static final int length = 250; 
    private static final int cueTipWidth = 4; 
    private static final int cueEndWidth = 6;


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

        cueStartNormalOne = new Vector2D(cueStart.x + nx * (cueTipWidth / 2), cueStart.y + ny * (cueTipWidth / 2));
        cueStartNormalTwo = new Vector2D(cueStart.x - nx * (cueTipWidth / 2), cueStart.y - ny * (cueTipWidth / 2));
        
        cueEndNormalOne = new Vector2D(cueEnd.x + nx * (cueEndWidth / 2), cueEnd.y + ny * (cueEndWidth / 2));
        cueEndNormalTwo = new Vector2D(cueEnd.x - nx * (cueEndWidth / 2), cueEnd.y - ny * (cueEndWidth / 2));
    }

    public Vector2D getCueStartNormalOne() {
        return cueStartNormalOne;
    }

    public Vector2D getCueStartNormalTwo() {
        return cueStartNormalTwo; 
    }

    public Vector2D getCueEndNormalOne() {
        return cueEndNormalOne; 
    }

    public Vector2D getCueEndNormalTwo() {
        return cueEndNormalTwo;
    }
}

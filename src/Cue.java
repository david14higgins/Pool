import java.awt.*;

public class Cue {
    //Game information
    private final int gameWidth;
    private final int gameHeight;
    private Vector2D mousePos; 
    private Vector2D cueStartPos; 
    private Vector2D cueEndPos; 
    private Vector2D whiteBallPos;
    private static final int length = 225; 



    public Cue(int gameWidth, int gameHeight) {
        this.gameWidth = gameWidth;
        this.gameHeight = gameHeight;
    }

    public void initializeCue(Vector2D whiteBallPos) {
        this.whiteBallPos = whiteBallPos; 
        cueStartPos = new Vector2D(whiteBallPos.x + 20, whiteBallPos.y);
        cueEndPos = new Vector2D(cueStartPos.x + length, cueStartPos.y); 
    }

    public Vector2D getCueStartPos() {
        return cueStartPos; 
    }

    public Vector2D getCueEndPos() {
        return cueEndPos; 
    }
}

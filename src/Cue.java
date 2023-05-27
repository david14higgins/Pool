import java.awt.*;

public class Cue {
    //Game information
    private final int gameWidth;
    private final int gameHeight;
    private int mouseX;
    private int mouseY;
    private int whiteBallX;
    private int whiteBallY;



    public Cue(int gameWidth, int gameHeight) {
        this.gameWidth = gameWidth;
        this.gameHeight = gameHeight;
    }


    public int getMouseX() {
        return mouseX;
    }

    public void setMouseX(int mouseX) {
        this.mouseX = mouseX;
    }

    public int getMouseY() {
        return mouseY;
    }

    public void setMouseY(int mouseY) {
        this.mouseY = mouseY;
    }

    public int getWhiteBallX() {
        return whiteBallX;
    }

    public void setWhiteBallX(int whiteBallX) {
        this.whiteBallX = whiteBallX;
    }

    public int getWhiteBallY() {
        return whiteBallY;
    }

    public void setWhiteBallY(int whiteBallY) {
        this.whiteBallY = whiteBallY;
    }
}

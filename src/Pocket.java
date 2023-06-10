public class Pocket {
    Position position;
    int radius;
    int gameWidth;
    int gameHeight;
    private Vector2D positionVec;

    public Pocket(Position position, int radius, int gameWidth,int gameHeight) {
        this.position = position;
        this.radius = radius;
        this.gameWidth = gameWidth;
        this.gameHeight = gameHeight;
        calculateVectorPosition();
    }

    private void calculateVectorPosition() {
        switch (position) {
            case UpperLeft -> positionVec = new Vector2D(radius, radius);
            case UpperMiddle -> positionVec = new Vector2D((double) (gameWidth / 2), radius);
            case UpperRight -> positionVec = new Vector2D(gameWidth - radius, radius);
            case BottomLeft -> positionVec = new Vector2D(radius, gameHeight - radius);
            case BottomMiddle -> positionVec = new Vector2D((double) (gameWidth / 2), gameHeight - radius);
            case BottomRight -> positionVec = new Vector2D(gameWidth - radius, gameHeight - radius);
        }
    }

    public boolean hasPocketed(Ball ball) {
        //Using pythagoras but both sides squared for computational speed 
        return Math.pow((positionVec.x - ball.position.x),2) + Math.pow((positionVec.y - ball.position.y), 2) <= Math.pow(radius, 2); 
    }

    public Vector2D getPositionVec() {
        return positionVec;
    }

    public enum Position {
        UpperLeft,
        UpperMiddle,
        UpperRight,
        BottomLeft,
        BottomMiddle,
        BottomRight
    }
}

public class ShotPredictor {
    

    //This object better structures information about the shot prediction 
    
    public Vector2D wbSource; 
    public Vector2D wbDestination; 
    public Vector2D wbDirectionBefore;
    public Vector2D wbDirectionAfter;
    public boolean hittingBall; 
    public Vector2D targetBall; 
    public Vector2D targetDirectionAfter; 

    public Vector2D wbAfterEndPoint; 
    public Vector2D targetAfterEndPoint; 

    private final int whiteBallPredictorLength = 20; 
    private final int targetBallPredictorLength = 40;

    public ShotPredictor() {

    }

    public void calculatePredictorPoints() {
        wbAfterEndPoint = new Vector2D(wbDestination.x + whiteBallPredictorLength * wbDirectionAfter.x, 
            wbDestination.y + whiteBallPredictorLength * wbDirectionAfter.y);
        if(hittingBall) {
            targetAfterEndPoint = new Vector2D(targetBall.x + targetBallPredictorLength * targetDirectionAfter.x, 
                targetBall.y + targetBallPredictorLength * targetDirectionAfter.y);
        }

    }
}

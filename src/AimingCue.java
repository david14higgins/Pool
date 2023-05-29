public class AimingCue extends Cue{
    public Vector2D mousePos; 
    private static final int restingDistFromWhiteBall = 20;
    public Vector2D whiteBallPos;


    public AimingCue() {
        super();
    }

    //Cue always sits horizontally to the right of white ball before it has been moved by player
    public void initializeCue(Vector2D whiteBallPos) {
        this.whiteBallPos = whiteBallPos; 
        cueStart = new Vector2D(whiteBallPos.x + restingDistFromWhiteBall, whiteBallPos.y);
        cueEnd = new Vector2D(cueStart.x + length, cueStart.y); 
        updateVerticesAndHitbox();

        //Inital direction unit vector is (-1, 0)
        directionX = 1; 
        directionY = 0;
    }

    //Updates position of cue based on mouse position and white ball 
    public void aimCue() {
        //updates direction unit vector

        //Get unit vector of mouse position to white ball position 
        directionX = mousePos.x - whiteBallPos.x; 
        directionY = mousePos.y - whiteBallPos.y; 
        double d = Math.sqrt(directionX * directionX + directionY * directionY);
        directionX = directionX / d; 
        directionY = directionY / d; 

        repositionCue();
    }
    
    //Repositions cue based on its current direction
    public void repositionCue() {
        //Reposition cue start and end based on this unit vector 
        cueStart.x = whiteBallPos.x + (directionX * restingDistFromWhiteBall);
        cueStart.y = whiteBallPos.y + (directionY * restingDistFromWhiteBall); 
        cueEnd.x = whiteBallPos.x + (directionX * (restingDistFromWhiteBall + length)); 
        cueEnd.y = whiteBallPos.y + (directionY * (restingDistFromWhiteBall + length)); 
        
        //Update vertices and hitbox
        updateVerticesAndHitbox();
    }

}
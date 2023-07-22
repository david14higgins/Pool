public class AimingCue extends Cue{
    //For positioning w.r.t to the white ball 
    private Vector2D whiteBallPos;
    //Unit vector of aiming cue stick's current direction
    private double directionX; 
    private double directionY; 
    //How far cue is drawn from the white ball (needs updating when power cue implemented)
    private final int minDragDistance = 20;
    private final int maxDragDistance = 300;
    private int dragDistance = 0; 
    


    public AimingCue(int length, int width) {
        super(length, width);
    }

    //Cue always sits horizontally to the right of white ball before it has been moved by player
    public void initializeCue() {
        cueStart = new Vector2D(whiteBallPos.x + minDragDistance, whiteBallPos.y);
        cueEnd = new Vector2D(cueStart.x + length, cueStart.y); 
        updateVerticesAndHitbox();

        //Inital direction unit vector is (-1, 0)
        directionX = -1; 
        directionY = 0;
    }

    //Updates position of cue based on mouse position and white ball 
    public void aimCue() {
        //updates direction unit vector

        //Get unit vector of mouse position to white ball position 
        directionX = whiteBallPos.x - mousePos.x; 
        directionY = whiteBallPos.y - mousePos.y; 
        double d = Math.sqrt(directionX * directionX + directionY * directionY);
        directionX = directionX / d; 
        directionY = directionY / d; 

        repositionCue();
    }
    
    //Repositions cue based on its current direction
    public void repositionCue() {
        //Reposition cue start and end based on this unit vector 
        cueStart.x = whiteBallPos.x - (directionX * (minDragDistance + dragDistance));
        cueStart.y = whiteBallPos.y - (directionY * (minDragDistance + dragDistance)); 
        cueEnd.x = whiteBallPos.x - (directionX * (minDragDistance + dragDistance + length)); 
        cueEnd.y = whiteBallPos.y - (directionY * (minDragDistance + dragDistance + length)); 
        
        //Update vertices and hitbox
        updateVerticesAndHitbox();
    }

   
    //Getters and Setters 
    public Vector2D getCueDirection() {
        return new Vector2D(directionX, directionY);
    }

    public int getMaxDragDistance() {
        return maxDragDistance; 
    }

    public void setWhiteBallPos(Vector2D value) {
        whiteBallPos = value;
    }

    public void setDragDistance(int value) {
        dragDistance = value;
    }
}
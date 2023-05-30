public class PowerCue extends Cue {
    private final int compartmentWidth; 
    private final int compartmentHeight;
    public Vector2D mouseDown; 
    public Vector2D mouseUp; 

    public PowerCue(int length, int width, int compartmentWidth, int compartmentHeight) {
        super(length, width);
        this.compartmentWidth = compartmentWidth;
        this.compartmentHeight = compartmentHeight;
    }

    public void repositionCue() {
        double mouseDragYDistance = mousePos.y - mouseDown.y; 
        if (mouseDragYDistance > 0) {
            cueStart = new Vector2D(compartmentWidth / 2, ((compartmentHeight - length) / 2) + mouseDragYDistance) ;
            cueEnd = new Vector2D(compartmentWidth / 2, (compartmentHeight - (compartmentHeight - length) / 2) + mouseDragYDistance);
            if (cueEnd.y > compartmentHeight) {
                cueEnd.y = compartmentHeight;
            }
            updateVerticesAndHitbox();
        }
    }

    public void initializeCue() {
        //Centre cue in its compartment
        cueStart = new Vector2D(compartmentWidth / 2, (compartmentHeight - length) / 2) ;
        cueEnd = new Vector2D(compartmentWidth / 2, compartmentHeight - (compartmentHeight - length) / 2);

        updateVerticesAndHitbox();
    }

    
    
}

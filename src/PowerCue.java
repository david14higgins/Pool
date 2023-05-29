public class PowerCue extends Cue {
    private final int compartmentWidth; 
    private final int compartmentHeight;

    public PowerCue(int length, int cueTipWidth, int cueEndWidth, int compartmentWidth, int compartmentHeight) {
        super(length, cueTipWidth, cueEndWidth);
        this.compartmentWidth = compartmentWidth;
        this.compartmentHeight = compartmentHeight;
    }

    public void repositionCue() {

    }

    public void initializeCue() {
        //Line up centers 
        cueStart.x = compartmentWidth / 2;
        cueEnd.x = compartmentWidth / 2;

        //Centre cue in power area 
        cueStart.y = (compartmentHeight - length) / 2;
        cueEnd.y = compartmentHeight - (compartmentHeight - length) / 2;

        updateVerticesAndHitbox();
    }

    
}

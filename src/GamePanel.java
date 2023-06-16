import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.AreaAveragingScaleFilter;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.awt.geom.RoundRectangle2D;


public class GamePanel extends JPanel implements Runnable, MouseListener, MouseMotionListener {
    //Dimensions
    public static final int WIDTH = 1155;
    public static final int HEIGHT = 615;

    private ArrayList<Compartment> compartments;
    private Compartment playArea;
    private Compartment powerArea; 
    private Compartment yellowPocketedArea; 
    private Compartment redPocketedArea; 
    private Compartment messageArea;

    public static final int FPS = 60;
    public static final double ElapsedTime = 0.017; //60FPS = 16.7 milliseconds between each frame
    //How many times we wish to subdivide the epoch
    public static final int nSimulationUpdates = 4;
    public static double fSimElapsedTime = ElapsedTime / nSimulationUpdates;

    Thread gameThread;

    Pool poolGame;

    PowerCue powerCue;

    private final double minShotPower = 0.05;

    public GamePanel() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(Color.gray);
        this.setDoubleBuffered(true);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.setFocusable(true);
        createCompartments();
        poolGame = new Pool(playArea.width, playArea.height);
        powerCue = new PowerCue(450, 6, powerArea.width, powerArea.height);
        powerCue.initializeCue();
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }
    @Override
    public void run() {
        double drawInterval = 1000000000/FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;
        long timer = 0;
        int drawCount = 0;


        while(gameThread != null && poolGame.ready) {
            currentTime = System.nanoTime();

            delta += (currentTime - lastTime) / drawInterval;
            timer += (currentTime - lastTime);
            lastTime = currentTime;

            if(delta >= 1) {
                update();
                repaint();
                delta--;
                drawCount++;

            }

            if (timer >= 1000000000) {
                System.out.println("FPS: " + drawCount);
                drawCount = 0;
                timer = 0;
            }
        }
    }

    public void update() {
        poolGame.update();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        //Colours 
        Color redBallsColour = Color.RED; 
        Color yellowBallsColour = Color.YELLOW; 
        Color whiteBallColour = Color.WHITE; 
        Color blackBallColour = Color.BLACK; 
        Color panelColour = Color.black; 
        Color playAreaBackgroundColor = Color.BLACK; 
        Color lineColour = Color.lightGray; 
        Color highlightColur = Color.GREEN;
        Color cushionColour = new Color(109, 103, 110);
        Color pocketColour = Color.darkGray; 
        Color textColour = Color.white; 
        

        //---------Paint Program Compartments-----------

        // Enable antialiasing for smooth edges
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //Play area border
        g2.setColor(panelColour);
        for (Compartment c : compartments) {
            // g2.fillRect(c.x - 5, c.y - 5, c.width + 10, 5);
            // g2.fillRect(c.x - 5, c.y, 5, c.height);
            // g2.fillRect(c.x + c.width, c.y, 5, c.height);
            // g2.fillRect(c.x - 5, c.y + c.height, c.width + 10, 5);


            int cornerRadius = 30; // Adjust the corner radius as needed

            // Create a rounded rectangle shape
            RoundRectangle2D roundedRect = new RoundRectangle2D.Double(c.x, c.y, c.width, c.height, cornerRadius, cornerRadius);


            g2.fill(roundedRect);
        }

        //Drawing pocketed balls counters 
        AlphaComposite alphaCompositeFilled = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
        AlphaComposite alphaCompositeTransparent = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);

        //Paint play area background 
        g2.setColor(playAreaBackgroundColor); 
        g2.fillRect(playArea.x, playArea.y, playArea.x + playArea.width, playArea.y + playArea.height);
        g2.setColor(lineColour);
        g2.setComposite(alphaCompositeTransparent);
        g2.drawLine(playArea.x + playArea.width * 3 / 4, playArea.y, playArea.x + playArea.width * 3 / 4, playArea.y + playArea.height);
        g2.setColor(highlightColur);
        if(!poolGame.broken && poolGame.whiteBallBeingDragged) {
            g2.fillRect(playArea.x + playArea.width * 3 / 4, playArea.y, playArea.width / 4, playArea.height);
        } else if (poolGame.broken && poolGame.whiteBallBeingDragged) {
            g2.fillRect(playArea.x, playArea.y, playArea.width, playArea.height);
        }
        g2.setComposite(alphaCompositeFilled);

        //---------Paint Edges-----------
        for (Edge edge : poolGame.edges) {
            g2.setColor(cushionColour);
            int paintPosStartX = (int) edge.getStart().x - edge.radius;
            int paintPosStartY = (int) edge.getStart().y - edge.radius;
            int paintPosEndX = (int) edge.getEnd().x - edge.radius;
            int paintPosEndY = (int) edge.getEnd().y - edge.radius;
            int diameter = edge.radius * 2;

            g2.fillArc(playArea.x + paintPosStartX, playArea.y + paintPosStartY, diameter, diameter, 0, 360 );
            g2.fillArc(playArea.x + paintPosEndX, playArea.y + paintPosEndY, diameter, diameter, 0, 360 );

            //g2.drawLine(playArea.x + (int) edge.getStart().x, playArea.y + (int) edge.getStart().y, playArea.x + (int) edge.getEnd().x, playArea.y + (int) edge.getEnd().y);
            g2.drawLine(playArea.x + (int) edge.getStartNormalOne().x, playArea.y + (int) edge.getStartNormalOne().y, playArea.x + (int) edge.getEndNormalOne().x, playArea.y + (int) edge.getEndNormalOne().y);
            g2.drawLine(playArea.x + (int) edge.getStartNormalTwo().x, playArea.y + (int) edge.getStartNormalTwo().y, playArea.x + (int) edge.getEndNormalTwo().x, playArea.y + (int) edge.getEndNormalTwo().y);
            Point p1 = new Point(playArea.x + (int) edge.getStartNormalOne().x, playArea.y + (int) edge.getStartNormalOne().y);
            Point p2 = new Point(playArea.x + (int) edge.getEndNormalOne().x, playArea.y + (int) edge.getEndNormalOne().y);
            Point p3 = new Point(playArea.x + (int) edge.getEndNormalTwo().x, playArea.y + (int) edge.getEndNormalTwo().y);
            Point p4 = new Point(playArea.x + (int) edge.getStartNormalTwo().x, playArea.y + (int) edge.getStartNormalTwo().y);
            Polygon edgeFill = new Polygon(new int[] {p1.x, p2.x, p3.x, p4.x, p1.x}, new int[] {p1.y, p2.y, p3.y, p4.y, p1.y}, 5);
            g2.fillPolygon(edgeFill);

            for (Polygon cushionArea : poolGame.cushionsInfo.getCushionPolygons()) {
                //Create deep copy of polygon
                Polygon newPoly = new Polygon(cushionArea.xpoints, cushionArea.ypoints, cushionArea.npoints);
                for (int i = 0; i < cushionArea.npoints; i++) {
                    newPoly.xpoints[i] += playArea.x; 
                    newPoly.ypoints[i] += playArea.y; 
                    
                }
                g2.fillPolygon(newPoly);
            }

            //Fill background gaps
            g2.setColor(panelColour);
            g2.fillRect(playArea.x, playArea.y, playArea.width, 10);
            g2.fillRect(playArea.x, playArea.y + playArea.height - 10, playArea.width, 10);
            g2.fillRect(playArea.x, playArea.y, 10, playArea.height);
            g2.fillRect(playArea.x + playArea.width - 10, playArea.y, 10, playArea.height);
        }

        //---------Paint Pockets
        
        for (Pocket.Position position: poolGame.pockets.keySet()) {
            g2.setColor(pocketColour);
            Pocket pocket = poolGame.pockets.get(position);
            int paintPointX = (int) pocket.getPositionVec().x - pocket.radius;
            int paintPointY = (int) pocket.getPositionVec().y - pocket.radius;
            int diameter = pocket.radius * 2;
            g2.fillArc(playArea.x + paintPointX, playArea.y + paintPointY, diameter, diameter, 0, 360);
        }

         //---------Paint Balls-----------
        for (Ball ball : poolGame.balls) {
            int paintXPos = (int) (ball.position.x - ball.radius);
            int paintYPos = (int) (ball.position.y - ball.radius);
            int diameter = (int) ball.radius*2;

            switch (ball.colour) {
                case White:
                    g2.setColor(whiteBallColour);
                    break;
                case Black:
                    g2.setColor(blackBallColour);
                    break;
                case Red:
                    g2.setColor(redBallsColour);
                    break;
                case Yellow:
                    g2.setColor(yellowBallsColour);
                    break;
            }

            g2.fillArc(playArea.x + paintXPos,playArea.y + paintYPos, diameter, diameter, 0, 360);
        }


        //Draw Aiming Cue
        g2.setColor(Color.white);
        if(poolGame.gameState == GameState.TAKING_SHOT) {
            Polygon cueVertices = poolGame.aimingCue.getCueVertices();
            for (int i = 0; i < cueVertices.npoints; i++) {
                //Update coordinates 
                cueVertices.xpoints[i] += playArea.x;
                cueVertices.ypoints[i] += playArea.y;
            }
            g2.fillPolygon(cueVertices);
        }

        //Drawing Power Cue 
        Polygon cueVertices = powerCue.getCueVertices();
        for (int i = 0; i < cueVertices.npoints; i++) {
            //Update coordinates 
            cueVertices.xpoints[i] += powerArea.x;
            cueVertices.ypoints[i] += powerArea.y;
        }
        g2.fillPolygon(cueVertices);


        if (poolGame.gameState == GameState.TAKING_SHOT) {
            //Draw Shot Prediction 
            g2.setColor(Color.white);
            ShotPredictor sp = poolGame.shotPredictor;
            //Line from white ball source to destination 
            g2.drawLine(playArea.x + (int) sp.wbSource.x, playArea.y + (int) sp.wbSource.y, playArea.x + (int) sp.wbDestination.x, playArea.y + (int) sp.wbDestination.y);
            //Draw white ball potential destination 
            int wbDestinationPaintXPos = (int) (sp.wbDestination.x - poolGame.whiteBall.radius); 
            int wbDestinationPaintYPos = (int) (sp.wbDestination.y - poolGame.whiteBall.radius); 
            int diameter = (int) poolGame.whiteBall.radius * 2;
            g2.drawArc(playArea.x + wbDestinationPaintXPos, playArea.y + wbDestinationPaintYPos, diameter, diameter, 0, 360);

            if(sp.hittingBall) {
                //Draw white ball prediction after collision 
                g2.drawLine(playArea.x + (int) sp.wbDestination.x, playArea.y + (int) sp.wbDestination.y, playArea.x + (int) sp.wbAfterEndPoint.x, playArea.y + (int) sp.wbAfterEndPoint.y);
                //Draw target ball prediction after collision 
                g2.drawLine(playArea.x + (int) sp.targetBall.x, playArea.y + (int) sp.targetBall.y, playArea.x + (int) sp.targetAfterEndPoint.x, playArea.y + (int) sp.targetAfterEndPoint.y);  
            }
        }

        //Reds 
        //Pocketed
        //g2.setColor(Color.RED);
        g2.setColor(redBallsColour);
        g2.setComposite(alphaCompositeFilled);
        int redBallX = 10;
        for (int i = 0; i < poolGame.numRedBallsPocketed; i++) {
            g2.fillArc(redPocketedArea.x + redBallX, redPocketedArea.y + 10, 20, 20, 0, 360);
            redBallX += 25;
        }
        //Unpocketed
        g2.setComposite(alphaCompositeTransparent);
        for (int i = poolGame.numRedBallsPocketed; i < 7; i++) {
            g2.fillArc(redPocketedArea.x + redBallX, redPocketedArea.y + 10, 20, 20, 0, 360);
            redBallX += 25;
        }
        //Black ball
        g2.setColor(blackBallColour);
        if(poolGame.playerOneRed && poolGame.blackPocketedByP1) {
            g2.setComposite(alphaCompositeFilled);
        } else {
            g2.setComposite(alphaCompositeTransparent);
        }
        g2.fillArc(redPocketedArea.x + redBallX, redPocketedArea.y + 10, 20, 20, 0, 360);

        //Yellows
        //Pocketed
        //g2.setColor(Color.YELLOW);
        g2.setColor(yellowBallsColour);
        g2.setComposite(alphaCompositeFilled);
        int yellowBallX = 10;
        for (int i = 0; i < poolGame.numYellowBallsPocketed; i++) {
            g2.fillArc(yellowPocketedArea.x + yellowBallX, yellowPocketedArea.y + 10, 20, 20, 0, 360);
            yellowBallX += 25;
        }
        //Unpocketed
        g2.setComposite(alphaCompositeTransparent);
        for (int i = poolGame.numYellowBallsPocketed; i < 7; i++) {
            g2.fillArc(yellowPocketedArea.x + yellowBallX, yellowPocketedArea.y + 10, 20, 20, 0, 360);
            yellowBallX += 25;
        }
        //Black ball
        g2.setColor(blackBallColour);
        if(!poolGame.playerOneRed && poolGame.blackPocketedByP2) {
            g2.setComposite(alphaCompositeFilled);
        } else {
            g2.setComposite(alphaCompositeTransparent);
        }
        g2.fillArc(yellowPocketedArea.x + yellowBallX, yellowPocketedArea.y + 10, 20, 20, 0, 360);

        //Restore opacity 
        g2.setComposite(alphaCompositeFilled);


        //Text Output 
        g2.setColor(textColour);

        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);


        String message = poolGame.outputMessage;

        //Adjust font size until message fits message area 
        boolean messageFits = false; 

        Font outputFont; 
        int fontSize = 24; 
        int stringWidth = 0; 
        int stringHeight = 0; 
        int ascent = 0; 
        int descent = 0;
        
        while(!messageFits) {
            try {
                outputFont = Font.createFont(Font.TRUETYPE_FONT, new File("Fonts/Inconsolata-VariableFont_wdth,wght.ttf"));
                outputFont = outputFont.deriveFont(Font.BOLD, fontSize);
                // Register the font with the graphics environment
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(outputFont);
            } catch (Exception e) {
                outputFont = new Font("Arial", Font.BOLD, fontSize); 
                e.printStackTrace();
            }

            g2.setFont(outputFont);
            FontMetrics fontMetrics = g2.getFontMetrics();
            stringWidth = fontMetrics.stringWidth(message);

            stringHeight = fontMetrics.getHeight();
            ascent = fontMetrics.getAscent();
            descent = fontMetrics.getDescent();
            messageFits = stringWidth < messageArea.width - 10; 
            
            if(!messageFits) {
                fontSize -= 2;
            }
        }

        int messageXPos = messageArea.x + (messageArea.width / 2) - (stringWidth / 2);
        int messageYPos = messageArea.y + ((messageArea.height  - ascent + descent) / 2) + (stringHeight / 2); 
        
        g2.drawString(message, messageXPos, messageYPos);

  

        g2.dispose();
    }


    //-----------This will all need adjusting based on the state of the pool game-----------------

    @Override
    public void mouseClicked(MouseEvent e) {
//        poolGame.userClick(e);
    }


//---------------Will need to update user interactions to just use left clicks----------------------

    @Override
    public void mousePressed(MouseEvent e) {
        if (playArea.containsMouse(e.getX(), e.getY()) && poolGame.gameState == GameState.TAKING_SHOT){
            //Shift coordinates to fit pool game's (0,0) coordinate system
            int xGameClick = e.getX() - playArea.x;
            int yGameClick = e.getY() - playArea.y;

            //LEFT CLICK - Static Collisions or (TEMPORARILY!) moving edges
            if(e.getButton() == MouseEvent.BUTTON1) {
                
                if(poolGame.ballClicked(poolGame.whiteBall, xGameClick, yGameClick)) {
                    if(poolGame.mayDragWhiteBall){
                        poolGame.whiteBall.selected = true;
                    }
                }

                //TEMP 
                //for (Ball b : poolGame.balls) {
                //    if (poolGame.ballClicked(b, xGameClick, yGameClick)) {
                //        b.selected = true; 
                //    }
                //}

                //Check if power cue is selected
                poolGame.aimingCue.checkClicked(xGameClick, yGameClick);

                // --------- For moving edges (disabled) ---------- 
                //for (Edge ed : poolGame.edges) {
                //    ed.checkClicked(xGameClick, yGameClick);
                //}
            //RIGHT CLICK - Dynamic Collisions
            } else if (e.getButton() == MouseEvent.BUTTON3) {
                //Adjust click coordinates to reflect play area's (0,0) coordinate system
                //Give pool game mouse down coordinates for shooting functionality
                poolGame.mouseDownX = xGameClick;
                poolGame.mouseDownY = yGameClick;
            }
        }

        if(powerArea.containsMouse(e.getX(), e.getY()) && poolGame.gameState == GameState.TAKING_SHOT) {
            int xPowerClick = e.getX() - powerArea.x;
            int yPowerClick = e.getY() - powerArea.y;

            if (e.getButton() == MouseEvent.BUTTON1) {
                if(powerCue.checkClicked(xPowerClick, yPowerClick)) {
                    powerCue.mouseDown = new Vector2D(xPowerClick, yPowerClick);
                }
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (playArea.containsMouse(e.getX(), e.getY())) {
            //Shift coordinates to fit pool game's (0,0) coordinate system
            int xGameClick = e.getX() - playArea.x;
            int yGameClick = e.getY() - playArea.y;

            if(poolGame.whiteBall.selected) {
                poolGame.dragWhiteBall(xGameClick, yGameClick); 
                poolGame.whiteBallBeingDragged = true; 
            }

            //Aiming cue movement 
            if(poolGame.aimingCue.selected) {
                poolGame.aimingCue.mousePos = new Vector2D(xGameClick, yGameClick);
                poolGame.aimingCue.aimCue();
                poolGame.updateShotPrediction();
            }

            //TEMP 
            //for (Ball b : poolGame.balls) {
            //    if(b.selected) {b.position = new Vector2D(xGameClick, yGameClick);}
           // }
        }

        if(powerArea.containsMouse(e.getX(), e.getY())) {
            int xPowerClick = e.getX() - powerArea.x;
            int yPowerClick = e.getY() - powerArea.y;

            //Power cue movement 
            if(powerCue.selected) {
                powerCue.mousePos = new Vector2D(xPowerClick, yPowerClick);
                double normalizedShotPower = powerCue.getNormalizedShotPower(); 
                poolGame.aimingCue.dragDistance = (int) (poolGame.aimingCue.maxDragDistance * normalizedShotPower);
                powerCue.repositionCue();
            }
            
        }

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (playArea.containsMouse(e.getX(), e.getY())) {
            int xGameClick = e.getX() - playArea.x;
            int yGameClick = e.getY() - playArea.y;
    
            if(e.getButton() == MouseEvent.BUTTON1) {
                //Deselect Ball (needs updating for white ball only)
                poolGame.whiteBall.selected = false; 
                poolGame.whiteBallBeingDragged = false;
    
                //Deselect aiming cue
                poolGame.aimingCue.selected = false;


                //TEMP 
                //for (Ball b : poolGame.balls) {
                //    b.selected = false; 
                //}
            }
        }

        if (powerArea.containsMouse(e.getX(), e.getY())) {
            int xPowerClick = e.getX() - powerArea.x;
            int yPowerClick = e.getY() - powerArea.y;

            if (e.getButton() == MouseEvent.BUTTON1) {
                if (powerCue.selected) {
                    powerCue.mouseUp = new Vector2D(xPowerClick, yPowerClick);
                    double normalizedShotPower = powerCue.getNormalizedShotPower();
                    //Only take shot if minimum shot power reached 
                    if (normalizedShotPower > minShotPower) {
                        poolGame.takeShot(normalizedShotPower);
                    }
                    powerCue.resetPowerCue();
                }
            }

            //Reset aiming cue's distance from white ball for the next shot
            poolGame.aimingCue.dragDistance = 0;
        }

    }



    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {}

    private void createCompartments() {
        compartments = new ArrayList<>();
        playArea = new Compartment(65, 10, 1080, 540);
        compartments.add(playArea);

        powerArea = new Compartment(10, 10, 40, 540);
        compartments.add(powerArea);

        redPocketedArea = new Compartment(10, 565, 215, 40);
        compartments.add(redPocketedArea);

        yellowPocketedArea = new Compartment(930, 565, 215, 40);
        compartments.add(yellowPocketedArea);

        messageArea = new Compartment(240, 565, 675, 40);
        compartments.add(messageArea);
    }

    //Getters and Setters

    public Compartment getPlayArea() {
        return playArea;
    }
}



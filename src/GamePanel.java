import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.util.ArrayList;
import java.awt.geom.RoundRectangle2D;


public class GamePanel extends JPanel implements Runnable, MouseListener, MouseMotionListener {
    //Panel Dimensions
    private static final int WIDTH = 1155;
    private static final int HEIGHT = 615;

    //Compartment Objects
    private ArrayList<Compartment> compartments;
    private Compartment playArea;
    private Compartment powerArea; 
    private Compartment yellowPocketedArea; 
    private Compartment redPocketedArea; 
    private Compartment messageArea;

    private static final int FPS = 60;
    private static final double ElapsedTime = 0.017; //60FPS = 16.7 milliseconds between each frame
    //How many times we wish to subdivide the epoch
    public static final int nSimulationUpdates = 4;
    public static double fSimElapsedTime = ElapsedTime / nSimulationUpdates;

    private Thread gameThread;

    private Pool poolGame;

    private PowerCue powerCue;

    //(How far should the power cue be dragged)
    private final double minShotPower = 0.05;

    //Set up game panel 
    public GamePanel() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(new Color(0, 70, 0));
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
    
    //Create game loop
    @Override
    public void run() {
        double drawInterval = 1000000000/FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;
        long timer = 0;
        int drawCount = 0;


        while(gameThread != null && poolGame.isReady()) {
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

    //-------- OUTPUT HANDLING ------------

    //Painting Colour Palette 
    private Color redBallsColour = Color.RED; 
    private Color yellowBallsColour = Color.YELLOW;
    private Color whiteBallColour = Color.WHITE; 
    private Color blackBallColour = Color.BLACK; 
    private Color panelColour = new Color(0, 50, 0); 
    private Color lineColour = Color.lightGray; 
    private Color highlightColur = Color.GREEN;
    private Color cushionColour = new Color(0, 70, 0);
    private Color pocketColour = Color.BLACK; 
    private Color textColour = Color.WHITE; 
    private Color cueColour = Color.BLACK; 

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        paintCompartments(g2);
        paintPlayArea(g2);
        paintCushions(g2);
        paintPockets(g2); 
        paintBalls(g2); 
        paintAimingCue(g2);
        paintPowerCue(g2); 
        paintShotPredictor(g2);
        paintPocketedBallsCounters(g2); 
        paintTextOutput(g2); 
        
        g2.dispose();
    } 

    private void paintCompartments(Graphics2D g2) {
        g2.setColor(panelColour);
        for (Compartment c : compartments) {
            int cornerRadius = 30; // Adjust the corner radius as needed
            // Create a rounded rectangle shape
            RoundRectangle2D roundedRect = new RoundRectangle2D.Double(c.x, c.y, c.width, c.height, cornerRadius, cornerRadius);
            g2.fill(roundedRect);
        }
    }

    //Paints game's white line and highlighted areas if white ball is being dragged 
    private void paintPlayArea(Graphics2D g2) {
        g2.setColor(lineColour);
        g2.drawLine(playArea.x + playArea.width * 3 / 4, playArea.y, playArea.x + playArea.width * 3 / 4, playArea.y + playArea.height);
        g2.setColor(highlightColur);
        if(!poolGame.isBroken() && poolGame.isWhiteBallBeingDragged()) {
            g2.fillRect(playArea.x + playArea.width * 3 / 4, playArea.y, playArea.width / 4, playArea.height);
        } else if (poolGame.isBroken() && poolGame.isWhiteBallBeingDragged()) {
            g2.fillRect(playArea.x, playArea.y, playArea.width, playArea.height);
        }
    }

    private void paintCushions(Graphics2D g2) {
        g2.setColor(cushionColour);
        for (Edge edge : poolGame.getEdges()) {
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

            for (Polygon cushionArea : poolGame.getCushionsInfo().getCushionPolygons()) {
                //Create deep copy of polygon
                Polygon newPoly = new Polygon(cushionArea.xpoints, cushionArea.ypoints, cushionArea.npoints);
                for (int i = 0; i < cushionArea.npoints; i++) {
                    newPoly.xpoints[i] += playArea.x; 
                    newPoly.ypoints[i] += playArea.y; 
                    
                }
                g2.fillPolygon(newPoly);
            } 
        }
        //Fill background gaps
        g2.fillRect(playArea.x, playArea.y, playArea.width, 10);
        g2.fillRect(playArea.x, playArea.y + playArea.height - 10, playArea.width, 10);
        g2.fillRect(playArea.x, playArea.y, 10, playArea.height);
        g2.fillRect(playArea.x + playArea.width - 10, playArea.y, 10, playArea.height);
    }

    private void paintPockets(Graphics2D g2) {
        g2.setColor(pocketColour);
        for (Pocket.Position position: poolGame.getPockets().keySet()) {
            Pocket pocket = poolGame.getPockets().get(position);
            int paintPointX = (int) pocket.getPositionVec().x - pocket.radius;
            int paintPointY = (int) pocket.getPositionVec().y - pocket.radius;
            int diameter = pocket.radius * 2;
            g2.fillArc(playArea.x + paintPointX, playArea.y + paintPointY, diameter, diameter, 0, 360);
        }
    }

    private void paintBalls(Graphics2D g2) {
        for (Ball ball : poolGame.getBalls()) {
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
    }

    private void paintAimingCue(Graphics2D g2) {
        if(poolGame.getGameState() == GameState.TAKING_SHOT) {
            g2.setColor(cueColour);
            Polygon cueVertices = poolGame.getAimingCue().getCueVertices();
            for (int i = 0; i < cueVertices.npoints; i++) {
                //Update coordinates 
                cueVertices.xpoints[i] += playArea.x;
                cueVertices.ypoints[i] += playArea.y;
            }
            g2.fillPolygon(cueVertices);
        }
    }

    private void paintPowerCue(Graphics2D g2) {
        g2.setColor(cueColour);
        Polygon cueVertices = powerCue.getCueVertices();
        for (int i = 0; i < cueVertices.npoints; i++) {
            //Update coordinates 
            cueVertices.xpoints[i] += powerArea.x;
            cueVertices.ypoints[i] += powerArea.y;
        }
        g2.fillPolygon(cueVertices);
    }

    private void paintShotPredictor(Graphics2D g2) {
        if (poolGame.getGameState() == GameState.TAKING_SHOT) {
            //Draw Shot Prediction 
            g2.setColor(Color.white);
            ShotPredictor sp = poolGame.getShotPredictor();
            //Line from white ball source to destination 
            g2.drawLine(playArea.x + (int) sp.wbSource.x, playArea.y + (int) sp.wbSource.y, playArea.x + (int) sp.wbDestination.x, playArea.y + (int) sp.wbDestination.y);
            //Draw white ball potential destination 
            int wbDestinationPaintXPos = (int) (sp.wbDestination.x - poolGame.getWhiteBall().radius); 
            int wbDestinationPaintYPos = (int) (sp.wbDestination.y - poolGame.getWhiteBall().radius); 
            int diameter = (int) poolGame.getWhiteBall().radius * 2;
            g2.drawArc(playArea.x + wbDestinationPaintXPos, playArea.y + wbDestinationPaintYPos, diameter, diameter, 0, 360);

            if(sp.hittingBall) {
                //Draw white ball prediction after collision 
                g2.drawLine(playArea.x + (int) sp.wbDestination.x, playArea.y + (int) sp.wbDestination.y, playArea.x + (int) sp.wbAfterEndPoint.x, playArea.y + (int) sp.wbAfterEndPoint.y);
                //Draw target ball prediction after collision 
                g2.drawLine(playArea.x + (int) sp.targetBall.x, playArea.y + (int) sp.targetBall.y, playArea.x + (int) sp.targetAfterEndPoint.x, playArea.y + (int) sp.targetAfterEndPoint.y);  
            }
        }
    }

    private void paintPocketedBallsCounters(Graphics2D g2) {
        //Two opacity settings (100% and 50%)
        AlphaComposite alphaCompositeFilled = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
        AlphaComposite alphaCompositeTransparent = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);

        //Pocketed Reds
        //g2.setColor(Color.RED);
        g2.setColor(redBallsColour);
        g2.setComposite(alphaCompositeFilled);
        int redBallX = 10;
        for (int i = 0; i < poolGame.getNumRedBallsPocketed(); i++) {
            g2.fillArc(redPocketedArea.x + redBallX, redPocketedArea.y + 10, 20, 20, 0, 360);
            redBallX += 25;
        }
        //Unpocketed Reds
        g2.setComposite(alphaCompositeTransparent);
        for (int i = poolGame.getNumRedBallsPocketed(); i < 7; i++) {
            g2.fillArc(redPocketedArea.x + redBallX, redPocketedArea.y + 10, 20, 20, 0, 360);
            redBallX += 25;
        }
        //Black ball
        g2.setColor(blackBallColour);
        if(poolGame.getPlayerOneRed() && poolGame.hasPlayerOnePocketedBlack()) {
            g2.setComposite(alphaCompositeFilled);
        } else {
            g2.setComposite(alphaCompositeTransparent);
        }
        g2.fillArc(redPocketedArea.x + redBallX, redPocketedArea.y + 10, 20, 20, 0, 360);

        //Pocketed Yellows
        g2.setColor(yellowBallsColour);
        g2.setComposite(alphaCompositeFilled);
        int yellowBallX = 10;
        for (int i = 0; i < poolGame.getNumYellowBallsPocketed(); i++) {
            g2.fillArc(yellowPocketedArea.x + yellowBallX, yellowPocketedArea.y + 10, 20, 20, 0, 360);
            yellowBallX += 25;
        }
        //Unpocketed Yellows
        g2.setComposite(alphaCompositeTransparent);
        for (int i = poolGame.getNumYellowBallsPocketed(); i < 7; i++) {
            g2.fillArc(yellowPocketedArea.x + yellowBallX, yellowPocketedArea.y + 10, 20, 20, 0, 360);
            yellowBallX += 25;
        }
        //Black ball
        g2.setColor(blackBallColour);
        if(!poolGame.getPlayerOneRed() && poolGame.hasPlayerTwoPocketedBlack()) {
            g2.setComposite(alphaCompositeFilled);
        } else {
            g2.setComposite(alphaCompositeTransparent);
        }
        g2.fillArc(yellowPocketedArea.x + yellowBallX, yellowPocketedArea.y + 10, 20, 20, 0, 360);

        //Restore opacity 
        g2.setComposite(alphaCompositeFilled);
    } 

    private void paintTextOutput(Graphics2D g2) {
        g2.setColor(textColour);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        String message = poolGame.getOutputMessage();

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

        //Calculate position
        int messageXPos = messageArea.x + (messageArea.width / 2) - (stringWidth / 2);
        int messageYPos = messageArea.y + ((messageArea.height  - ascent + descent) / 2) + (stringHeight / 2); 
        
        g2.drawString(message, messageXPos, messageYPos);
    }


   
    //----------INPUT HANDLING--------------

    @Override
    public void mousePressed(MouseEvent e) {
        if (!poolGame.isGameOver()) {
            if (playArea.containsMouse(e.getX(), e.getY()) && poolGame.getGameState() == GameState.TAKING_SHOT){
                //Shift coordinates to fit pool game's (0,0) coordinate system
                int xGameClick = e.getX() - playArea.x;
                int yGameClick = e.getY() - playArea.y;

                //LEFT CLICK - Static Collisions or (TEMPORARILY!) moving edges
                if(e.getButton() == MouseEvent.BUTTON1) {
                    
                    if(poolGame.getWhiteBall().clicked(xGameClick, yGameClick)) {
                        if(poolGame.isWhiteBallDraggable()){
                            poolGame.getWhiteBall().selected = true;
                        }
                    }

                    // ---------- For moving balls with mouse (disabled) --------
                    // for (Ball b : poolGame.balls) {
                    //    if (poolGame.ballClicked(b, xGameClick, yGameClick)) {
                    //        b.selected = true; 
                    //    }
                    // }

                    //Check if power cue is selected
                    poolGame.getAimingCue().checkClicked(xGameClick, yGameClick);

                    // --------- For moving edges (disabled) ---------- 
                    //for (Edge ed : poolGame.edges) {
                    //    ed.checkClicked(xGameClick, yGameClick);
                    //}
                } 
            }

            if(powerArea.containsMouse(e.getX(), e.getY()) && poolGame.getGameState() == GameState.TAKING_SHOT) {
                int xPowerClick = e.getX() - powerArea.x;
                int yPowerClick = e.getY() - powerArea.y;

                if (e.getButton() == MouseEvent.BUTTON1) {
                    if(powerCue.checkClicked(xPowerClick, yPowerClick)) {
                        powerCue.mouseDown = new Vector2D(xPowerClick, yPowerClick);
                    }
                }
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if(!poolGame.isGameOver()) {
            if (playArea.containsMouse(e.getX(), e.getY())) {
                //Shift coordinates to fit pool game's (0,0) coordinate system
                int xGameClick = e.getX() - playArea.x;
                int yGameClick = e.getY() - playArea.y;

                if(poolGame.getWhiteBall().selected) {
                    poolGame.dragWhiteBall(xGameClick, yGameClick); 
                    poolGame.setWhiteBallBeingDragged(true);
                }

                //Aiming cue movement 
                if(poolGame.getAimingCue().selected) {
                    poolGame.getAimingCue().mousePos = new Vector2D(xGameClick, yGameClick);
                    poolGame.getAimingCue().aimCue();
                    poolGame.updateShotPrediction();
                }

                // --------- For moving edges (disabled) ---------- 
                // for (Ball b : poolGame.balls) {
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
                    poolGame.getAimingCue().setDragDistance((int) (poolGame.getAimingCue().getMaxDragDistance() * normalizedShotPower));
                    powerCue.repositionCue();
                }
            }
        }   
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (!poolGame.isGameOver()) {
            if (playArea.containsMouse(e.getX(), e.getY())) {
                if(e.getButton() == MouseEvent.BUTTON1) {
                    //Deselect Ball (needs updating for white ball only)
                    poolGame.getWhiteBall().selected = false; 
                    poolGame.setWhiteBallBeingDragged(false);
        
                    //Deselect aiming cue
                    poolGame.getAimingCue().selected = false;


                    // --------- For moving edges (disabled) ---------- 
                    // for (Ball b : poolGame.balls) {
                    //    b.selected = false; 
                    // }
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
                poolGame.getAimingCue().setDragDistance(0); 
            }
        } else {
            poolGame.restartGame();
        }
    }


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

    //Empty methods (do not need implementing)
    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {}

     @Override
    public void mouseClicked(MouseEvent e) {}
}



import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.AreaAveragingScaleFilter;
import java.util.ArrayList;


public class GamePanel extends JPanel implements Runnable, MouseListener, MouseMotionListener {
    //Dimensions
    public static final int WIDTH = 1155;
    public static final int HEIGHT = 615;

    private ArrayList<Compartment> compartments;
    private Compartment playArea;
    private Compartment powerArea; 

    public static final int FPS = 60;
    public static final double ElapsedTime = 0.017; //60FPS = 16.7 milliseconds between each frame
    //How many times we wish to subdivide the epoch
    public static final int nSimulationUpdates = 4;
    public static double fSimElapsedTime = ElapsedTime / nSimulationUpdates;

    Thread gameThread;

    Pool poolGame;

    PowerCue powerCue;

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

        g2.setColor(Color.black);

        //---------Paint Balls-----------


        for (Ball ball : poolGame.balls) {
            int paintXPos = (int) ball.position.x - ball.radius;
            int paintYPos = (int) ball.position.y - ball.radius;
            int diameter = ball.radius*2;

            switch (ball.colour) {
                case White:
                    g2.setColor(Color.WHITE);
                    break;
                case Black:
                    g2.setColor(Color.BLACK);
                    break;
                case Red:
                    g2.setColor(Color.RED);
                    break;
                case Yellow:
                    g2.setColor(Color.YELLOW);
                    break;
            }

            g2.fillArc(playArea.x + paintXPos,playArea.y + paintYPos, diameter, diameter, 0, 360);
        }

        //---------Paint Edges-----------
        for (Edge edge : poolGame.edges) {
            g2.setColor(Color.BLACK);
            int paintPosStartX = (int) edge.getStart().x - edge.radius;
            int paintPosStartY = (int) edge.getStart().y - edge.radius;
            int paintPosEndX = (int) edge.getEnd().x - edge.radius;
            int paintPosEndY = (int) edge.getEnd().y - edge.radius;
            int diameter = edge.radius * 2;

            g2.drawArc(playArea.x + paintPosStartX, playArea.y + paintPosStartY, diameter, diameter, 0, 360 );
            g2.drawArc(playArea.x + paintPosEndX, playArea.y + paintPosEndY, diameter, diameter, 0, 360 );

            //g2.drawLine(playArea.x + (int) edge.getStart().x, playArea.y + (int) edge.getStart().y, playArea.x + (int) edge.getEnd().x, playArea.y + (int) edge.getEnd().y);
            g2.drawLine(playArea.x + (int) edge.getStartNormalOne().x, playArea.y + (int) edge.getStartNormalOne().y, playArea.x + (int) edge.getEndNormalOne().x, playArea.y + (int) edge.getEndNormalOne().y);
            g2.drawLine(playArea.x + (int) edge.getStartNormalTwo().x, playArea.y + (int) edge.getStartNormalTwo().y, playArea.x + (int) edge.getEndNormalTwo().x, playArea.y + (int) edge.getEndNormalTwo().y);


        }

        //---------Paint Pockets
        g2.setColor(Color.green);
        for (Pocket.Position position: poolGame.pockets.keySet()) {
            Pocket pocket = poolGame.pockets.get(position);
            int paintPointX = (int) pocket.getPositionVec().x - pocket.radius;
            int paintPointY = (int) pocket.getPositionVec().y - pocket.radius;
            int diameter = pocket.radius * 2;
            g2.fillArc(playArea.x + paintPointX, playArea.y + paintPointY, diameter, diameter, 0, 360);
        }

        //Draw Aiming Cue
        g2.setColor(Color.black);
        if(poolGame.gameState == GameState.TAKING_SHOT) {
            Polygon cueVertices = poolGame.aimingCue.getCueVertices();
            for (int i = 0; i < cueVertices.npoints; i++) {
                //Update coordinates 
                cueVertices.xpoints[i] += playArea.x;
                cueVertices.ypoints[i] += playArea.y;
            }
            g2.drawPolygon(cueVertices);
        }

        //Drawing Power Cue 
        Polygon cueVertices = powerCue.getCueVertices();
        for (int i = 0; i < cueVertices.npoints; i++) {
            //Update coordinates 
            cueVertices.xpoints[i] += powerArea.x;
            cueVertices.ypoints[i] += powerArea.y;
        }
        g2.drawPolygon(cueVertices);


        //---------Paint Program Compartments-----------

        //Play area border
        g2.setColor(Color.white);
        for (Compartment c : compartments) {
            g2.fillRect(c.x - 5, c.y - 5, c.width + 10, 5);
            g2.fillRect(c.x - 5, c.y, 5, c.height);
            g2.fillRect(c.x + c.width, c.y, 5, c.height);
            g2.fillRect(c.x - 5, c.y + c.height, c.width + 10, 5);
        }


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
                //Select ball for drag event - will need restricting 
                for(Ball b : poolGame.balls) {
                    if (poolGame.ballClicked(b, xGameClick, yGameClick)) {
                        b.selected = true;
                    }
                }

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

    //Will need adjusting for just white ball dragging
    @Override
    public void mouseDragged(MouseEvent e) {
        if (playArea.containsMouse(e.getX(), e.getY())) {
            //Shift coordinates to fit pool game's (0,0) coordinate system
            int xGameClick = e.getX() - playArea.x;
            int yGameClick = e.getY() - playArea.y;

            //For ball movement - will need restricting 
            for (Ball b : poolGame.balls) {
                if(b.selected) {
                    b.position.x = xGameClick;
                    b.position.y = yGameClick;
                    if (b.equals(poolGame.whiteBall)) {
                        poolGame.aimingCue.whiteBallPos = b.position;
                        poolGame.aimingCue.repositionCue();
                    } 
                }
            }

            //Aiming cue movement 
            if(poolGame.aimingCue.selected) {
                poolGame.aimingCue.mousePos = new Vector2D(xGameClick, yGameClick);
                poolGame.aimingCue.aimCue();
            }

        
            /* --------- For moving edges (disabled) ---------- 
            for (Edge ed : poolGame.edges) {
                if(ed.startSelected) {
                    Vector2D newStart = new Vector2D(xGameClick, yGameClick);
                    ed.setStart(newStart);
                } else if (ed.endSelected) {
                    Vector2D newEnd = new Vector2D(xGameClick, yGameClick);
                    ed.setEnd(newEnd);
                }
            }
            */
        }

        if(powerArea.containsMouse(e.getX(), e.getY())) {
            int xPowerClick = e.getX() - powerArea.x;
            int yPowerClick = e.getY() - powerArea.y;

            //Power cue movement 
            if(powerCue.selected) {
                powerCue.mousePos = new Vector2D(xPowerClick, yPowerClick);
                powerCue.repositionCue();
            }
            
        }

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (playArea.containsMouse(e.getX(), e.getY())) {
            int xGameClick = e.getX() - playArea.x;
            int yGameClick = e.getY() - playArea.y;
    
            //LEFT CLICK - Static Collisions
            if(e.getButton() == MouseEvent.BUTTON1) {
                //Deselect Ball (needs updating for white ball only)
                for (Ball b : poolGame.balls) {
                    if (b.selected) {
                        b.selected = false;
                    }
                }
    
                //Deselect aiming cue
                poolGame.aimingCue.selected = false;
                
    
            //RIGHT CLICK - Dynamic Collisions (To Be Removed!)
            } else if (e.getButton() == MouseEvent.BUTTON3) {
                //Adjust click coordinates to reflect play area's (0,0) coordinate system
                poolGame.mouseUpX = xGameClick;
                poolGame.mouseUpY = yGameClick;
                //poolGame.ballShoot();
            }
        }

        if (powerArea.containsMouse(e.getX(), e.getY())) {
            int xPowerClick = e.getX() - powerArea.x;
            int yPowerClick = e.getY() - powerArea.y;

            

            if (e.getButton() == MouseEvent.BUTTON1) {
                if (powerCue.selected) {
                    powerCue.mouseUp = new Vector2D(xPowerClick, yPowerClick);
                    double normalizedShotPower = powerCue.getNormalizedShotPower();
                    poolGame.takeShot(normalizedShotPower);
                    powerCue.resetPowerCue();
                }
            }
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

        Compartment redPocketedArea = new Compartment(10, 565, 275, 40);
        compartments.add(redPocketedArea);

        Compartment yellowPocketedArea = new Compartment(870, 565, 275, 40);
        compartments.add(yellowPocketedArea);

        Compartment messageArea = new Compartment(300, 565, 555, 40);
        compartments.add(messageArea);
    }

    //Getters and Setters

    public Compartment getPlayArea() {
        return playArea;
    }
}



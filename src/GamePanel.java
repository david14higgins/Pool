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

    public int playAreaX = 65;
    public int playAreaY = 10;

    public static final int FPS = 60;
    public static final double ElapsedTime = 0.017; //60FPS = 16.7 milliseconds between each frame
    //How many times we wish to subdivide the epoch
    public static final int nSimulationUpdates = 4;
    public static double fSimElapsedTime = ElapsedTime / nSimulationUpdates;

    Thread gameThread;

    Pool poolGame;

    public GamePanel() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(Color.gray);
        this.setDoubleBuffered(true);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.setFocusable(true);
        createCompartments();
        poolGame = new Pool(playArea.getCompartmentWidth(), playArea.getCompartmentHeight());
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

            g2.fillArc(playAreaX + paintXPos,playAreaY + paintYPos, diameter, diameter, 0, 360);
        }

        //---------Paint Edges-----------
        for (Edge edge : poolGame.edges) {
            g2.setColor(Color.BLACK);
            int paintPosStartX = (int) edge.getStart().x - edge.radius;
            int paintPosStartY = (int) edge.getStart().y - edge.radius;
            int paintPosEndX = (int) edge.getEnd().x - edge.radius;
            int paintPosEndY = (int) edge.getEnd().y - edge.radius;
            int diameter = edge.radius * 2;

            g2.drawArc(playAreaX + paintPosStartX, playAreaY + paintPosStartY, diameter, diameter, 0, 360 );
            g2.drawArc(playAreaX + paintPosEndX, playAreaY + paintPosEndY, diameter, diameter, 0, 360 );

            //g2.drawLine(playAreaX + (int) edge.getStart().x, playAreaY + (int) edge.getStart().y, playAreaX + (int) edge.getEnd().x, playAreaY + (int) edge.getEnd().y);
            g2.drawLine(playAreaX + (int) edge.getStartNormalOne().x, playAreaY + (int) edge.getStartNormalOne().y, playAreaX + (int) edge.getEndNormalOne().x, playAreaY + (int) edge.getEndNormalOne().y);
            g2.drawLine(playAreaX + (int) edge.getStartNormalTwo().x, playAreaY + (int) edge.getStartNormalTwo().y, playAreaX + (int) edge.getEndNormalTwo().x, playAreaY + (int) edge.getEndNormalTwo().y);


        }

        //---------Paint Pockets
        g2.setColor(Color.green);
        for (Pocket.Position position: poolGame.pockets.keySet()) {
            Pocket pocket = poolGame.pockets.get(position);
            int paintPointX = (int) pocket.getPositionVec().x - pocket.radius;
            int paintPointY = (int) pocket.getPositionVec().y - pocket.radius;
            int diameter = pocket.radius * 2;
            g2.fillArc(playAreaX + paintPointX, playAreaY + paintPointY, diameter, diameter, 0, 360);
        }

        //Draw Cue
        g2.setColor(Color.black);
        if(poolGame.gameState == GameState.TAKING_SHOT) {
            Vector2D[] cueVertices = poolGame.cue.getCueVertices();
            int[] xPoints = new int[4];  
            int[] yPoints = new int[4];  
            for(int i = 0; i < 4; i++) {
                xPoints[i] = playAreaX + ((int) cueVertices[i].x);
                yPoints[i] = playAreaY + ((int) cueVertices[i].y);
            }
            g2.drawPolygon(xPoints, yPoints, 4);

            //Draw Hitbox (temporarily)
            g2.setColor(Color.red);
            Vector2D[] cueHitboxVertices = poolGame.cue.getCueHitboxVertices();
            int[] hitboxXPoints = new int[4];
            int[] hitboxYPoints = new int[4];
            for(int j = 0; j < 4; j++) {
                hitboxXPoints[j] = playAreaX + ((int) cueHitboxVertices[j].x);
                hitboxYPoints[j] = playAreaY + ((int) cueHitboxVertices[j].y);
            }
            g2.drawPolygon(hitboxXPoints, hitboxYPoints, 4);
        }
        
    


        //---------Paint Program Compartments-----------

        //Play area border
        g2.setColor(Color.white);
        for (Compartment c : compartments) {
            g2.fillRect(c.getCompartmentX() - 5, c.getCompartmentY() - 5, c.getCompartmentWidth() + 10, 5);
            g2.fillRect(c.getCompartmentX() - 5, c.getCompartmentY(), 5, c.getCompartmentHeight());
            g2.fillRect(c.getCompartmentX() + c.getCompartmentWidth(), c.getCompartmentY(), 5, c.getCompartmentHeight());
            g2.fillRect(c.getCompartmentX() - 5, c.getCompartmentY() + c.getCompartmentHeight(), c.getCompartmentWidth() + 10, 5);
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
        if (playArea.containsMouse(e.getX(), e.getY())){
            //Shift coordinates to fit pool game's (0,0) coordinate system
            int xGameClick = e.getX() - playArea.getCompartmentX();
            int yGameClick = e.getY() - playArea.getCompartmentY();

            //LEFT CLICK - Static Collisions or (TEMPORARILY!) moving edges
            if(e.getButton() == MouseEvent.BUTTON1) {
                //Select ball for drag event - adjust for
                for(Ball b : poolGame.balls) {
                    if (poolGame.ballClicked(b, xGameClick, yGameClick)) {
                        b.selected = true;
                    }
                }

                for (Edge ed : poolGame.edges) {
                    ed.checkClicked(xGameClick, yGameClick);
                }
            //RIGHT CLICK - Dynamic Collisions
            } else if (e.getButton() == MouseEvent.BUTTON3) {
                //Adjust click coordinates to reflect play area's (0,0) coordinate system
                //Give pool game mouse down coordinates for shooting functionality
                poolGame.mouseDownX = xGameClick;
                poolGame.mouseDownY = yGameClick;
            }
        }
    }

    //Will need adjusting for just white ball dragging
    @Override
    public void mouseDragged(MouseEvent e) {
        if (playArea.containsMouse(e.getX(), e.getY())) {
            //Shift coordinates to fit pool game's (0,0) coordinate system
            int xGameClick = e.getX() - playArea.getCompartmentX();
            int yGameClick = e.getY() - playArea.getCompartmentY();

            for (Ball b : poolGame.balls) {
                if(b.selected) {
                    b.position.x = xGameClick;
                    b.position.y = yGameClick;
                }
            }

            for (Edge ed : poolGame.edges) {
                if(ed.startSelected) {
                    Vector2D newStart = new Vector2D(xGameClick, yGameClick);
                    ed.setStart(newStart);
                } else if (ed.endSelected) {
                    Vector2D newEnd = new Vector2D(xGameClick, yGameClick);
                    ed.setEnd(newEnd);
                }
            }
        }

    }

    @Override
    public void mouseReleased(MouseEvent e) {
            int xGameClick = e.getX() - playArea.getCompartmentX();
            int yGameClick = e.getY() - playArea.getCompartmentY();

            //LEFT CLICK - Static Collisions
            if(e.getButton() == MouseEvent.BUTTON1) {
                //Deselect Ball
                for (Ball b : poolGame.balls) {
                    if (b.selected) {
                        b.selected = false;
                    }
                }

                //Deselect Edges
                for (Edge ed : poolGame.edges) {
                    ed.startSelected = false;
                    ed.endSelected = false;
                }
            //RIGHT CLICK - Dynamic Collisions
            } else if (e.getButton() == MouseEvent.BUTTON3) {
                //Adjust click coordinates to reflect play area's (0,0) coordinate system
                poolGame.mouseUpX = xGameClick;
                poolGame.mouseUpY = yGameClick;
                poolGame.ballShoot();
            }


    }



    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {
        if (playArea.containsMouse(e.getX(), e.getY())) {
            int xGameClick = e.getX() - playArea.getCompartmentX();
            int yGameClick = e.getY() - playArea.getCompartmentY();
            poolGame.setMouseX(xGameClick);
            poolGame.setMouseY(yGameClick);
            poolGame.setMouseFocused(true);
        } else {
            poolGame.setMouseFocused(false);
        }
    }

    private void createCompartments() {
        compartments = new ArrayList<>();
        playArea = new Compartment(65, 10, 1080, 540);
        compartments.add(playArea);

        Compartment powerArea = new Compartment(10, 10, 40, 540);
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



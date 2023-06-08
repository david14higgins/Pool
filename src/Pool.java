import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;


public class Pool {
    //Game information
    private int gameWidth;
    private int gameHeight;

    //Ball data structures
    public ArrayList<Ball> balls;
    public Ball[] redBalls = new Ball[7];
    public Ball[] yellowBalls = new Ball[7];
    //At least need white ball field for cue to update its position
    public Ball whiteBall;
    public Ball blackBall;

    //Data structure for handling collisions
    public ArrayList<Ball[]> collidingPairs;

    //Aming cue data structure
    public AimingCue aimingCue;

    //Line segment data structures
    public ArrayList<Edge> edges;

    //Pockets data structure
    public HashMap<Pocket.Position, Pocket> pockets;

    public static final double DRAG = 0.8;

    public boolean ready = false;

    public double mouseDownX;
    public double mouseDownY;

    public double mouseUpX;
    public double mouseUpY;

    private int mouseX;
    private int mouseY;

    public GameState gameState;

    private final int maxShotPower = 2000;


    public Pool(int width, int height) {
        this.gameWidth = width;
        this.gameHeight = height;
        this.gameState = GameState.TAKING_SHOT;

        createBalls();
        createAimingCue();
        createPockets();
        createCushions();
        
        //Don't start game loop until balls and edges set up
        ready = true;
    }

    private void createBalls() {
        //Create balls
        balls = new ArrayList<>();
        
        int ballRadius = 12 ;

        //White ball
        whiteBall = new Ball(ballRadius, new Vector2D(3 * gameWidth / 4, gameHeight / 2), Ball.BallColours.White);
        balls.add(whiteBall);

        //The following positions balls in their triangle 

        //Order of balls going left to right, top to bottom in triangle
        String ballOrder = "YRYYBRRYRYYRRYR";
        //Tracks progress for assigning colour
        int orderProgress = 0; 

        //How distant adjacent columns x values are
        int ballsDistanceX = (int) Math.ceil(Math.sqrt(3) * ballRadius); 

        //This variable updates for setting the ball position
        Vector2D ballPosition = new Vector2D((gameWidth / 4) + (2 * ballsDistanceX), gameHeight / 2);

        //Iterate right to left in triangle 
        for (int i = 0; i < 5; i++) {
            //Iterate each column in the triangle top to bottom 
            for (int j = 0; j <= i; j++) {
                //Pick ball colour - bit long winded, can probably be improved
                char ballColourChar = ballOrder.charAt(orderProgress);
                Ball.BallColours ballColour = null; 
                switch(ballColourChar) {
                    case 'Y': ballColour = Ball.BallColours.Yellow; break;
                    case 'R': ballColour = Ball.BallColours.Red; break; 
                    case 'B': ballColour = Ball.BallColours.Black; break; 
                }
                //Create ball, add to data structure and update progress 
                Ball ball = new Ball(ballRadius, new Vector2D(ballPosition.x, ballPosition.y), ballColour);
                balls.add(ball);
                orderProgress += 1;

                //Recalculate position variable for the next ball 
                //For balls in the same column, we just move down the distance of one ball 
                ballPosition.y = ballPosition.y + (2 * ballRadius);
            }
            //When moving to the next column, update the x value by the special x distance we calculated (pythag)
            ballPosition.x = ballPosition.x - ballsDistanceX;
            //Also reset y to new height
            ballPosition.y = (gameHeight / 2) - ((i + 1) * ballRadius); 
        }
    }

    private void createAimingCue() {
        //Create Cue and initialize 
        aimingCue = new AimingCue(300, 4);
        aimingCue.whiteBallPos = whiteBall.position;
        aimingCue.initializeCue();
    }

    private void createPockets() {
        //Create Pockets
        int cornerRadius = 25;
        int middleRadius = 20;
        pockets = new HashMap<>();
        Pocket.Position[] positions = Pocket.Position.values();
        for (Pocket.Position position : positions) {
            if (position == Pocket.Position.UpperMiddle || position == Pocket.Position.BottomMiddle) {
                Pocket pocket = new Pocket(position, middleRadius, gameWidth, gameHeight);
                pockets.put(position, pocket);
            } else {
                Pocket pocket = new Pocket(position, cornerRadius, gameWidth, gameHeight);
                pockets.put(position, pocket);
            }
        }
    }

    private void createCushions() {
        edges = new ArrayList<>();

        //Create Cushions
        int edgeRadius = 6;
        
        //Retrieve two pockets for information purposes
        Pocket ul = pockets.get(Pocket.Position.UpperLeft);
        Pocket um = pockets.get(Pocket.Position.UpperMiddle);
 
        CushionsInfo info = new CushionsInfo(ul.radius, um.radius, edgeRadius, gameWidth, gameHeight);

        Vector2D cushionEndCoords[] = info.getCushionCoords();

        for (int i = 0; i < cushionEndCoords.length - 1; i++) {
            Vector2D start = cushionEndCoords[i];
            Vector2D end = cushionEndCoords[i+1];
            Edge edge = new Edge(start.x, start.y, end.x, end.y, edgeRadius);
            edges.add(edge);
        }

        //Add closing edge 
        Vector2D start = cushionEndCoords[cushionEndCoords.length - 1];
        Vector2D end = cushionEndCoords[0];
        Edge edge = new Edge(start.x, start.y, end.x, end.y, edgeRadius);
        edges.add(edge);
    }


    public void update() {
    
        //Reset on every update
        collidingPairs = new ArrayList<>();

        for (int n = 0; n < GamePanel.nSimulationUpdates; n++) {
            //Move the balls
            moveBalls();
            //Check for collisions
            checkForCollision();
            //Static Collisions
            handleStaticCollisions();
            //Dynamic Collisions
            handleDynamicCollisions();
            //Clear colliding balls
            collidingPairs.clear();
            //Check balls have stopped moving
            checkBallsStationary();
        }
    }

    public void moveBalls() {
        for (Ball b : balls) {
            //Add drag to ball to emulate friction
            b.acceleration.x = -1*b.velocity.x * Pool.DRAG;
            b.acceleration.y = -1*b.velocity.y * Pool.DRAG;

            //Update ball velocity based on acceleration
            b.velocity.x += b.acceleration.x * GamePanel.fSimElapsedTime;
            b.velocity.y += b.acceleration.y * GamePanel.fSimElapsedTime;

            //Update ball position based on velocity
            b.position.x += b.velocity.x * GamePanel.fSimElapsedTime;
            b.position.y += b.velocity.y * GamePanel.fSimElapsedTime;

            //Wrap ball around screen (for now)
            if (b.position.x < 0) {b.position.x += gameWidth;}
            if (b.position.x >= gameWidth) {b.position.x -= gameWidth;}
            if (b.position.y < 0) {b.position.y += gameHeight;}
            if (b.position.y >= gameHeight) {b.position.y -= gameHeight;}

            //Clamp velocity near zero
            if (Math.pow(b.velocity.x, 2) + Math.pow(b.velocity.y, 2) < 0.1) {
                b.velocity.x = 0;
                b.velocity.y = 0;
            }
        }
    }


    //Check for collisions between balls or with balls and edges
    private void checkForCollision() {
        for(int i = 0; i < balls.size(); i++) {
            for (Edge edge : edges) {
                checkForEdgeCollision(edge, balls.get(i));
            }

            //Check for collisions between different balls
            for(int j = i + 1; j < balls.size(); j++) {
                if(i != j) {
                    checkForBallsCollision(balls.get(i), balls.get(j));
                }

            }
        }
    }

    //We check and handle in one method since values computed in the check are useful in the handling
    private void checkForEdgeCollision(Edge edge, Ball currentBall) {
        double lineX1 = edge.getEnd().x - edge.getStart().x;
        double lineY1 = edge.getEnd().y - edge.getStart().y;

        double lineX2 = currentBall.position.x - edge.getStart().x;
        double lineY2 = currentBall.position.y - edge.getStart().y;

        double edgeLength = lineX1 * lineX1 + lineY1 * lineY1;

        double t = Math.max(0, Math.min(edgeLength, (lineX1 * lineX2 + lineY1 * lineY2))) / edgeLength;

        Vector2D closestPoint = new Vector2D(edge.getStart().x + t * lineX1, edge.getStart().y + t * lineY1);

        double distance = Math.sqrt(Math.pow((currentBall.position.x - closestPoint.x), 2) + Math.pow((currentBall.position.y - closestPoint.y), 2));

        if (distance <= currentBall.radius + edge.radius) {
            handleEdgeCollision(edge, currentBall, distance, closestPoint);
        }
    }

    private void handleEdgeCollision(Edge edge, Ball ball, double distance, Vector2D closestPoint) {
        //Create a fake ball for collision
        //Ball fakeBall = new Ball(closestPoint.x, closestPoint.y, edge.radius, -1, null);
        Ball fakeBall = new Ball(edge.radius, new Vector2D(closestPoint.x, closestPoint.y), null);
        fakeBall.mass = ball.mass;
        fakeBall.velocity.x = -1 * ball.velocity.x;
        fakeBall.velocity.y = -1 * ball.velocity.y;

        collidingPairs.add(new Ball[] {ball, fakeBall});

        double overlap = (distance - ball.radius - fakeBall.radius);

        ball.position.x -= overlap * (ball.position.x - fakeBall.position.x) / distance;
        ball.position.y -= overlap * (ball.position.y - fakeBall.position.y) / distance;
    }

    private void checkBallsStationary() {
        boolean allStationary = true;
        for(Ball ball : balls) {
            if(!ball.isStationary()) {
                allStationary = false;
            }
        }
        if(allStationary) { 
            gameState = GameState.TAKING_SHOT;
            aimingCue.repositionCue();
        }
    }

    private void checkForBallsCollision(Ball ball1, Ball ball2) {
        Ball[] ballsPair = new Ball[] {ball1, ball2};
        if(Math.pow(ball1.position.x - ball2.position.x, 2) + Math.pow(ball1.position.y - ball2.position.y, 2) <= Math.pow(ball1.radius + ball2.radius,2)) {
            collidingPairs.add(ballsPair);
        }
    }

    private void handleStaticCollisions() {
        for (Ball[] collidingPair : collidingPairs) {
            Ball ball1 = collidingPair[0];
            Ball ball2 = collidingPair[1];

            double distance = Math.sqrt(Math.pow(ball1.position.x - ball2.position.x, 2) + Math.pow(ball1.position.y - ball2.position.y, 2));
            double overlap = 0.5 * (distance - ball1.radius - ball2.radius);

            ball1.position.x -= overlap * (ball1.position.x - ball2.position.x) / distance;
            ball1.position.y -= overlap * (ball1.position.y - ball2.position.y) / distance;

            ball2.position.x += overlap * (ball1.position.x - ball2.position.x) / distance;
            ball2.position.y += overlap * (ball1.position.y - ball2.position.y) / distance;
        }
    }

    private void handleDynamicCollisions() {
        for (Ball[] collidingPair : collidingPairs) {
            //This will always be an array with two balls
            Ball ball1 = collidingPair[0];
            Ball ball2 = collidingPair[1];

            double totalMass = ball1.mass + ball2.mass;
            Vector2D x1_x2 = Vector2D.subtract(ball1.position, ball2.position);
            Vector2D x2_x1 = Vector2D.subtract(ball2.position, ball1.position);
            Vector2D v1_v2 = Vector2D.subtract(ball1.velocity, ball2.velocity);
            Vector2D v2_v1 = Vector2D.subtract(ball2.velocity, ball1.velocity);
            double v1_scalar = 2*ball2.mass / totalMass * Vector2D.dotProduct(v1_v2, x1_x2) / Math.pow(Vector2D.magnitude(x1_x2), 2);
            double v2_scalar = 2*ball1.mass / totalMass * Vector2D.dotProduct(v2_v1, x2_x1) / Math.pow(Vector2D.magnitude(x2_x1), 2);

            ball1.velocity = Vector2D.subtract(ball1.velocity, Vector2D.scalar(v1_scalar, x1_x2));
            ball2.velocity = Vector2D.subtract(ball2.velocity, Vector2D.scalar(v2_scalar, x2_x1));
        }
    }
    //Should this be public?
    public boolean ballClicked(Ball ball, double mouseX, double mouseY) {
        return Math.sqrt(Math.pow(ball.position.x - mouseX, 2) + Math.pow(ball.position.y - mouseY, 2)) <= ball.radius;
    }


    //Probably needs changing
    public void takeShot(double normalizedShotPower) {
        whiteBall.velocity.x = normalizedShotPower * maxShotPower * aimingCue.getCueDirection().x;
        whiteBall.velocity.y = normalizedShotPower * maxShotPower * aimingCue.getCueDirection().y;
        gameState = GameState.BALLS_MOVING;
    }

    //Getters and Setters


    public int getMouseX() {
        return mouseX;
    }

    public void setMouseX(int mouseX) {
        this.mouseX = mouseX;
    }

    public int getMouseY() {
        return mouseY;
    }

    public void setMouseY(int mouseY) {
        this.mouseY = mouseY;
    }

}

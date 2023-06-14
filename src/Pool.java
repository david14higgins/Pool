import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;



import java.awt.Polygon;


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
    private int ballRadius; 

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

    public ShotPredictor shotPredictor; 

    //Pocketed balls must be removed from the game instantly, but processed at the end of the shot, hence the separate data structures 
    private ArrayList<Ball> ballsToRemove; 
    private ArrayList<Ball> pocketedBallsToProcess; 

    public CushionsInfo cushionsInfo; 

    public int numYellowBallsPocketed = 0; 
    public int numRedBallsPocketed = 0; 
    public boolean broken; 
    public boolean decided; 
    public boolean playerOneRed; 
    public boolean playerOneTurn; 
    public boolean mayDragWhiteBall; 
    public boolean whiteBallBeingDragged;
    public boolean firstBallHitBool; 
    public boolean blackPocketedByP1;
    public boolean blackPocketedByP2;
    public boolean gameOver;


    private Ball firstBallHitBall; 

    public String outputMessage; 

    public Pool(int width, int height) {
        this.gameWidth = width;
        this.gameHeight = height;
        this.gameState = GameState.TAKING_SHOT;

        createBalls();
        createAimingCue();
        createPockets();
        createCushions();
        createShotPredictor();

        //Instantiate arraylists 
        collidingPairs = new ArrayList<>();
        ballsToRemove = new ArrayList<>();
        pocketedBallsToProcess = new ArrayList<>(); 

        initalizeGameSettings(); 
        
        //Don't start game loop until game is prepared
        ready = true;
    }

    private void initalizeGameSettings() {
        //Balls haven't been broken yet
        broken = false; 
        //Colours haven't been decided yet
        decided = false; 
        //Player one starts
        playerOneTurn = true; 
        //May move white ball before break 
        mayDragWhiteBall = true;
        //White ball not dragged yet 
        whiteBallBeingDragged = false;
        //A ball has not been hit first yet 
        firstBallHitBool = false; 
        //Neither player has pocketed black ball yet 
        blackPocketedByP1 = false; 
        blackPocketedByP2 = false; 
        //Game not over 
        gameOver = false; 
        //Starting output message 
        outputMessage = "Player One to break. You may move the white ball";


        //TEMP 
        playerOneRed = true;
        playerOneTurn = true;
    }

    private void createBalls() {
        //Create balls
        balls = new ArrayList<>();
        
        ballRadius = 12;

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

    private void createShotPredictor() {
        shotPredictor = new ShotPredictor(); 
        updateShotPrediction();
    }

    private void createPockets() {
        //Create Pockets
        int cornerRadius = 25;
        int middleRadius = 20;
        pockets = new HashMap<>();
        Pocket.Position[] positions = Pocket.Position.values();
        for (Pocket.Position position : positions) {
            //Centre pockets have a smaller radius
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
 
        cushionsInfo = new CushionsInfo(ul.radius, um.radius, edgeRadius, gameWidth, gameHeight);

        Vector2D cushionEndCoords[] = cushionsInfo.getCushionCoords();

        //first and last element of above array is the same so straightforward loop can be used 
        for (int i = 0; i < cushionEndCoords.length - 1; i++) {
            Vector2D start = cushionEndCoords[i];
            Vector2D end = cushionEndCoords[i+1];
            Edge edge = new Edge(start.x, start.y, end.x, end.y, edgeRadius);
            edges.add(edge);
        }

    }


    public void update() {
    
        if (gameState == gameState.BALLS_MOVING) {
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
                //Finds any balls that have been pockets 
                checkPocketed(); 
                //Processes all pocketed balls (depending on colour and game state)
                removePocketedBalls();
                //Clear arraylist that tracks which balls to process
                ballsToRemove.clear();
                //Check balls have stopped moving
                checkBallsStationary();
            }
        } else if(gameState == gameState.PREPARE_TO_TAKE_SHOT) {
            aimingCue.repositionCue();
            updateShotPrediction();
            firstBallHitBall = null; 
            firstBallHitBool = false; 
            gameState = gameState.TAKING_SHOT;
        } else if(gameState == gameState.TAKING_SHOT) {
            aimingCue.repositionCue();
        } else if (gameState == gameState.PROCESSING_SHOT) {
            processShot();
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
                checkForEdgeCollision(edge, balls.get(i), true);
            }

            //Check for collisions between different balls
            for(int j = i + 1; j < balls.size(); j++) {
                if(i != j) {
                    Ball ball1 = balls.get(i); 
                    Ball ball2 = balls.get(j); 
                    boolean collision = checkForBallsCollision(ball1, ball2, true);
                    //Assign first ball hit information (for processing the shot later)
                    if(collision && !firstBallHitBool) {
                        if(ball1.colour == Ball.BallColours.White) {
                            firstBallHitBall = ball2; 
                            firstBallHitBool = true; 
                        } else if (ball2.colour == Ball.BallColours.White) {
                            firstBallHitBall = ball1; 
                            firstBallHitBool = true; 
                        }
                    }
                }
            }
        }
    }

    //We check and handle in one method since values computed in the check are useful in the handling
    private boolean checkForEdgeCollision(Edge edge, Ball currentBall, boolean handle) {
        //Handle declares if we want to handle the collision or just detect it

        double lineX1 = edge.getEnd().x - edge.getStart().x;
        double lineY1 = edge.getEnd().y - edge.getStart().y;

        double lineX2 = currentBall.position.x - edge.getStart().x;
        double lineY2 = currentBall.position.y - edge.getStart().y;

        double edgeLength = lineX1 * lineX1 + lineY1 * lineY1;

        double t = Math.max(0, Math.min(edgeLength, (lineX1 * lineX2 + lineY1 * lineY2))) / edgeLength;

        Vector2D closestPoint = new Vector2D(edge.getStart().x + t * lineX1, edge.getStart().y + t * lineY1);

        double distance = Math.sqrt(Math.pow((currentBall.position.x - closestPoint.x), 2) + Math.pow((currentBall.position.y - closestPoint.y), 2));

        if (distance <= currentBall.radius + edge.radius) {
            if (handle) {
                handleEdgeCollision(edge, currentBall, distance, closestPoint);
            }
            return true; 
        } else {
            return false;
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
            gameState = GameState.PROCESSING_SHOT;
        }
    }

    private boolean checkForBallsCollision(Ball ball1, Ball ball2, boolean handle) {
        //Handle declares if we want to handle the collision or just detect it
        Ball[] ballsPair = new Ball[] {ball1, ball2};
        if(Math.pow(ball1.position.x - ball2.position.x, 2) + Math.pow(ball1.position.y - ball2.position.y, 2) <= Math.pow(ball1.radius + ball2.radius,2)) {
            if (handle) {
                collidingPairs.add(ballsPair);
            }
            return true;
        } else {
            return false;
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

    //Checks and handles balls which have been pocketed
    private void checkPocketed() {
        //Iterate balls and pockets
        for (Ball ball : balls) {
            for (Pocket pocket : pockets.values()) {
                if(pocket.hasPocketed(ball)) {
                    /*If the ball has been pocketed, we change its velocity so heads towards the centre of the pocket 
                    and we reduce its size. It will stay "pocketed" and give the illusion of falling. When it's size 
                    is nothing, we can begin the process of removing it from the game*/
                    Vector2D newVel = new Vector2D(10 * (pocket.getPositionVec().x - ball.position.x), 10 * (pocket.getPositionVec().y - ball.position.y));
                    ball.velocity = newVel;
                    ball.radius = ball.radius - 0.2;
                    if(ball.radius <= 0) {
                        ballsToRemove.add(ball);
                        pocketedBallsToProcess.add(ball); 
                    }
                }
            }
        }
    }

    //Removes pocketed balls and updates settings for the pocketed balls counters
    private void removePocketedBalls() {
        //Remove balls from the game 
        for (Ball ball : ballsToRemove) {
            if(ball.colour == Ball.BallColours.Red) {
                numRedBallsPocketed += 1;
                balls.remove(ball);
            } else if (ball.colour == Ball.BallColours.Yellow) {
                numYellowBallsPocketed += 1;
                balls.remove(ball);
            } else if(ball.colour == Ball.BallColours.Black) {
                if(playerOneTurn) {
                    blackPocketedByP1 = true; 
                } else {
                    blackPocketedByP2 = true;
                }
                balls.remove(ball);
            } else { //White ball
                balls.remove(ball);
            }
            
        }
    }

    private void processShot() { 
        boolean processed = false; 
        processed = earlyBlackBallPocketed();        
        //The above scenario is the only one that concerns itself with the broken boolean

        //These methods check for certain outcomes. Results of earlier checks becomes assumptions in later checks to reduce work 
        //e.g. if checks after "noBallsHit()" are performed, this implies a ball has been hit and other checks do not need to worry about this
        if(!broken) {broken = true;}
        if(!processed) {processed = blackBallNotPocketedAlone();}
        if(!processed) {processed = blackBallPocketedIndirectly();}
        if(!processed) {processed = blackBallPocketedCorrectly();}
        if(!processed) {processed = pocketWhiteBallOnly();}
        if(!processed) {processed = noBallsHit();}
        if(!processed) {processed = blackBallHitEarly();}
        if(!processed) {processed = unpocketedShot();}
        

        pocketedBallsToProcess.clear();
        gameState = GameState.PREPARE_TO_TAKE_SHOT; 
    }

    //The following are a series of methods to deal with different situations that might have occured 
    private boolean earlyBlackBallPocketed() {
        for (Ball ball : pocketedBallsToProcess) {
            if (ball.colour == Ball.BallColours.Black) {
                //Black ball pocketed during break
                if (!broken) {
                    gameOver = true; 
                    outputMessage = "Black ball pocketed during break. Click anywhere to re-rack";
                    return true; 
                } else {
                    //Check player one pocketed early
                    if(playerOneTurn && ((playerOneRed && numRedBallsPocketed < 7) || (!playerOneRed && numYellowBallsPocketed < 7))) { 
                        gameOver = true; 
                        outputMessage = "Player one pocketed black early. Player two wins!";
                        return true; 
                    } //Check player two pocketed early 
                    else if (!playerOneTurn && ((playerOneRed && numYellowBallsPocketed < 7) || (!playerOneRed && numRedBallsPocketed < 7))) {
                        gameOver = true; 
                        outputMessage = "Player two pocketed black early. Player one wins!";
                        return true;
                    }
                }
            }
        }
        //None of the above scenarios have occured 
        return false; 
    }

    //Potting the black ball along with any other ball in a shot is considered a foul that loses the game 
    private boolean blackBallNotPocketedAlone() {
        for(Ball ball : pocketedBallsToProcess) {
            if (ball.colour == Ball.BallColours.Black) {
                if (pocketedBallsToProcess.size() >= 2) {
                    if(playerOneTurn) {
                        outputMessage = "Black ball not pocketed alone. Player two wins!";
                    } else {
                        outputMessage = "Black ball not pocketed alone. Player one wins!";
                    }
                    gameOver = true; 
                    return true; 
                }
            } 
        }
        return false; 
    }

    //The black ball was pocketed at the correct time, however, not through a direct shot 
    private boolean blackBallPocketedIndirectly() {
        if (pocketedBallsToProcess.size() == 1) {
            if (pocketedBallsToProcess.get(0).colour == Ball.BallColours.Black) {
                if (firstBallHitBool) {
                    if (firstBallHitBall.colour != Ball.BallColours.Black) {
                        if(playerOneTurn) {
                            outputMessage = "Black ball pocketed indirectly. Player two wins!";
                        } else {
                            outputMessage = "Black ball pocketed indirectly. Player one wins!";
                        }
                        gameOver = true; 
                        return true; 
                    }
                }
            }
        }
        return false; 
    }

    private boolean blackBallPocketedCorrectly() {
        if (pocketedBallsToProcess.size() == 1) {
            if (pocketedBallsToProcess.get(0).colour == Ball.BallColours.Black) {
                if (decided) {
                    if (playerOneTurn && ((playerOneRed && numRedBallsPocketed == 7) || (!playerOneRed && numYellowBallsPocketed == 7))) {
                        outputMessage = "Player one wins!";
                        gameOver = true; 
                        return true;
                    } else if (!playerOneTurn && ((!playerOneRed && numRedBallsPocketed == 7) || (playerOneRed && numYellowBallsPocketed == 7))) {
                        outputMessage = "Player two wins!";
                        gameOver = true; 
                        return true; 
                    }
                } 
            }
        }
        return false; 
    }

    //A foul shot where no balls were hit and opponent may move white ball anywhere 
    private boolean noBallsHit() {
        if (firstBallHitBool == false) {
            //Simply swap player turns and allow white ball to be moved anywhere
            if(playerOneTurn) {
                outputMessage = "No ball hit, player two you may move the white ball";
                playerOneTurn = false; 
            } else {
                outputMessage = "No ball hit, player one you may move the white bal";
                playerOneTurn = true;
            }
            mayDragWhiteBall = true; 
            return true; 
        }
        return false; 
    }

    //It is a foul shot if the black ball is hit whilst you still have coloured balls 
    private boolean blackBallHitEarly() {
        if(firstBallHitBall.colour == Ball.BallColours.Black) {
            //Check player one 
            if (playerOneTurn && ((playerOneRed && numRedBallsPocketed < 7) || (!playerOneRed && numYellowBallsPocketed < 7))) {
                outputMessage = "Black ball hit, player two you may move the white ball"; 
                playerOneTurn = false; 
                mayDragWhiteBall = true; 
                return true; 
            } else if (!playerOneTurn && ((playerOneRed && numYellowBallsPocketed < 7) || (!playerOneRed && numRedBallsPocketed < 7))) {
                outputMessage = "Black ball hit, player one you may move the white ball"; 
                playerOneTurn = true; 
                mayDragWhiteBall = true; 
                return true; 
            }
        }
        return false; 
    }

    //A shot where no balls are pocketed 
    private boolean unpocketedShot() {
        if (pocketedBallsToProcess.size() == 0) {
            //Simply swap player turns and but do not allow white ball to be moved anywhere
            if(playerOneTurn) {
                outputMessage = "Player two's turn";
                playerOneTurn = false; 
            } else {
                outputMessage = "Player one's turn";
                playerOneTurn = true;
            }
            mayDragWhiteBall = false; 
            return true; 
        }
        return false; 
    }

    //
    private boolean pocketWhiteBallOnly() {
        if (pocketedBallsToProcess.size() == 1) {
            if (pocketedBallsToProcess.get(0).colour == Ball.BallColours.White) {
                replaceWhiteBall();
                //reset aimingCue and shot predictor 
                aimingCue.initializeCue();
                aimingCue.whiteBallPos = whiteBall.position; 
                aimingCue.repositionCue();
                updateShotPrediction();
                if(playerOneTurn) {
                    outputMessage = "Potted white ball, player two you may move the white ball"; 
                    playerOneTurn = false; 
                } else {
                    outputMessage = "Potted white ball, player one you may move the white ball"; 
                    playerOneTurn = true; 
                }
                mayDragWhiteBall = true;
                return true; 
            }
        }
        return false;
    }

    //Place white ball back in its original place (or near)
    private void replaceWhiteBall() {
        whiteBall.radius = ballRadius; 
        Boolean validPosition = false; 
        int maxXDistance = 0; 
        int maxYDistance = 0; 
        while(!validPosition) {
            Boolean collision = false; 
            double xDistance = Math.random() * maxXDistance;
            double yDistance = Math.random() * maxYDistance - (maxYDistance / 2);
            whiteBall.position = new Vector2D(3 * gameWidth / 4 + xDistance, gameHeight / 2 + yDistance);
            for (Ball b : balls) {
                if(checkForBallsCollision(whiteBall, b, false)) {
                    collision = true; 
                }
            }
            validPosition = collision ? false : true; 
            maxXDistance += 10; 
            maxYDistance += 20; 
        }
        balls.add(whiteBall); 
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

    //Shot Prediction 
    public void updateShotPrediction() {
        //create fake white ball 
        Ball fakeWhiteBall = new Ball(whiteBall.radius, new Vector2D(whiteBall.position.x, whiteBall.position.y), null);
        boolean collision = false; 
        while(!collision) {
            Vector2D newPos = new Vector2D(fakeWhiteBall.position.x + aimingCue.getCueDirection().x, fakeWhiteBall.position.y + aimingCue.getCueDirection().y);
            fakeWhiteBall.position = newPos;
            
            for (Edge edge : edges) {
                if (checkForEdgeCollision(edge, fakeWhiteBall, false)) {
                    //Update shot predictor details 
                    collision = true;

                    shotPredictor.hittingBall = false;
                    shotPredictor.wbSource = new Vector2D(whiteBall.position.x, whiteBall.position.y);
                    shotPredictor.wbDestination = new Vector2D(fakeWhiteBall.position.x, fakeWhiteBall.position.y);
                }
            }

            for (Ball ball : balls) {
                if (ball.colour != Ball.BallColours.White) {
                    if(checkForBallsCollision(fakeWhiteBall, ball, false)) {
                        //Simulate a collision between the fake white ball and its target and update shot predictor details
                        collision = true;

                        shotPredictor.hittingBall = true; 
                        shotPredictor.wbSource = new Vector2D(whiteBall.position.x, whiteBall.position.y);
                        shotPredictor.wbDestination = new Vector2D(fakeWhiteBall.position.x, fakeWhiteBall.position.y);
                        shotPredictor.wbDirectionBefore = new Vector2D(aimingCue.getCueDirection().x, aimingCue.getCueDirection().y);
                        shotPredictor.targetBall = new Vector2D(ball.position.x, ball.position.y);
                        
                        
                        //Collision simulation
                        //(We only care about the direction after the 'collision' so we can give a fake velocity magnitude)
                        fakeWhiteBall.velocity = new Vector2D(100 * aimingCue.getCueDirection().x, 100 * aimingCue.getCueDirection().y);
                        double totalMass = fakeWhiteBall.mass + ball.mass;
                        Vector2D x1_x2 = Vector2D.subtract(fakeWhiteBall.position, ball.position);
                        Vector2D x2_x1 = Vector2D.subtract(ball.position, fakeWhiteBall.position);
                        Vector2D v1_v2 = Vector2D.subtract(fakeWhiteBall.velocity, ball.velocity);
                        Vector2D v2_v1 = Vector2D.subtract(ball.velocity, fakeWhiteBall.velocity);
                        double v1_scalar = 2*ball.mass / totalMass * Vector2D.dotProduct(v1_v2, x1_x2) / Math.pow(Vector2D.magnitude(x1_x2), 2);
                        double v2_scalar = 2*fakeWhiteBall.mass / totalMass * Vector2D.dotProduct(v2_v1, x2_x1) / Math.pow(Vector2D.magnitude(x2_x1), 2);

                        shotPredictor.wbDirectionAfter = Vector2D.unitVector(Vector2D.subtract(fakeWhiteBall.velocity, Vector2D.scalar(v1_scalar, x1_x2)));
                        shotPredictor.targetDirectionAfter = Vector2D.unitVector(Vector2D.subtract(ball.velocity, Vector2D.scalar(v2_scalar, x2_x1)));

                        shotPredictor.calculatePredictorPoints();
                    }
                }
            }
        }
        
    }

    //Updates white ball position with given position if move is legal (doesn't cause collision)
    public void dragWhiteBall(int newXPos, int newYPos) {
        if(mayDragWhiteBall) {
            //Save original position if new position is invalid
            double originalX = whiteBall.position.x; 
            double originalY = whiteBall.position.y; 

            //Try new position 
            whiteBall.position = new Vector2D(newXPos, newYPos);
        
            //Check collision with balls 
            for (Ball ball : balls) {
                if (ball.colour != Ball.BallColours.White) {
                    if(checkForBallsCollision(whiteBall, ball, false)) {
                        //Restore to original position 
                        whiteBall.position = new Vector2D(originalX, originalY);
                    }
                }
            }

            //Check collision with edges 
            for (Edge edge : edges) {
                if(checkForEdgeCollision(edge, whiteBall, false)) {
                    //Restore to original position 
                    whiteBall.position = new Vector2D(originalX, originalY);
                }
            }

            //Check not in pockets
            for (Pocket pocket : pockets.values()) {
                if (pocket.hasPocketed(whiteBall)) {
                    //Restore to original position 
                    whiteBall.position = new Vector2D(originalX, originalY);
                }
            }

            //Check not inside cushions
            for (Polygon cushionArea : cushionsInfo.getCushionPolygons()) {
                if (cushionArea.contains(whiteBall.position.x, whiteBall.position.y)) {
                    //Restore to original position 
                    whiteBall.position = new Vector2D(originalX, originalY);
                }
            }

            //Check inside playing area
            if (!(whiteBall.position.x >= 10 && whiteBall.position.x <= gameWidth - 10 && whiteBall.position.y >= 10 && whiteBall.position.y <= gameHeight - 10)) {
                //Restore to original position 
                whiteBall.position = new Vector2D(originalX, originalY);
            }

            //Check if in restricted quarter during break 
            if(!broken) {
                if (!(whiteBall.position.x >= 3 * gameWidth / 4 && whiteBall.position.x <= gameWidth - 10 && whiteBall.position.y >= 10 && whiteBall.position.y <= gameHeight - 10)) {
                //Restore to original position 
                whiteBall.position = new Vector2D(originalX, originalY);
            }
            }

            //Update aiming cue and shot predictor after white ball drag 
            aimingCue.whiteBallPos = whiteBall.position;
            aimingCue.repositionCue();
            updateShotPrediction();
        }
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

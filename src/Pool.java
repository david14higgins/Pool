import java.util.ArrayList;
import java.util.HashMap;
import java.awt.Polygon;

public class Pool {
    //Game information
    private int gameWidth;
    private int gameHeight;

    //Balls information
    private ArrayList<Ball> balls;
    private Ball whiteBall;
    private final int ballRadius = 12; 

    //Data structure for handling collisions
    private ArrayList<Ball[]> collidingPairs;

    //Aiming cue data structure
    private AimingCue aimingCue;

    //Line segment data structures
    private ArrayList<Edge> edges;

    //Pockets data structure
    private HashMap<Pocket.Position, Pocket> pockets;

    //Game settings 
    private static final double DRAG = 0.8;
    private final int maxShotPower = 2000;

    //Shot predictor object
    private ShotPredictor shotPredictor; 

    //Object contains information on cushion position
    private CushionsInfo cushionsInfo; 

    //Game state
    private GameState gameState;
    private boolean ready;
    private int numYellowBallsPocketed; 
    private int numRedBallsPocketed; 
    private boolean broken; 
    private boolean decided; 
    private boolean playerOneRed; 
    private boolean playerOneTurn; 
    private boolean mayDragWhiteBall; 
    private boolean whiteBallBeingDragged;
    private boolean firstBallHitBool; 
    private boolean blackPocketedByP1;
    private boolean blackPocketedByP2;
    private boolean gameOver;
    private Ball firstBallHitBall; 
    private String outputMessage; 


    //Data structures to aid with the processing and removal of pocketed balls  
    private ArrayList<Ball> ballsToRemove; 
    private ArrayList<Ball> pocketedBallsToProcess; 


    public Pool(int width, int height) {
        this.gameWidth = width;
        this.gameHeight = height;

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
        this.gameState = GameState.TAKING_SHOT;
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
        //No balls pocketed yet 
        numRedBallsPocketed = 0; 
        numYellowBallsPocketed = 0; 
        //Game not over 
        gameOver = false; 
        //Starting output message 
        outputMessage = "Player One to break. You may move the white ball";
        //Ready to play 
        ready = true; 
    }

    private void createBalls() {
        //Create balls
        balls = new ArrayList<>();
        
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
        aimingCue.setWhiteBallPos(whiteBall.position);
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
 
        //CushionsInfo object used for calculations 
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

    //Update the game based on its current state
    public void update() {
        if (gameState == GameState.BALLS_MOVING) {
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
        } else if(gameState == GameState.PREPARE_TO_TAKE_SHOT) {
            aimingCue.repositionCue();
            updateShotPrediction();
            firstBallHitBall = null; 
            firstBallHitBool = false; 
            gameState = GameState.TAKING_SHOT;
        } else if(gameState == GameState.TAKING_SHOT) {
            aimingCue.repositionCue();
        } else if (gameState == GameState.PROCESSING_SHOT) {
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

    //------ Used in early days of the app - static collisions will not be possible in the game ------
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

    //Checks a series of scenarios that could have taken place after a shot 
    //Each scenario has its own method which will update game settings if it has occured 
    private void processShot() { 
        boolean handled = false; 
        handled = blackPocketedOnBreak();        
        //The above scenario is the only one that concerns itself with the broken boolean
        if(!broken) {broken = true;}
        //These methods check for certain outcomes. Results of earlier checks becomes assumptions in later checks to reduce work 
        //e.g. if checks after "noBallsHit()" are performed, this implies a ball has been hit and other checks do not need to worry about this
        //Certain methods also act differently if player colours are yet to be assigned
        if(!handled) {handled = blackBallPocketedEarly();}
        if(!handled) {handled = blackBallNotPocketedAlone();}
        if(!handled) {handled = blackBallPocketedIndirectly();}
        if(!handled) {handled = blackBallPocketedCorrectly();}
        if(!handled) {handled = blackBallHitEarly();}
        if(!handled) {handled = whiteBallPocketedOnly();}
        if(!handled) {handled = whiteBallPocketedAmongOthers();}
        if(!handled) {handled = noBallsHit();}
        if(!handled) {handled = noBallsPocketed();}
        if(!handled) {handled = redBallsPocketedOnly();}
        if(!handled) {handled = yellowBallsPocketedOnly();}
        if(!handled) {handled = redsAndYellowsPocketed();}        
    
        pocketedBallsToProcess.clear();
        gameState = GameState.PREPARE_TO_TAKE_SHOT; 
    }

    //The following are a series of methods to deal with different situations that might have occured 
    private boolean blackPocketedOnBreak() {
        for (Ball ball : pocketedBallsToProcess) {
            if (ball.colour == Ball.BallColours.Black) {
                if(!broken) {
                    gameOver = true; 
                    outputMessage = "Black ball pocketed during break. Click anywhere to re-rack";
                    return true; 
                }
            }
        }
        return false; 
    }

    //The black ball has been pocketed before all of a player's coloured balls
    private boolean blackBallPocketedEarly() {
        for (Ball ball : pocketedBallsToProcess) {
            if (ball.colour == Ball.BallColours.Black) {
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

    //Checking for winning end-game scenario
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

    //It is a foul shot if the black ball is hit whilst you still have coloured balls 
    private boolean blackBallHitEarly() {
        if (firstBallHitBool) {
            if(firstBallHitBall.colour == Ball.BallColours.Black) {
                //Check player one 
                if (playerOneTurn && ((playerOneRed && numRedBallsPocketed < 7) || (!playerOneRed && numYellowBallsPocketed < 7))) {
                    if(!decided) {
                        outputMessage = "Black ball hit, player two you may move the white ball"; 
                    } else {
                        outputMessage = "Black ball hit, player two " + (playerOneRed ? "(yellow)" : "(red)") + " you may move the white ball";
                    }
                    playerOneTurn = false; 
                    mayDragWhiteBall = true; 
                    return true; 
                } else if (!playerOneTurn && ((playerOneRed && numYellowBallsPocketed < 7) || (!playerOneRed && numRedBallsPocketed < 7))) {
                    if (!decided) {
                        outputMessage = "Black ball hit, player one you may move the white ball"; 
                    } else {
                        outputMessage = "Black ball hit, player one " + (playerOneRed ? "(red)" : "(yellow)") + " you may move the white ball"; 
                    }
                    playerOneTurn = true; 
                    mayDragWhiteBall = true; 
                    return true; 
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
                if (!decided) {
                    outputMessage = "No ball hit, player two you may move the white ball";
                } else {
                    outputMessage = "No ball hit, player two " + (playerOneRed ? "(yellow)" : "(red)") + " you may move the white ball"; 
                }
                playerOneTurn = false; 
            } else {
                if (!decided) {
                    outputMessage = "No ball hit, player one you may move the white ball";
                } else {
                    outputMessage = "No ball hit, player one " + (playerOneRed ? "(red)" : "(yellow)") + " you may move the white ball"; 
                }
                playerOneTurn = true;
            }
            mayDragWhiteBall = true; 
            return true; 
        }
        return false; 
    }

    //A shot where no balls are pocketed 
    private boolean noBallsPocketed() {
        if (pocketedBallsToProcess.size() == 0) {
            //If colours decided, need to ensure their own colour was hit, otherwise no foul
            if(decided) {
                if(playerOneTurn) {
                    if(playerOneRed) {
                        if(firstBallHitBall.colour == Ball.BallColours.Red) { //no foul
                            outputMessage = "Player two's turn (yellow)";
                            mayDragWhiteBall = false; 
                        } else {
                            outputMessage = "Hit opponent's ball. Player two (yellow), you may move the white ball"; 
                            mayDragWhiteBall = true; 
                        }
                        playerOneTurn = false; 
                        return true;
                    } else { //Player one yellow
                        if(firstBallHitBall.colour == Ball.BallColours.Yellow) { //no foul
                            outputMessage = "Player two's turn (red)";
                            mayDragWhiteBall = false; 
                        } else {
                            outputMessage = "Hit opponent's ball. Player two (red), you may move the white ball"; 
                            mayDragWhiteBall = true; 
                        }
                        playerOneTurn = false; 
                        return true;
                    }
                } else { //Player two's turn 
                    if(!playerOneRed) { //Player two red
                        if(firstBallHitBall.colour == Ball.BallColours.Red) { //no foul
                            outputMessage = "Player one's turn (yellow)";
                            mayDragWhiteBall = false; 
                        } else {
                            outputMessage = "Hit opponent's ball. Player one (yellow), you may move the white ball"; 
                            mayDragWhiteBall = true; 
                        }
                        playerOneTurn = true; 
                        return true;
                    } else { //Player two yellow
                        if(firstBallHitBall.colour == Ball.BallColours.Yellow) { //no foul
                            outputMessage = "Player one's turn (red)";
                            mayDragWhiteBall = false; 
                        } else {
                            outputMessage = "Hit opponent's ball. Player one (red), you may move the white ball"; 
                            mayDragWhiteBall = true; 
                        }
                        playerOneTurn = true; 
                        return true;
                    }
                }
            } else { //Undecided - simply swap turns 
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
        }
        return false; 
    }

    //A shot whereby only a white ball went in 
    private boolean whiteBallPocketedOnly() {
        if (pocketedBallsToProcess.size() == 1) {
            if (pocketedBallsToProcess.get(0).colour == Ball.BallColours.White) {
                replaceWhiteBall();
                if(playerOneTurn) {
                    if (!decided) {
                        outputMessage = "Potted white ball, player two you may move the white ball"; 
                    } else {
                        outputMessage = "Potted white ball, player two " + (playerOneRed ? "(yellow)" : "(red)") + " you may move the white ball"; 
                    }
                    playerOneTurn = false; 
                } else {
                    if (!decided) {
                        outputMessage = "Potted white ball, player one you may move the white ball";
                    } else {
                        outputMessage = "Potted white ball, player one " + (playerOneRed ? "(red)" : "(yellow)") + " you may move the white ball";
                    }
                    playerOneTurn = true; 
                }
                mayDragWhiteBall = true;
                return true; 
            }
        }
        return false;
    }

    //A shot whereby multiple colours went in, including a white ball 
    //Could be the shot where player colours are allocated 
    private boolean whiteBallPocketedAmongOthers() {
        for (Ball b : pocketedBallsToProcess) {
            if (b.colour == Ball.BallColours.White) {
                replaceWhiteBall();

                //A white ball has been potted. The first non white ball potted must either be the first or second ball in the array list
                Ball otherColouredBall;
                if(!(pocketedBallsToProcess.get(0).colour == Ball.BallColours.White)) {
                    otherColouredBall = pocketedBallsToProcess.get(0);
                } else {
                    otherColouredBall = pocketedBallsToProcess.get(1);
                }

                if (decided) {
                    if(playerOneTurn) {
                    outputMessage = "Potted white ball, player two " + (playerOneRed ? "(yellow)" : "(red)") + " you may move the white ball"; 
                    playerOneTurn = false; 
                    } else {
                        outputMessage = "Potted white ball, player one " + (playerOneRed ? "(red)" : "(yellow)") + " you may move the white ball"; 
                        playerOneTurn = true; 
                    }
                    mayDragWhiteBall = true;
                    return true; 
                } else { //Could be undecided so we assign values based on the colour of the first ball potted 
                    if(playerOneTurn && otherColouredBall.colour == Ball.BallColours.Red) {
                        playerOneRed = true; 
                        playerOneTurn = false; 
                        outputMessage = "Player one is red but potted white ball, player two (yellow) you may move the white ball";
                    } else if (playerOneTurn && otherColouredBall.colour == Ball.BallColours.Yellow) {
                        playerOneRed = false; 
                        playerOneTurn = false; 
                        outputMessage = "Player one is yellow but potted white ball, player two (red) you may move the white ball";
                    } else if (!playerOneTurn && otherColouredBall.colour == Ball.BallColours.Red) {
                        playerOneRed = false; 
                        playerOneTurn = true; 
                        outputMessage = "Player two is red but potted white ball, player one you may (yellow) move the white ball";
                    } else if (!playerOneTurn && otherColouredBall.colour == Ball.BallColours.Yellow){
                        playerOneRed = true; 
                        playerOneTurn = true; 
                        outputMessage = "Player two is yellow but potted white ball, player one (red) you may move the white ball";
                    }
                    mayDragWhiteBall = true; 
                    decided = true; 
                    return true; 
                }
            }
        }
        return false; 
    }

    //For when just red balls have been pocketed 
    //Could be the shot where player colours are allocated 
    private boolean redBallsPocketedOnly() {
        if (pocketedBallsToProcess.size() > 0) { 
            //Assume true and look for counterexample 
            boolean allRed = true; 
            for (Ball ball : pocketedBallsToProcess) {
                if (ball.colour != Ball.BallColours.Red){
                    allRed = false; 
                }
            }
            if(allRed) {
                if(decided) {
                    if(playerOneTurn) {
                        if(playerOneRed) {
                            if(firstBallHitBall.colour == Ball.BallColours.Red) {
                                outputMessage = "Player one's turn (red)";
                                mayDragWhiteBall = false; 
                                return true; 
                            } else {
                                outputMessage = "Hit opponent's ball. Player two (yellow) you may move the white ball";
                                playerOneTurn = false; 
                                mayDragWhiteBall = true; 
                                return true; 
                            }
                        } else {
                            outputMessage = "Pocketed opponent's ball. Player two (red) you may move the white ball";
                            playerOneTurn = false; 
                            mayDragWhiteBall = true; 
                            return true; 
                        }
                    } else {
                        if (!playerOneRed) { //Player two red 
                            if(firstBallHitBall.colour == Ball.BallColours.Red) {
                                outputMessage = "Player two's turn (red)";
                                mayDragWhiteBall = false; 
                                return true; 
                            } else {
                                outputMessage = "Hit opponent's ball. Player one (yellow) you may move the white ball";
                                playerOneTurn = true; 
                                mayDragWhiteBall = true; 
                                return true; 
                            }
                        } else {
                            outputMessage = "Pocketed opponent's ball. Player one (red) you may move the white ball";
                            playerOneTurn = true; 
                            mayDragWhiteBall = true; 
                            return true; 
                        }
                    }
                } else { //Undecided - assign colours 
                    if (playerOneTurn) {
                        playerOneRed = true; 
                        outputMessage = "Player one you are reds. You may continue your turn"; 
                    } else {
                        playerOneRed = false; 
                        outputMessage = "Player two you are reds. You may continue your turn";
                    }
                    mayDragWhiteBall = false; 
                    decided = true; 
                    return true; 
                }
            }
        }
        return false; 
    }

    //For when just yellow balls have been pocketed 
    //Could be the shot where player colours are allocated 
    private boolean yellowBallsPocketedOnly() {
        if (pocketedBallsToProcess.size() > 0) {
            //Assume true and look for counterexample 
            boolean allYellow = true; 
            for (Ball ball : pocketedBallsToProcess) {
                if (ball.colour != Ball.BallColours.Yellow){
                    allYellow = false; 
                }
            }
            
            if(allYellow) {
                if(decided) {
                    if(playerOneTurn) {
                        if(!playerOneRed) {
                            if(firstBallHitBall.colour == Ball.BallColours.Yellow) {
                                outputMessage = "Player one's turn (yellow)";
                                mayDragWhiteBall = false; 
                                return true; 
                            } else {
                                outputMessage = "Hit opponent's ball. Player two (red) you may move the white ball";
                                playerOneTurn = false; 
                                mayDragWhiteBall = true; 
                                return true; 
                            }
                        } else {
                            outputMessage = "Pocketed opponent's ball. Player two (yellow) you may move the white ball";
                            playerOneTurn = false; 
                            mayDragWhiteBall = true; 
                            return true; 
                        }
                    } else {
                        if (playerOneRed) { //Player two yellow
                            if(firstBallHitBall.colour == Ball.BallColours.Yellow) {
                                outputMessage = "Player two's turn (yellow)";
                                mayDragWhiteBall = false; 
                                return true; 
                            } else {
                                outputMessage = "Hit opponent's ball. Player one (red) you may move the white ball";
                                playerOneTurn = true; 
                                mayDragWhiteBall = true; 
                                return true; 
                            }
                        } else {
                            outputMessage = "Pocketed opponent's ball. Player one (yellow) you may move the white ball";
                            playerOneTurn = true; 
                            mayDragWhiteBall = true; 
                            return true; 
                        }
                    }
                } else { //Undecided - assign colours 
                    if (playerOneTurn) {
                        playerOneRed = false; 
                        outputMessage = "Player one you are yellows. You may continue your turn"; 
                    } else {
                        playerOneRed = true; 
                        outputMessage = "Player two you are yellows. You may continue your turn";
                    }
                    mayDragWhiteBall = false; 
                    decided = true; 
                    return true; 
                }
            }
        }
        return false; 
    }

    //When both red and yellow balls have been pocketed 
    //(Can assume only red and yellows because otherwise a white ball or black ball event would have been handled)
    private boolean redsAndYellowsPocketed() {
        boolean redPocketed = false; 
        boolean yellowPocketed = false; 
        for (Ball ball : pocketedBallsToProcess) {
            if(ball.colour == Ball.BallColours.Red) {
                redPocketed = true; 
            } else if (ball.colour == Ball.BallColours.Yellow) {
                yellowPocketed = true; 
            }
        }

        if(redPocketed && yellowPocketed) {
            if(decided) {
                if(playerOneTurn) {
                    outputMessage = "Pocketed opponent's ball. Player two " + (playerOneRed ? "(yellow)" : "(red)") + " you may move the white ball"; 
                    playerOneTurn = false; 
                } else {
                    outputMessage = "Pocketed opponent's ball. Player one " + (playerOneRed ? "(red)" : "(yellow)") + " you may move the white ball"; 
                    playerOneTurn = true; 
                }
                mayDragWhiteBall = true; 
                return true;
            } else { //If undecided, let player continue with first potted ball 
                Ball firstPocketed = pocketedBallsToProcess.get(0); 
                if(firstPocketed.colour == Ball.BallColours.Red) {
                    if(playerOneTurn) {
                        playerOneRed = true; 
                        outputMessage = "Player one you are reds. You may continue your turn";
                    } else {
                        playerOneRed = false; 
                        outputMessage = "Player two you are reds. You may continue your turn";
                    }
                } else { //Can now only be yellow otherwise a different scenario would have been triggered
                    if(playerOneTurn) {
                        playerOneRed = false; 
                        outputMessage = "Player one you are yellows. You may continue your turn"; 
                    } else {
                        playerOneRed = true; 
                        outputMessage = "Player two you are yellows. You may continue your turn"; 
                    }
                }
                decided = true;
                mayDragWhiteBall = false; 
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
        //reset aimingCue and shot predictor 
        aimingCue.initializeCue();
        aimingCue.setWhiteBallPos(whiteBall.position);
        aimingCue.repositionCue();
        updateShotPrediction();
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
            aimingCue.setWhiteBallPos(whiteBall.position);
            aimingCue.repositionCue();
            updateShotPrediction();
        }
    }

    public void restartGame() {
        balls = null; 
        aimingCue = null; 
        shotPredictor = null; 

        createBalls();
        createAimingCue();
        createShotPredictor();
        
        //Instantiate arraylists 
        collidingPairs = new ArrayList<>();
        ballsToRemove = new ArrayList<>();
        pocketedBallsToProcess = new ArrayList<>(); 
        
        initalizeGameSettings();

        gameState = GameState.TAKING_SHOT;
    }


    //Getters 
    public ArrayList<Ball> getBalls() {
        return balls; 
    }

    public Ball getWhiteBall() {
        return whiteBall; 
    }

    public AimingCue getAimingCue() {
        return aimingCue;
    }

    public ArrayList<Edge> getEdges() {
        return edges; 
    }

    public HashMap<Pocket.Position, Pocket> getPockets() {
        return pockets;
    }

    public ShotPredictor getShotPredictor() {
        return shotPredictor; 
    }

    public CushionsInfo getCushionsInfo() {
        return cushionsInfo; 
    }

    public GameState getGameState() {
        return gameState; 
    }

    public boolean isReady() {
        return ready; 
    }

    public int getNumRedBallsPocketed() {
        return numRedBallsPocketed; 
    }

    public int getNumYellowBallsPocketed() {
        return numYellowBallsPocketed; 
    }

    public boolean getPlayerOneRed() {
        return playerOneRed; 
    }

    public boolean isBroken() {
        return broken; 
    }

    public boolean isWhiteBallDraggable() {
        return mayDragWhiteBall; 
    }

    public boolean isWhiteBallBeingDragged() {
        return whiteBallBeingDragged;
    }

    public void setWhiteBallBeingDragged(boolean value) {
        whiteBallBeingDragged = value; 
    }

    public boolean hasPlayerOnePocketedBlack() {
        return blackPocketedByP1;
    }

    public boolean hasPlayerTwoPocketedBlack() {
        return blackPocketedByP2;
    }

    public String getOutputMessage() {
        return outputMessage; 
    }

    public boolean isGameOver() {
        return gameOver; 
    }
}

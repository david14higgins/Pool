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

    //These coordinates are totally dependent on the size of the balls and the play area
    private double[][] yellowBallPos = {{322, 270}, {296, 285}, {270, 240}, {244, 255}, {244, 315}, {218, 210}, {218, 300}};
    private double[][] redBallPos = {{296, 255}, {270, 300}, {244, 225}, {244, 285}, {218, 240}, {218, 270}, {218, 330}};

    private int mouseX;
    private int mouseY;
    private boolean mouseFocused;

    public GameState gameState;

    private final int maxShotPower = 2000;


    public Pool(int width, int height) {
        this.gameWidth = width;
        this.gameHeight = height;
        this.gameState = GameState.TAKING_SHOT;

        //Create balls
        balls = new ArrayList<>();
        edges = new ArrayList<>();

        int ballRadius = 12 ;

        //White ball
        whiteBall = new Ball(ballRadius, Ball.BallColours.White);
        whiteBall.position = new Vector2D(3 * gameWidth / 4, gameHeight / 2);
        balls.add(whiteBall);

        //Triange of balls positioned based on black ball location 
        Vector2D blackBallPos = new Vector2D(gameWidth / 4, gameHeight / 2);

        String ballOrder = "";

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j <= i; j++) {
                
            }
        }


        //for (int i = 0; i < 7; i++) {
        //    //Yellow ball
        //    //Ball yBall = new Ball(yellowBallPos[i][0], yellowBallPos[i][1], ballRadius, 2 * i, Ball.BallColours.Yellow);
        //    Ball yBall = new Ball(ballRadius, Ball.BallColours.Yellow);
        //    balls.add(yBall);
        //    yellowBalls[i] = yBall;

            //Red ball
            //Ball rBall = new Ball(redBallPos[i][0], redBallPos[i][1], ballRadius, 2 * i + 1, Ball.BallColours.Red);
        //    Ball rBall = new Ball(ballRadius, Ball.BallColours.Red);
        //    balls.add(rBall);
        //    redBalls[i] = rBall;
        //}

        //Black ball
        //blackBall = new Ball(270, 270, ballRadius, 14, Ball.BallColours.Black);
        //blackBall = new Ball(ballRadius, Ball.BallColours.Black);
        //balls.add(blackBall);

        //Create Cue and initialize 
        aimingCue = new AimingCue(300, 4);
        aimingCue.whiteBallPos = whiteBall.position;
        aimingCue.initializeCue();


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


        //Create Cushions
        int edgeRadius = 6;
        int centreCushionAngle = 30;

        //Retrieve two pockets for information purposes
        Pocket ul = pockets.get(Pocket.Position.UpperLeft);
        Pocket um = pockets.get(Pocket.Position.UpperMiddle);


        CushionsInfo info = new CushionsInfo(ul.radius, um.radius, edgeRadius, gameWidth, gameHeight);

        for (CushionsInfo.Cushion cushion: CushionsInfo.Cushion.values()) {
            Vector2D[] coords = info.getCushionCoords(cushion);
            for (int k = 0; k < coords.length - 1; k++) {
                Vector2D start = coords[k];
                Vector2D end = coords[k+1];
                Edge edge = new Edge(start.x, start.y, end.x, end.y, edgeRadius);
                edges.add(edge);
            }

        }

        //Don't start game loop until balls and edges set up
        ready = true;
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
        Ball fakeBall = new Ball(edge.radius, null);
        fakeBall.position = new Vector2D(closestPoint.x, closestPoint.y);
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
        System.out.println(normalizedShotPower * maxShotPower * aimingCue.getCueDirection().x);
        System.out.println(normalizedShotPower * maxShotPower * aimingCue.getCueDirection().y);
        // for (Ball b : balls) {
        //     if (ballClicked(b, mouseDownX, mouseDownY)) {
        //         b.velocity.x = 5 * (b.position.x - mouseUpX);
        //         b.velocity.y = 5 * (b.position.y - mouseUpY);
        //         b.stationar y = false;
        //     }
        // }
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

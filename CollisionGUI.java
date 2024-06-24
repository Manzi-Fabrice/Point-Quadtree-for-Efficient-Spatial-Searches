import java.awt.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Using a quadtree for collision detection
 */
public class CollisionGUI extends InteractiveGUI {
	private static final int width=800, height=600;		// size of the window
	private List<MovingPoint> points;					// all the points
	private List<MovingPoint> colliders;				// the points that collided at this time
	private char collisionHandler = 'c';				// when there's a collision, 'c'olor them, or 'd'estroy them
	private int delay = 100;							// timer control

	public CollisionGUI() {
		super("super-collider", width, height);

		points = new ArrayList<MovingPoint>();

		// Timer drives the animation. Ticks every delay milliseconds
		startTimer();
	}

	/**
	 * Adds a MovingPoint at the (x,y) location
	 */
	private void add(int x, int y) {
		points.add(new MovingPoint(x,y,width,height));
	}

	/**
	 * InteractiveGUI method, here creating a new MovingPoint when the mouse is clicked
	 * repaint method erases the window and calls draw
	 * @param x,y location of the mouse when pressed
	 */
    @Override
	public void handleMousePress(int x, int y) {
		add(x,y);
		repaint();
	}

	/**
	 * InteractiveGUI method, get the key pressed
	 * @param k the key pressed
	 */
    @Override
	public void handleKeyPress(char k) {
		if (k == 'f') { // faster
			if (delay>1) delay /= 2;
			setTimerDelay(delay);
			System.out.println("delay:"+delay);
		}
		else if (k == 's') { // slower
			delay *= 2;
			setTimerDelay(delay);
			System.out.println("delay:"+delay);
		}
		else if (k == 'r') { // add some new points at random positions
			for (int i=0; i<10; i++) {
				add((int)(width*Math.random()), (int)(height*Math.random()));
			}
			repaint();
		}
		else if (k == 'c' || k == 'd') { // control how collisions are handled
			collisionHandler = k;
			System.out.println("collision:"+k);
		}
	}

	/**
	 * InteractiveGUI method, here drawing all the points and then re-drawing the colliders in red
	 * @param g a reference to the window on which to draw the MovingPoints
	 */
    @Override
	public void draw(Graphics g) {
		// Ask all the points to draw themselves.
		g.setColor(Color.BLACK);
		if (points != null) {
			for (MovingPoint point : points) {
				point.draw(g);
			}
		}
		// Ask the colliders to draw themselves in red.
		if (colliders != null) {
			g.setColor(Color.RED);
			for (MovingPoint point : colliders) {
				point.draw(g);
			}
		}
	}

	/**
	 * Sets colliders to include all points in contact with another point
	 */
	private void findColliders() {
		// Create a quadtree with the 0th point as root
		PointQuadtree <MovingPoint> newTree = new PointQuadtree<>(points.get(0),0,0,width,height);

		// Now insert the rest of the points into the quadtree
		for (MovingPoint p: points){
			if (p != points.get(0)) newTree.insert(p);
		}

		// Create a list to hold the colliders
		colliders = new ArrayList<>();
		// For each point, see if it collided with another point
		for (MovingPoint p: points){
			List <MovingPoint> collisions = newTree.findInCircle(p.getX(), p.getY(), p.getR());
			//ensures point is not just "colliding" with self
			if (collisions.size() > 1){
				colliders.addAll(collisions);

			}
		}



	}

	/**
	 * InteractiveGUI method, here moving all the points and checking for collisions
	 */
    @Override
	public void handleTimer() {
		// Ask all the points to move themselves.
		for (MovingPoint point : points) {
			point.move();
		}
		// Check for collisions
		if (points.size() > 0) {
			findColliders();
			if (collisionHandler=='d') {
				points.removeAll(colliders);
				colliders = null;
			}
		}
		// Now update the drawing
		repaint();
	}

	public static void main(String[] args) {
        new CollisionGUI();
	}
}

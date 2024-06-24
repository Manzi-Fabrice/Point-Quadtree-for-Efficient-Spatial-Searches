import java.awt.*;
import java.util.List;
import javax.swing.SwingUtilities;

/**
 * Driver for interacting with a quadtree:
 * inserting points, viewing the tree, and finding points near a mouse press
 */
public class DotTreeGUI extends InteractiveGUI {
	private static final int width=800, height=600;		// size of the universe
	private static final int pointRadius = 5;			// to draw Point, so it's visible
	private static final Color[] rainbow = {Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.BLUE, Color.MAGENTA};
			// to color different levels differently

	private PointQuadtree<Point> tree = null;		// holds the Points
	private char mode = 'a';						// 'a': adding; 'q': querying with the mouse
	private int mouseX, mouseY;						// current mouse location, when querying
	private int mouseRadius = 10;					// circle around mouse location, for querying
	private boolean trackMouse = false;				// if true, then print out where the mouse is as it moves
	private List<Point> found = null;				// who was found near mouse, when querying
	
	public DotTreeGUI() {
		super("dottree", width, height);
	}

	/**
	 * InteractiveGUI method, here keeping track of the location and redrawing to show it
	 * @param x,y  location of mouse as it moves
	 */
	@Override
	public void handleMouseMotion(int x, int y) {
		if (mode == 'q') {
			mouseX = x; mouseY = y;
			repaint();  //erases the window and calls draw
		}
		if (trackMouse) {
			System.out.println("mouse location: " + x + "," + y);
		}
	}

	/**
	 * InteractiveGUI method, here either adding a new point or querying near the mouse
	 * @param x - x location of mouse when pressed
	 * @param y - y location of mouse when pressed
	 */
	@Override
	public void handleMousePress(int x, int y) {
		// Add a new Point at x,y in the quadtree (or make a new quadtree if tree is null)
		if (mode == 'a') {
			if (tree== null){
				tree = new PointQuadtree<>(new Point(x, y), 0,0,width, height);
			} else {
				tree.insert(new Point(x,y));
			}

		}
		else if (mode == 'q') {
			// Set "found" to what tree says is near the mouse press
			found = tree.findInCircle(x, y, mouseRadius);

		}
		else {
			System.out.println("clicked at "+x+","+y);
		}
		repaint(); //erase the window and call draw
	}
	
	/**
	 * A simple testing procedure, making sure actual is expected, and printing a message if not
	 * @param x		query x coordinate
	 * @param y		query y coordinate
	 * @param r		query circle radius
	 * @param expectedCircleRectangle	how many times Geometry.circleIntersectsRectangle is expected to be called
	 * @param expectedInCircle			how many times Geometry.pointInCircle is expected to be called
	 * @param expectedHits				how many points are expected to be found
	 * @return  0 if passed; 1 if failed
	 */
	private int testFind(int x, int y, int r, int expectedCircleRectangle, int expectedInCircle, int expectedHits) {
		Geometry.resetNumInCircleTests();
		Geometry.resetNumCircleRectangleTests();
		int errs = 0;
		int num = tree.findInCircle(x, y, r).size();
		String which = "("+x+","+y+")@"+r;
		if (Geometry.getNumCircleRectangleTests() != expectedCircleRectangle) {
			errs++;
			System.err.println(which+": wrong # circle-rectangle, got "+Geometry.getNumCircleRectangleTests()+" but expected "+expectedCircleRectangle);
		}
		if (Geometry.getNumInCircleTests() != expectedInCircle) {
			errs++;
			System.err.println(which+": wrong # in circle, got "+Geometry.getNumInCircleTests()+" but expected "+expectedInCircle);
		}
		if (num != expectedHits) {
			errs++;
			System.err.println(which+": wrong # hits, got "+num+" but expected "+expectedHits);
		}
		return errs;
	}
	
	/**
	 * test tree 0 -- first three points from figure in handout
	 * hardcoded point locations for 800x600
	 */
	private void test0() {
		found = null;
		tree = new PointQuadtree<Point>(new Point(400,300), 0,0,800,600); // start with A
		tree.insert(new Point(150,450)); // B
		tree.insert(new Point(250,550)); // C
		int bad = 0;
		bad += testFind(0,0,900,3,3,3);		// rect for all; circle for all; find all
		bad += testFind(400,300,10,3,2,1);	// rect for all; circle for A,B; find A
		bad += testFind(150,450,10,3,3,1);	// rect for all; circle for all; find B
		bad += testFind(250,550,10,3,3,1);	// rect for all; circle for all; find C
		bad += testFind(150,450,200,3,3,2);	// rect for all; circle for all; find B, C
		bad += testFind(140,440,10,3,2,0);	// rect for all; circle for A,B; find none
		bad += testFind(750,550,10,2,1,0);	// rect for A,B; circle for A; find none
		if (bad==0) System.out.println("test 0 passed!");
	}

	/**
	 * test tree 1 -- figure in handout
	 * hardcoded point locations for 800x600
	 */
	private void test1() {
		found = null;
		tree = new PointQuadtree<Point>(new Point(300,400), 0,0,800,600); // start with A
		tree.insert(new Point(150,450)); // B
		tree.insert(new Point(250,550)); // C
		tree.insert(new Point(450,200)); // D
		tree.insert(new Point(200,250)); // E
		tree.insert(new Point(350,175)); // F
		tree.insert(new Point(500,125)); // G
		tree.insert(new Point(475,250)); // H
		tree.insert(new Point(525,225)); // I
		tree.insert(new Point(490,215)); // J
		tree.insert(new Point(700,550)); // K
		tree.insert(new Point(310,410)); // L
		int bad = 0;
		bad += testFind(150,450,10,6,3,1); 	// rect for A [D] [E] [B [C]] [K]; circle for A, B, C; find B
		bad += testFind(500,125,10,8,3,1);	// rect for A [D [G F H]] [E] [B] [K]; circle for A, D, G; find G
		bad += testFind(300,400,15,10,6,2);	// rect for A [D [G F H]] [E] [B [C]] [K [L]]; circle for A,D,E,B,K,L; find A,L
		bad += testFind(495,225,50,10,6,3);	// rect for A [D [G F H [I [J]]]] [E] [B] [K]; circle for A,D,G,H,I,J; find H,I,J
		bad += testFind(0,0,900,12,12,12);	// rect for all; circle for all; find all
		if (bad==0) System.out.println("test 1 passed!");
	}







	/**
	 * InteractiveGUI method, here toggling the mode between 'a' and 'q'
	 * and increasing/decreasing mouseRadius via +/-
	 */
	@Override
	public void handleKeyPress(char key) {
		if (key=='a') {
			mode = key;
		}
		else if (key=='q') {
			mode = key;
			//get the mouse's current location relative to the window
			Point location = MouseInfo.getPointerInfo().getLocation();
			SwingUtilities.convertPointFromScreen(location, panel);
			mouseX = location.x; mouseY = location.y;
		}
		else if (key=='+') {
			mouseRadius += 10;
		}
		else if (key=='-') {
			mouseRadius -= 10;
			if (mouseRadius < 0) mouseRadius=0;
		}
		else if (key=='m') {
			trackMouse = !trackMouse;
		}
		else if (key=='0') {
			test0();
		}
		else if (key=='1') {
			test1();
		}

		repaint(); //erase the window and call draw
	}
	
	/**
	 * InteractiveGUI method, here drawing the quadtree
	 * and if in query mode, the mouse location and any found Points
	 * @param g reference to the window on which to draw points and circle around mouse
	 */
	@Override
	public void draw(Graphics g) {
		if (tree != null) drawTree(g, tree, 0);
		if (mode == 'q') {
			g.setColor(Color.BLACK);
			g.drawOval(mouseX-mouseRadius, mouseY-mouseRadius, 2*mouseRadius, 2*mouseRadius);			
			if (found != null) {
				g.setColor(Color.BLACK);
				for (Point p : found) {
					g.fillOval((int)p.getX()- pointRadius, (int)p.getY()- pointRadius, 2* pointRadius, 2* pointRadius);
				}
			}
		}
	}

	/**
	 * Draws the Point tree
	 * @param g		the graphics object for drawing
	 * @param tree	a Point tree (not necessarily root)
	 * @param level	how far down from the root qt is (0 for root, 1 for its children, etc.)
	 */
	public void drawTree(Graphics g, PointQuadtree<Point> tree, int level) {
		// Set the color for this level
		g.setColor(rainbow[level % rainbow.length]);
		// Draw this node's Point and lines through it
		Point d = tree.getPoint();
		g.fillOval((int)d.getX()- pointRadius, (int)d.getY()- pointRadius, 2* pointRadius, 2* pointRadius);
		g.drawLine(tree.getX1(), (int)tree.getPoint().getY(), tree.getX2(), (int)tree.getPoint().getY());
		g.drawLine((int)tree.getPoint().getX(), tree.getY1(), (int)tree.getPoint().getX(), tree.getY2());

		// Recurse with children
		if (tree.getChild(4) != null) {  // Southeast child
			drawTree(g, tree.getChild(4), level + 1);
		}
		if (tree.getChild(3) != null) {  // Southwest child
			drawTree(g, tree.getChild(3), level + 1);
		}
		if (tree.getChild(1) != null) {  // Northeast child
			drawTree(g, tree.getChild(1), level + 1);
		}
		if (tree.getChild(2) != null) {  // Northwest child
			drawTree(g, tree.getChild(2), level + 1);
		}

	}

	public static void main(String[] args) {
		new DotTreeGUI();
	}
}

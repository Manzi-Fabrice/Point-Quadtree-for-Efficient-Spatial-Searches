import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * A point quadtree: stores an element at a 2D position, with children at the subdivided quadrants
 * E extends Point2D to ensure whatever the PointQuadTree holds, it implements getX and getY
 */
public class PointQuadtree<E extends Point2D> extends Geometry {
	private E point;							// the point anchoring this node
	private int x1, y1;							// upper-left corner of the region
	private int x2, y2;							// bottom-right corner of the region
	private PointQuadtree<E> c1, c2, c3, c4;	// children

	/**
	 * Initializes a leaf quadtree, holding the point in the rectangle
	 */
	public PointQuadtree(E point, int x1, int y1, int x2, int y2) {
		this.point = point;
		this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2;
	}

	// Getters
	public E getPoint() { return point; }
	public int getX1() { return x1; }
	public int getY1() { return y1; }
	public int getX2() { return x2; }
	public int getY2() { return y2; }

	/**
	 * Returns the child (if any) at the given quadrant, 1-4
	 * @param quadrant	1 through 4
	 * @return child for quadrant
	 */
	public PointQuadtree<E> getChild(int quadrant) {
		if (quadrant==1) return c1;
		if (quadrant==2) return c2;
		if (quadrant==3) return c3;
		if (quadrant==4) return c4;
		return null;
	}

	/**
	 * Returns whether there is a child at the given quadrant, 1-4
	 * @param quadrant	1 through 4
	 */
	public boolean hasChild(int quadrant) {
		return (quadrant==1 && c1!=null) || (quadrant==2 && c2!=null) || (quadrant==3 && c3!=null) || (quadrant==4 && c4!=null);
	}

	/**
	 * Inserts the point into the tree
	 */

	public void insert(E newPoint) {
		// Check which quadrant the newPoint falls into
		// (midpoint coordinates of this node's region)
		int x = (int) this.point.getX();
		int y = (int) this.point.getY();

		if (newPoint.getX() > x) {
			if (newPoint.getY() < y) {
				// Quadrant 1: Northeast
				if (!this.hasChild(1)) {
					c1 = new PointQuadtree<>(newPoint, x, y1, x2, y);
				} else {
					c1.insert(newPoint);
				}
			} else {
				// Quadrant 4: Southeast
				if (!this.hasChild(4)) {
					c4 = new PointQuadtree<>(newPoint, x, y, x2, y2);
				} else {
					c4.insert(newPoint);
				}
			}
		} else {
			if (newPoint.getY() < y) {
				// Quadrant 2: Northwest
				if (!this.hasChild(2)) {
					c2 = new PointQuadtree<>(newPoint, x1, y1, x, y);
				} else {
					c2.insert(newPoint);
				}
			} else {
				// Quadrant 3: Southwest
				if (!this.hasChild(3)) {
					c3 = new PointQuadtree<>(newPoint, x1, y, x, y2);
				} else {
					c3.insert(newPoint);
				}
			}
		}
	}


	/**
	 * Finds the number of points in the quadtree (including its descendants)
	 */
	public int size() {
		// Start with 1 to count the current node itself
		int count = 1;

		// Recursively add the size of each non-null child
		if (c1 != null) count += c1.size();
		if (c2 != null) count += c2.size();
		if (c3 != null) count += c3.size();
		if (c4 != null) count += c4.size();

		return count;
	}
	
	/**
	 * Builds a list of all the points in the quadtree (including its descendants)
	 * @return List with all points in the quadtree
	 */

	public List<E> allPoints() {
		List<E> points = new ArrayList<>();  // Create a new list to hold all points
		points.add(point);  // Add the current node's point

		// Recursively add points from each non-null child
		if (c1 != null) points.addAll(c1.allPoints());
		if (c2 != null) points.addAll(c2.allPoints());
		if (c3 != null) points.addAll(c3.allPoints());
		if (c4 != null) points.addAll(c4.allPoints());

		return points;
	}


	/**
	 * Uses the quadtree to find all points within the circle
	 * @param cx	circle center x
	 * @param cy  	circle center y
	 * @param cr  	circle radius
	 * @return    	the points in the circle (and the qt's rectangle)
	 */
	public List<E> findInCircle(double cx, double cy, double cr) {
		//creates list of points in circle
		List<E> results = new ArrayList<>();
		findInCircleHelper(cx, cy, cr, results);
		return results;
	}

	private void findInCircleHelper(double cx, double cy, double cr, List<E> results) {
		//proceed as long as circle intersects rectangle
		if (Geometry.circleIntersectsRectangle(cx, cy, cr, x1, y1, x2, y2)) {
			// Check if the point at this node is within the circle
			if (pointInCircle(point.getX(), point.getY(), cx, cy, cr)) {
				results.add(point);
			}

			// Recursively check child nodes if the circle intersects their quadrants
			if (c1 != null) {
				c1.findInCircleHelper(cx, cy, cr, results);
			}
			if (c2 != null) {
				c2.findInCircleHelper(cx, cy, cr, results);
			}
			if (c3 != null) {
				c3.findInCircleHelper(cx, cy, cr, results);
			}
			if (c4 != null) {
				c4.findInCircleHelper(cx, cy, cr, results);
			}
		}
	}

	}




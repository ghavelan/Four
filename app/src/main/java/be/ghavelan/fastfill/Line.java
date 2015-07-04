package be.ghavelan.fastfill;

import android.graphics.Point;

import be.ghavelan.fastfill.Utility.Orientation;

/**
 * <p>
 * Computes a line segment (horizontal or vertical segment only).
 * </p>
 * <p>
 * If the segment is horizontal, the first point will always be the one with the smallest X.<br/>
 * If the segment is vertical, the first point will always be the one with the smallest Y.
 * </p>
 */

public class Line {
    //First point
    private Point start;
    //Second point
    private Point end;
    //constant is X or Y-coordinate depending on the orientation of the segment.
    private int constant;
    //Orientation of the segment : Horizontal or vertical.
    private Orientation orientation;

    /**
     * Constructs a line segment.
     *
     * @param A First point.
     * @param B Second point.
     */
    public Line(Point A, Point B) {

        start = A;
        end = B;

        if (isHorizontal()) {
            orientation = Orientation.HORIZONTAL;
            constant = start.y;
        } else if (isVertical()) {
            orientation = Orientation.VERTICAL;
            constant = start.x;
        } else {
            orientation = Orientation.OBLIQUE;
            constant = Integer.MIN_VALUE;
        }

        sort();

    }

    /**
     * Gives the first point.
     *
     * @return First point of the segment.
     */
    public Point getStart() {
        return start;
    }

    /**
     * Gives the second point.
     *
     * @return Second point of the segment.
     */
    public Point getEnd() {
        return end;
    }

    /**
     * Checks if the segment is horizontal.
     *
     * @return True if the segment is horizontal.
     */
    private boolean isHorizontal() {
        return start.y == end.y && start.x != end.x;
    }

    /**
     * Checks if the segment is vertical.
     *
     * @return True if the segment is vertical.
     */
    private boolean isVertical() {
        return start.x == end.x && start.y != end.y;
    }

    /**
     * Checks if the given point is inside the segment.
     *
     * @param P Point.
     * @return True if the segment contains the given point.
     */
    public boolean contains(Point P) {

        if (orientation == Orientation.HORIZONTAL) {

            return P.y == constant && (end.x > start.x ? P.x >= start.x && P.x <= end.x :
                    P.x >= end.x && P.x <= start.x);

        } else if (orientation == Orientation.VERTICAL) {

            return P.x == constant && (end.y > start.y ? P.y >= start.y && P.y <= end.y :
                    P.y >= end.y && P.y <= start.y);

        }

        return false;

    }

    /**
     * Gets the orientation of the segment.
     *
     * @return Orientation.
     */
    public Orientation getOrientation() {
        return orientation;
    }

    /**
     * Gets the constant value of the segment.
     *
     * @return X or Y-coordinate depending on the orientation of the segment.
     */
    public int getConstant() {
        return constant;
    }

    /**
     * <p>
     * Sorts the segment.
     * </p>
     * The first point (<em>start</em>)is always the point with the smallest X (horizontal case).
     * The first point (<em>start</em>) is always the point with the smallest Y (vertical case).
     */
    private void sort() {

        int tmp;

        if (orientation == Orientation.HORIZONTAL) {

            if (start.x > end.x) {
                tmp = start.x;
                start.set(end.x, start.y);
                end.set(tmp, end.y);
            }

        } else if (orientation == Orientation.VERTICAL) {

            if (start.y > end.y) {
                tmp = start.y;
                start.set(start.x, end.y);
                end.set(end.x, tmp);
            }
        }
    }

}

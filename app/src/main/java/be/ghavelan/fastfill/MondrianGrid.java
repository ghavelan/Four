package be.ghavelan.fastfill;

import android.graphics.Point;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import be.ghavelan.fastfill.Utility.Orientation;

/**
 * <p>
 * This class is responsible of the grid's computation (set of horizontal and vertical lines).
 * </p>
 */
public class MondrianGrid {

    //To pick a random number
    public static Random RANDOM = new Random();
    //Collection of colored shapes : to bound a shape (rectangle) with a given color (int)
    Map<Rect, Integer> shapes;
    //Width and height of the grid
    private int width;
    private int height;
    //Grid = collection of orthogonal lines
    private List<Line> line;
    //Minimum number of pixels between 2 horizontal or vertical lines (depending on the screen's density)
    private int OnX;
    private int OnY;
    //Free zones on x and y (there is a minimum distance between horizontal and vertical lines)
    private Intervals freeOnX;
    private Intervals freeOnY;
    //Top bound of the game view
    private Line line_up;
    //Rejection distance on X
    private Line line_up_left;
    private Line line_up_right;
    //Left bound of the game view
    private Line line_left;
    //Rejection distance on Y
    private Line line_left_up;
    private Line line_left_down;
    //Intersection points
    private List<Point> intersectionPoints;

    /**
     * Constructs a grid.
     *
     * @param w Width of the grid.
     * @param h Height of the grid.
     */
    public MondrianGrid(int w, int h) {

        this.width = w;
        this.height = h;
        this.line_up = new Line(new Point(0, 0), new Point(width, 0));
        this.line_left = new Line(new Point(0, 0), new Point(0, height));
        this.freeOnX = new Intervals(Orientation.HORIZONTAL, 0);
        this.freeOnY = new Intervals(Orientation.VERTICAL, 0);
        this.line = new ArrayList<>();
        this.line.clear();
        this.shapes = new LinkedHashMap<>();
        this.shapes.clear();
        this.intersectionPoints = new ArrayList<>();
        this.intersectionPoints.clear();
    }

    /**
     * Minimum distance between lines (in pixels).
     *
     * @param px x spacing.
     * @param py y spacing.
     */
    public void setRejectionDistance(int px, int py) {

        this.OnX = px;
        this.OnY = py;
        this.line_up_left = new Line(new Point(0, 0), new Point(px, 0));
        this.line_left_up = new Line(new Point(0, 0), new Point(0, py));
        this.line_up_right = new Line(new Point(width - px, 0), new Point(width, 0));
        this.line_left_down = new Line(new Point(0, height - py), new Point(0, height));

    }

    /**
     * Computes a random orientation (horizontal or vertical).
     *
     * @return Orientation.
     */
    private Orientation randOrientation() {

        return (RANDOM.nextInt(2) == 0) ? Orientation.HORIZONTAL : Orientation.VERTICAL;

    }

    /**
     * Computes the grid.
     *
     * @param numberOfLines Maximum number of lines within the grid.
     */
    public void computeMondrian(int numberOfLines) {

        //Initialization of the 2 zones with the top and left sides of the grid's perimeter
        this.freeOnX.clear();
        this.freeOnY.clear();
        this.freeOnX.addSegment(line_up);
        this.freeOnY.addSegment(line_left);
        //Removes length OnX and OnY of the 2 extremities of the available zones
        this.freeOnX.remove(line_up_left);
        this.freeOnX.remove(line_up_right);
        this.freeOnY.remove(line_left_up);
        this.freeOnY.remove(line_left_down);
        //Random point (x or y value selected in freeOnX or freeOnY)
        Point randomPoint;
        //Segment centered on randomPoint (exclusion zones)
        Line lineToRemove;
        //Intersection points
        intersectionPoints.clear();
        //Orientation
        Orientation randOrientation = null;
        //Current line, randomly selected
        Line currentLine = null;
        //To count the number of lines
        int k = 0;
        //To freeze the randomness
        boolean freeze = false;

        randomLabel:
        while (k < numberOfLines) {
            //Select a random orientation for a line (vertical or horizontal)
            if (!freeze) {
                randOrientation = randOrientation();

            }

            switch (randOrientation) {

                case HORIZONTAL:
                    //Select a random point available on Y (via freeOnY) to draw the line
                    if (!freeOnY.isEmpty()) {
                        randomPoint = freeOnY.selectRandomPoint(RANDOM);
                        //Remove line from free zone
                        lineToRemove = new Line(new Point(0, randomPoint.y - OnY), new Point(0, randomPoint.y + OnY));
                        freeOnY.remove(lineToRemove);
                        currentLine = new Line(randomPoint, new Point(width, randomPoint.y));

                        if (k == 0) {
                            line.add(currentLine);
                            k++;
                            continue randomLabel;
                        }

                    } else {
                        if (!freeOnX.isEmpty()) {
                            freeze = true;
                            randOrientation = Orientation.VERTICAL;
                            continue randomLabel;
                        } else {

                            break randomLabel;
                        }

                    }
                    break;

                case VERTICAL:
                    //Select a random point available on X to draw the line
                    if (!freeOnX.isEmpty()) {
                        randomPoint = freeOnX.selectRandomPoint(RANDOM);
                        lineToRemove = new Line(new Point(randomPoint.x - OnX, 0), new Point(randomPoint.x + OnX, 0));
                        freeOnX.remove(lineToRemove);
                        currentLine = new Line(randomPoint, new Point(randomPoint.x, height));

                        if (k == 0) {
                            line.add(currentLine);
                            k++;
                            continue randomLabel;
                        }

                    } else {

                        if (!freeOnY.isEmpty()) {
                            freeze = true;
                            randOrientation = Orientation.HORIZONTAL;
                            continue randomLabel;
                        } else {
                            break randomLabel;
                        }
                    }
                    break;
            }

            if (k > 0) {

                if (freeze) {
                    freeze = false;
                }

                intersectionPoints = findIntersection(currentLine);

                if (!intersectionPoints.isEmpty()) {

                    int number = intersectionPoints.size();
                    int[] indexes;

                    if (number > 2) {

                        indexes = Utility.getRandomInRange(number, 2);
                        line.add(new Line(intersectionPoints.get(indexes[0]),
                                intersectionPoints.get(indexes[1])));
                        k++;

                    } else {
                        line.add(currentLine);
                        k++;
                    }
                }
            }
        }
    }

    /**
     * Returns intersection points between a segment (horizontal or vertical) and the other
     * segments in the collection (perimeter is taken into account).
     * No need to sort the segments before.
     *
     * @param currentLine Line (horizontal or vertical).
     * @return A collection of intersection points.
     */
    public List<Point> findIntersection(Line currentLine) {

        if (currentLine != null) {

            //To store intersection points
            List<Point> listPoints = new ArrayList<>();
            listPoints.clear();
            int constant = currentLine.getConstant();

            if (!line.isEmpty()) {

                if (currentLine.getOrientation() == Orientation.HORIZONTAL) {
                    /*Intersections with the perimeter*/
                    listPoints.add(new Point(0, constant));
                    listPoints.add(new Point(width, constant));

                    for (int i = 0, lineSize = line.size(); i < lineSize; i++) {
                        Line aLine = line.get(i);

                        if (aLine.getOrientation() == Orientation.VERTICAL) {

                            if (aLine.getStart().y < constant && aLine.getEnd().y > constant) {

                                listPoints.add(new Point(aLine.getConstant(), constant));

                            } else if (aLine.getStart().y > constant && aLine.getEnd().y < constant) {

                                listPoints.add(new Point(aLine.getConstant(), constant));

                            } else if (aLine.getStart().y == constant || aLine.getEnd().y == constant) {

                                listPoints.add(new Point(aLine.getConstant(), constant));

                            }

                        }

                    }

                } else if (currentLine.getOrientation() == Orientation.VERTICAL) {

                    listPoints.add(new Point(constant, 0));
                    listPoints.add(new Point(constant, height));

                    for (int i = 0, lineSize = line.size(); i < lineSize; i++) {
                        Line aLine = line.get(i);

                        if (aLine.getOrientation() == Orientation.HORIZONTAL) {

                            if (aLine.getStart().x < constant && aLine.getEnd().x > constant) {

                                listPoints.add(new Point(constant, aLine.getConstant()));

                            } else if (aLine.getStart().x > constant && aLine.getEnd().x < constant) {

                                listPoints.add(new Point(constant, aLine.getConstant()));

                            } else if (aLine.getStart().x == constant || aLine.getEnd().x == constant) {

                                listPoints.add(new Point(constant, aLine.getConstant()));

                            }

                        }

                    }

                }

            }
           /*If no segment other than the perimeter is in the zone, intersection points are
             given by the intersection of the current line and the perimeter */
            else {

                if (currentLine.getOrientation() == Orientation.HORIZONTAL) {

                    listPoints.add(new Point(0, constant));
                    listPoints.add(new Point(width, constant));

                } else if (currentLine.getOrientation() == Orientation.VERTICAL) {

                    listPoints.add(new Point(constant, 0));
                    listPoints.add(new Point(constant, height));

                }

            }

            return listPoints;

        } else {

            return null;
        }

    }

    /**
     * Returns all the lines within the collection.
     *
     * @return All the segments defining the Mondrian zone (without the perimeter).
     */
    public List<Line> getLines() {

        return line;
    }

    /**
     * Returns the grid's width.
     *
     * @return Width of the grid.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the grid's height.
     *
     * @return Height of the grid.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns the shapes.
     *
     * @return Shapes with their color.
     */
    public Map<Rect, Integer> getShapes() {
        return shapes;

    }

    /**
     * Adds a shape with a given color.
     *
     * @param rect  Shape to add.
     * @param color Color of the shape.
     */
    public void putShape(Rect rect, int color) {
        shapes.put(rect, color);

    }

    /**
     * Clears the shapes.
     */
    public void clearShapes() {
        shapes.clear();
    }

}

package be.ghavelan.fastfill;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import be.ghavelan.fastfill.Utility.Orientation;

/**
 * <p>
 * Manages a collection of intervals (segments) with the aim to keep a minimum distance
 * between horizontal and vertical lines.
 * </p>
 * All segments must have the same orientation (HORIZONTAL or VERTICAL).<br/>
 * The segments must also be aligned (i.e constant x or y coordinate depending on the orientation).<br/>
 */
public class Intervals {
    //Set of segments
    private List<Line> setOfSegments;
    //Orientation of all segments
    private Orientation orientation;
    //Altitude of all segments
    private int constant;

    /**
     * Constructor.
     *
     * @param orientation Orientation of the segment (horizontal or vertical).
     * @param alt         x-coordinate if the segment is vertical and y-coordinate otherwise.
     */
    public Intervals(Orientation orientation, int alt) {
        this.orientation = orientation;
        this.constant = alt;
        this.setOfSegments = new ArrayList<>();
        this.setOfSegments.clear();
    }

    /**
     * Adds a segment in the collection if both orientation and altitude are corrects.
     *
     * @param segment Segment to add to the collection.
     */
    public void addSegment(Line segment) {
        //If the segment is in the right orientation with the correct altitude
        if (segment.getOrientation() == orientation && segment.getConstant() == constant
                && (orientation == Orientation.HORIZONTAL || orientation == Orientation.VERTICAL)) {

            setOfSegments.add(segment);

        }
    }

    /**
     * Removes all the segments.
     */
    public void clear() {
        setOfSegments.clear();
    }

    /**
     * Cuts the set of segments with the segment given in argument.<br/>
     * Works only if the orientation is horizontal or vertical.
     *
     * @param toRemove Cutting segment.
     */
    public void remove(Line toRemove) {

        //Do something only when the toRemove segment has the correct orientation and constant
        if (toRemove.getConstant() == constant && toRemove.getOrientation() == orientation
                && (orientation == Orientation.HORIZONTAL || orientation == Orientation.VERTICAL)) {

            //Copy list used to get the relevant segments
            List<Line> copy = new ArrayList<>();
            copy.clear();
            //Running over the set of segments
            if (!setOfSegments.isEmpty()) {
                for (int i = 0, setOfSegmentsSize = setOfSegments.size(); i < setOfSegmentsSize; i++) {
                    Line line = setOfSegments.get(i);
                    //If both points of the cutting segment are contained in the current line segment
                    if (line.contains(toRemove.getStart()) && line.contains(toRemove.getEnd())) {
                        //If neither starting points nor ending points coincide
                        if (!Utility.isEquals(line.getStart(), toRemove.getStart()) &&
                                !Utility.isEquals(line.getEnd(), toRemove.getEnd())) {
                            //Add 2 segments
                            copy.add(new Line(line.getStart(), toRemove.getStart()));
                            copy.add(new Line(toRemove.getEnd(), line.getEnd()));

                        }
                        //If at least one extremity coincide : same starting point
                        else if (Utility.isEquals(line.getStart(), toRemove.getStart()) &&
                                !Utility.isEquals(line.getEnd(), toRemove.getEnd())) {

                            //Add 1 segment
                            copy.add(new Line(toRemove.getEnd(), line.getEnd()));

                        }
                        //If at least one extremity coincide : same ending point
                        else if (!Utility.isEquals(line.getStart(), toRemove.getStart()) &&
                                Utility.isEquals(line.getEnd(), toRemove.getEnd())) {

                            //Add 1 segment
                            copy.add(new Line(line.getStart(), toRemove.getStart()));

                        }
                        //If both points are the same (do nothing)

                    }
                    //Else if only one extremity of the toRemove segment is contained in the line segment
                    else if (line.contains(toRemove.getStart()) && !line.contains(toRemove.getEnd())) {

                        if (!Utility.isEquals(toRemove.getStart(), line.getStart())) {
                            copy.add(new Line(line.getStart(), toRemove.getStart()));
                        }
                    }
                    //If only one extremity of the toRemove segment is contained in the line segment
                    else if (!line.contains(toRemove.getStart()) && line.contains(toRemove.getEnd())) {

                        if (!Utility.isEquals(toRemove.getEnd(), line.getEnd())) {
                            copy.add(new Line(toRemove.getEnd(), line.getEnd()));
                        }
                    }
                    //If the line segment and the toRemove segment are not overlapping
                    else if (!line.contains(toRemove.getStart()) && !line.contains(toRemove.getEnd())
                            && !toRemove.contains(line.getStart()) && !toRemove.contains(
                            line.getEnd())) {

                        copy.add(line);

                    }

                }
            }

            setOfSegments.clear();

            if (!copy.isEmpty()) {
                setOfSegments.addAll(copy);
            }

        }

    }

    /**
     * Returns a random point in the collection.
     *
     * @param rand Random variable.
     * @return A point.
     */
    public Point selectRandomPoint(Random rand) {

        Point point = null;

        if (!setOfSegments.isEmpty()) {
            //Randomly select a segment in the set
            Line chosenSeg = setOfSegments.get(rand.nextInt(setOfSegments.size()));
            int val;

            switch (orientation) {

                case HORIZONTAL:
                    val = Utility.randInt(chosenSeg.getStart().x, chosenSeg.getEnd().x, rand);
                    point = new Point(val, constant);
                    break;

                case VERTICAL:
                    val = Utility.randInt(chosenSeg.getStart().y, chosenSeg.getEnd().y, rand);
                    point = new Point(constant, val);
                    break;
            }

        }

        return point;

    }

    /**
     * Checks if the collection is empty.
     *
     * @return True if the set is empty.
     */
    public boolean isEmpty() {

        return setOfSegments.isEmpty();

    }

}

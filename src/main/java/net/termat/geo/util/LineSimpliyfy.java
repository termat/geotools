package net.termat.geo.util;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;


/*
 * ORIGINAL-CODE
 *
 * https://github.com/hgoebl/simplify-java
 */

public class LineSimpliyfy {

    public LineSimpliyfy() {}

    public List<Point2D> simplify(List<Point2D> points,double tolerance,boolean highestQuality) {
        if (points == null || points.size()<= 2) {
            return points;
        }
        double sqTolerance = tolerance * tolerance;
        if (!highestQuality) {
            points = simplifyRadialDistance(points.toArray(new Point2D[points.size()]), sqTolerance);
        }else{
            points = simplifyDouglasPeucker(points.toArray(new Point2D[points.size()]), sqTolerance);
        }
        return points;
    }

    List<Point2D> simplifyRadialDistance(Point2D[] points, double sqTolerance) {
        Point2D point = null;
        Point2D prevPoint = points[0];
        List<Point2D> newPoints = new ArrayList<Point2D>();
        newPoints.add(prevPoint);
        for (int i = 1; i < points.length; ++i) {
            point = points[i];
            if (point.distanceSq(prevPoint) > sqTolerance) {
                newPoints.add(point);
                prevPoint = point;
            }
        }
        if (prevPoint != point) {
            newPoints.add(point);
        }
        return newPoints;
    }

    private static class Range {
        private Range(int first, int last) {
            this.first = first;
            this.last = last;
        }
        int first;
        int last;
    }

    List<Point2D> simplifyDouglasPeucker(Point2D[] points, double sqTolerance) {
        BitSet bitSet = new BitSet(points.length);
        bitSet.set(0);
        bitSet.set(points.length - 1);
        List<Range> stack = new ArrayList<Range>();
        stack.add(new Range(0, points.length - 1));
        while (!stack.isEmpty()) {
            Range range = stack.remove(stack.size() - 1);
            int index = -1;
            double maxSqDist = 0f;
            // find index of point with maximum square distance from first and last point
            for (int i = range.first + 1; i < range.last; ++i) {
                double sqDist = getSquareSegmentDistance(points[i], points[range.first], points[range.last]);
                if (sqDist > maxSqDist) {
                    index = i;
                    maxSqDist = sqDist;
                }
            }
            if (maxSqDist > sqTolerance) {
                bitSet.set(index);
                stack.add(new Range(range.first, index));
                stack.add(new Range(index, range.last));
            }
        }
        List<Point2D> newPoints = new ArrayList<Point2D>(bitSet.cardinality());
        for (int index = bitSet.nextSetBit(0); index >= 0; index = bitSet.nextSetBit(index + 1)) {
            newPoints.add(points[index]);
        }
        return newPoints;
    }

    public double getSquareSegmentDistance(Point2D p0, Point2D p1, Point2D p2){
        double x0, y0, x1, y1, x2, y2, dx, dy, t;
        x1 = p1.getX();
        y1 = p1.getY();
        x2 = p2.getX();
        y2 = p2.getY();
        x0 = p0.getX();
        y0 = p0.getY();
        dx = x2 - x1;
        dy = y2 - y1;
        if (dx != 0.0d || dy != 0.0d) {
            t = ((x0 - x1) * dx + (y0 - y1) * dy)
                    / (dx * dx + dy * dy);

            if (t > 1.0d) {
                x1 = x2;
                y1 = y2;
            } else if (t > 0.0d) {
                x1 += dx * t;
                y1 += dy * t;
            }
        }
        dx = x0 - x1;
        dy = y0 - y1;
        return dx * dx + dy * dy;
    }
}

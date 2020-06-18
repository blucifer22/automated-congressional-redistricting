package levin;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import levin.District;
import levin.circle.SECCircle;
import levin.circle.SECPoint;
import levin.circle.Smallestenclosingcircle;
import levin.compactnessScore.HaversineDistance;

/** Manages several measures of compactness and the dynamic calculation thereof
 * @author Harry Levin (presumably)
 *
 */
public class Compactness {
    private District district;
    private double area;
    private double perimeter;
    private Geometry convexHull;
    private double smallestCircleArea;
    private double smallestCirclePerim;

    /**
     * Calculate several key compactness metrics and store them as class variables.
     * @param d The District that we're measuring
     */
    public Compactness(District d) {
        this.district = d;
        Geometry districtGeom = d.getRealGeometry(); // Gets the geometry of the district (Real geometry may be redundant?)
        this.area = districtGeom.getArea(); // Get area
        this.perimeter = districtGeom.getLength(); // Get perimeter
        this.convexHull = districtGeom.convexHull(); // Calculate the convex hull
    }

    /**
     * Calculates the Convex Hull Ratio measure of compactness.
     * The Convex Hull Ratio is defined as the area of the district
     * divided by the area of the convex hull of the same district.
     * @return The Convex Hull Ratio compactness measure of the district.
     */
    public double getConvexHullMeasure() {
        double result = this.district.getGeometry().getArea() / this.convexHull.getArea(); // Calculates the Convex Hull Ratio
        if (!this.validate(result)) { // Sanity check the result
            System.err.println("Result out of bounds: " + result); // Panic if we break math
            System.exit(0);
        }
        return result; // Return the result
    }

    /**
     * Calculates the Reock Compactness Ratio (ultimately unused in the paper, presumably left behind as legacy?)
     * The Reock Compactness Ratio is the ratio of a District's area to that of the minimum bounding circle that
     * surrounds it. The scale is self normalizing from 0 to 1, with 0 indicating minimally compact and 1 being most
     * compact.
     * @return The Reock Compactness Ratio for the provided District
     */
    public double getReockMeasure() {
        double result = this.district.getGeometry().getArea() / this.smallestCircleArea;
        if (!this.validate(result)) {
            System.err.println("Result out of bounds: " + result);
            System.exit(0);
        }
        return result;
    }

    /**
     * Calculate the Polsby-Popper Measure of district compactness.
     * The Polsby-Popper Ratio is a measure of the ratio between the area of a District
     * and the area of a circle whose circumference is the same as the District's area.
     * @return The Polsby-Popper Measure of compactness for the district.
     */
    public double getPolsbyPopperMeasure() {
        double districtPerim = this.district.getGeometry().getLength();
        double radius = districtPerim / 6.283185307179586;
        double circleArea = Math.pow(radius, 2.0) * 3.141592653589793;
        double result = this.district.getGeometry().getArea() / circleArea;
        if (!this.validate(result)) {
            System.err.println("Result out of bounds: " + result);
            System.exit(0);
        }
        return result;
    }

    /**
     * Calculate the Modified Schwartzberg Measure of district compactness.
     * The Modified Schwartzberg Ratio is a measure of the ratio between the perimeter of a circle with
     * the same area as the district and the perimeter of the actual district.
     * @return Modified Schwartzberg Ratio of compactness for the district.
     */
    public double getModifiedSchwartzberg() {
        double districtArea = this.district.getGeometry().getArea();
        double radius = Math.sqrt(districtArea / 3.141592653589793);
        double circlePerim = radius * 2.0 * 3.141592653589793;
        double result = circlePerim / this.district.getGeometry().getLength();
        if (!this.validate(result)) {
            System.err.println("Result out of bounds: " + result);
            System.exit(0);
        }
        return result;
    }

    /**
     * A simple validation check for the various measures of compactness.
     * Since all of the measures are normed from 0 to 1, if a measure exceeds that
     * range there must be an upstream/implementation error.
     * @param result The value of the measure of compactness in question.
     * @return Whether or not the result is within the range of 0 to 1 and thus, valid
     */
    private boolean validate(double result) {
        if (result >= 0.0 && result <= 1.0) {
            return true;
        }
        return false;
    }

    /**
     * Calculate the radius of the smallest enclosing circle of a given District.
     * NOTE: The Reock measure seems to have been thrown out for some reason (possibly complexity?),
     * thus this and its upstream libraries were not used for much of anything.
     * @param d The District in question
     * @return The radius of the smallest enclosing circle for the District in question.
     */
    public double getDistrictSmallestEnclosingCircleRadius(District d) {
        List<SECPoint> points = this.getPointsForGeometry(d.getGeometry());
        Smallestenclosingcircle circle = new Smallestenclosingcircle();
        SECCircle b = Smallestenclosingcircle.makeSECCircle(points);
        double radius = b.r;
        System.out.println("radius= " + radius);
        SECPoint centerPoint = b.c;
        double[] center = new double[]{centerPoint.x, centerPoint.y};
        System.out.println("center= " + center[0] + " , " + center[1]);
        double[] coordinates = new double[]{center[1], center[0]};
        HaversineDistance h = new HaversineDistance(Double.valueOf(center[0]), Double.valueOf(center[0] - radius), Double.valueOf(center[1]), Double.valueOf(center[1]));
        double actualRadius = h.getDistance();
        return actualRadius;
    }

    /**
     * Converts between "standard" Geometry points and SECPoints
     * @param geom A Geometry object
     * @return A list of SECPoints equivalent to the constituent points of the inputted geometry
     */
    public List<SECPoint> getPointsForGeometry(Geometry geom) {
        Coordinate[] coordinates = geom.getCoordinates();
        System.out.println("Num Coordinates: " + coordinates.length);
        ArrayList<SECPoint> points = new ArrayList<SECPoint>();
        Coordinate[] arrcoordinate = coordinates;
        int n = arrcoordinate.length;
        int n2 = 0;
        while (n2 < n) {
            Coordinate c = arrcoordinate[n2];
            SECPoint p = new SECPoint(c.x, c.y);
            points.add(p);
            ++n2;
        }
        return points;
    }
}
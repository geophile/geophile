package com.geophile.z.spatialobject.jts;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class PlayWithJTS
{
    public static void main(String[] args) throws ParseException
    {
        GeometryFactory factory = new GeometryFactory();
        WKTReader reader= new WKTReader(factory);
        Point p1 = (Point) reader.read("POINT(10 10)");
        print("p1: %s", p1);
        Point p2 = factory.createPoint(new Coordinate(20, 20));
        print("p2: %s", p2);
    }

    private static void print(String template, Object ... args)
    {
        System.out.println(String.format(template, args));
    }
}

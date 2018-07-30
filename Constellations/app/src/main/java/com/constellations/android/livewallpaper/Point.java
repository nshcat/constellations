package com.constellations.android.livewallpaper;


public class Point
{
    private int x;
    private int y;

    public static Point ORIGIN = new Point(0, 0);

    @Override
    public String toString()
    {
        return "(" + x + ", " + y + ")";
    }

    public Point()
    {
        this.x = 0;
        this.y = 0;
    }

    public Point(int x_, int y_)
    {
        this.x = x_;
        this.y = y_;
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public void setX(int val)
    {
        x = val;
    }

    public void setY(int val)
    {
        y = val;
    }

    public double distanceTo(Point other)
    {
        final int dx = x - other.x;
        final int dy = y - other.y;

        return Math.sqrt((double)(dx*dx) + (double)(dy*dy));
    }

    public boolean isNear(Point other, double maxDist)
    {
        final int dx = x - other.x;
        final int dy = y - other.y;

        final double distance = this.distanceTo(other);//Math.sqrt( (double)(dx*dx) + (double)(dy*dy));

        return (distance <= maxDist);
    }

    public Point clone()
    {
        return new Point(x, y);
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj == null)
            return false;

        if(!(obj instanceof Point))
            return false;

        Point p = (Point)obj;

        return (this.x == p.x) && (this.y == p.y);
    }
}

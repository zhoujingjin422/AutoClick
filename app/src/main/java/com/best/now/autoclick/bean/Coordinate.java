package com.best.now.autoclick.bean;

/**
 *
 * @author Administrator
 *
 */
public class Coordinate {
    public Coordinate() {
        // TODO Auto-generated constructor stub
    }

    public Coordinate(float x, float y) {
        // TODO Auto-generated constructor stub
        this.x = x;
        this.y = y;
    }

    private float x;
    private float y;

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return "[x:" + x + ", y:" + y + "]";
    }
}


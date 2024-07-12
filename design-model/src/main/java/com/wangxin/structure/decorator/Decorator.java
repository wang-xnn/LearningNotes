package com.wangxin.structure.decorator;

public abstract class Decorator implements Shape{
    protected Shape shape;

    public Decorator(Shape shape) {
        this.shape = shape;
    }

    public void draw() {
        shape.draw();
    }
}

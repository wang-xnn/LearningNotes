package com.wangxin.structure.decorator;

public class DecoratorTest {
    public static void main(String[] args) {
        Shape circle = new Circle();
        Shape rectangle = new Rectangle();


        Shape edgeCircle = new EdgeDecorator(circle);
        edgeCircle.draw();
        Shape contentDecorator = new ContentDecorator(edgeCircle);
        contentDecorator.draw();
    }
}

package com.wangxin.structure.decorator;

import lombok.Data;


public class EdgeDecorator extends Decorator{
    private String color;

    // 图形绘制边的颜色
    private final String DEFAULT_COLOR = "black";

    public EdgeDecorator(Shape shape) {
        super(shape);
        color = DEFAULT_COLOR;
    }

    @Override
    public void draw() {
        super.draw();
        drawEdge(color);
    }

    public void drawEdge(String color) {
        if (color == null) {
            color = DEFAULT_COLOR;
        }
        System.out.println("draw edge: color is " + color);
    }
}

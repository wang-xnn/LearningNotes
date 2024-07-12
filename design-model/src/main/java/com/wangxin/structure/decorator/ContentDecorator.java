package com.wangxin.structure.decorator;
public class ContentDecorator extends Decorator{
    private String tag;

    // 图形中间的图案
    private final String defaultTag = "star";

    public ContentDecorator(Shape shape) {
        super(shape);
        this.tag = defaultTag;
    }

    @Override
    public void draw() {
        super.draw();
        drawContent(tag);
    }
    public void drawContent(String tag) {
        if (tag == null) {
            tag = defaultTag;
        }
        System.out.println("Content tag:" + tag);
    }
}

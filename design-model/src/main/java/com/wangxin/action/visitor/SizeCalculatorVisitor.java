package src.main.java.com.wangxin.action.visitor;

public class SizeCalculatorVisitor implements Visitor{
    private int total = 0;
    @Override
    public void visit(FileSystemElement fileSystemElement) {
        total += fileSystemElement.getSize();
    }

    @Override
    public void visit(Directory directory) {
        directory.getElements().forEach(element -> element.accept(this));
    }


    public int getTotal() {
        return total;
    }
}

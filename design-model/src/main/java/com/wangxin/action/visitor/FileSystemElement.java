package src.main.java.com.wangxin.action.visitor;

public class FileSystemElement implements Element{
    private String name;
    private int size;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public FileSystemElement(String name, int size) {
        this.name = name;
        this.size = size;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }
}

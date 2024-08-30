package src.main.java.com.wangxin.action.visitor;

import java.util.ArrayList;
import java.util.List;

public class Directory implements Element{
    private String name;
    private List<FileSystemElement> fileSystemElements;

    public Directory(String name) {
        this.name = name;
        fileSystemElements = new ArrayList<>();
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addElement(FileSystemElement element) {
        fileSystemElements.add(element);
    }

    public List<FileSystemElement> getElements() {
        return fileSystemElements;
    }
}

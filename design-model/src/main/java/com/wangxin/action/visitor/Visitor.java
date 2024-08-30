package src.main.java.com.wangxin.action.visitor;

public interface Visitor {
    void visit(FileSystemElement fileSystemElement);

    void visit(Directory directory);
}

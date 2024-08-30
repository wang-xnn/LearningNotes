package src.main.java.com.wangxin.action.visitor;

public class VistorTest {
    public static void main(String[] args) {
        Directory root = new Directory("root");
        root.addElement(new FileSystemElement("file1", 100));
        root.addElement(new FileSystemElement("file2", 200));
        FileSystemElement file = new FileSystemElement("file3", 300);

        SizeCalculatorVisitor sizeCalculatorVisitor = new SizeCalculatorVisitor();
        root.accept(sizeCalculatorVisitor);
        System.out.println(sizeCalculatorVisitor.getTotal());
        file.accept(sizeCalculatorVisitor);
        System.out.println(sizeCalculatorVisitor.getTotal());
        FileNameListVisitor fileNameListVisitor = new FileNameListVisitor();
        root.accept(fileNameListVisitor);
        System.out.println(fileNameListVisitor.getFileNames());
        file.accept(fileNameListVisitor);
        System.out.println(fileNameListVisitor.getFileNames());
    }
}

package src.main.java.com.wangxin.action.visitor;

import java.util.ArrayList;
import java.util.List;

public class FileNameListVisitor implements Visitor{
    private List<String> fileNames = new ArrayList<>();

    public void visit(Directory directory) {
        System.out.println("Directory: " + directory.getName());
        for (FileSystemElement element : directory.getElements()) {
            element.accept(this);
        }
    }

    public void visit(FileSystemElement fileSystemElement) {
        fileNames.add(fileSystemElement.getName());
    }

    public List<String> getFileNames() {
        return fileNames;
    }
}

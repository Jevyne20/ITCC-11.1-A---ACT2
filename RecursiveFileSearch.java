import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class FileNode {
    String name;
    boolean isDirectory;
    List<FileNode> children;

    FileNode(String name, boolean isDirectory) {
        this.name = name;
        this.isDirectory = isDirectory;
        this.children = new ArrayList<>();
    }

    void addChild(FileNode child) {
        if (this.isDirectory) {
            this.children.add(child);
        }
    }
}

class FileSearchEvent {
    String filePath;

    FileSearchEvent(String filePath) {
        this.filePath = filePath;
    }
}

interface FileSearchListener {
    void fileFound(FileSearchEvent event);
}

class FileSearch {
    private List<FileSearchListener> listeners = new ArrayList<>();
    private String targetExtension;
    private String startingPath;
    private PrintWriter writer;
    private FileNode root;

    public FileSearch(String startingPath, String targetExtension, PrintWriter writer, FileNode root) {
        this.startingPath = startingPath;
        this.targetExtension = targetExtension;
        this.writer = writer;
        this.root = root;
    }

    public void addListener(FileSearchListener listener) {
        listeners.add(listener);
    }

    private void fireFileFoundEvent(String filePath) {
        FileSearchEvent event = new FileSearchEvent(filePath);
        for (FileSearchListener listener : listeners) {
            listener.fileFound(event);
        }
    }

    public void search(FileNode node, String currentPath) {
        if (node == null) {
            return;
        }

        if (!node.isDirectory) {
            if (node.name.endsWith(targetExtension)) {
                String filePath = currentPath + "\\" + node.name;
                fireFileFoundEvent(filePath);
                writer.println("File found: " + filePath);
            }
        } else {
            for (FileNode child : node.children) {
                search(child, currentPath + "\\" + node.name);
            }
        }
    }

    public void performSearch() {
        System.out.println("Searching...");
        search(root, startingPath);
        System.out.println("Search completed. Results saved to search_results.txt.");
    }
}

public class RecursiveFileSearch {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter directory path: ");
        String directoryPath = scanner.nextLine();
        System.out.print("Enter file extension to search for: ");
        String fileExtension = scanner.nextLine();

        FileNode root = buildMockFileSystem(directoryPath);

        try (PrintWriter writer = new PrintWriter(new FileWriter("search_results.txt"))) {
            FileSearch fileSearch = new FileSearch(directoryPath, fileExtension, writer, root);

            fileSearch.addListener(event -> {
                System.out.println("File found: " + event.filePath);
            });

            fileSearch.performSearch();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    private static FileNode buildMockFileSystem(String startingPath) {
        FileNode root = new FileNode(startingPath, true);
        FileNode folder1 = new FileNode("subfolder", true);
        FileNode file1 = new FileNode("notes.txt", false);
        FileNode file2 = new FileNode("todo.txt", false);
        FileNode file3 = new FileNode("document.java", false);
        root.addChild(file1);
        root.addChild(folder1);
        folder1.addChild(file2);
        root.addChild(file3);
        return root;
    }
}
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileListMaker {
    private static final Scanner input = new Scanner(System.in);
    private static final List<String> items = new ArrayList<>();
    private static Path currentFile = null;
    private static boolean needsToBeSaved = false;

    public static void main(String[] args) {
        boolean running = true;
        while (running) {
            showMenu();
            String choice = readText("Choose an option: ").trim().toUpperCase();
            try {
                switch (choice) {
                    case "A" -> add();
                    case "D" -> delete();
                    case "I" -> insert();
                    case "M" -> move();
                    case "V" -> view();
                    case "O" -> open();
                    case "S" -> save();
                    case "C" -> clear();
                    case "Q" -> {
                        if (protectUnsaved()) {
                            running = false;
                            System.out.println("Goodbye!");
                        }
                    }
                    default -> System.out.println("Invalid option. Try again.");
                }
            } catch (IOException ex) {
                System.out.println("File error: " + ex.getMessage());
            }
        }
    }

    private static void showMenu() {
        System.out.println("\n=== File List Maker ===");
        System.out.println("A - Add | D - Delete | I - Insert | M - Move | V - View");
        System.out.println("O - Open | S - Save | C - Clear | Q - Quit");
        System.out.println("Current file: " + (currentFile == null ? "(new list)" : currentFile.getFileName())
                + (needsToBeSaved ? " [UNSAVED CHANGES]" : ""));
    }

    private static String readText(String prompt) {
        System.out.print(prompt);
        return input.nextLine();
    }

    private static int readIndex(String prompt, int min, int max) {
        while (true) {
            try {
                int number = Integer.parseInt(readText(prompt).trim());
                if (number >= min && number <= max) return number;
            } catch (NumberFormatException ignored) { }
            System.out.println("Enter a number from " + min + " to " + max + ".");
        }
    }

    private static boolean yesNo(String prompt) {
        while (true) {
            String answer = readText(prompt + " (Y/N): ").trim();
            if (answer.equalsIgnoreCase("Y")) return true;
            if (answer.equalsIgnoreCase("N")) return false;
            System.out.println("Please enter Y or N.");
        }
    }

    private static void view() {
        if (items.isEmpty()) {
            System.out.println("The list is empty.");
            return;
        }
        System.out.println("--- Current list ---");
        for (int i = 0; i < items.size(); i++) System.out.println((i + 1) + ". " + items.get(i));
    }

    private static void add() {
        String item = readText("Item to add: ");
        items.add(item);
        needsToBeSaved = true;
        System.out.println("Item added.");
    }

    private static void delete() {
        if (items.isEmpty()) { System.out.println("Nothing to delete."); return; }
        view();
        int index = readIndex("Item number to delete: ", 1, items.size());
        System.out.println("Deleted: " + items.remove(index - 1));
        needsToBeSaved = true;
    }

    private static void insert() {
        int index = readIndex("Insert at position (1-" + (items.size() + 1) + "): ", 1, items.size() + 1);
        String item = readText("Item to insert: ");
        items.add(index - 1, item);
        needsToBeSaved = true;
        System.out.println("Item inserted.");
    }

    private static void move() {
        if (items.isEmpty()) { System.out.println("Nothing to move."); return; }
        view();
        int from = readIndex("Move item number: ", 1, items.size());
        int to = readIndex("Move to position: ", 1, items.size());
        if (from == to) { System.out.println("Item already in that position."); return; }
        String item = items.remove(from - 1);
        items.add(to - 1, item);
        needsToBeSaved = true;
        System.out.println("Item moved.");
    }

    private static void clear() {
        if (items.isEmpty()) { System.out.println("List already empty."); return; }
        if (yesNo("Clear all items?")) {
            items.clear();
            needsToBeSaved = true;
            System.out.println("List cleared. Save to keep this change.");
        }
    }

    // Returns false if the user cancels the operation or saving fails.
    private static boolean protectUnsaved() throws IOException {
        if (!needsToBeSaved) return true;
        while (true) {
            String answer = readText("Unsaved changes: (S)ave, (D)iscard, or (C)ancel? ").trim().toUpperCase();
            switch (answer) {
                case "S":
                    save();
                    return !needsToBeSaved;
                case "D": return true;
                case "C": return false;
                default: System.out.println("Enter S, D, or C.");
            }
        }
    }

    private static Path txtPath(String name) {
        name = name.trim();
        if (!name.toLowerCase().endsWith(".txt")) name += ".txt";
        return Path.of(name);
    }

    private static void open() throws IOException {
        if (!protectUnsaved()) { System.out.println("Open cancelled."); return; }
        String name = readText("Filename to open (.txt): ").trim();
        if (name.isEmpty()) { System.out.println("Open cancelled: no filename."); return; }
        Path path = txtPath(name);
        List<String> loaded = loadFile(path);
        items.clear();
        items.addAll(loaded);
        currentFile = path;
        needsToBeSaved = false;
        System.out.println("Loaded " + items.size() + " items from " + path + ".");
    }

    private static void save() throws IOException {
        Path path = currentFile;
        if (path == null) {
            String name = readText("New list filename (.txt): ").trim();
            if (name.isEmpty()) { System.out.println("Save cancelled: no filename."); return; }
            path = txtPath(name);
            if (Files.exists(path) && !yesNo("File exists. Overwrite it?")) {
                System.out.println("Save cancelled.");
                return;
            }
        }
        saveFile(path, items);
        currentFile = path;
        needsToBeSaved = false;
        System.out.println("Saved " + items.size() + " items to " + path + ".");
    }

    private static List<String> loadFile(Path path) throws IOException {
        return Files.readAllLines(path, StandardCharsets.UTF_8);
    }

    private static void saveFile(Path path, List<String> data) throws IOException {
        Files.write(path, data, StandardCharsets.UTF_8);
    }
}

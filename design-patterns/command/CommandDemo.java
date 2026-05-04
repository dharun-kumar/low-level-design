import java.util.Stack;

class TextEditor {
    private StringBuilder content = new StringBuilder();

    public void write(int position, String text) {
        content.insert(position, text);
    }

    public void remove(int position, int length) {
        content.delete(position, position + length);
    }

    public String getContent() {
        return content.toString();
    }
}

interface Command {
    void execute();
    void undo();
}

class WriteCommand implements Command {
    private final TextEditor editor;
    private final int position;
    private final String text;

    public WriteCommand(TextEditor editor, int position, String text) {
        this.editor = editor;
        this.position = position;
        this.text = text;
    }

    @Override
    public void execute() {
        editor.write(position, text);
    }

    @Override
    public void undo() {
        editor.remove(position, text.length());
    }
}

class RemoveCommand implements Command {
    private final TextEditor editor;
    private final int position;
    private final int length;
    private String text;

    public RemoveCommand(TextEditor editor, int position, int length) {
        this.editor = editor;
        this.position = position;
        this.length = length;
    }

    @Override
    public void execute() {
        text = editor.getContent().substring(position, position + length);
        editor.remove(position, position + length);
    }

    @Override
    public void undo() {
        editor.write(position, text);
    }
}

class Invoker {
    private Stack<Command> undoStack = new Stack<>();
    private Stack<Command> redoStack = new Stack<>();

    public void executeCommand(Command command) {
        command.execute();
        undoStack.push(command);
    }

    public void undo() {
        if(!undoStack.isEmpty()) {
            Command command = undoStack.pop();
            command.undo();
            redoStack.push(command);
        }
    }

    public void redo() {
        if(!redoStack.isEmpty()) {
            Command command = redoStack.pop();
            command.execute();
            undoStack.push(command);
        }
    }
}

public class CommandDemo {
    public static void main(String[] main) {
        TextEditor editor = new TextEditor();
        Invoker invoker = new Invoker();

        invoker.executeCommand(new WriteCommand(editor, 0, "Hello "));
        System.out.println(editor.getContent());

        invoker.executeCommand(new WriteCommand(editor, 6, "World !"));
        System.out.println(editor.getContent());

        invoker.executeCommand(new RemoveCommand(editor, 6, 7));
        System.out.println(editor.getContent());

        invoker.undo();
        System.out.println(editor.getContent());

        invoker.redo();
        System.out.println(editor.getContent());
    }
}
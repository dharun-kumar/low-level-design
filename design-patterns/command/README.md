# Command Pattern

## What is it?
Encapsulates a request as an object. This lets you **parameterise** operations, **queue** them, **log** them, and support **undo/redo** — all without the invoker knowing anything about the receiver.

---

## Example — Text Editor with Undo / Redo

Every edit operation (`write`, `remove`) is wrapped in a `Command` object. The `Invoker` maintains an undo stack and a redo stack, enabling full undo/redo without the editor itself tracking history.

```
Client
  │ creates
  ▼
WriteCommand / RemoveCommand          TextEditor (Receiver)
──────────────────────────            ────────────────────
- editor: TextEditor        ────────▶ + write(pos, text)
- position: int                       + remove(pos, length)
- text: String                        + getContent()
+ execute()
+ undo()
        │ passed to
        ▼
     Invoker
  ─────────────────────
  - undoStack: Stack
  - redoStack: Stack
  + executeCommand(cmd) → cmd.execute(); undoStack.push(cmd)
  + undo()              → cmd.undo();   redoStack.push(cmd)
  + redo()              → cmd.execute(); undoStack.push(cmd)
```

**Undo/Redo flow:**
```
execute(WriteCommand "Hello")  → content: "Hello"     | undo: [W]
execute(WriteCommand "World")  → content: "Hello World"| undo: [W,W]
execute(RemoveCommand 6,5)     → content: "Hello"     | undo: [W,W,R]
undo()                         → content: "Hello World"| redo: [R]
redo()                         → content: "Hello"     | undo: [W,W,R]
```

---

## Key Roles

| Role | Class |
|---|---|
| Command Interface | `Command` |
| Concrete Commands | `WriteCommand`, `RemoveCommand` |
| Receiver | `TextEditor` |
| Invoker | `Invoker` |
| Client | `CommandDemo` |

---

## Design Decisions
- **`RemoveCommand` stores deleted text** in `execute()` — needed to restore it in `undo()`. The command captures state at execution time, not creation time.
- **Two stacks** — redo stack is cleared implicitly when a new command executes (not shown here but the standard pattern); undo stack grows with every execute.
- **Receiver (`TextEditor`) stays clean** — it has no knowledge of history or undo. Single Responsibility stays intact.

---

## When to Use
- You need **undo/redo** functionality.
- You want to **queue or schedule** operations.
- You need to support **transactional** behaviour (rollback on failure).
- Operations should be **logged** and replayable.

## Real-World Examples
- Text editors, IDEs (Ctrl+Z / Ctrl+Y)
- Database transactions (commit / rollback)
- Job queues / task schedulers
- GUI button actions (each button wraps a command)

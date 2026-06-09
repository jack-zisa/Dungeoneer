package dev.creoii.dungeoneer.client.editor.action;

import java.util.ArrayList;
import java.util.List;

public class CompositeAction implements EditorAction {
    private final List<EditorAction> actions;

    public CompositeAction() {
        this.actions = new ArrayList<>();
    }

    public int size() {
        return actions.size();
    }

    public void add(EditorAction action) {
        actions.add(action);
    }

    @Override
    public void redo() {
        actions.forEach(EditorAction::redo);
    }

    @Override
    public void undo() {
        for (int i = actions.size() - 1; i >= 0; i--) {
            actions.get(i).undo();
        }
    }
}

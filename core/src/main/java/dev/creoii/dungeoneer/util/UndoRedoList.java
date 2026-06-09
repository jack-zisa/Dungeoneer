package dev.creoii.dungeoneer.util;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class UndoRedoList<E> {
    private final ArrayList<E> list = new ArrayList<>();
    private int size = 0;

    public void add(E element) {
        while (list.size() > size) {
            list.removeLast();
        }
        list.add(element);
        size++;
    }

    @Nullable
    public E redo() {
        if (!canRedo())
            return null;

        E action = list.get(size);
        ++size;
        return action;
    }

    @Nullable
    public E undo() {
        if (!canUndo())
            return null;

        E action = list.get(size - 1);
        --size;
        return action;
    }

    public boolean canRedo() {
        return size < list.size();
    }

    public boolean canUndo() {
        return size > 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        size = 0;
        list.clear();
    }
}

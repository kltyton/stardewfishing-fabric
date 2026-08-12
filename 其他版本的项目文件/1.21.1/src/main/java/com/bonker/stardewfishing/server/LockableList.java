package com.bonker.stardewfishing.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class LockableList<T> extends ArrayList<T> {
    private boolean locked = false;

    public void lock() {
        locked = true;
    }

    public void unlock() {
        locked = false;
    }

    public boolean isLocked() {
        return locked;
    }

    @Override
    public void trimToSize() {
        if (locked) return;
        super.trimToSize();
    }

    @Override
    public void ensureCapacity(int minCapacity) {
        if (locked) return;
        super.ensureCapacity(minCapacity);
    }

    @Override
    public T set(int index, T element) {
        if (locked) return null;
        return super.set(index, element);
    }

    @Override
    public boolean add(T t) {
        if (locked) return false;
        return super.add(t);
    }

    @Override
    public void add(int index, T element) {
        if (locked) return;
        super.add(index, element);
    }

    @Override
    public T remove(int index) {
        if (locked) return null;
        return super.remove(index);
    }

    @Override
    public boolean remove(Object o) {
        if (locked) return false;
        return super.remove(o);
    }

    @Override
    public void clear() {
        if (locked) return;
        super.clear();
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        if (locked) return false;
        return super.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        if (locked) return false;
        return super.addAll(index, c);
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        if (locked) return;
        super.removeRange(fromIndex, toIndex);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        if (locked) return false;
        return super.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        if (locked) return false;
        return super.retainAll(c);
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        if (locked) return false;
        return super.removeIf(filter);
    }

    @Override
    public void replaceAll(UnaryOperator<T> operator) {
        if (locked) return;
        super.replaceAll(operator);
    }

    @Override
    public void sort(Comparator<? super T> c) {
        if (locked) return;
        super.sort(c);
    }
}

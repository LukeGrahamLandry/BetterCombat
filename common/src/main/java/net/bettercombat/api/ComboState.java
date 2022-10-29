package net.bettercombat.api;

import java.util.Objects;

public final class ComboState {
    private final int current;
    private final int total;

    public ComboState(int current, int total) {
        this.current = current;
        this.total = total;
    }

    public int current() {
        return current;
    }

    public int total() {
        return total;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        ComboState that = (ComboState) obj;
        return this.current == that.current &&
                this.total == that.total;
    }

    @Override
    public int hashCode() {
        return Objects.hash(current, total);
    }

    @Override
    public String toString() {
        return "ComboState[" +
                "current=" + current + ", " +
                "total=" + total + ']';
    }
}

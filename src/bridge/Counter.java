package bridge;

public class Counter {

    private int length = 0;
    private int count = 0;

    public void add(int length) {
        this.length += length;
        count++;
    }

    @Override
    public String toString() {
        return length + "B/" + count + "x";
    }
}

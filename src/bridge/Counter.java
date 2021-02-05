package bridge;

public class Counter {

    private int contentLength = 0;
    private int contentCount = 0;
    private long contentTime = 0;
    public void content(int length, long time) {
        this.contentLength += length;
        contentCount++;

        contentTime += time;
    }

    private int zeroCount = 0;
    private long zeroTime = 0;
    public void zero(long time) {
        zeroCount++;

        zeroTime += time;
    }

    @Override
    public String toString() {
        return "content "
                + contentLength / 1024 / 1024 + "MB "
                + "/ " + contentLength + "B "
                + "/ " + contentCount / 1000 + "kx "
                + "/ " + contentCount + "x "
                + "/ " + contentTime / 1000 / 1000 + "ms "
                + "/ " + contentTime + "ns"
                + ", zero "
                + zeroCount / 1000 + "kx "
                + "/ " + zeroCount + "x "
                + "/ " + zeroTime / 1000 / 1000 + "ms "
                + "/ " + zeroTime + "ns";
    }
}

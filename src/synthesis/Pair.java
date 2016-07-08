package synthesis;

public class Pair<T, S> {
    public T fst;
    public S snd;

    public Pair(T fst, S snd) {
        this.fst = fst;
        this.snd = snd;
    }

    @Override
    public String toString() {
        return fst + ", " + snd;
    }
}

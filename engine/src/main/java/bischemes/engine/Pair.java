package bischemes.engine;

public class Pair<T> {
	public T a;
	public T b;

	public int hashCode() {
		return a.hashCode() + 97 * b.hashCode();
	}

	public boolean equals(Object o) {
		if (o instanceof Pair<?>) {
			Pair<?> ot = (Pair<?>) o;
			return (ot.a == a && ot.b == b) || (ot.b == a && ot.a == b);
		} else {
			return false;
		}
	}

	public Pair(T a, T b) {
		this.a = a;
		this.b = b;
	}
}

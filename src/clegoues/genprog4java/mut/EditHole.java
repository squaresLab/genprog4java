package clegoues.genprog4java.mut;


public interface EditHole<T> extends Comparable<EditHole<T>> {

	public String getName();
	public T getCode();
	public void setCode(T hole);
	public void setName(String name);
	public double getWeight();
	public void setWeight(double weight);

}

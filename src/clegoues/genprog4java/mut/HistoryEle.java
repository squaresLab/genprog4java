package clegoues.genprog4java.mut;

public class HistoryEle {
	private Mutation mtype = null;
	private int location = -1;
	private int fixCode = -1;
	public HistoryEle() { }
	public HistoryEle(Mutation m, int location) {
		this.mtype = m;
		this.location = location;
	}
	public HistoryEle(Mutation m, int loc, int fix) {
		this.mtype = m;
		this.location = loc;
		this.fixCode = fix;
	}
	public Mutation getMtype() {
		return mtype;
	}
	public void setMtype(Mutation mtype) {
		this.mtype = mtype;
	}
	public int getLocation() {
		return location;
	}
	public void setLocation(int location) {
		this.location = location;
	}
	public int getFixCode() {
		return fixCode;
	}
	public void setFixCode(int fixCode) {
		this.fixCode = fixCode;
	}
	public String toString() {
		String result = "";
		switch(this.mtype) {
		case APPEND:
			result += "a(" + this.location + "," + this.fixCode + ")";
			break;
		case DELETE: result += "d(" + this.location + ")";
			break;
		case SWAP: result += "s(" + this.location + "," + this.fixCode + ")";
			break;
		case REPLACE: result += "r(" + this.location + "," + this.fixCode + ")";
			break;
		}
		return result;
	}
	
}

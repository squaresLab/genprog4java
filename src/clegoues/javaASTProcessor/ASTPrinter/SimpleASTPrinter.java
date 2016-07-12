package clegoues.javaASTProcessor.ASTPrinter;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Queue;


public class SimpleASTPrinter extends Indenter implements IASTPrinter {

	private final PrintWriter printer;
	private Queue<String> outputQueue = new LinkedList<String>();
	

	public SimpleASTPrinter(OutputStream destination) {
		this.printer = new PrintWriter(destination);
	}

	// FIXME: this overloading of the bool is really not cool, but I'm just playng...
	public void startElement(String name, boolean pushName) {
		outputQueue.add(name);
	}

	private void flushQueue() {
		while(!outputQueue.isEmpty()) {
			String next = outputQueue.remove();
			printer.print(next + " ");
		}
	}

	public void endElement(String name, boolean isList) {
		this.flushQueue();
	}

	
	public void startType(String name, boolean pushName) {
		outputQueue.add(name);
	}

	public void endType(String name, boolean pushName) {
		this.flushQueue();
		printer.println();
	}

	public void literal(String name, Object value) {
		outputQueue.add(name);
	}
	public void literal(String name) {
		this.literal(name,null);
	}
	
	public void startPrint() {
	}

	public void endPrint() {
		this.flushQueue();
		printer.println();
		printer.flush();
		printer.close();
	}
}
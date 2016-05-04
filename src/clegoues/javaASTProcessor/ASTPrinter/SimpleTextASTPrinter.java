package clegoues.javaASTProcessor.ASTPrinter;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.lang3.StringEscapeUtils;


public class SimpleTextASTPrinter extends Indenter implements IASTPrinter {
	private final PrintWriter printer;

	public SimpleTextASTPrinter(OutputStream destination) {
		this.printer = new PrintWriter(destination);
	}
	
	private final Stack<Boolean> hasItemsStack = new Stack<Boolean>() {
		private static final long serialVersionUID = 1L;

		{
			// Make sure there is one item on the stack which represents
			// the root node.
			push(false);
		}
	};	
	
	public void startElement(String name, boolean isList) {
		
		if (hasItemsStack.peek() == true) {
			printer.println(",");
		} else {
			hasItemsStack.pop();
			hasItemsStack.push(true);
		}

		printer.println(getIndentString() + name + ": " + (isList ? "[" : "{"));
		indent();
		hasItemsStack.push(false);
	}

	public void endElement(String name, boolean isList) {
		unindent();
		printer.print("\n" + getIndentString() + (isList ? "]" : "}"));
		hasItemsStack.pop();
	}

	
	public void startType(String name, boolean parentIsList) {
		if (hasItemsStack.peek() == true) {
			printer.println(",");
		} else {
			hasItemsStack.pop();
			hasItemsStack.push(true);
		}

		if (parentIsList) {
			printer.println(getIndentString() + "{");
			indent();
		}

		printer.print(getIndentString() + name + "\"");
	}

	public void endType(String name, boolean parentIsList) {
		if (parentIsList) {
			unindent();
			printer.print("\n" + getIndentString() + "}");
		}
	}

	private static final Set<Class<? extends Object>> JSON_ALLOWED_WRAPPER_TYPES = new HashSet<Class<? extends Object>>(
			Arrays.asList(Boolean.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class,
					ArrayList.class));

	private static boolean isJsonAllowedType(Class<? extends Object> clazz) {
		return JSON_ALLOWED_WRAPPER_TYPES.contains(clazz);
	}

	public void literal(String name, Object value) {
		if (hasItemsStack.peek() == true) {
			printer.println(",");
		} else {
			hasItemsStack.pop();
			hasItemsStack.push(true);
		}

		printer.print(getIndentString() + name + ": " + (value == null || isJsonAllowedType(value.getClass()) ? value
				: "\"" + StringEscapeUtils.escapeJson(value.toString()) + "\""));
	}

	public void startPrint() {
		printer.println("{");
		indent();
	}

	public void endPrint() {
		printer.println();
		unindent();
		printer.println("}");
		printer.flush();
		printer.close();
	}
}

package org.baldurproject.compile;

import java.util.LinkedList;
import java.util.List;

public class VerilogModule {
	private final String name;
	private final List<String> ports;
	private final List<String> wires;
	private final List<String> initialization;
	private final List<String> body;
	
	public VerilogModule(
		final String name
	) {
		this.name = name;
		this.ports = new LinkedList<String>();
		this.wires = new LinkedList<String>();
		this.initialization = new LinkedList<String>();
		this.body = new LinkedList<String>();
	}

	public void addPort(final String port) {
		ports.add(port);
	}

	public void addWire(final String wire) {
		wires.add(wire);
	}
	
	public void addInitialization(final String initial) {
		initialization.add(initial);
	}

	public void addBody(final String code) {
		body.add(code);
	}
	
	public String toVerilog() {
		return 
			"module " + name + " (\n" +
				indent(toVerilog(ports)).replaceAll(",$", "") +
			");\n" +
				indent(toVerilog(wires)) +
				"\n" +
				"  initial begin\n" +	
				indent(indent(toVerilog(initialization))) +
				"  end\n" +
				"\n" +
				indent(toVerilog(body)) +
			"endmodule\n";
	}
	
    private String toVerilog(List<String> lines) {
		StringBuilder b =
			new StringBuilder();
		
		for (String port : lines) {
			b.append(port).append("\n");
		}
		
		return b.toString();
	}

	private static String indent(final String input) {
        if (input != null) {
            return "  " + input.replaceAll("\n", "\n  ").replaceAll("\n  $", "\n");
        } else {
            return "null";
        }
    }
}

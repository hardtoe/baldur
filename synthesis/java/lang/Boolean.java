package java.lang;

import org.baldurproject.Inline;

public class Boolean {
	private final boolean value;
	
	@Inline
	public Boolean(final boolean value) {
		this.value = value;
	}
}
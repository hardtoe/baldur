// This file is part of the Java Compiler Kit (JKit)
//
// The Java Compiler Kit is free software; you can 
// redistribute it and/or modify it under the terms of the 
// GNU General Public License as published by the Free Software 
// Foundation; either version 2 of the License, or (at your 
// option) any later version.
//
// The Java Compiler Kit is distributed in the hope
// that it will be useful, but WITHOUT ANY WARRANTY; without 
// even the implied warranty of MERCHANTABILITY or FITNESS FOR 
// A PARTICULAR PURPOSE.  See the GNU General Public License 
// for more details.
//
// You should have received a copy of the GNU General Public 
// License along with the Java Compiler Kit; if not, 
// write to the Free Software Foundation, Inc., 59 Temple Place, 
// Suite 330, Boston, MA  02111-1307  USA
//
// (C) David James Pearce, 2009. 

package jkit.jil.tree;

import java.lang.reflect.Array;
import java.util.*;


import jkit.compiler.SyntacticAttribute;
import jkit.compiler.SyntacticElementImpl;
import jkit.jil.tree.Type.Primitive;


public class JilField extends SyntacticElementImpl implements jkit.compiler.Clazz.Field {
	private String name;
	private Type type;

	private List<Modifier> modifiers;
	
	private Object value;
	private String verilogName;
	
	/**
     * Construct an object representing a field of a JVM class.
     * 
     * @param name - The name of the field.
     * @param type - The (fully generic) type of the field.
     * @param modifiers - Any modifiers of the field (e.g. public, static, etc)
     */
	public JilField(
		final String name, 
		final Type type, 
		final List<Modifier> modifiers,
		final SyntacticAttribute... attributes
	) {
		super(attributes);
		this.name = name;
		this.type = type;
		this.modifiers = modifiers;
	}

	public void setType(Type type) {
		this.type = type;
	}
	
	/**
	 * Set a module field initial value as determined by the
	 * elaboration process.
	 * 
	 * @param value
	 */
	public void setElabValue(final Object value) {
		this.value = value;
	}
	
	/**
	 * Get the module field initial elaboration value.
	 * 
	 * @return
	 */
	public Object getElabValue() {
		return this.value;
	}
	
	/**
     * Construct an object representing a field of a JVM class.
     * 
     * @param name - The name of the field.
     * @param type - The (fully generic) type of the field.
     * @param modifiers - Any modifiers of the field (e.g. public, static, etc)
     */
	public JilField(
		final String name, 
		final Type type, 
		final List<Modifier> modifiers,
		final List<SyntacticAttribute> attributes
	) {
		super(attributes);
		this.name = name;
		this.type = type;
		this.modifiers = modifiers;
	}
	
	/**
     * Access the name of this field.  
     * 
     * @return
     */
	public String name() {
		return name;
	}
	
	/**
     * Access the type of this field. This is useful for determining it's
     * package, and/or any generic parameters it declares.
     * 
     * @return
     */
	public Type type() {
		return type;
	}
	
	/**
     * Access the modifiers contained in this field object. The returned list
     * may be modified by adding, or removing modifiers. The returned list is
     * always non-null.
     * 
     * @return
     */
	public List<Modifier> modifiers() { return modifiers; }
	
	/**
     * Check whether this field has one of the "base" modifiers (e.g. static,
     * public, private, etc). These are found in Modifier.ACC_
     * 
     * @param modifier
     * @return true if it does!
     */
	public boolean hasModifier(Class modClass) {
		for(Modifier m : modifiers) {
			if(m.getClass().equals(modClass)) {
				return true;
			}			
		}
		return false;
	}
	
	/**
	 * Check whether this field is abstract
	 */
	public boolean isAbstract() {
		for(Modifier m : modifiers) {
			if(m instanceof Modifier.Abstract) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check whether this field is final
	 */
	public boolean isFinal() {		
		for(Modifier m : modifiers) {
			if(m instanceof Modifier.Final) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check whether this field is static
	 */
	public boolean isStatic() {		
		for(Modifier m : modifiers) {
			if(m instanceof Modifier.Static) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check whether this field is public
	 */
	public boolean isPublic() {
		for(Modifier m : modifiers) {
			if(m instanceof Modifier.Public) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check whether this field is protected
	 */
	public boolean isProtected() {
		for(Modifier m : modifiers) {
			if(m instanceof Modifier.Protected) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check whether this field is private
	 */
	public boolean isPrivate() {		
		for(Modifier m : modifiers) {
			if(m instanceof Modifier.Private) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Check whether this method is synthetic
	 */
	public boolean isSynthetic() {
		for (Modifier m : modifiers) {
			if (m instanceof Modifier.Synthetic) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isConstant() {
		return false;
	}
	
	public Object constant() {
		return null;
	}
	
	@Override
	public String toString() {
		/*
		private String name;
		private Type type;
		private List<Modifier> modifiers;
		*/
		
		if (getElabValue() == null) {
			return modifiers + " " + type + " " + name;
			
		} else {
			return modifiers + " " + type + " " + name + " = " + getElabValue();
		}
	}

	public String getVerilogName() {
		return verilogName;
	}

	public String getVerilogType() {
		Type t;
		
		if (type instanceof Type.Array) {
			t = ((Type.Array) type).element();
			
		} else {
			t = type;
		}
		
		if (t instanceof Type.Bool) {
			return "[0:0]";
			
		} else if (t instanceof Type.Byte) {
			return "[7:0]";
			
		} else if (t instanceof Type.Char) {
			return "[7:0]";
			
		} else if (t instanceof Type.Short) {
			return "[15:0]";
			
		} else if (t instanceof Type.Int) {
			return "[31:0]";
			
		} else if (t instanceof Type.Long) {
			return "[63:0]";
			
		} else {
			return "[0:0]"; // FIXME: this is a hack for Net!
			//throw new Error("Unknown type: " + type);
		}
	}

	public void setVerilogName(String verilogName) {
		if (verilogName != null) {
			this.verilogName = verilogName.replaceAll("<.+?>", "").replaceAll("\\.", "_");
		}
	}

	public String getVerilogArrayDimensions() {
		try {
			return "[" + (Array.getLength(getElabValue()) - 1) + ":0]";
		} catch (IllegalArgumentException e) {
			return "";
		}
	}
}

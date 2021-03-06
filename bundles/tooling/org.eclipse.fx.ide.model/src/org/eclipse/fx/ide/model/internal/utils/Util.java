/*******************************************************************************
 * Copyright (c) 2012 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl<tom.schindl@bestsolution.at> - initial API and implementation
 *******************************************************************************/
package org.eclipse.fx.ide.model.internal.utils;

import org.eclipse.fx.ide.model.IFXPrimitiveProperty;
import org.eclipse.fx.ide.model.IFXPrimitiveProperty.Type;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;


public class Util {
	@SuppressWarnings("unchecked")
	public static <R> R getAnnotationMemberValue(IAnnotation annotation, String name) throws JavaModelException {
		for (IMemberValuePair pair : annotation.getMemberValuePairs()) {
			if( name.equals(pair.getMemberName()) ) {
				return (R) pair.getValue();
			}
		}
		return null;
	}
	
	public static boolean checkStatemask(int state, int mask) {
		return (state & mask) != 0;
	}
	
	public static String getFQNType(IType referenceType, String name) throws JavaModelException {
		// no need to resolve it is already an FQN
		if( name.contains(".") ) {
			return name;
		}
		
		// This is a primitive type
		for( Type t : IFXPrimitiveProperty.Type.values() ) {
			if( t.jvmType().equals(name) ) {
				return null;
			}
		}
		
		String[][] parts = referenceType.resolveType(name);
		if( parts != null && parts.length > 0 ) {
			return toFQN(parts[0]);
		} else {
			//FIXME Log it
		}
		return null;
	}
	
	public static String toFQN(String[] array) {
		StringBuilder b = new StringBuilder();
		
		for( String s : array ) {
			if( b.length() > 0 && ! b.toString().endsWith(".") ) {
				b.append(".");
			}
			b.append(s);
		}
		
		return b.toString();
	}
	
	public static String toFQN(IType owner, String signature) throws JavaModelException {
		String genericType = Signature.toString(signature);
		String eType = genericType;
		
		if( genericType.contains("<") ) {
			eType = genericType.substring(genericType.indexOf('<')+1, genericType.indexOf('>'));	
		}
		
		// FIXME Is there a better way?
		if( eType.contains("super") ) {
			eType = eType.substring(eType.indexOf("super")+"super".length()).trim();
		} else if( eType.contains("extends") ) {
			eType = eType.substring(eType.indexOf("extends")+"extends".length()).trim();
		}
		
		String fqn = getFQNType(owner,eType); 
		return fqn;
	}
}

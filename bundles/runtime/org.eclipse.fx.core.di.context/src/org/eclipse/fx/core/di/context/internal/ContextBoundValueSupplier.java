/*******************************************************************************
 * Copyright (c) 2013 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *******************************************************************************/
package org.eclipse.fx.core.di.context.internal;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.inject.Inject;

import org.eclipse.e4.core.di.IInjector;
import org.eclipse.e4.core.di.suppliers.ExtendedObjectSupplier;
import org.eclipse.e4.core.di.suppliers.IObjectDescriptor;
import org.eclipse.e4.core.di.suppliers.IRequestor;
import org.eclipse.e4.core.internal.di.Requestor;
import org.eclipse.fx.core.adapter.AdapterService;
import org.eclipse.fx.core.di.ContextBoundValue;
import org.eclipse.fx.core.di.ContextValue;

/**
 * Supplier which handles the {@link ContextValue}
 */
@SuppressWarnings("restriction")
public class ContextBoundValueSupplier extends ExtendedObjectSupplier {
	@Inject
	AdapterService adapterService;
	
	@Override
	public Object get(IObjectDescriptor descriptor, IRequestor requestor, boolean track, boolean group) {
		ContextValue qualifier = descriptor.getQualifier(ContextValue.class);
		Requestor r = (Requestor) requestor;
		EclipseContextBoundValue<?> c = r.getInjector().make(EclipseContextBoundValue.class, r.getPrimarySupplier());
		c.setContextKey(qualifier.contextKey());
		
		Class<?> desiredClass = getDesiredClass(descriptor.getDesiredType());
		
		if( desiredClass == ContextBoundValue.class) {
			return c;
		} else {
			if( desiredClass != null ) {
				return c.adaptTo(desiredClass);	
			}
			return IInjector.NOT_A_VALUE;
		}
	}

	private static Class<?> getDesiredClass(Type desiredType) {
		if (desiredType instanceof Class<?>)
			return (Class<?>) desiredType;
		if (desiredType instanceof ParameterizedType) {
			Type rawType = ((ParameterizedType) desiredType).getRawType();
			if (rawType instanceof Class<?>)
				return (Class<?>) rawType;
		}
		return null;
	}
}

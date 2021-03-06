/*******************************************************************************
 * Copyright (c) 2013 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl<tom.schindl@bestsolution.at> - initial API and implementation
 *******************************************************************************/
package org.eclipse.fx.core;

/**
 * Class with static utility methods
 */
public class Util {
	/**
	 * @return <code>true</code> if we are on JavaFX 2
	 */
	public static boolean isFX2() {
		return System.getProperty("javafx.version") != null && System.getProperty("javafx.version").startsWith("2"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}

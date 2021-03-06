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
package org.eclipse.fx.core.databinding;

import javafx.application.Platform;

import org.eclipse.core.databinding.observable.Realm;

/**
 * JavaFX realm implementation
 */
public class JFXRealm extends Realm {
	/**
	 * Create a default realm
	 */
	public static void createDefault() {
		setDefault(new JFXRealm());
	}
	
	@Override
	public boolean isCurrent() {
		return Platform.isFxApplicationThread();
	}
	
	@Override
	public void asyncExec(Runnable runnable) {
		Platform.runLater(runnable);
	}
	
	@Override
	public void exec(Runnable runnable) {
		if( isCurrent() ) {
			runnable.run();
		} else {
			Platform.runLater(runnable);
		}
	}
}
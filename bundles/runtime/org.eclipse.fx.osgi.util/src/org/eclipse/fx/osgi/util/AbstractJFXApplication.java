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
package org.eclipse.fx.osgi.util;

import java.util.HashMap;
import java.util.Map;

import javafx.application.Application;
import javafx.stage.Stage;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.fx.core.databinding.JFXRealm;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

/**
 * Application base class for Equinox JavaFX Applications
 */
public abstract class AbstractJFXApplication implements IApplication {
	static AbstractJFXApplication SELF;
	
	IApplicationContext applicationContext;
	Object returnValue;
	EventAdmin eventAdmin;
	
	/**
	 * Dummy class for bootstrap
	 */
	public static class JFXApp extends Application {
		private AbstractJFXApplication osgiApp = SELF;
		private IApplicationContext applicationContext;
		
		@Override
		public void start(final Stage primaryStage) throws Exception {
			this.applicationContext = this.osgiApp.applicationContext;
			
			JFXRealm.createDefault();
			this.osgiApp.jfxStart(this.applicationContext,JFXApp.this,primaryStage);
			
			if( this.osgiApp.eventAdmin != null ) {
				Map<String, Object> map = new HashMap<String, Object>();
//				map.put("name", value);
				this.osgiApp.eventAdmin.sendEvent(new Event("efxapp/applicationLaunched", map)); //$NON-NLS-1$
			}
		}
		
		@Override
		public void stop() throws Exception {
			super.stop();
			this.osgiApp.returnValue = this.osgiApp.jfxStop();
		}
	}
	
	@Override
	public final Object start(IApplicationContext context) throws Exception {
		SELF = this;
		this.applicationContext = context;
		this.applicationContext.applicationRunning();
		
		Bundle b = FrameworkUtil.getBundle(AbstractJFXApplication.class);
		BundleContext bundleContext = b.getBundleContext();
		ServiceReference<EventAdmin> ref = bundleContext.getServiceReference(EventAdmin.class);
		if( ref != null ) {
			this.eventAdmin = bundleContext.getService(ref);
		}
		
		// Looks like OS-X wants to have the context class loader to locate FX-Classes
		Thread.currentThread().setContextClassLoader(Application.class.getClassLoader());
		
		Application.launch(JFXApp.class);
		
		try {
			return this.returnValue == null ? IApplication.EXIT_OK : this.returnValue;
		} finally {
			this.returnValue = null;
		}
	}

	@Override
	public final void stop() {
		// Nothing
	}

	protected abstract void jfxStart(IApplicationContext applicationContext, Application jfxApplication, Stage primaryStage);
	
	@SuppressWarnings("static-method")
	protected Object jfxStop() {
		return IApplication.EXIT_OK;
	}
}

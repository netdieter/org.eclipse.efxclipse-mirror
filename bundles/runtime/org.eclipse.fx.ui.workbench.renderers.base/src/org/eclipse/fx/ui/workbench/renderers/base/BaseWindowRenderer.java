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
package org.eclipse.fx.ui.workbench.renderers.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.di.InjectionException;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindowElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.ISaveHandler;
import org.eclipse.e4.ui.workbench.modeling.ISaveHandler.Save;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fx.core.log.Log;
import org.eclipse.fx.core.log.Logger;
import org.eclipse.fx.ui.workbench.renderers.base.widget.WCallback;
import org.eclipse.fx.ui.workbench.renderers.base.widget.WLayoutedWidget;
import org.eclipse.fx.ui.workbench.renderers.base.widget.WWidget;
import org.eclipse.fx.ui.workbench.renderers.base.widget.WWindow;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;



@SuppressWarnings("restriction")
public abstract class BaseWindowRenderer<N> extends BaseRenderer<MWindow,WWindow<N>> {
	// derived from SWT implementation
	private class DefaultSaveHandler implements ISaveHandler {
		private MWindow element;
		private WWindow<N> widget;

		private DefaultSaveHandler(MWindow element, WWindow<N> widget) {
			this.element = element;
			this.widget = widget;
		}

//FIXME Once we compile against Kepler final we should add the @Override
//		@Override
		public boolean saveParts(Collection<MPart> dirtyParts, boolean confirm) {
			if (confirm) {
				List<MPart> dirtyPartsList = Collections
						.unmodifiableList(new ArrayList<MPart>(dirtyParts));
				Save[] decisions = promptToSave(dirtyPartsList);
				for (Save decision : decisions) {
					if (decision == Save.CANCEL) {
						return false;
					}
				}

				for (int i = 0; i < decisions.length; i++) {
					if (decisions[i] == Save.YES) {
						if (!save(dirtyPartsList.get(i), false)) {
							return false;
						}
					}
				}
				return true;
			}

			for (MPart dirtyPart : dirtyParts) {
				if (!save(dirtyPart, false)) {
					return false;
				}
			}
			return true;
		}

//FIXME Once we compile against Kepler final we should add the @Override		
//		@Override
		public boolean save(MPart dirtyPart, boolean confirm) {
			if (confirm) {
				switch (promptToSave(dirtyPart)) {
				case NO:
					return true;
				case CANCEL:
					return false;
				case YES:
					break;
				}
			}

			Object client = dirtyPart.getObject();
			try {
				ContextInjectionFactory.invoke(client, Persist.class,
						dirtyPart.getContext());
			} catch (InjectionException e) {
				logger.error("Failed to persist contents of part", e);
				return false;
			} catch (RuntimeException e) {
				logger.error("Failed to persist contents of part via DI", e);
				return false;
			}
			return true;
		}

		@Override
		public Save[] promptToSave(Collection<MPart> dirtyParts) {
			return BaseWindowRenderer.this.promptToSave(element, dirtyParts,
					widget);
		}

		@Override
		public Save promptToSave(MPart dirtyPart) {
			return BaseWindowRenderer.this.promptToSave(element, dirtyPart,
					widget);
			// Collection<MPart> c = Collections.singleton(dirtyPart);
			// return BaseWindowRenderer.this.promptToSave(resourceUtilities,c,
			// widget)[0];
		}
	}

	public static final String KEY_FULL_SCREEN = "efx.window.fullscreen";

	@Inject
	@Log
	Logger logger;
	
	@PostConstruct
	void init(IEventBroker eventBroker) {
		registerEventListener(eventBroker, UIEvents.Window.TOPIC_X);
		registerEventListener(eventBroker, UIEvents.Window.TOPIC_Y);
		registerEventListener(eventBroker, UIEvents.Window.TOPIC_WIDTH);
		registerEventListener(eventBroker, UIEvents.Window.TOPIC_HEIGHT);
		registerEventListener(eventBroker, UIEvents.UILabel.TOPIC_LABEL);
		registerEventListener(eventBroker, UIEvents.UILabel.TOPIC_TOOLTIP);
		registerEventListener(eventBroker, UIEvents.UIElement.TOPIC_VISIBLE);
		eventBroker.subscribe(UIEvents.Window.TOPIC_WINDOWS, new EventHandler() {
			
			@Override
			public void handleEvent(Event event) {
				Object changedObj = event.getProperty(UIEvents.EventTags.ELEMENT);
				if( changedObj instanceof MPerspective ) {
					MPerspective perspective = (MPerspective) changedObj;
					if( BaseWindowRenderer.this == perspective.getRenderer() ) {
						String eventType = (String) event.getProperty(UIEvents.EventTags.TYPE);
						if( UIEvents.EventTypes.ADD.equals(eventType) ) {
							MUIElement element = (MUIElement) event.getProperty(UIEvents.EventTags.NEW_VALUE);
							if( element instanceof MWindow ) {
								handleWindowAdd((MWindow) element);
							} else if( element instanceof MWindowElement ) {
								handleChildAdd((MWindowElement) element);
							}
						} else if( UIEvents.EventTypes.REMOVE.equals(eventType) ) {
							MUIElement element = (MUIElement) event.getProperty(UIEvents.EventTags.OLD_VALUE);
							if( element instanceof MWindow ) {
								handleWindowRemove((MWindow) element);	
							} else if( element instanceof MWindowElement ) {
								handleChildRemove((MWindowElement) element);
							}
						}
					}
				}
			}
		});
	}
	
	void handleWindowAdd(MWindow element) {
		engineCreateWidget(element);
	}
	
	void handleWindowRemove(MWindow element) {
		// Nothing to do here
	}
	
	void handleChildAdd(MWindowElement element) {
		engineCreateWidget(element);
	}
	
	void handleChildRemove(MWindowElement element) {
		if (element.isToBeRendered() && element.isVisible() && element.getWidget() != null) {
			hideChild((MWindow)(MUIElement)element.getParent(), element);	
		}
	}
	
	@Override
	protected void initWidget(final MWindow element, final WWindow<N> widget) {
		widget.registerActivationCallback(new WCallback<Boolean, Void>() {
			
			@Override
			public Void call(Boolean param) {
				if( param.booleanValue() ) {
					MUIElement parentME = element.getParent();
					if (parentME instanceof MApplication) {
						MApplication app = (MApplication) parentME;
						app.setSelectedElement(element);
						element.getContext().activate();
					} else if (parentME == null) {
						parentME = (MUIElement) ((EObject) element).eContainer();
						if (parentME instanceof MContext) {
							element.getContext().activate();
						}
					}	
				}
				return null;
			}
		});
		widget.setOnCloseCallback(new WCallback<WWindow<N>, Boolean>() {

			@Override
			public Boolean call(WWindow<N> param) {
				//TODO Call out to lifecycle
				
				//Set the render flag for other windows
				//TODO What do we do with: other top-level windows, ... 
				if( ! ((MApplicationElement)param.getDomElement().getParent() instanceof MApplication) ) {
					param.getDomElement().setToBeRendered(false);
				}
				return Boolean.TRUE;
			}
		});
		getModelContext(element).set(ISaveHandler.class, new DefaultSaveHandler(element, widget));
	}
	
	protected abstract Save[] promptToSave(MWindow element, Collection<MPart> dirtyParts, WWindow<N> widget);
	protected abstract Save promptToSave(MWindow element, MPart dirtyPart, WWindow<N> widget);

	@Override
	public void doProcessContent(MWindow element) {
		WWindow<N> windowWidget = getWidget(element);
		
		Object nativeWidget = windowWidget.getWidget();
		
		if( nativeWidget != null ) {
			element.getContext().set(nativeWidget.getClass().getName(), nativeWidget);
		}
		
		if (element.getMainMenu() != null) {
			WLayoutedWidget<MMenu> menuWidget = engineCreateWidget(element.getMainMenu());
			if( menuWidget != null ) {
				windowWidget.setMainMenu(menuWidget);	
			}
		}
		
		if( element instanceof MTrimmedWindow ) {
			for( MTrimBar tm : ((MTrimmedWindow)element).getTrimBars() ) {
				if( tm.isToBeRendered() && tm.isVisible() ) {
					WLayoutedWidget<MTrimBar> trimWidget = engineCreateWidget(tm);
					if( trimWidget != null ) {
						trimWidget.addStyleClasses(tm.getSide().name());
						switch (tm.getSide()) {
						case TOP:
							windowWidget.setTopTrim(trimWidget);
							break;
						case RIGHT:
							windowWidget.setRightTrim(trimWidget);
							break;
						case BOTTOM:
							windowWidget.setBottomTrim(trimWidget);
							break;
						case LEFT:
							windowWidget.setLeftTrim(trimWidget);
							break;
						default:
							break;
						}					
					}
				}
			}
		}
		
		for( MWindowElement e : element.getChildren() ) {
			if( e.isToBeRendered() && e.isVisible() ) {
				WLayoutedWidget<MWindowElement> widget = engineCreateWidget(e);
				if( widget != null ) {
					windowWidget.addChild(widget);
				}	
			}
		}
		
		for( MWindow w : element.getWindows() ) {
			if( w.isVisible() && w.isToBeRendered() ) {
				WWidget<MWindow> widget = engineCreateWidget(w);
				if( widget != null ) {
					@SuppressWarnings("unchecked")
					WWindow<N> ww = (WWindow<N>) w.getWidget();
					windowWidget.addChild(ww);
				}
			}
		}
	}

	@Override
	public void postProcess(MWindow element) {
		super.postProcess(element);
		//Only top level windows are shown explicitly
		if( ((EObject)element).eContainer() instanceof MApplication ) {
			if( element.isVisible() ) {
				WWindow<N> window = getWidget(element);
				if( window != null ) {
					window.show();
				}
			}
		}
	}

	@Override
	public void childRendered(MWindow parentElement, MUIElement element) {
		if( inContentProcessing(parentElement) ) {
			return;
		}
		
		if( element instanceof MWindowElement ) {
			WWindow<N> window = getWidget(parentElement);
			if( window != null ) {
				int idx = getRenderedIndex(parentElement, element);
				@SuppressWarnings("unchecked")
				WLayoutedWidget<MWindowElement> widget = (WLayoutedWidget<MWindowElement>) element.getWidget();
				window.addChild(idx, widget);
			}			
		} else if( element instanceof MWindow ) {
			WWindow<N> window = getWidget(parentElement);
			if( window != null ) {
				WWindow<N> ww = (WWindow<N>) element.getWidget();
				window.addChild(ww);
			}
		}
	}
	
	@Override
	public void hideChild(MWindow container, MUIElement changedObj) {
		if( changedObj instanceof MWindowElement ) {
			WWindow<N> window = getWidget(container);
			if( window != null ) {
				@SuppressWarnings("unchecked")
				WLayoutedWidget<MWindowElement> widget = (WLayoutedWidget<MWindowElement>) changedObj.getWidget();
				window.removeChild(widget);
			}	
		} else if( changedObj instanceof MWindow ) {
			WWindow<N> window = getWidget(container);
			if( window != null ) {
				WWindow<N> ww = (WWindow<N>) changedObj.getWidget();
				window.removeChild(ww);
			}
		}
	}
	
	@Override
	public void destroyWidget(MWindow element) {
		if( element.getWidget() instanceof WWindow<?> ) {
			WWindow<MWindow> w = (WWindow<MWindow>) element.getWidget();
			w.close();
		}
		super.destroyWidget(element);
	}
}

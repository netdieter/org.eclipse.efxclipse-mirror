/******************************************************************************* 
 * Copyright (c) 2012 TESIS DYNAware GmbH and others. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 *     Torsten Sommer <torsten.sommer@tesis.de> - initial API and implementation 
 *******************************************************************************/
package org.eclipse.fx.emf.edit.ui;

import javafx.scene.Node;
import javafx.scene.control.Cell;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.edit.provider.IItemColorProvider;
import org.eclipse.emf.edit.provider.IItemFontProvider;
import org.eclipse.emf.edit.provider.IItemLabelProvider;

/**
 * This list cell factory wraps an {@link AdapterFactory} and delegates calls to its {@link ListCell}s to the
 * corresponding adapter-implemented item provider interfaces.
 * 
 * <ul>
 * <li>{@link Cell#setText(String)} and {@link Cell#setGraphic(Node)} are delegated to
 * {@link IItemLabelProvider}</li>
 * <li>{@link Cell#setTextFill(javafx.scene.paint.Paint)} and the CSS property
 * <code>-fx-background-color</code> are delegated to {@link IItemColorProvider}</li>
 * <li>
 * {@link Cell#setFont(javafx.scene.text.Font)} is delegated to {@link IItemFontProvider}</li>
 * </ul>
 */
public class AdapterFactoryListCellFactory extends AdapterFactoryCellFactory implements Callback<ListView<Object>, ListCell<Object>> {

	/**
	 * Create a new adapter factory for list cells
	 * 
	 * @param adapterFactory
	 *            the adapter factory
	 */
	public AdapterFactoryListCellFactory(AdapterFactory adapterFactory) {
		super(adapterFactory);
	}

	@Override
	public ListCell<Object> call(ListView<Object> param) {

		final ListCell<Object> listCell = new ListCell<Object>() {

			Object currentItem = null;
			ICellEditHandler cellEditHandler;

			AdapterImpl adapter = new AdapterImpl() {
				@Override
				public void notifyChanged(Notification msg) {
					update(msg.getNotifier());
				}
			};

			@Override
			protected void updateItem(Object item, boolean empty) {
				super.updateItem(item, empty);

				// check if the item changed
				if (item != this.currentItem) {

					// remove the adapter if attached
					if (this.currentItem instanceof Notifier)
						((Notifier) this.currentItem).eAdapters().remove(this.adapter);

					// update the current item
					this.currentItem = item;

					// attach the adapter to the new item
					if (this.currentItem instanceof Notifier)
						((Notifier) this.currentItem).eAdapters().add(this.adapter);
				}

				// notify the listeners
				for (ICellUpdateListener cellUpdateListener : AdapterFactoryListCellFactory.this.cellUpdateListeners)
					cellUpdateListener.updateItem(this, item, empty);

				update(item);
			}

			@Override
			public void startEdit() {
				super.startEdit();
				this.cellEditHandler = getCellEditHandler(this);
				if (this.cellEditHandler != null)
					this.cellEditHandler.startEdit(this);
			}

			@Override
			public void commitEdit(Object newValue) {
				super.commitEdit(newValue);
				if (this.cellEditHandler != null)
					this.cellEditHandler.commitEdit(this, newValue);
			}

			@Override
			public void cancelEdit() {
				super.cancelEdit();
				if (this.cellEditHandler != null)
					this.cellEditHandler.cancelEdit(this);
				update(getItem());
			}

			void update(Object item) {
				applyItemProviderStyle(item, this, AdapterFactoryListCellFactory.this.adapterFactory);
			}

		};

		for (ICellCreationListener cellCreationListener : this.cellCreationListeners)
			cellCreationListener.cellCreated(listCell);

		return listCell;
	}

}

package org.eclipse.fx.ecp.ui.controls.multi;

import java.net.URL;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecp.edit.ECPControlContext;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;

public class EmbeddedReferenceControl extends AbstractEmbeddedControl {

	final protected Hyperlink hyperlink;
	final protected AdapterImpl valueAdapter;
	protected EObject value;

	public EmbeddedReferenceControl(IItemPropertyDescriptor propertyDescriptor, final ECPControlContext context, int initialIndex) {
		super(propertyDescriptor, context, initialIndex);
		hyperlink = new Hyperlink();
		getChildren().add(0, hyperlink);
		HBox.setHgrow(hyperlink, Priority.ALWAYS);
		hyperlink.setMaxWidth(Double.MAX_VALUE);

		hyperlink.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				if (eList.size() > index) {
					Object item = eList.get(index);
					if (item instanceof EObject)
						context.openInNewContext((EObject) item);
				}
			}

		});

		upButton.getStyleClass().add("left-pill");

		valueAdapter = new AdapterImpl() {

			@Override
			public void notifyChanged(Notification msg) {
				update();
			}

		};

		update();
	}

	@Override
	protected void update() {
		super.update();

		if (eList.size() > index) {
			final EObject newValue = (EObject) eList.get(index);

			if (newValue != value) {
				if (value != null)
					value.eAdapters().remove(valueAdapter);
				newValue.eAdapters().add(valueAdapter);
				value = newValue;
			}

			ComposedAdapterFactory adapterFactory = new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE);

			// TODO check why this is not working:
			// IItemLabelProvider labelProvider =
			// ComposedAdapterFactory.Descriptor.Registry.INSTANCE//(IItemLabelProvider)
			// EcoreUtil.getRegisteredAdapter(value, IItemLabelProvider.class);
			IItemLabelProvider labelProvider = (IItemLabelProvider) adapterFactory.adapt(value, IItemLabelProvider.class);

			String text = labelProvider.getText(value);

			URL image = (URL) labelProvider.getImage(value);
			ImageView imageView = new ImageView(image.toExternalForm());

			hyperlink.setText(text);
			hyperlink.setGraphic(imageView);
		} else {
			hyperlink.setText(null);
			hyperlink.setGraphic(null);
		}
	}

}
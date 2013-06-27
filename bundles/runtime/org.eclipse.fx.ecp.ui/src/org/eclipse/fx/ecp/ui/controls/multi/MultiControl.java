package org.eclipse.fx.ecp.ui.controls.multi;

import java.net.URL;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.SkinBase;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecp.edit.ECPControlContext;
import org.eclipse.emf.edit.command.MoveCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.fx.ecp.ui.ECPControl;
import org.eclipse.fx.ecp.ui.ECPUIPlugin;
import org.eclipse.fx.ecp.ui.controls.ECPControlBase;
import org.eclipse.fx.ecp.ui.controls.ValidationMessage;
import org.osgi.framework.Bundle;

@SuppressWarnings("all")
public class MultiControl extends ECPControlBase {

	private EList<Object> values;
	private VBox controlsBox;

	class Skin extends SkinBase<MultiControl> {

		protected Skin(MultiControl multiControl) {
			super(multiControl);
		}

	}

	public MultiControl(final IItemPropertyDescriptor propertyDescriptor, final ECPControlContext context) {
		super(propertyDescriptor, context);

		setSkin(new Skin(this));

		values = (EList<Object>) modelElement.eGet(feature);

		VBox vBox = new VBox();
		getChildren().add(vBox);
		vBox.setSpacing(4);

		controlsBox = new VBox();
		vBox.getChildren().add(controlsBox);

		// TODO move to css
		controlsBox.setSpacing(4);

		for (int i = 0; i < values.size(); i++) {
			controlsBox.getChildren().add(createEmbeddedControl(propertyDescriptor, context, i));
		}

		if (feature.getEType() instanceof EEnum) {
			vBox.getChildren().add(new EnumAddControl(propertyDescriptor, context));
		} else if (feature.getEType() instanceof EDataType) {
			vBox.getChildren().add(new TextFieldAddControl(propertyDescriptor, context));
		} else if (feature.getEType() instanceof EObject) {
			EReference reference = (EReference) feature;
			if (reference.isContainment())
				vBox.getChildren().add(new ReferenceAddControl(editingDomain, reference, modelElement));
			else
				vBox.getChildren().add(new ReferenceDropControl(editingDomain, reference, modelElement));
		}

		createModelElementAdapter();
	}

	@Override
	protected AdapterImpl createModelElementAdapter() {

		return new AdapterImpl() {

			@Override
			public void notifyChanged(Notification msg) {

				if (msg.getFeature() == feature) {

					final int position = msg.getPosition();

					ObservableList<Node> children = controlsBox.getChildren();
					switch (msg.getEventType()) {
					case Notification.ADD:
						controlsBox.getChildren().add(createEmbeddedControl(propertyDescriptor, context, position));
						break;
					case Notification.REMOVE:
						children.remove(position);
						break;
					case Notification.MOVE:
					case Notification.SET:
					case Notification.UNSET:
						break;
					case Notification.ADD_MANY:
						while (children.size() < values.size())
							children.add(createEmbeddedControl(propertyDescriptor, context, 0));
						break;
					case Notification.REMOVE_MANY:
						while (children.size() > values.size())
							children.remove(0);
						break;
					default:
						throw new UnsupportedOperationException();
					}

					for (int i = 0; i < children.size(); i++) {
						Node node = children.get(i);
						if (node instanceof AbstractEmbeddedControl)
							((AbstractEmbeddedControl) node).setIndex(i);
					}

				}

			}

		};

	}

	private AbstractEmbeddedControl createEmbeddedControl(final IItemPropertyDescriptor propertyDescriptor,
			final ECPControlContext context, int i) {

		if (feature instanceof EReference) {
			return new EmbeddedReferenceControl(propertyDescriptor, context, i);
		} else if (feature.getEType() instanceof EEnum) {
			return new EmbeddedEnumControl(propertyDescriptor, context, i);
		} else {
			Class<?> instanceClass = feature.getEType().getInstanceClass();
			if (Boolean.class.isAssignableFrom(instanceClass)) {
				return new EmbeddedCheckboxControl(propertyDescriptor, context, i);
			} else if (Number.class.isAssignableFrom(instanceClass)
					|| (instanceClass.isPrimitive() && instanceClass != boolean.class && instanceClass != char.class)) {
				return new EmbeddedNumberFieldControl(propertyDescriptor, context, i);
			} else {
				return new EmbeddedTextFieldControl(propertyDescriptor, context, i);
			}
		}

	}

	public static ImageView getImage(String resourcePath) {
		Bundle bundle = Platform.getBundle(ECPUIPlugin.PLUGIN_ID);
		Path path = new Path(resourcePath);
		URL url = FileLocator.find(bundle, path, null);
		return new ImageView(url.toExternalForm());
	}

	public static class Factory implements ECPControl.Factory {

		@Override
		public ECPControlBase createControl(IItemPropertyDescriptor itemPropertyDescriptor, ECPControlContext context) {
			return new MultiControl(itemPropertyDescriptor, context);
		}

	}

	@Override
	protected void update() {
		// TODO Auto-generated method stub
	}

}
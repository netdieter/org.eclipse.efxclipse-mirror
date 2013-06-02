package org.eclipse.fx.ecp.ui.controls.multi;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.Enumerator;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecp.edit.ECPControlContext;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;

public class EmbeddedEnumControl extends AbstractEmbeddedControl {

	private volatile boolean internal;
	final protected ChoiceBox<Enumerator> choiceBox;

	public EmbeddedEnumControl(IItemPropertyDescriptor propertyDescriptor, ECPControlContext context, int initialIndex) {
		super(propertyDescriptor, context, initialIndex);

		final EClassifier type = feature.getEType();
		final EEnum eEnum = (EEnum) type;
		final EList<EEnumLiteral> enumLiterals = eEnum.getELiterals();
		
		choiceBox = new ChoiceBox<>();
		getChildren().add(0, choiceBox);
		choiceBox.getStyleClass().add("left-pill");
		choiceBox.setMaxWidth(Double.MAX_VALUE);
		HBox.setHgrow(choiceBox, Priority.ALWAYS);
		
		upButton.getStyleClass().add("center-pill");
		
		if (feature.isUnsettable())
			choiceBox.getItems().add(null);

		for (EEnumLiteral literal : enumLiterals)
			choiceBox.getItems().add(literal.getInstance());
		
		final SingleSelectionModel<Enumerator> selectionModel = choiceBox.getSelectionModel();
		
		selectionModel.selectedItemProperty().addListener(new ChangeListener<Enumerator>() {

			@Override
			public void changed(ObservableValue<? extends Enumerator> observableValue, Enumerator oldValue, Enumerator newValue) {
				if (!internal) {
					Command command = SetCommand.create(editingDomain, modelElement, feature, newValue != null ? newValue : SetCommand.UNSET_VALUE, index);
					if (command.canExecute()) {
						editingDomain.getCommandStack().execute(command);
					} else {
						internal = true;
						selectionModel.select(oldValue);
						internal = false;
					}
				}
			}

		});

		setIndex(initialIndex);
	}

	@Override
	protected void update() {
		super.update();
		Enumerator value = (Enumerator) eList.get(index);
		internal = true;
		choiceBox.getSelectionModel().select(value);
		internal = false;
	}

}
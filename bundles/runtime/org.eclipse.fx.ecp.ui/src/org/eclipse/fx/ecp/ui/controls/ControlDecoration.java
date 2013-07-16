package org.eclipse.fx.ecp.ui.controls;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import jidefx.scene.control.popup.BalloonPopupOutline;
import jidefx.scene.control.popup.ShapedPopup;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EStructuralFeature;

import com.fxexperience.javafx.animation.BounceInTransition;
import com.fxexperience.javafx.animation.BounceOutTransition;
import com.google.common.base.Objects;

public class ControlDecoration extends AnchorPane {

	protected Label label;
	protected final EStructuralFeature feature;
	protected final ECPControlBase control;
	protected final ShapedPopup shapedPopup;
	protected Diagnostic diagnostic;
	protected Text popupText;

	public ControlDecoration(EStructuralFeature feature, ECPControlBase control) {
		this.feature = feature;
		this.control = control;

		getChildren().add(control);

		shapedPopup = new ShapedPopup();
		shapedPopup.setAutoHide(false);
		shapedPopup.setInsets(new Insets(20));
		BalloonPopupOutline popupOutline = new BalloonPopupOutline();
		popupOutline.setArrowSide(Side.TOP);
		shapedPopup.setPopupOutline(popupOutline);
		popupText = new Text();
		popupText.setWrappingWidth(300);
		shapedPopup.setPopupContent(popupText);

		ImageView imageView = new ImageView(getClass().getResource("asterisk.png").toExternalForm());
		label = new Label();
		label.setGraphic(imageView);

		getChildren().add(label);
		AnchorPane.setTopAnchor(label, -1.0);
		AnchorPane.setLeftAnchor(label, -14.0);
		label.setOpacity(0);
		label.setOnMouseEntered(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent arg0) {
				if (diagnostic != null)
					shapedPopup.showPopup(label, Pos.BOTTOM_CENTER, 0, 5);
			}

		});

		label.setOnMouseExited(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent arg0) {
				if (diagnostic != null)
					shapedPopup.hide();
			}

		});
	}

	public void handleValidation(Diagnostic diagnostic) {

		Diagnostic newDiagnostic = null;

		if (diagnostic != null) {
			for (Diagnostic childDiagnostic : diagnostic.getChildren()) {
				Object feature = childDiagnostic.getData().get(1);
				if (feature == this.feature) {
					newDiagnostic = childDiagnostic;
					break;
				}
			}
		}

		if (newDiagnostic != null && (this.diagnostic == null || !equals(newDiagnostic, this.diagnostic)))
			popupText.setText(newDiagnostic.getMessage());

		if (newDiagnostic != null && !equals(this.diagnostic, newDiagnostic)) {
			BounceInTransition bounceInTransition = new BounceInTransition(label);
			bounceInTransition.play();
		} else if (newDiagnostic == null && label.getOpacity() == 1.0) {
			BounceOutTransition bounceOutTransition = new BounceOutTransition(label);
			bounceOutTransition.play();
		}

		this.diagnostic = newDiagnostic;
	}

	public void dispose() {
		control.dispose();
	}
	
	protected static boolean equals(Diagnostic d1, Diagnostic d2) {
		if(d1 != null && d2 != null)
			return Objects.equal(d1.getMessage(), d2.getMessage());
		else
			return false;
	}

}
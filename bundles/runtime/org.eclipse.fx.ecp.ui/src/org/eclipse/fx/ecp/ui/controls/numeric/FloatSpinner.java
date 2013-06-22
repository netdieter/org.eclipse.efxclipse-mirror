package org.eclipse.fx.ecp.ui.controls.numeric;

import org.eclipse.emf.ecp.edit.ECPControlContext;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.fx.ecp.ui.ECPControl;
import org.eclipse.fx.ecp.ui.controls.ECPControlBase;

public class FloatSpinner extends NumberSpinner {

	public FloatSpinner(IItemPropertyDescriptor propertyDescriptor, ECPControlContext context) {
		super(propertyDescriptor, context);
		setSkin(new NumberSpinnerSkin<NumberSpinner, Float>(this) {

			@Override
			boolean validate(String literal) {
				if (literal.matches("^\\-?[0-9]+\\.?[0-9]*$")) {
					try {
						Double.parseDouble(literal);
						return true;
					} catch (NumberFormatException e) {
						// do nothing
					}
				}
				return false;
			}

			@Override
			Float decrease(Float value) {
				return value - 1;
			}

			@Override
			Float increase(Float value) {
				return value + 1;
			}

			@Override
			Float parseValue(String literal) {
				try {
					return Float.parseFloat(literal);
				} catch (NumberFormatException e) {
					return 0.0f;
				}
			}

		});
	}

	public static class Factory implements ECPControl.Factory {

		@Override
		public ECPControlBase createControl(IItemPropertyDescriptor itemPropertyDescriptor, ECPControlContext context) {
			return new FloatSpinner(itemPropertyDescriptor, context);
		}

	}

}
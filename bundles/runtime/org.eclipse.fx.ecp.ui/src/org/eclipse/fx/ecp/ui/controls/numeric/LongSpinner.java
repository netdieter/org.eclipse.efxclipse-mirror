package org.eclipse.fx.ecp.ui.controls.numeric;

import org.eclipse.emf.ecp.edit.ECPControlContext;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.fx.ecp.ui.ECPControl;
import org.eclipse.fx.ecp.ui.controls.ECPControlBase;

public class LongSpinner extends NumberSpinner {

	public LongSpinner(IItemPropertyDescriptor propertyDescriptor, ECPControlContext context) {
		super(propertyDescriptor, context);
		
		setSkin(new NumberSpinnerSkin<NumberSpinner, Long>(this) {

			@Override
			boolean validate(String literal) {
				if (literal.matches("^\\-?[0-9]*$")) {
					try {
						Integer.parseInt(literal);
						return true;
					} catch (NumberFormatException e) {
						// do nothing
					}
				}
				return false;
			}

			@Override
			Long decrease(Long value) {
				return value - 1;
			}

			@Override
			Long increase(Long value) {
				return value + 1;
			}

			@Override
			Long parseValue(String literal) {
				try {
					return Long.parseLong(literal);
				} catch (NumberFormatException e) {
					return 0l;
				}
			}

		});
	}
	
	public static class Factory implements ECPControl.Factory {

		@Override
		public ECPControlBase createControl(IItemPropertyDescriptor itemPropertyDescriptor, ECPControlContext context) {
			return new LongSpinner(itemPropertyDescriptor, context);
		}

	}

}
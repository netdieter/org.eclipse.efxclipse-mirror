package org.eclipse.fx.ecp.handlers;

import java.util.Collection;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecp.core.ECPProject;
import org.eclipse.emf.edit.command.CopyToClipboardCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.fx.ecp.dummy.DummyWorkspace;


public class DuplicateModelElementHandler {
	
	private Command command;
	private EditingDomain editingDomain;

	@CanExecute
	public boolean canAddNewElement(@Optional @Named("modelExplorer.selectedItems") Collection<?> selectedItems) {
		// TODO: get this from ECPWorkspaceManager.getProject()
		ECPProject project = DummyWorkspace.INSTANCE.getProject();
		editingDomain = project.getEditingDomain();
		
		
		
		command = CopyToClipboardCommand.create(editingDomain, selectedItems);
		
		return command.canExecute();
	}

	@Execute
	public void executeCommand() {
		editingDomain.getCommandStack().execute(command);
	}

}

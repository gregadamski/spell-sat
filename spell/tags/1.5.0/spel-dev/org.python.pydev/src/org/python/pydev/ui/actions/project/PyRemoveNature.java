package org.python.pydev.ui.actions.project;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.python.pydev.core.docutils.StringUtils;
import org.python.pydev.plugin.PydevPlugin;
import org.python.pydev.plugin.nature.PythonNature;

/**
 * Action to remove the pydev nature from a given project.
 * 
 * @author Fabio
 */
public class PyRemoveNature implements IObjectActionDelegate {

    /**
     * The project that was selected (may be null).
     */
    protected IProject selectedProject;

    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        // empty
    }

    /**
     * Actually remove the python nature from the project.
     */
    public void run(IAction action) {
        if(selectedProject == null){
            return;
        }
        
        if (!MessageDialog.openConfirm(null, "Confirm Remove Pydev Nature", StringUtils.format(
                "Are you sure that you want to remove the Pydev nature from %s?", selectedProject.getName()))) {
            return;
        }
        
        try {
            PythonNature.removeNature(selectedProject, null);
        } catch (Throwable e) {
            PydevPlugin.log(e);
        }
        
    }

    /**
     * A project was just selected
     */
    public void selectionChanged(IAction action, ISelection selection) {
        selectedProject = null;
        
        if (selection.isEmpty() || !(selection instanceof IStructuredSelection)) {
            return;
        }
        
        IStructuredSelection selections = (IStructuredSelection) selection;
        Object project = selections.getFirstElement();
        if(!(project instanceof IProject)){
            return;
        }
        
        this.selectedProject = (IProject) project;
    }

}

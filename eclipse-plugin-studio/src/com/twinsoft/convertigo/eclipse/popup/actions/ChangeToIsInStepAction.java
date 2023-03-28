/*
 * Copyright (c) 2001-2023 Convertigo SA.
 * 
 * This program  is free software; you  can redistribute it and/or
 * Modify  it  under the  terms of the  GNU  Affero General Public
 * License  as published by  the Free Software Foundation;  either
 * version  3  of  the  License,  or  (at your option)  any  later
 * version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;  without even the implied warranty of
 * MERCHANTABILITY  or  FITNESS  FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program;
 * if not, see <http://www.gnu.org/licenses/>.
 */

package com.twinsoft.convertigo.eclipse.popup.actions;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.twinsoft.convertigo.beans.core.DatabaseObject;
import com.twinsoft.convertigo.beans.core.Sequence;
import com.twinsoft.convertigo.beans.core.Step;
import com.twinsoft.convertigo.beans.core.StepEvent;
import com.twinsoft.convertigo.beans.core.StepWithExpressions;
import com.twinsoft.convertigo.beans.steps.IfExistStep;
import com.twinsoft.convertigo.beans.steps.IsInStep;
import com.twinsoft.convertigo.beans.steps.IsInThenElseStep;
import com.twinsoft.convertigo.beans.steps.ThenStep;
import com.twinsoft.convertigo.eclipse.ConvertigoPlugin;
import com.twinsoft.convertigo.eclipse.views.projectexplorer.ProjectExplorerView;
import com.twinsoft.convertigo.eclipse.views.projectexplorer.TreeParent;
import com.twinsoft.convertigo.eclipse.views.projectexplorer.TreePath;
import com.twinsoft.convertigo.eclipse.views.projectexplorer.model.DatabaseObjectTreeObject;
import com.twinsoft.convertigo.eclipse.views.projectexplorer.model.StepTreeObject;
import com.twinsoft.convertigo.eclipse.views.projectexplorer.model.TreeObject;

public class ChangeToIsInStepAction extends MyAbstractAction {

	public ChangeToIsInStepAction() {
	}

	/* (non-Javadoc)
	 * @see com.twinsoft.convertigo.eclipse.popup.actions.MyAbstractAction#run()
	 */
	@Override
	public void run() {
		Display display = Display.getDefault();
		Cursor waitCursor = new Cursor(display, SWT.CURSOR_WAIT);		
		
		Shell shell = getParentShell();
		shell.setCursor(waitCursor);
		
        try {
    		ProjectExplorerView explorerView = getProjectExplorerView();
    		if (explorerView != null) {
    			TreeObject treeObject = explorerView.getFirstSelectedTreeObject();
    			Object databaseObject = treeObject.getObject();
    			// For IsInThenElse step
    			if ((databaseObject != null) && (databaseObject instanceof IsInThenElseStep)) {
    				IsInThenElseStep ifThenElseStep = (IsInThenElseStep)databaseObject;
    				if (ifThenElseStep.hasThenElseSteps()) {
    					ThenStep thenStep = ifThenElseStep.getThenStep();
    					List<Step> list = thenStep.getSteps();
    					TreePath[] selectedPaths = new TreePath[list.size()];
    					for (int i=0; i<list.size(); i++) {
    						StepTreeObject stepTreeObject = (StepTreeObject)explorerView.findTreeObjectByUserObject(list.get(i));
    						selectedPaths[i] = new TreePath(stepTreeObject);
    					}
    						
						TreeParent treeParent = treeObject.getParent();
						DatabaseObjectTreeObject parentTreeObject = null;
						if (treeParent instanceof DatabaseObjectTreeObject)
							parentTreeObject = (DatabaseObjectTreeObject)treeParent;
						else
							parentTreeObject = (DatabaseObjectTreeObject)treeParent.getParent();
						
		        		if (parentTreeObject != null) {
    						// New jIf step
		        			IsInStep ifStep = new IsInStep();
		        			
		        			// Set properties
		        			ifStep.setSourceDefinition(ifThenElseStep.getSourceDefinition());
		        			ifStep.setTestDefinition(ifThenElseStep.getTestDefinition());
		        			ifStep.setComment(ifThenElseStep.getComment());
		        			ifStep.setCondition(ifThenElseStep.getCondition());
		        			ifStep.setEnabled(ifThenElseStep.isEnabled());
		        			ifStep.setOutput(ifThenElseStep.isOutput());
		        			ifStep.setName(ifThenElseStep.getName());
		        			
    						ifStep.bNew = true;
    						ifStep.hasChanged = true;
    						
    						// Add new jIf step to parent
    						DatabaseObject parentDbo = ifThenElseStep.getParent();
    						parentDbo.add(ifStep);
    						
    						// Set correct order
    						if (parentDbo instanceof StepWithExpressions)
    							((StepWithExpressions)parentDbo).insertAtOrder(ifStep,ifThenElseStep.priority);
    						else if (parentDbo instanceof Sequence)
    							((Sequence)parentDbo).insertAtOrder(ifStep,ifThenElseStep.priority);
    						
    						// Add new jIf step in Tree
    						StepTreeObject stepTreeObject = new StepTreeObject(explorerView.viewer,ifStep);
    						treeParent.addChild(stepTreeObject);

    						// Cut/Paste steps under jIf step
    						if (selectedPaths.length > 0) {
    							new ClipboardAction(ConvertigoPlugin.clipboardManagerDND).cut(explorerView, selectedPaths, ProjectExplorerView.TREE_OBJECT_TYPE_DBO_STEP);
	    						for (int i = 0 ; i < ConvertigoPlugin.clipboardManagerDND.objects.length ; i++) {
	    							ConvertigoPlugin.clipboardManagerDND.cutAndPaste(ConvertigoPlugin.clipboardManagerDND.objects[i], stepTreeObject);
	    						}
	    						ConvertigoPlugin.clipboardManagerDND.reset();
    						}
    						
    		   				// Delete IfThenElse step
    						long oldPriority = ifThenElseStep.priority;
    						// Save oldName
    						String oldName = ifThenElseStep.getName();
    						// Now delete
    						ifThenElseStep.delete();    		   				
    						// Set name after deletion
    						ifStep.setName(oldName);
    						
    		   				// Simulate move of IfThenElse to If
    						ifStep.getSequence().fireStepMoved(new StepEvent(ifStep,String.valueOf(oldPriority)));
    						
		        			parentTreeObject.hasBeenModified(true);
			                explorerView.reloadTreeObject(parentTreeObject);
			                explorerView.setSelectedTreeObject(explorerView.findTreeObjectByUserObject(ifStep));
		        		}
					}
    			}
    			
    			// For IfExist step
    			if ((databaseObject != null) && (databaseObject instanceof IfExistStep)) {
   				
    				IfExistStep ifExistStep = (IfExistStep)databaseObject;
					List<Step> list = ifExistStep.getSteps();
					TreePath[] selectedPaths = new TreePath[list.size()];
					for (int i=0; i<list.size(); i++) {
						StepTreeObject stepTreeObject = (StepTreeObject)explorerView.findTreeObjectByUserObject(list.get(i));
						selectedPaths[i] = new TreePath(stepTreeObject);
					}
					
					TreeParent treeParent = treeObject.getParent();
					DatabaseObjectTreeObject parentTreeObject = null;
					if (treeParent instanceof DatabaseObjectTreeObject)
						parentTreeObject = (DatabaseObjectTreeObject)treeParent;
					else
						parentTreeObject = (DatabaseObjectTreeObject)treeParent.getParent();
					
	        		if (parentTreeObject != null) {
						// New jIf step
	        			IsInStep ifStep = new IsInStep();
	        			
	        			// Set properties
	        			ifStep.setSourceDefinition(ifExistStep.getSourceDefinition());
	        			ifStep.setComment(ifExistStep.getComment());
	        			ifStep.setCondition(ifExistStep.getCondition());
	        			ifStep.setEnabled(ifExistStep.isEnabled());
	        			ifStep.setOutput(ifExistStep.isOutput());
	        			ifStep.setName(ifExistStep.getName());
	        			
	        			ifStep.bNew = true;
						ifStep.hasChanged = true;
						
						// Add new jIf step to parent
						DatabaseObject parentDbo = ifExistStep.getParent();
						parentDbo.add(ifStep);
						
						// Set correct order
						if (parentDbo instanceof StepWithExpressions)
							((StepWithExpressions)parentDbo).insertAtOrder(ifStep,ifExistStep.priority);
						else if (parentDbo instanceof Sequence)
							((Sequence)parentDbo).insertAtOrder(ifStep,ifExistStep.priority);
						
						// Add new jIf step in Tree
						StepTreeObject stepTreeObject = new StepTreeObject(explorerView.viewer,ifStep);
						treeParent.addChild(stepTreeObject);

						// Cut/Paste steps under jIf step
						if (selectedPaths.length > 0) {
    						new ClipboardAction(ConvertigoPlugin.clipboardManagerDND).cut(explorerView, selectedPaths, ProjectExplorerView.TREE_OBJECT_TYPE_DBO_STEP);
    						for (int i = 0 ; i < ConvertigoPlugin.clipboardManagerDND.objects.length ; i++) {
    							ConvertigoPlugin.clipboardManagerDND.cutAndPaste(ConvertigoPlugin.clipboardManagerDND.objects[i], stepTreeObject);
    						}
    						ConvertigoPlugin.clipboardManagerDND.reset();
						}
						
		   				// Delete IfThenElse step
						long oldPriority = ifExistStep.priority;											
						// Save oldName
						String oldName = ifExistStep.getName();
						// Now delete
						ifExistStep.delete();    		   				
						// Set name after deletion
						ifStep.setName(oldName);						
						
		   				// Simulate move of IfThenElse to If
						ifStep.getSequence().fireStepMoved(new StepEvent(ifStep,String.valueOf(oldPriority)));
						
	        			parentTreeObject.hasBeenModified(true);
		                explorerView.reloadTreeObject(parentTreeObject);
		                explorerView.setSelectedTreeObject(explorerView.findTreeObjectByUserObject(ifStep));
	        		}
					
    			}
    		}
        	
        }
        catch (Throwable e) {
        	ConvertigoPlugin.logException(e, "Unable to change step to IsIn step!");
        }
        finally {
			shell.setCursor(null);
			waitCursor.dispose();
        }
	}
}

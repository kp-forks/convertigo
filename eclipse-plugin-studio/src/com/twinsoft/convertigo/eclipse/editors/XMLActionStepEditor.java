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

package com.twinsoft.convertigo.eclipse.editors;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;

import com.twinsoft.convertigo.beans.common.XMLVector;
import com.twinsoft.convertigo.eclipse.property_editors.StepSourceEditor;

public class XMLActionStepEditor {
	static public String[] getColumnNames() {
		return new String[] { "Description", "Source", "Default value"};
	}
	
	static public Object[] getDefaultData() {
		return new Object[] { "description", new XMLVector<XMLVector<Object>>(), ""};
	}
	
	static public CellEditor[] getColumnEditor(Composite parent) {
		CellEditor[] columnEditors = new CellEditor[3];
    	columnEditors[0] = new TextCellEditor(parent);
    	columnEditors[1] = new StepSourceEditor(parent);
    	columnEditors[2] = new TextCellEditor(parent);
		return columnEditors;
	}

}

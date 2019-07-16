/*
 * Copyright (c) 2001-2019 Convertigo SA.
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

package com.twinsoft.convertigo.eclipse.views.mobile;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.codehaus.jettison.json.JSONArray;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.part.ViewPart;

import com.teamdev.jxbrowser.browser.Browser;
import com.twinsoft.convertigo.eclipse.editors.mobile.ApplicationComponentEditor;
import com.twinsoft.convertigo.eclipse.swt.C8oBrowser;
import com.twinsoft.convertigo.engine.Engine;

public class MobileDebugView extends ViewPart implements IPartListener2 {
	
	C8oBrowser c8oBrowser;
	Browser browser;
	
	public MobileDebugView() {
		
	}

	@Override
	public void dispose() {
		getSite().getPage().removePartListener(this);
		c8oBrowser.dispose();
		super.dispose();
	}

	@Override
	public void createPartControl(Composite parent) {
		c8oBrowser = new C8oBrowser(parent, SWT.NONE);
		browser = c8oBrowser.getBrowser();
		browser.zoom().disable();
		c8oBrowser.setText("<head><style>color: $foreground$; background-color: $background$;</style></head>"
				+ "<body>please select a mobile application editor</body>");
		
		onActivated(getSite().getPage().getActiveEditor());
		getSite().getPage().addPartListener(this);
	}

	@Override
	public void setFocus() {
		c8oBrowser.setFocus();
	}

	private void onActivated(IWorkbenchPart part) {
		if (part instanceof ApplicationComponentEditor) {
			String url = ((ApplicationComponentEditor) part).getDebugUrl();
			if (url != null) {
				try (CloseableHttpResponse response = Engine.theApp.httpClient4.execute(new HttpGet(url + "/json"))) {
					JSONArray json = new JSONArray(IOUtils.toString(response.getEntity().getContent(), "UTF-8"));
					url = json.getJSONObject(0).getString("devtoolsFrontendUrl");
				} catch (Exception e) {
				}
				browser.navigation().loadUrl(url);
			}
		}
	}
	
	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
		IWorkbenchPart part = partRef.getPart(false);
		onActivated(part);
	}

	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void partClosed(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void partOpened(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void partHidden(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void partVisible(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		partRef.toString();
	}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		
	}

}
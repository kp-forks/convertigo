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

package com.twinsoft.convertigo.eclipse.swt;

import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;

import com.teamdev.jxbrowser.browser.Browser;
import com.teamdev.jxbrowser.engine.Engine;
import com.teamdev.jxbrowser.engine.EngineOptions;
import com.teamdev.jxbrowser.engine.RenderingMode;
import com.teamdev.jxbrowser.navigation.event.LoadFinished;
import com.teamdev.jxbrowser.view.swing.BrowserView;
import com.twinsoft.convertigo.beans.core.Project;
import com.twinsoft.convertigo.engine.EnginePropertiesManager;
import com.twinsoft.convertigo.engine.util.Crypto2;
import com.twinsoft.convertigo.engine.util.FileUtils;

public class C8oBrowser extends Composite {
	
	private static Thread threadSwt = null;
	private static Map<String, Engine> browserContexts = new HashMap<>();
	private static final String jxKey = "x31d140170e500d2cb6ea40186207fc4648722a44e93ce99816d812fb04ad802339785879b7429b466dea337c536d1d13aa4de4d2c578c252dd5885460c0f511067527743a0df65e6be52632fe108f782";

	private String debugUrl;
	private BrowserView browserView;

	private void init(Composite parent, Engine browserContext) {
		browserView = BrowserView.newInstance(browserContext.newBrowser());
		Frame frame = SWT_AWT.new_Frame(this);
		frame.add(browserView);
		threadSwt = parent.getDisplay().getThread();
		parent.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				dispose();
			}
		});
	}
	
	public C8oBrowser(Composite parent, int style) {
		this(parent, style, (Project) null);
	}

	public C8oBrowser(Composite parent, int style, Project project) {
		super(parent, style | SWT.EMBEDDED | SWT.NO_BACKGROUND);
		boolean retry = false;
		do {
			File browserIdFile = null;
			String browserId = "default";
			if (project != null) {
				browserIdFile = new File(project.getDirPath() + "/_private/browser_id");
				browserId = Long.toString(System.currentTimeMillis(), Character.MAX_RADIX);
				try {
					browserId = FileUtils.readFileToString(browserIdFile, "UTF-8");
				} catch (Exception e) {
					try {
						FileUtils.write(browserIdFile, browserId, "UTF-8");
					} catch (IOException e1) {
					}
				}	
			}
			
			File browserWorks = new File(com.twinsoft.convertigo.engine.Engine.USER_WORKSPACE_PATH + "/browser-works");
			browserWorks.mkdirs();
			Engine browserContext = browserContexts.get(browserId);
			if (browserContext == null) {
				int debugPort; 
				try (ServerSocket sock = new ServerSocket(0)) {
					debugPort = sock.getLocalPort();
				} catch (Exception e) {
					debugPort = 18081 + browserContexts.size();
				}
				String key = Crypto2.decodeFromHexString(EnginePropertiesManager.PropertyName.CRYPTO_PASSPHRASE.getDefaultValue(), jxKey);
				browserContext = Engine.newInstance(EngineOptions.newBuilder(RenderingMode.HARDWARE_ACCELERATED)
						.userDataDir(Paths.get(com.twinsoft.convertigo.engine.Engine.USER_WORKSPACE_PATH, "browser-works", browserId))
						.licenseKey(key)
						.remoteDebuggingPort(debugPort).build());
				browserContexts.put(browserId, browserContext);
			}
			debugUrl = "http://localhost:" + browserContext.options().remoteDebuggingPort().get();
			try {
				init(parent, browserContext);
			} catch (Exception e) {
				if (!retry) {
					if (browserIdFile != null) {
						browserIdFile.delete();
					}
					retry = true;
				} else {
					throw e;
				}
			}
		} while (retry);
	}

	public C8oBrowser(Composite parent, int style, Engine browserContext) {
		super(parent, style | SWT.EMBEDDED | SWT.NO_BACKGROUND);
		init(parent, browserContext);
	}
	
	@Override
	public void dispose() {
		run(() -> {
			getBrowser().close();;
		});
		super.dispose();
	}

	public BrowserView getBrowserView() {
		return browserView;
	}

	public Browser getBrowser() {
		return browserView.getBrowser();
	}
	
	public void setText(String html) {
		if (html.contains("$background$")) {
			org.eclipse.swt.graphics.Color bg = getBackground();
			String background = "rgb(" + bg.getRed() + ", " + bg.getGreen() + ", " + bg.getBlue() + ")";
			String foreground = bg.getRed() < 128 ? "white" : "black";
			String link = bg.getRed() < 128 ? "cyan" : "blue";
			html = html.replace("$background$", background).replace("$foreground$", foreground).replace("$link$", link);
		}
		final String h = html;
		if (html.contains("</html>")) {
			getBrowser().mainFrame().get().loadHtml(h);
		} else {
			getBrowser().mainFrame().get().document().get().documentElement().get().innerHtml(h);
		}
	}
	
	public void reloadText() {
		getBrowser().mainFrame().ifPresent(frame -> setText(frame.html()));
	}

	public void setUrl(String url) {
		getBrowser().navigation().loadUrl(url);
	}
		
	@Override
	public boolean setFocus() {
		C8oBrowser.run(() -> browserView.requestFocus());
		return super.setFocus();
	}

	public void addProgressListener(ProgressListener progressListener) {
		getBrowser().navigation().on(LoadFinished.class, event -> {
			progressListener.completed(null);
		});
	}
	
	public String getDebugUrl() {
		return debugUrl;
	}
	
	public static void run(Runnable runnable) {
		if (threadSwt != null && threadSwt.equals(Thread.currentThread())) {
			com.twinsoft.convertigo.engine.Engine.execute(runnable);
		} else {
			runnable.run();
		}
	}
	
	public static void main(String[] args) {
		System.out.println(Crypto2.encodeToHexString(com.twinsoft.convertigo.engine.EnginePropertiesManager.PropertyName.CRYPTO_PASSPHRASE.getDefaultValue(), args[0]));
	}
}
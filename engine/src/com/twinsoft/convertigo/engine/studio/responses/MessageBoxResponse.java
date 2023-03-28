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

package com.twinsoft.convertigo.engine.studio.responses;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.twinsoft.convertigo.engine.admin.util.DOMUtils;

public class MessageBoxResponse extends AbstractResponse {

	private String message;

	public MessageBoxResponse(String message) {
		super();
		this.message = message;
	}

	@Override
	public Element toXml(Document document, String qname) throws Exception {
		// Create dialog
		Element messageBox = document.createElement("messageBox");
		messageBox.appendChild(DOMUtils.createElementWithText(document, "title", "Convertigo"));
		messageBox.appendChild(DOMUtils.createElementWithText(document, "message", message));

		// Create response
		Element response = super.toXml(document, qname);
		response.appendChild(messageBox);

		return response;
	}
}

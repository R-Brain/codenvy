/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package com.codenvy.onpremises;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;

/**
 * Sends browser to correct factory acceptance URL after login is done.
 *
 * @author Max Shaposhnik
 */
@Singleton
public class FactoryRedirectServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String redirect =
                UriBuilder.fromPath("/dashboard")
                          .fragment("load-factory")
                          .build()
                          .toString();
        if (request.getQueryString() != null) {
            redirect = redirect.concat("?").concat(request.getQueryString());
        }
        response.sendRedirect(redirect);
    }

}

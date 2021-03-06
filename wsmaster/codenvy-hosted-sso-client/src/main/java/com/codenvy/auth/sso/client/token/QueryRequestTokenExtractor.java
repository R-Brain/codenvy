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
package com.codenvy.auth.sso.client.token;

import javax.servlet.http.HttpServletRequest;

/** Extract sso token from query parameters. */
public class QueryRequestTokenExtractor implements RequestTokenExtractor {
    @Override
    public String getToken(HttpServletRequest req) {
        String query = req.getQueryString();
        if (query != null) {
            int start = query.indexOf("&token=");
            if (start != -1 || query.startsWith("token=")) {
                int end = query.indexOf('&', start + 7);
                if (end == -1) {
                    end = query.length();
                }
                if (end != start + 7) {
                    return query.substring(start + 7, end);
                }
            }
        }
        return null;
    }
}

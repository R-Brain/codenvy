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
package com.codenvy.organization.api.listener;

import com.codenvy.api.permission.server.jpa.listener.RemovePermissionsOnLastUserRemovedEventSubscriber;
import com.codenvy.organization.api.OrganizationManager;
import com.codenvy.organization.spi.jpa.JpaMemberDao;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.model.impl.UserImpl;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Listens for {@link UserImpl} removal events, and checks if the removing user is the last who has "setPermissions"
 * permission to particular organization, and if it is, then removes organization itself.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class RemoveOrganizationOnLastUserRemovedEventSubscriber
        extends RemovePermissionsOnLastUserRemovedEventSubscriber<JpaMemberDao> {

    @Inject
    private OrganizationManager organizationManager;

    @Override
    public void remove(String instanceId) throws ServerException {
        organizationManager.remove(instanceId);
    }
}

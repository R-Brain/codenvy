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
package com.codenvy.organization.api.event;

import com.codenvy.organization.shared.event.EventType;
import com.codenvy.organization.shared.event.MemberEvent;
import com.codenvy.organization.shared.model.Organization;

import org.eclipse.che.api.core.model.user.User;

/**
 * Defines the event for organization member removal.
 *
 * @author Anton Korneta
 */
public class MemberRemovedEvent implements MemberEvent {

    private final String       initiator;
    private final User         member;
    private final Organization organization;

    public MemberRemovedEvent(String initiator,
                              User member,
                              Organization organization) {
        this.initiator = initiator;
        this.member = member;
        this.organization = organization;
    }

    @Override
    public EventType getType() {
        return EventType.MEMBER_REMOVED;
    }

    @Override
    public Organization getOrganization() {
        return organization;
    }

    @Override
    public User getMember() {
        return member;
    }

    /** Returns name of user who initiated member removal */
    public String getInitiator() {
        return initiator;
    }

}

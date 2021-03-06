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
package com.codenvy.plugin.pullrequest.client;

import org.eclipse.che.plugin.pullrequest.client.ContributeMessages;
import org.eclipse.che.plugin.pullrequest.client.parts.contribute.StagesProvider;
import org.eclipse.che.plugin.pullrequest.client.steps.CommitWorkingTreeStep;
import org.eclipse.che.plugin.pullrequest.client.steps.CreateForkStep;
import org.eclipse.che.plugin.pullrequest.client.steps.DetectPullRequestStep;
import org.eclipse.che.plugin.pullrequest.client.steps.IssuePullRequestStep;
import org.eclipse.che.plugin.pullrequest.client.steps.PushBranchOnForkStep;
import org.eclipse.che.plugin.pullrequest.client.steps.PushBranchOnOriginStep;
import org.eclipse.che.plugin.pullrequest.client.steps.UpdatePullRequestStep;
import org.eclipse.che.plugin.pullrequest.client.workflow.Context;
import org.eclipse.che.plugin.pullrequest.client.workflow.Step;
import com.google.common.collect.ImmutableSet;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * Provides displayed stages for Bitbucket contribution workflow.
 *
 * @author Mihail Kuznyetsov
 */
@Singleton
public class BitbucketStagesProvider implements StagesProvider {

    private static final Set<Class<? extends Step>> UPDATE_FORK_STEP_DONE_TYPES;
    private static final Set<Class<? extends Step>> UPDATE_ORIGIN_STEP_DONE_TYPES;
    private static final Set<Class<? extends Step>> CREATION_ORIGIN_STEP_DONE_TYPES;
    private static final Set<Class<? extends Step>> CREATION_FORK_STEP_DONE_TYPES;

    static {
        UPDATE_FORK_STEP_DONE_TYPES = ImmutableSet.of(PushBranchOnForkStep.class,
                                                      UpdatePullRequestStep.class);
        UPDATE_ORIGIN_STEP_DONE_TYPES = ImmutableSet.of(PushBranchOnOriginStep.class,
                                                        UpdatePullRequestStep.class);
        CREATION_FORK_STEP_DONE_TYPES = ImmutableSet.of(CreateForkStep.class,
                                                        PushBranchOnForkStep.class,
                                                        IssuePullRequestStep.class);
        CREATION_ORIGIN_STEP_DONE_TYPES = ImmutableSet.of(PushBranchOnOriginStep.class,
                                                          IssuePullRequestStep.class);
    }

    private final ContributeMessages messages;

    @Inject
    public BitbucketStagesProvider(final ContributeMessages messages) {
        this.messages = messages;
    }

    @Override
    public List<String> getStages(final Context context) {
        if (context.isUpdateMode()) {
            return asList(messages.contributePartStatusSectionNewCommitsPushedStepLabel(),
                          messages.contributePartStatusSectionPullRequestUpdatedStepLabel());
        }
        if (context.isForkAvailable()) {
            return asList(messages.contributePartStatusSectionForkCreatedStepLabel(),
                          messages.contributePartStatusSectionBranchPushedForkStepLabel(),
                          messages.contributePartStatusSectionPullRequestIssuedStepLabel());
        } else {
            return asList(messages.contributePartStatusSectionBranchPushedOriginStepLabel(),
                          messages.contributePartStatusSectionPullRequestIssuedStepLabel());
        }
    }

    @Override
    public Set<Class<? extends Step>> getStepDoneTypes(Context context) {
        if (context.isUpdateMode()) {
            return context.isForkAvailable() ? UPDATE_FORK_STEP_DONE_TYPES : UPDATE_ORIGIN_STEP_DONE_TYPES;
        }
        return context.isForkAvailable() ? CREATION_FORK_STEP_DONE_TYPES : CREATION_ORIGIN_STEP_DONE_TYPES;
    }

    @Override
    public Set<Class<? extends Step>> getStepErrorTypes(Context context) {
        return getStepDoneTypes(context);
    }

    @Override
    public Class<? extends Step> getDisplayStagesType(Context context) {
        return context.isUpdateMode() ? CommitWorkingTreeStep.class : DetectPullRequestStep.class;
    }
}

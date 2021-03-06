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
package org.eclipse.che.ide.ext.bitbucket.server;

/**
 * Defines URL templates for hosted version of BitBucket.
 *
 * @author Igor Vinokur
 */
public class BitbucketURLTemplates implements URLTemplates {

    private static final String BITBUCKET_API_URL     = "https://api.bitbucket.org";
    private static final String BITBUCKET_2_0_API_URL = BITBUCKET_API_URL + "/2.0";
    private static final String BITBUCKET_1_0_API_URL = BITBUCKET_API_URL + "/1.0";

    @Override
    public String repositoryUrl(String owner, String repositorySlug) {
        return BITBUCKET_2_0_API_URL + "/repositories/" + owner + "/" + repositorySlug;
    }

    @Override
    public String userUrl() {
        return BITBUCKET_2_0_API_URL + "/user";
    }

    @Override
    public String pullRequestUrl(String owner, String repositorySlug) {
        return BITBUCKET_2_0_API_URL + "/repositories/" + owner + "/" + repositorySlug + "/pullrequests";
    }

    @Override
    public String updatePullRequestUrl(String owner, String repositorySlug, int pullRequestId) {
        return BITBUCKET_2_0_API_URL + "/repositories/" + owner + "/" + repositorySlug + "/pullrequests/" + pullRequestId;
    }

    @Override
    public String forksUrl(String owner, String repositorySlug) {
        return BITBUCKET_2_0_API_URL + "/repositories/" + owner + "/" + repositorySlug + "/forks";
    }

    @Override
    public String forkRepositoryUrl(String owner, String repositorySlug) {
        return BITBUCKET_1_0_API_URL + "/repositories/" + owner + "/" + repositorySlug + "/fork";
    }
}

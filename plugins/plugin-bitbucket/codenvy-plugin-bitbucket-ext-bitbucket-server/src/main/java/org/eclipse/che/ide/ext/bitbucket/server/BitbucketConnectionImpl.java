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

import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketPullRequest;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketPullRequestsPage;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketRepositoriesPage;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketRepository;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketRepositoryFork;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketUser;
import org.eclipse.che.security.oauth.shared.OAuthTokenProvider;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import static java.net.URLEncoder.encode;
import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.HttpMethod.PUT;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.che.commons.json.JsonHelper.toJson;
import static org.eclipse.che.commons.json.JsonNameConventions.CAMEL_UNDERSCORE;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.eclipse.che.ide.ext.bitbucket.server.rest.BitbucketRequestUtils.doRequest;
import static org.eclipse.che.ide.ext.bitbucket.server.rest.BitbucketRequestUtils.getBitbucketPage;
import static org.eclipse.che.ide.ext.bitbucket.server.rest.BitbucketRequestUtils.getJson;
import static org.eclipse.che.ide.ext.bitbucket.server.rest.BitbucketRequestUtils.parseJsonResponse;
import static org.eclipse.che.ide.ext.bitbucket.server.rest.BitbucketRequestUtils.postJson;

/**
 * Implementation of {@link BitbucketConnection} for hosted version of Bitbucket.
 *
 * @author Igor Vinokur
 */
public class BitbucketConnectionImpl implements BitbucketConnection {

    private final URLTemplates       urlTemplates;
    private final OAuthTokenProvider tokenProvider;

    BitbucketConnectionImpl(OAuthTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
        this.urlTemplates = new BitbucketURLTemplates();
    }

    @Override
    public BitbucketUser getUser() throws ServerException, IOException, BitbucketException {
        final String response = getJson(this, urlTemplates.userUrl());
        return parseJsonResponse(response, BitbucketUser.class);
    }

    @Override
    public BitbucketRepository getRepository(String owner, String repositorySlug) throws IOException, BitbucketException, ServerException {
        final String response = getJson(this, urlTemplates.repositoryUrl(owner, repositorySlug));
        return parseJsonResponse(response, BitbucketRepository.class);
    }

    @Override
    public List<BitbucketPullRequest> getRepositoryPullRequests(String owner, String repositorySlug) throws
                                                                                                     ServerException,
                                                                                                     IOException,
                                                                                                     BitbucketException {
        final List<BitbucketPullRequest> pullRequests = new ArrayList<>();
        BitbucketPullRequestsPage pullRequestsPage = newDto(BitbucketPullRequestsPage.class);

        do {
            final String nextPageUrl = pullRequestsPage.getNext();
            final String url = nextPageUrl == null ? urlTemplates.pullRequestUrl(owner, repositorySlug) : nextPageUrl;

            pullRequestsPage = getBitbucketPage(this, url, BitbucketPullRequestsPage.class);
            pullRequests.addAll(pullRequestsPage.getValues());
        } while (pullRequestsPage.getNext() != null);

        return pullRequests;
    }

    @Override
    public BitbucketPullRequest openPullRequest(String owner,
                                                String repositorySlug,
                                                BitbucketPullRequest pullRequest) throws ServerException, IOException, BitbucketException {
        final String url = urlTemplates.pullRequestUrl(owner, repositorySlug);
        final String response = postJson(this, url, toJson(pullRequest, CAMEL_UNDERSCORE));
        return parseJsonResponse(response, BitbucketPullRequest.class);
    }

    @Override
    public BitbucketPullRequest updatePullRequest(String owner,
                                                  String repositorySlug,
                                                  BitbucketPullRequest pullRequest) throws ServerException,
                                                                                           IOException,
                                                                                           BitbucketException {
        final String url = urlTemplates.updatePullRequestUrl(owner, repositorySlug, pullRequest.getId());
        String response = doRequest(this, PUT, url, APPLICATION_JSON, toJson(pullRequest));
        return parseJsonResponse(response, BitbucketPullRequest.class);
    }

    @Override
    public List<BitbucketRepository> getRepositoryForks(String owner, String repositorySlug) throws IOException,
                                                                                                    BitbucketException,
                                                                                                    ServerException {
        final List<BitbucketRepository> repositories = new ArrayList<>();
        BitbucketRepositoriesPage repositoryPage = newDto(BitbucketRepositoriesPage.class);

        do {
            final String nextPageUrl = repositoryPage.getNext();
            final String url = nextPageUrl == null ? urlTemplates.forksUrl(owner, repositorySlug) : nextPageUrl;

            repositoryPage = getBitbucketPage(this, url, BitbucketRepositoriesPage.class);
            repositories.addAll(repositoryPage.getValues());
        } while (repositoryPage.getNext() != null);

        return repositories;
    }

    @Override
    public BitbucketRepositoryFork forkRepository(String owner,
                                                  String repositorySlug,
                                                  String forkName,
                                                  boolean isForkPrivate) throws IOException, BitbucketException, ServerException {
        final String url = urlTemplates.forkRepositoryUrl(owner, repositorySlug);
        final String data = "name=" + encode(forkName, "UTF-8") + "&is_private=" + isForkPrivate;
        final String response = doRequest(this, POST, url, APPLICATION_FORM_URLENCODED, data);
        return parseJsonResponse(response, BitbucketRepositoryFork.class);
    }

    @Override
    public void authorizeRequest(HttpURLConnection http, String requestMethod, String requestUrl) throws IOException {
        final OAuthToken token = tokenProvider.getToken("bitbucket", EnvironmentContext.getCurrent().getSubject().getUserId());
        if (token != null) {
            http.setRequestProperty(AUTHORIZATION, "Bearer " + token.getToken());
        }
    }
}

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
package com.codenvy.selenium.dashboard.license;

import com.codenvy.selenium.core.client.OnpremTestLicenseServiceClient;
import com.codenvy.selenium.pageobject.dashboard.CodenvyAdminDashboard;
import com.codenvy.selenium.pageobject.dashboard.DashboardSettings;
import com.google.inject.Inject;

import org.eclipse.che.commons.json.JsonParseException;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestAuthServiceClient;
import org.eclipse.che.selenium.core.client.TestUserServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.AdminTestUser;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.user.TestUserImpl;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.dashboard.CreateWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.DashboardPopup;
import org.eclipse.che.selenium.pageobject.dashboard.DashboardWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.NavigationBar;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.inject.Named;
import java.io.IOException;

import static java.util.Optional.ofNullable;
import static org.testng.Assert.assertEquals;

/**
 * @author Dmytro Nochevnov
 */
public class UserLicenseNumberLimitExceededTest {

    private static final String WS_NAME = "license";

    @Inject
    private DefaultTestUser defaultTestUser;

    // test users number = 2 = 4 - (<admin> + <main_user>)
    private TestUser user1;
    private TestUser user2;

    @Inject
    @Named("license-for-4-seats-till-2020.01.11")
    private String                         nonExpiredLicense;
    @Inject
    private DashboardSettings              dashboardSettingsPage;
    @Inject
    private CodenvyAdminDashboard          dashboardPage;
    @Inject
    private DashboardPopup                 dashboardPopup;
    @Inject
    private DashboardWorkspace             dashboardWorkspace;
    @Inject
    private NotificationsPopupPanel        notifications;
    @Inject
    private TestAuthServiceClient          authServiceClient;
    @Inject
    private OnpremTestLicenseServiceClient licenseServiceClient;
    @Inject
    private SeleniumWebDriver              seleniumWebDriver;
    @Inject
    private TestUserServiceClient          testUserServiceClient;
    @Inject
    private TestWorkspaceServiceClient     workspaceServiceClient;
    @Inject
    private AdminTestUser                  adminTestUser;
    @Inject
    private CreateWorkspace                createWorkspace;
    @Inject
    private NavigationBar                  navigationBar;

    @BeforeClass
    public void setupClass() throws Exception {
        licenseServiceClient.removeLicense();
    }

    @BeforeMethod
    public void setupMethod() throws Exception {
        licenseServiceClient.addLicense(nonExpiredLicense);
        registerUsers();
        licenseServiceClient.removeLicense();
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        licenseServiceClient.removeLicense();
        ofNullable(user1).ifPresent(TestUser::delete);
        ofNullable(user2).ifPresent(TestUser::delete);
    }

    @AfterClass
    public void tearDown() throws Exception {
        workspaceServiceClient.delete(WS_NAME, defaultTestUser.getName());
        workspaceServiceClient.delete(WS_NAME, adminTestUser.getName());
    }

    @Test
    public void shouldNotAllowNonAdminToStartWorkspace() {
        // when
        dashboardPage.open(user1.getAuthToken());

        checkErrorMessageOnRunWorkspace("Failed\n"
                                        +
                                        "The Codenvy license has reached its user limit - you can access the user dashboard but not the IDE.");
    }

    @Test
    public void shouldDisplayNagMessage() throws IOException, JsonParseException {
        // when
        dashboardPage.open(adminTestUser.getAuthToken());

        // then
        dashboardPopup.waitPopupDisplays();
        assertEquals(dashboardPopup.getPopupTitle(), "User License Has Reached Its Limit");
        assertEquals(dashboardPopup.getPopupMessage(), "Your user license has reached its limit. You cannot add more users.");
        dashboardPopup.closePopup();

        assertEquals(dashboardPage.getLicenseNagMessage(),
                     "No valid license detected. Codenvy is free for 3 users. Please upgrade. Your mother would approve!");

        /* should not allow to start workspace */
        checkErrorNotificationInIde("Start Workspace Error\n"
                                    +
                                    "There are currently 4 users registered in Codenvy but your license only allows 3. Users cannot start workspaces.");

        /* check disappear nag message after renew license */
        // when
        dashboardPage.open(adminTestUser.getAuthToken());
        gotoSettingsPage();
        dashboardSettingsPage.applyLicense(nonExpiredLicense);
        dashboardSettingsPage.waitOnRemoveLicenseButtonAppears();

        // then
        dashboardPage.waitNagMessageDisappear();
    }

    private void checkErrorMessageOnRunWorkspace(String errorMessage) {
        navigationBar.waitNavigationBar();
        navigationBar.clickOnMenu(NavigationBar.MenuItem.WORKSPACES);

        dashboardWorkspace.clickOnNewWorkspaceBtn();
        createWorkspace.waitToolbar();
        createWorkspace.typeWorkspaceName(WS_NAME);
        createWorkspace.clickCreate();

        // TODO work around issue https://github.com/codenvy/codenvy/issues/2329
        // TODO work around automatic run of workspace in time of creation

        dashboardPage.waitNotificationMessage(errorMessage);
    }

    private void checkErrorNotificationInIde(String errorMessage) {
        navigationBar.waitNavigationBar();
        navigationBar.clickOnMenu(NavigationBar.MenuItem.WORKSPACES);

        dashboardWorkspace.clickOnNewWorkspaceBtn();
        createWorkspace.waitToolbar();
        createWorkspace.typeWorkspaceName(WS_NAME);
        createWorkspace.clickCreate();

        dashboardWorkspace.clickOpenInIdeWsBtn();
        seleniumWebDriver.switchFromDashboardIframeToIde();

        notifications.waitExpectedMessageOnProgressPanelAndClosed(errorMessage);
    }

    private void gotoSettingsPage() {
        dashboardPage.waitPageOpened();
        dashboardPage.clickOnSettingsItem();
        dashboardSettingsPage.waitPageOpened();
    }

    private void registerUsers() throws Exception {
        user1 = new TestUserImpl(testUserServiceClient, workspaceServiceClient, authServiceClient);
        user2 = new TestUserImpl(testUserServiceClient, workspaceServiceClient, authServiceClient);
    }
}

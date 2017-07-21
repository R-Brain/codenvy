/*
 *  [2012] - [2017] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.selenium.dashboard.organization;

import com.codenvy.organization.shared.dto.OrganizationDto;
import com.codenvy.selenium.core.client.OnpremTestOrganizationServiceClient;
import org.eclipse.che.selenium.pageobject.dashboard.NavigationBar;
import com.codenvy.selenium.pageobject.dashboard.organization.OrganizationListPage;
import com.codenvy.selenium.pageobject.dashboard.organization.OrganizationPage;
import com.google.inject.Inject;

import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.user.AdminTestUser;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Test validates organization views for simple user.
 *
 * @author Ann Shumilova
 */
public class UserOrganizationTest {
    private static final Logger LOG = LoggerFactory.getLogger(UserOrganizationTest.class);

    private OrganizationDto parentOrganization;
    private OrganizationDto childOrganization;

    @Inject
    private OrganizationListPage                organizationListPage;
    @Inject
    private OrganizationPage                    organizationPage;
    @Inject
    private NavigationBar                       navigationBar;
    @Inject
    private Dashboard                           dashboard;
    @Inject
    private OnpremTestOrganizationServiceClient organizationServiceClient;
    @Inject
    private DefaultTestUser                     testUser;
    @Inject
    private AdminTestUser                       adminTestUser;

    @BeforeClass
    public void setUp() throws Exception {
        parentOrganization = organizationServiceClient.createOrganization(NameGenerator.generate("organization", 5), adminTestUser.getAuthToken());
        childOrganization = organizationServiceClient
                .createOrganization(NameGenerator.generate("organization", 5), parentOrganization.getId(), adminTestUser.getAuthToken());

        organizationServiceClient.addOrganizationMember(parentOrganization.getId(), testUser.getId(), adminTestUser.getAuthToken());
        organizationServiceClient.addOrganizationMember(childOrganization.getId(), testUser.getId(), adminTestUser.getAuthToken());

        dashboard.open(testUser.getAuthToken());
    }

    @AfterClass
    public void tearDown() throws Exception {
        organizationServiceClient.deleteOrganizationById(childOrganization.getId(), adminTestUser.getAuthToken());
        organizationServiceClient.deleteOrganizationById(parentOrganization.getId(), adminTestUser.getAuthToken());
    }

    @Test(priority = 1)
    public void testOrganizationListComponents() {
        navigationBar.waitNavigationBar();
        int organizationsCount = 2;
        navigationBar.clickOnMenu(NavigationBar.MenuItem.ORGANIZATIONS);
        organizationListPage.waitForOrganizationsToolbar();
        organizationListPage.waitForOrganizationsList();

        assertEquals(navigationBar.getMenuCounterValue(NavigationBar.MenuItem.ORGANIZATIONS), String.valueOf(organizationsCount));
        assertEquals(organizationListPage.getOrganizationsToolbarTitle(), "Organizations");
        assertEquals(navigationBar.getMenuCounterValue(NavigationBar.MenuItem.ORGANIZATIONS), String.valueOf(organizationsCount));
        assertEquals(organizationListPage.getOrganizationListItemCount(), organizationsCount);
        assertFalse(organizationListPage.isAddOrganizationButtonVisible());
        assertTrue(organizationListPage.isSearchInputVisible());
        //Check all headers are present:
        ArrayList<String> headers = organizationListPage.getOrganizationListHeaders();
        assertTrue(headers.contains(OrganizationListPage.OrganizationListHeader.NAME.getTitle()));
        assertTrue(headers.contains(OrganizationListPage.OrganizationListHeader.MEMBERS.getTitle()));
        assertTrue(headers.contains(OrganizationListPage.OrganizationListHeader.TOTAL_RAM.getTitle()));
        assertTrue(headers.contains(OrganizationListPage.OrganizationListHeader.AVAILABLE_RAM.getTitle()));
        assertTrue(headers.contains(OrganizationListPage.OrganizationListHeader.SUB_ORGANIZATIONS.getTitle()));
        assertTrue(headers.contains(OrganizationListPage.OrganizationListHeader.ACTIONS.getTitle()));

        assertTrue(
                organizationListPage.getValues(OrganizationListPage.OrganizationListHeader.NAME).contains(parentOrganization.getQualifiedName()));
        assertTrue(organizationListPage.getValues(OrganizationListPage.OrganizationListHeader.NAME).contains(childOrganization.getQualifiedName()));
    }

    @Test(priority = 2)
    public void testParentOrganization() {
        organizationListPage.clickOnOrganization(parentOrganization.getName());

        organizationPage.waitOrganizationName(parentOrganization.getName());
        assertTrue(organizationPage.isOrganizationNameReadonly());
        assertTrue(organizationPage.isWorkspaceCapReadonly());
        assertTrue(organizationPage.isRunningCapReadonly());
        assertTrue(organizationPage.isRAMCapReadonly());
        assertTrue(organizationPage.isWorkspaceCapReadonly());
        assertFalse(organizationPage.isDeleteButtonVisible());

        organizationPage.clickMembersTab();
        organizationPage.waitMembersList();
        assertFalse(organizationPage.isAddMemberButtonVisible());

        organizationPage.clickSubOrganizationsTab();
        organizationListPage.waitForOrganizationsList();
        assertFalse(organizationListPage.isAddOrganizationButtonVisible());
        assertFalse(organizationListPage.isAddSubOrganizationButtonVisible());
        assertTrue(organizationListPage.getValues(OrganizationListPage.OrganizationListHeader.NAME).contains(childOrganization.getQualifiedName()));

        organizationPage.clickBackButton();
        organizationListPage.waitForOrganizationsList();
        assertEquals(organizationListPage.getOrganizationListItemCount(), 2);
    }

    @Test(priority = 3)
    public void testChildOrganization() {
        navigationBar.clickOnMenu(NavigationBar.MenuItem.ORGANIZATIONS);
        organizationListPage.waitForOrganizationsToolbar();
        organizationListPage.waitForOrganizationsList();
        organizationListPage.clickOnOrganization(childOrganization.getQualifiedName());

        organizationPage.waitOrganizationName(childOrganization.getQualifiedName());
        assertTrue(organizationPage.isOrganizationNameReadonly());
        assertTrue(organizationPage.isWorkspaceCapReadonly());
        assertTrue(organizationPage.isRunningCapReadonly());
        assertTrue(organizationPage.isRAMCapReadonly());
        assertTrue(organizationPage.isWorkspaceCapReadonly());
        assertFalse(organizationPage.isDeleteButtonVisible());

        organizationPage.clickMembersTab();
        organizationPage.waitMembersList();
        assertFalse(organizationPage.isAddMemberButtonVisible());

        organizationPage.clickSubOrganizationsTab();
        organizationListPage.waitForSubOrganizationsEmptyList();
        assertFalse(organizationListPage.isAddOrganizationButtonVisible());
        assertFalse(organizationListPage.isAddSubOrganizationButtonVisible());

        organizationPage.clickBackButton();
        organizationPage.waitOrganizationName(parentOrganization.getName());
    }
}

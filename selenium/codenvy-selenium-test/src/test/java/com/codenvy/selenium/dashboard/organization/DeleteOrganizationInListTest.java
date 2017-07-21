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
import com.codenvy.selenium.pageobject.dashboard.ConfirmDialog;
import org.eclipse.che.selenium.pageobject.dashboard.NavigationBar;
import com.codenvy.selenium.pageobject.dashboard.organization.OrganizationListPage;
import com.google.inject.Inject;

import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.user.AdminTestUser;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static org.eclipse.che.selenium.pageobject.dashboard.NavigationBar.MenuItem.ORGANIZATIONS;
import static com.codenvy.selenium.pageobject.dashboard.organization.OrganizationListPage.OrganizationListHeader.NAME;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Test validates organization creation and actions on it in the list of organizations.
 *
 * @author Ann Shumilova
 */
public class DeleteOrganizationInListTest {
    private static final Logger LOG = LoggerFactory.getLogger(DeleteOrganizationInListTest.class);

    private List<OrganizationDto> organizations;
    private OrganizationDto       organization;

    @Inject
    private OrganizationListPage                organizationListPage;
    @Inject
    private NavigationBar                       navigationBar;
    @Inject
    private ConfirmDialog                       confirmDialog;
    @Inject
    private Dashboard                           dashboard;
    @Inject
    private OnpremTestOrganizationServiceClient organizationServiceClient;
    @Inject
    private AdminTestUser                       adminTestUser;

    @BeforeClass
    public void setUp() throws Exception {
        dashboard.open(adminTestUser.getAuthToken());

        String organizationName = NameGenerator.generate("organization", 5);
        organization = organizationServiceClient.createOrganization(organizationName, adminTestUser.getAuthToken());
        organizations = organizationServiceClient.getOrganizations(adminTestUser.getAuthToken());
    }

    @AfterClass
    public void tearDown() throws Exception {
        organizationServiceClient.deleteOrganizationById(organization.getId(), adminTestUser.getAuthToken());
    }

    @Test
    public void testOrganizationDeletionFromList() {
        navigationBar.waitNavigationBar();
        int organizationsCount = organizations.size();

        navigationBar.clickOnMenu(ORGANIZATIONS);
        organizationListPage.waitForOrganizationsToolbar();
        assertEquals(navigationBar.getMenuCounterValue(ORGANIZATIONS), String.valueOf(organizationsCount));
        navigationBar.clickOnMenu(ORGANIZATIONS);
        organizationListPage.waitForOrganizationsToolbar();
        organizationListPage.waitForOrganizationsList();
        assertEquals(organizationListPage.getOrganizationListItemCount(), organizationsCount);
        assertTrue(organizationListPage.getValues(NAME).contains(organization.getName()));

        // Tests delete:
        organizationListPage.clickOnDeleteButton(organization.getName());
        confirmDialog.waitOpened();
        assertEquals(confirmDialog.getTitle(), "Delete organization");
        assertEquals(confirmDialog.getMessage(), String.format("Would you like to delete organization '%s'?", organization.getName()));
        assertEquals(confirmDialog.getConfirmButtonTitle(), "Delete");
        assertEquals(confirmDialog.getCancelButtonTitle(), "Close");
        confirmDialog.closeDialog();
        confirmDialog.waitClosed();
        assertEquals(organizationListPage.getOrganizationListItemCount(), organizationsCount);

        organizationListPage.clickOnDeleteButton(organization.getName());
        confirmDialog.waitOpened();
        confirmDialog.clickCancel();
        confirmDialog.waitClosed();
        assertEquals(organizationListPage.getOrganizationListItemCount(), organizationsCount);

        organizationListPage.clickOnDeleteButton(organization.getName());
        confirmDialog.waitOpened();
        confirmDialog.clickConfirm();
        confirmDialog.waitClosed();
        organizationListPage.waitForOrganizationsList();
        WaitUtils.sleepQuietly(3);
        assertEquals(organizationListPage.getOrganizationListItemCount(), organizationsCount - 1);
        assertFalse(organizationListPage.getValues(NAME).contains(organization.getName()));
        assertEquals(navigationBar.getMenuCounterValue(ORGANIZATIONS), String.valueOf(organizationsCount - 1));
    }
}

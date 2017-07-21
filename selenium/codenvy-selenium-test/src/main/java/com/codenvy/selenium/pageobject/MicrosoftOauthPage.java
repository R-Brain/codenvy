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
package com.codenvy.selenium.pageobject;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;

/**
 * @author Musienko Maxim
 */
@Singleton
public class MicrosoftOauthPage {
    private final SeleniumWebDriver seleniumWebDriver;

    @Inject
    public MicrosoftOauthPage(SeleniumWebDriver seleniumWebDriver) {
        this.seleniumWebDriver = seleniumWebDriver;
        PageFactory.initElements(seleniumWebDriver, this);
    }

    private static final String LOGIN_PAGE = "https://login.live.com/";

    private interface Locators {
        String EMAIL_OR_PHONE_FIELD_ID = "input[type='email']";
        String PASSWORD_FIELD_ID       = "//input[@name='passwd' and @type='password']";
        String SIGN_IN                 = "input[value='Sign in']";
        String ACCEPT_APP_BTN_CSS      = "button#accept-button";
    }

    @FindBy(css = Locators.EMAIL_OR_PHONE_FIELD_ID)
    WebElement emailField;

    @FindBy(xpath = Locators.PASSWORD_FIELD_ID)
    WebElement passwordField;


    @FindBy(css = Locators.SIGN_IN)
    WebElement signInBtn;

    @FindBy(css = Locators.ACCEPT_APP_BTN_CSS)
    WebElement acceptBtn;

    /**
     * type the user email or login
     *
     * @param userEmail
     *         email/login to Microsoft account,  defined by user
     */
    public void typeEmailOrPhone(String userEmail) {
        new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC).until(ExpectedConditions.visibilityOf(emailField))
                                                                                .sendKeys(userEmail);
        new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC).until(ExpectedConditions.visibilityOf(emailField))
                                                                                .sendKeys(Keys.ENTER);
    }


    /**
     * type the user password or email
     *
     * @param userPassword
     *         password to Microsoft account, defined by user
     */
    public void typeUserPassword(String userPassword) {
        new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC).until(ExpectedConditions.urlContains(LOGIN_PAGE));
        new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
                .until(ExpectedConditions.elementToBeClickable(passwordField))
                .sendKeys(userPassword);
    }

    /**
     * click in Sign in button
     */
    public void clickOnSignInBtn() {
        new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC).until(ExpectedConditions.visibilityOf(signInBtn)).click();
    }

    public void clickOnAcceptBtn() {
        new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC).until(ExpectedConditions.visibilityOf(acceptBtn)).click();
    }

    /**
     * performs login to MicrosoftAccount
     *
     * @param password
     * @param login
     */
    public void loginToMicrosoftAccount(String login, String password) {
        typeEmailOrPhone(login);
        typeUserPassword(password);
        clickOnSignInBtn();
    }

}

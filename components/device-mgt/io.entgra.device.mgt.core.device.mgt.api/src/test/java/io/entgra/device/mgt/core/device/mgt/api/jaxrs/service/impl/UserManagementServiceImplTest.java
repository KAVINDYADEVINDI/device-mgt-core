/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.impl;

import io.entgra.device.mgt.core.device.mgt.common.Device;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.context.CarbonContext;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.configuration.mgt.ConfigurationManagementException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.OTPManagementException;
import io.entgra.device.mgt.core.device.mgt.common.invitation.mgt.DeviceEnrollmentInvitation;
import io.entgra.device.mgt.core.device.mgt.common.spi.OTPManagementService;
import io.entgra.device.mgt.core.device.mgt.core.otp.mgt.service.OTPManagementServiceImpl;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderServiceImpl;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.BasicUserInfo;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.EnrollmentInvitation;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.UserInfo;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.api.UserManagementService;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.Constants;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.DeviceMgtAPIUtils;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.ws.rs.core.Response;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This is a test case for {@link UserManagementService}.
 */
@PowerMockIgnore({"javax.ws.rs.*", "org.apache.log4j.*", "org.mockito.*"})
@SuppressStaticInitializationFor({"io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.DeviceMgtAPIUtils",
        "org.wso2.carbon.context.CarbonContext"})
@PrepareForTest({DeviceMgtAPIUtils.class, MultitenantUtils.class, CarbonContext.class})
public class UserManagementServiceImplTest {
    private UserStoreManager userStoreManager;
    private UserManagementService userManagementService;
    private DeviceManagementProviderService deviceManagementProviderService;
    private OTPManagementService otpManagementService;
    private static final String DEFAULT_DEVICE_USER = "Internal/devicemgt-user";
    private UserRealm userRealm;
    private EnrollmentInvitation enrollmentInvitation;
    private DeviceEnrollmentInvitation deviceEnrollmentInvitation;
    private List<String> userList;
    private static final String TEST_USERNAME = "test";
    private static final String TEST2_USERNAME = "test2";
    private static final String TEST3_USERNAME = "test3";

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @BeforeClass
    public void setup() throws UserStoreException {
        initMocks(this);
        userManagementService = new UserManagementServiceImpl();
        userStoreManager = Mockito.mock(UserStoreManager.class, Mockito.RETURNS_MOCKS);
        deviceManagementProviderService = Mockito
                .mock(DeviceManagementProviderServiceImpl.class, Mockito.CALLS_REAL_METHODS);
        otpManagementService = Mockito.mock(OTPManagementServiceImpl.class, Mockito.CALLS_REAL_METHODS);
        userRealm = Mockito.mock(UserRealm.class);
        RealmConfiguration realmConfiguration = Mockito.mock(RealmConfiguration.class);
        Mockito.doReturn(null).when(realmConfiguration).getSecondaryRealmConfig();
        Mockito.doReturn(realmConfiguration).when(userRealm).getRealmConfiguration();
        enrollmentInvitation = new EnrollmentInvitation();
        List<String> recipients = new ArrayList<>();
        recipients.add(TEST_USERNAME);
        enrollmentInvitation.setDeviceType("android");
        enrollmentInvitation.setRecipients(recipients);
        userList = new ArrayList<>();
        userList.add(TEST_USERNAME);
        deviceEnrollmentInvitation = new DeviceEnrollmentInvitation();
        deviceEnrollmentInvitation.setUsernames(userList);
    }

    @Test(description = "This method tests the addUser method of UserManagementService")
    public void testAddUser() throws UserStoreException, ConfigurationManagementException, DeviceManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getUserStoreManager"))
                .toReturn(this.userStoreManager);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        Mockito.doReturn(true).when(userStoreManager).isExistingUser("admin");
        CarbonContext carbonContext = Mockito.mock(CarbonContext.class, Mockito.RETURNS_MOCKS);
        PowerMockito.stub(PowerMockito.method(CarbonContext.class, "getThreadLocalCarbonContext"))
                .toReturn(carbonContext);
        Mockito.when(carbonContext.getTenantId()).thenReturn(-1234);
        Mockito.when(carbonContext.getTenantDomain()).thenReturn("carbon.super");
        Mockito.when(carbonContext.getUsername()).thenReturn("admin");
        Mockito.doReturn(0).when(deviceManagementProviderService).getDeviceCount(TEST_USERNAME);
        Mockito.doAnswer(new Answer() {
            private int count = 0;

            public Object answer(InvocationOnMock invocation) {
                if (count == 0) {
                    count++;
                    return false;
                } else {
                    return true;
                }
            }
        }).when(userStoreManager).isExistingUser(TEST_USERNAME);

        Mockito.doReturn("test@test.com").when(userStoreManager)
                .getUserClaimValue(TEST_USERNAME, Constants.USER_CLAIM_EMAIL_ADDRESS, null);
        Mockito.doReturn(TEST_USERNAME).when(userStoreManager)
                .getUserClaimValue(TEST_USERNAME, Constants.USER_CLAIM_FIRST_NAME, null);
        Mockito.doReturn(TEST_USERNAME).when(userStoreManager)
                .getUserClaimValue(TEST_USERNAME, Constants.USER_CLAIM_LAST_NAME, null);
        Mockito.doNothing().when(userStoreManager)
                .addUser(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        UserInfo userInfo = new UserInfo();
        userInfo.setUsername("admin");
        Response response = userManagementService.addUser(userInfo);
        Assert.assertEquals(response.getStatus(), Response.Status.CONFLICT.getStatusCode(),
                "Same user can be added " + "twice");
        userInfo = getUserInfo();
        Mockito.doReturn(true).when(userStoreManager).isExistingRole(DEFAULT_DEVICE_USER);
        Mockito.doNothing().when(deviceManagementProviderService).sendRegistrationEmail(Mockito.any());
        response = userManagementService.addUser(userInfo);
        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode(), "User addition failed");
    }

    @Test(description = "This method tests the getUser method of UserManagementService", dependsOnMethods =
            "testAddUser")
    public void testGetUser() throws UserStoreException, DeviceManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getUserStoreManager"))
                .toReturn(this.userStoreManager);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        CarbonContext carbonContext = Mockito.mock(CarbonContext.class, Mockito.RETURNS_MOCKS);
        PowerMockito.stub(PowerMockito.method(CarbonContext.class, "getThreadLocalCarbonContext"))
                .toReturn(carbonContext);
        Mockito.when(carbonContext.getTenantId()).thenReturn(-1234);
        Mockito.when(carbonContext.getTenantDomain()).thenReturn("carbon.super");
        Mockito.doReturn(0).when(deviceManagementProviderService).getDeviceCount(TEST_USERNAME);
        Response response = userManagementService.getUser(TEST_USERNAME, null, null);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(), "User retrieval failed");
        BasicUserInfo userInfo = (BasicUserInfo) response.getEntity();
        Assert.assertEquals(userInfo.getFirstname(), TEST_USERNAME,
                "Retrieved user object is different from the original one " + "saved");

        Mockito.doReturn(false).when(userStoreManager).isExistingUser(TEST2_USERNAME);
        response = userManagementService.getUser(TEST2_USERNAME, null, null);
        Assert.assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode(),
                "Non-existing user was retrieved successfully");
    }

    @Test(description = "This method tests the updateUser method of UserManagementService", dependsOnMethods =
            {"testGetUser"})
    public void testUpdateUser() throws UserStoreException, DeviceManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getUserStoreManager"))
                .toReturn(this.userStoreManager);
        CarbonContext carbonContext = Mockito.mock(CarbonContext.class, Mockito.RETURNS_MOCKS);
        PowerMockito.stub(PowerMockito.method(CarbonContext.class, "getThreadLocalCarbonContext"))
                .toReturn(carbonContext);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        Mockito.when(carbonContext.getTenantId()).thenReturn(-1234);
        Mockito.when(carbonContext.getTenantDomain()).thenReturn("carbon.super");
        Mockito.when(carbonContext.getUsername()).thenReturn("admin");
        Mockito.doReturn(0).when(deviceManagementProviderService).getDeviceCount(TEST_USERNAME);
        Response response = userManagementService.updateUser(TEST2_USERNAME, null, null);
        Assert.assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode(),
                "Non-existing user was successfully updated");
        String[] roles = { "Internal/everyone", DEFAULT_DEVICE_USER };
        Mockito.doReturn(roles).when(userStoreManager).getRoleListOfUser(TEST_USERNAME);
        Mockito.doNothing().when(userStoreManager).updateRoleListOfUser(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.doNothing().when(userStoreManager).setUserClaimValues(Mockito.any(), Mockito.any(), Mockito.any());
        response = userManagementService.updateUser(TEST_USERNAME, null, getUserInfo());
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(), "Updating the user info failed");
    }


    @Test(description = "This method tests the getRolesOfUser method of UserManagementService", dependsOnMethods =
            {"testUpdateUser"})
    public void testGetRolesOfUser() {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getUserStoreManager"))
                .toReturn(this.userStoreManager);
        Response response = userManagementService.getRolesOfUser(TEST2_USERNAME, null);
        Assert.assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode(),
                "Roles of a non-existing user was successfully retrieved");
        response = userManagementService.getRolesOfUser(TEST_USERNAME, null);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "Retrieval of roles of a existing user failed.");
    }

    @Test(description = "This method tests the IsUserExists method of UserManagementService", dependsOnMethods =
            {"testGetRolesOfUser"})
    public void testIsUserExists() {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getUserStoreManager"))
                .toReturn(this.userStoreManager);
        Response response = userManagementService.isUserExists(TEST2_USERNAME);
        boolean responseEntity = (boolean) response.getEntity();
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "Check for existence of user failed");
        Assert.assertFalse(responseEntity, "Non-existing user is identified as already existing user");
        response = userManagementService.isUserExists(TEST_USERNAME);
        responseEntity = (boolean) response.getEntity();
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "Check for existence of user failed");
        Assert.assertTrue(responseEntity, "Existing user is identified as non-existing user");
    }

    @Test(description = "This method tests the send invitation method of UserManagementService", dependsOnMethods =
            {"testIsUserExists"})
    public void testSendInvitation() throws OTPManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getOTPManagementService"))
                .toReturn(this.otpManagementService);
        Mockito.doNothing().when(otpManagementService).sendDeviceEnrollmentInvitationMail(Mockito.any());
        Response response = userManagementService.inviteExistingUsersToEnrollDevice(deviceEnrollmentInvitation);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "Inviting existing users to enroll device failed");
    }

    @Test(description = "This method tests the getUserNames method of UserManagementService", dependsOnMethods =
            {"testSendInvitation"})
    public void testGetUserNames() throws UserStoreException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getUserStoreManager"))
                .toReturn(this.userStoreManager);
        Mockito.doReturn(new String[] { TEST_USERNAME }).when(userStoreManager)
                .listUsers(Mockito.anyString(), Mockito.anyInt());
        Response response = userManagementService.getUserNames(TEST_USERNAME, null, "00", 0, 0);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "Getting user names is failed for a valid request");

    }

    @Test(description = "This method tests the getUsers method of UserManagementService",
            dependsOnMethods = {"testGetUserNames"})
    public void testGetUsers() throws DeviceManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getUserStoreManager"))
                .toReturn(userStoreManager);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        Response response = userManagementService.getUsers(null, "00", 0, 10, null);
        CarbonContext carbonContext = Mockito.mock(CarbonContext.class, Mockito.RETURNS_MOCKS);
        PowerMockito.stub(PowerMockito.method(CarbonContext.class, "getThreadLocalCarbonContext"))
                .toReturn(carbonContext);
        Mockito.when(carbonContext.getTenantId()).thenReturn(-1234);
        Mockito.when(carbonContext.getTenantDomain()).thenReturn("carbon.super");
        Mockito.doReturn(0).when(deviceManagementProviderService).getDeviceCount(TEST_USERNAME);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(), "GetUsers request failed");
    }

    @Test(description = "This method tests the inviteToEnrollDevice method of UserManagementService",
            dependsOnMethods = "testGetUsers")
    public void testInviteToEnrollDevice() throws ConfigurationManagementException, DeviceManagementException {
        URL resourceUrl = ClassLoader.getSystemResource("testng.xml");
        System.setProperty("carbon.home", resourceUrl.getPath());
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getUserStoreManager"))
                .toReturn(this.userStoreManager);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getAuthenticatedUser")).toReturn(TEST_USERNAME);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        Mockito.doNothing().when(deviceManagementProviderService).sendEnrolmentInvitation(Mockito.any(), Mockito.any());
        EnrollmentInvitation enrollmentInvitation = new EnrollmentInvitation();
        List<String> recipients = new ArrayList<>();
        recipients.add(TEST_USERNAME);
        enrollmentInvitation.setDeviceType("android");
        enrollmentInvitation.setRecipients(recipients);
        Response response = userManagementService.inviteToEnrollDevice(enrollmentInvitation);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "Inviting users to enroll device failed");
    }

    @Test(description = "This method tests the removeUser method of UserManagementService", dependsOnMethods =
            "testInviteToEnrollDevice")
    public void testRemoveUser() throws DeviceManagementException, UserStoreException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getUserStoreManager"))
                .toReturn(this.userStoreManager);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        CarbonContext carbonContext = Mockito.mock(CarbonContext.class, Mockito.RETURNS_MOCKS);
        PowerMockito.stub(PowerMockito.method(CarbonContext.class, "getThreadLocalCarbonContext"))
                .toReturn(carbonContext);
        Mockito.when(carbonContext.getTenantId()).thenReturn(-1234);
        Mockito.when(carbonContext.getTenantDomain()).thenReturn("carbon.super");
        Mockito.doReturn(0).when(deviceManagementProviderService).getDeviceCount(TEST_USERNAME);
        Mockito.doNothing().when(userStoreManager).deleteUser(Mockito.anyString());
        Response response = userManagementService.removeUser(TEST_USERNAME, null);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "Cannot remove user, the request failed");
        response = userManagementService.removeUser(TEST2_USERNAME, null);
        Assert.assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode(),
                "Successfully removed non-existing user");
    }

    @Test(description = "This method tests the behaviour of getUserCount method of UserManagementService",
            dependsOnMethods = {"testRemoveUser"})
    public void testGetUserCount() throws UserStoreException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getUserRealm")).toReturn(userRealm);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getUserStoreCountRetrieverService"))
                .toReturn(null);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getUserStoreManager"))
                .toReturn(this.userStoreManager);
        Response response = userManagementService.getUserCount();
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(), "User count retrieval failed");
    }

    @Test(description = "This method tests the behaviour of methods when there is an issue with "
            + "DeviceManagementProviderService", dependsOnMethods = {"testGetUserCount"})
    public void testNegativeScenarios1()
            throws ConfigurationManagementException, DeviceManagementException, OTPManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getUserStoreManager"))
                .toReturn(this.userStoreManager);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getAuthenticatedUser")).toReturn(TEST_USERNAME);
        Mockito.reset(deviceManagementProviderService);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getOTPManagementService"))
                .toReturn(this.otpManagementService);
        Mockito.reset(otpManagementService);
        Mockito.doThrow(new DeviceManagementException()).when(deviceManagementProviderService)
                .sendEnrolmentInvitation(Mockito.any(), Mockito.any());
        Mockito.doThrow(new OTPManagementException()).when(otpManagementService)
                .sendDeviceEnrollmentInvitationMail(Mockito.any());
        Response response = userManagementService.inviteExistingUsersToEnrollDevice(deviceEnrollmentInvitation);
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "Invite existing users to enroll device succeeded under erroneous conditions");
        response = userManagementService.inviteToEnrollDevice(enrollmentInvitation);
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "Invite existing users to enroll device succeeded under erroneous conditions");
    }

    @Test(description = "This method tests the behaviour of the different methods when there is an issue is "
            + "userStoreManager", dependsOnMethods = {"testNegativeScenarios1"})
    public void testNegativeScenarios2() throws UserStoreException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getUserStoreManager"))
                .toReturn(this.userStoreManager);
        CarbonContext carbonContext = Mockito.mock(CarbonContext.class, Mockito.RETURNS_MOCKS);
        PowerMockito.stub(PowerMockito.method(CarbonContext.class, "getThreadLocalCarbonContext"))
                .toReturn(carbonContext);
        Mockito.when(carbonContext.getTenantId()).thenReturn(-1234);
        Mockito.when(carbonContext.getUsername()).thenReturn("admin");
        Mockito.when(carbonContext.getTenantDomain()).thenReturn("carbon.super");
        Mockito.doThrow(new UserStoreException()).when(userStoreManager).isExistingUser(TEST3_USERNAME);
        Response response = userManagementService.getUser(TEST3_USERNAME, null, null);
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "Response returned successful for a user retrieval with problematic inputs");
        UserInfo userInfo = new UserInfo();
        userInfo.setUsername(TEST3_USERNAME);
        response = userManagementService.addUser(userInfo);
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "Response returned successful for a user addition with problematic inputs");
        response = userManagementService.updateUser(TEST3_USERNAME, null, userInfo);
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "Response returned successful for a user updating request with problematic inputs");
        response = userManagementService.removeUser(TEST3_USERNAME, null);
        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode(),
                "Response returned successful for a user removal request with problematic inputs");
        response = userManagementService.getRolesOfUser(TEST3_USERNAME, null);
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "Response returned successful for a user role retrieval request with problematic inputs");
        response = userManagementService.isUserExists(TEST3_USERNAME);
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "Response returned successful for checking existence of user under problematic conditions");
    }

    @Test(description = "This method tests the behaviour of various methods when there is an issue with UserStore "
            + "Manager", dependsOnMethods = {"testNegativeScenarios2"})
    public void testNegativeScenarios3() throws UserStoreException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getUserStoreManager"))
                .toReturn(this.userStoreManager);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getUserRealm")).toReturn(userRealm);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getUserStoreCountRetrieverService"))
                .toReturn(null);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getAuthenticatedUser")).toReturn(TEST_USERNAME);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getDeviceManagementService"))
                .toReturn(this.deviceManagementProviderService);
        Mockito.reset(this.userStoreManager);
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getOTPManagementService"))
                .toReturn(this.otpManagementService);
        Mockito.doThrow(new UserStoreException()).when(userStoreManager)
                .getUserClaimValue(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.doThrow(new UserStoreException()).when(userStoreManager)
                .listUsers(Mockito.anyString(), Mockito.anyInt());
        Response response = userManagementService.getUsers(TEST_USERNAME, "00", 0, 10, null);
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "Response returned successful for a users retrieval request.");
        response = userManagementService.getUserCount();
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "Response returned successful for a user count retrieval request.");
        response = userManagementService.getUserNames(TEST_USERNAME, null, "00", 0, 10);
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "Response returned successful for a user count retrieval request.");
        response = userManagementService.inviteToEnrollDevice(enrollmentInvitation);
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "Invite existing users to enroll device succeeded under erroneous conditions");
        response = userManagementService.inviteExistingUsersToEnrollDevice(deviceEnrollmentInvitation);
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "Invite existing users to enroll device succeeded under erroneous conditions");
    }

    /**
     * To get the user info of a user
     *
     * @return UserInfo of the User.
     */
    private UserInfo getUserInfo() {
        UserInfo userInfo = new UserInfo();
        userInfo.setUsername(TEST_USERNAME);
        userInfo.setFirstname(TEST_USERNAME);
        userInfo.setLastname(TEST_USERNAME);
        userInfo.setEmailAddress("test@test.com");
        return userInfo;
    }

}

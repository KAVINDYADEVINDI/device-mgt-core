/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.mgt.extensions.pull.notification.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import io.entgra.application.mgt.common.services.ApplicationManager;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import io.entgra.device.mgt.core.policy.mgt.core.PolicyManagerService;

/**
 * @scr.component name="org.wso2.carbon.device.mgt.extensions.pull.notification.internal.PullNotificationServiceComponent" immediate="true"
 * @scr.reference name="carbon.device.mgt.provider"
 * interface="org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setDeviceManagementProviderService"
 * unbind="unsetDeviceManagementProviderService"
 * @scr.reference name="io.entgra.device.mgt.core.policy.mgt.core"
 * interface="io.entgra.device.mgt.core.policy.mgt.core.PolicyManagerService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setPolicyManagerService"
 * unbind="unsetPolicyManagerService"
 * @scr.reference name="org.wso2.carbon.application.mgt.service"
 * interface="io.entgra.application.mgt.common.services.ApplicationManager"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setApplicationManagerService"
 * unbind="unsetApplicationManagerService"
 */
public class PullNotificationServiceComponent {

    private static final Log log = LogFactory.getLog(PullNotificationServiceComponent.class);

    @SuppressWarnings("unused")
    protected void activate(ComponentContext componentContext) {
        try {
            //Do nothing
            if (log.isDebugEnabled()) {
                log.debug("pull notification provider implementation bundle has been successfully " +
                        "initialized");
            }
        } catch (Throwable e) {
            log.error("Error occurred while initializing pull notification provider " +
                    "implementation bundle", e);
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        //Do nothing
    }

    protected void setDeviceManagementProviderService(DeviceManagementProviderService deviceManagementProviderService) {
        PullNotificationDataHolder.getInstance().setDeviceManagementProviderService(deviceManagementProviderService);
    }

    protected void unsetDeviceManagementProviderService(DeviceManagementProviderService deviceManagementProviderService) {
        PullNotificationDataHolder.getInstance().setDeviceManagementProviderService(null);
    }

    protected void setPolicyManagerService(PolicyManagerService policyManagerService) {
        PullNotificationDataHolder.getInstance().setPolicyManagerService(policyManagerService);
    }

    protected void unsetPolicyManagerService(PolicyManagerService policyManagerService) {
        PullNotificationDataHolder.getInstance().setPolicyManagerService(null);
    }

    protected void setApplicationManagerService(ApplicationManager applicationManagerService){
        PullNotificationDataHolder.getInstance().setApplicationManager(applicationManagerService);
    }

    protected void unsetApplicationManagerService(ApplicationManager applicationManagerService){
        PullNotificationDataHolder.getInstance().setApplicationManager(null);
    }

}

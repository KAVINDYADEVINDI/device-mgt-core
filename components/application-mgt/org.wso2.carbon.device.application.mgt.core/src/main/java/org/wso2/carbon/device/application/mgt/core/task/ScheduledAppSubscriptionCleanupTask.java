/*
 * Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.application.mgt.core.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.common.exception.SubscriptionManagementException;
import org.wso2.carbon.device.application.mgt.common.services.SubscriptionManager;
import org.wso2.carbon.device.application.mgt.core.impl.SubscriptionManagerImpl;
import org.wso2.carbon.ntask.core.Task;

import java.util.Map;

public class ScheduledAppSubscriptionCleanupTask implements Task {
    private static Log log = LogFactory.getLog(ScheduledAppSubscriptionCleanupTask.class);
    private SubscriptionManager subscriptionManager;

    @Override
    public void setProperties(Map<String, String> properties) {
        //no properties required
    }

    @Override
    public void init() {
        if (this.subscriptionManager == null) {
            this.subscriptionManager = new SubscriptionManagerImpl();
        }
    }

    @Override
    public void execute() {
        try {
            subscriptionManager.cleanScheduledSubscriptions();
        } catch (SubscriptionManagementException e) {
            log.error("Error occurred while cleaning up tasks.");
        }
    }
}

/*
 *  Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.entgra.device.mgt.core.application.mgt.core.util.subscription.mgt;

import io.entgra.device.mgt.core.application.mgt.common.SubscriptionInfo;
import io.entgra.device.mgt.core.application.mgt.core.util.subscription.mgt.impl.GroupBasedSubscriptionManagementHelperServiceImpl;
import io.entgra.device.mgt.core.application.mgt.core.util.subscription.mgt.impl.RoleBasedSubscriptionManagementHelperServiceImpl;
import io.entgra.device.mgt.core.application.mgt.core.util.subscription.mgt.service.SubscriptionManagementHelperService;

import java.util.Objects;

public class SubscriptionManagementServiceProvider {
    private SubscriptionManagementServiceProvider() {}

    private static class SubscriptionManagementProviderServiceHolder {
        private static final SubscriptionManagementServiceProvider INSTANCE = new SubscriptionManagementServiceProvider();
    }

    public static SubscriptionManagementServiceProvider getInstance() {
        return SubscriptionManagementProviderServiceHolder.INSTANCE;
    }

    public SubscriptionManagementHelperService getSubscriptionManagementHelperService(SubscriptionInfo subscriptionInfo) {
        return getSubscriptionManagementHelperService(subscriptionInfo.getSubscriptionType());
    }

    private SubscriptionManagementHelperService getSubscriptionManagementHelperService(String subscriptionType) {
        if (Objects.equals(subscriptionType, "role")) return RoleBasedSubscriptionManagementHelperServiceImpl.getInstance();
        if (Objects.equals(subscriptionType, "group")) return GroupBasedSubscriptionManagementHelperServiceImpl.getInstance();
        throw new UnsupportedOperationException("Subscription type: " + subscriptionType + " not supports");
    }
}

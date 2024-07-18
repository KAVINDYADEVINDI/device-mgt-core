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

package io.entgra.device.mgt.core.application.mgt.core.util.subscription.mgt.impl;

import io.entgra.device.mgt.core.application.mgt.common.DeviceSubscription;
import io.entgra.device.mgt.core.application.mgt.common.DeviceSubscriptionFilterCriteria;
import io.entgra.device.mgt.core.application.mgt.common.SubscriptionEntity;
import io.entgra.device.mgt.core.application.mgt.common.SubscriptionInfo;
import io.entgra.device.mgt.core.application.mgt.common.dto.ApplicationReleaseDTO;
import io.entgra.device.mgt.core.application.mgt.common.dto.DeviceSubscriptionDTO;
import io.entgra.device.mgt.core.application.mgt.common.exception.ApplicationManagementException;
import io.entgra.device.mgt.core.application.mgt.common.exception.DBConnectionException;
import io.entgra.device.mgt.core.application.mgt.core.exception.ApplicationManagementDAOException;
import io.entgra.device.mgt.core.application.mgt.core.exception.NotFoundException;
import io.entgra.device.mgt.core.application.mgt.core.util.ConnectionManagerUtil;
import io.entgra.device.mgt.core.application.mgt.core.util.HelperUtil;
import io.entgra.device.mgt.core.application.mgt.core.util.subscription.mgt.SubscriptionManagementHelperUtil;
import io.entgra.device.mgt.core.application.mgt.core.util.subscription.mgt.service.SubscriptionManagementHelperService;
import io.entgra.device.mgt.core.device.mgt.common.Device;
import io.entgra.device.mgt.core.device.mgt.common.PaginationRequest;
import io.entgra.device.mgt.core.device.mgt.common.PaginationResult;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class UserBasedSubscriptionManagementHelperServiceImpl implements SubscriptionManagementHelperService {
    private static final Log log = LogFactory.getLog(UserBasedSubscriptionManagementHelperServiceImpl.class);
    private UserBasedSubscriptionManagementHelperServiceImpl() {}
    private static class UserBasedSubscriptionManagementHelperServiceImplHolder {
        private static final UserBasedSubscriptionManagementHelperServiceImpl INSTANCE
                = new UserBasedSubscriptionManagementHelperServiceImpl();
    }
    public static UserBasedSubscriptionManagementHelperServiceImpl getInstance() {
        return UserBasedSubscriptionManagementHelperServiceImpl.UserBasedSubscriptionManagementHelperServiceImplHolder.INSTANCE;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<DeviceSubscription> getStatusBaseSubscriptions(SubscriptionInfo subscriptionInfo, int limit, int offset)
            throws ApplicationManagementException {
        final boolean isUnsubscribe = Objects.equals("unsubscribe", subscriptionInfo.getSubscriptionStatus());
        List<DeviceSubscriptionDTO> deviceSubscriptionDTOS;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            ConnectionManagerUtil.openDBConnection();
            List<Device> deviceListOwnByUser = new ArrayList<>();
            PaginationRequest paginationRequest = new PaginationRequest(offset, limit);
            paginationRequest.setOwner(subscriptionInfo.getIdentifier());
            paginationRequest.setStatusList(Arrays.asList("ACTIVE", "INACTIVE", "UNREACHABLE"));
            PaginationResult ownDeviceIds = HelperUtil.getDeviceManagementProviderService().
                    getAllDevicesIdList(paginationRequest);
            if (ownDeviceIds.getData() != null) {
                deviceListOwnByUser.addAll((List<Device>)ownDeviceIds.getData());
            }

            List<Integer> deviceIdsOwnByUser = deviceListOwnByUser.stream().map(Device::getId).collect(Collectors.toList());

            ApplicationReleaseDTO applicationReleaseDTO = applicationReleaseDAO.
                    getReleaseByUUID(subscriptionInfo.getApplicationUUID(), tenantId);
            if (applicationReleaseDTO == null) {
                String msg = "Couldn't find an application release for application release UUID: " +
                        subscriptionInfo.getApplicationUUID();
                log.error(msg);
                throw new NotFoundException(msg);
            }

            String deviceSubscriptionStatus = SubscriptionManagementHelperUtil.getDeviceSubscriptionStatus(subscriptionInfo);
            DeviceSubscriptionFilterCriteria deviceSubscriptionFilterCriteria = subscriptionInfo.getDeviceSubscriptionFilterCriteria();

            if (Objects.equals("NEW", deviceSubscriptionStatus)) {
                deviceSubscriptionDTOS = subscriptionDAO.getSubscriptionDetailsByDeviceIds(applicationReleaseDTO.getId(),
                        isUnsubscribe, tenantId, deviceIdsOwnByUser, null,
                        subscriptionInfo.getSubscriptionType(), deviceSubscriptionFilterCriteria.getTriggeredBy(),
                        null, -1, -1);

                List<Integer> deviceIdsOfSubscription = deviceSubscriptionDTOS.stream().
                        map(DeviceSubscriptionDTO::getDeviceId).collect(Collectors.toList());

                List<Integer> newDeviceIds = HelperUtil.getDeviceManagementProviderService().
                        getDevicesNotInGivenIdList(deviceIdsOfSubscription, new PaginationRequest(offset, limit));
                deviceSubscriptionDTOS = newDeviceIds.stream().map(DeviceSubscriptionDTO::new).collect(Collectors.toList());
            } else {
                deviceSubscriptionDTOS = subscriptionDAO.getSubscriptionDetailsByDeviceIds(applicationReleaseDTO.getId(),
                        isUnsubscribe, tenantId, deviceIdsOwnByUser, null,
                        subscriptionInfo.getSubscriptionType(), deviceSubscriptionFilterCriteria.getTriggeredBy(),
                        null, limit, offset);
            }


            return SubscriptionManagementHelperUtil.getDeviceSubscriptionData(deviceSubscriptionDTOS,
                    subscriptionInfo.getDeviceSubscriptionFilterCriteria());

        } catch (DeviceManagementException e) {
            String msg = "Error encountered while getting device details";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (ApplicationManagementDAOException | DBConnectionException e) {
            String msg = "Error encountered while connecting to the database";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public List<SubscriptionEntity> getSubscriptions(SubscriptionInfo subscriptionInfo, int limit, int offset)
            throws ApplicationManagementException {
        final boolean isUnsubscribe = Objects.equals("unsubscribe", subscriptionInfo.getSubscriptionStatus());
        final int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            ConnectionManagerUtil.openDBConnection();
            ApplicationReleaseDTO applicationReleaseDTO = applicationReleaseDAO.
                    getReleaseByUUID(subscriptionInfo.getApplicationUUID(), tenantId);
            if (applicationReleaseDTO == null) {
                String msg = "Couldn't find an application release for application release UUID: " +
                        subscriptionInfo.getApplicationUUID();
                log.error(msg);
                throw new NotFoundException(msg);
            }
            return subscriptionDAO.
                    getUserSubscriptionsByAppReleaseID(applicationReleaseDTO.getId(), isUnsubscribe, tenantId, offset, limit);
        } catch (DBConnectionException | ApplicationManagementDAOException e) {
            String msg = "Error encountered while connecting to the database";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public void getSubscriptionStatistics() throws ApplicationManagementException {
        // todo: analytics engine
    }
}

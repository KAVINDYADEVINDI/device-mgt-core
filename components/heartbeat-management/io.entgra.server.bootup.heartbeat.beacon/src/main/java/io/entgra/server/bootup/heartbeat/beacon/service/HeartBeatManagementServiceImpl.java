/*
 * Copyright (c) 2020, Entgra Pvt Ltd. (http://www.wso2.org) All Rights Reserved.
 *
 * Entgra Pvt Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.server.bootup.heartbeat.beacon.service;

import io.entgra.server.bootup.heartbeat.beacon.config.HeartBeatBeaconConfig;
import io.entgra.server.bootup.heartbeat.beacon.dao.HeartBeatBeaconDAOFactory;
import io.entgra.server.bootup.heartbeat.beacon.dao.HeartBeatDAO;
import io.entgra.server.bootup.heartbeat.beacon.dao.exception.HeartBeatDAOException;
import io.entgra.server.bootup.heartbeat.beacon.dto.HeartBeatEvent;
import io.entgra.server.bootup.heartbeat.beacon.dto.ServerContext;
import io.entgra.server.bootup.heartbeat.beacon.exception.HeartBeatManagementException;
import io.entgra.server.bootup.heartbeat.beacon.internal.HeartBeatBeaconDataHolder;
import org.wso2.carbon.device.mgt.common.ServerCtxInfo;
import org.wso2.carbon.device.mgt.common.exceptions.TransactionManagementException;

import java.sql.SQLException;
import java.util.Map;

public class HeartBeatManagementServiceImpl implements HeartBeatManagementService {

    private final HeartBeatDAO heartBeatDAO;

    public HeartBeatManagementServiceImpl(){
        this.heartBeatDAO = HeartBeatBeaconDAOFactory.getHeartBeatDAO();
    }


    @Override
    public ServerCtxInfo getServerCtxInfo() throws HeartBeatManagementException {
        int hashIndex = -1;
        ServerContext localServerCtx = null;
        ServerCtxInfo serverCtxInfo = null;
        if(HeartBeatBeaconConfig.getInstance().isEnabled()) {
            try {
                HeartBeatBeaconDAOFactory.openConnection();
                int timeOutIntervalInSeconds = HeartBeatBeaconConfig.getInstance().getServerTimeOutIntervalInSeconds();
                int timeSkew = HeartBeatBeaconConfig.getInstance().getTimeSkew();
                int cumilativeTimeOut = timeOutIntervalInSeconds + timeSkew;
                String localServerUUID = HeartBeatBeaconDataHolder.getInstance().getLocalServerUUID();
                Map<String, ServerContext> serverCtxMap = heartBeatDAO.getActiveServerDetails(cumilativeTimeOut);
                if (!serverCtxMap.isEmpty()) {
                    localServerCtx = serverCtxMap.get(localServerUUID);
                    if (localServerCtx != null) {
                        hashIndex = localServerCtx.getIndex();
                        serverCtxInfo = new ServerCtxInfo(serverCtxMap.size(), hashIndex);
                    }
                }
            } catch (SQLException e) {
                String msg = "Error occurred while opening a connection to the underlying data source";
                throw new HeartBeatManagementException(msg, e);
            } catch (HeartBeatDAOException e) {
                String msg = "Error occurred while retrieving active server count.";
                throw new HeartBeatManagementException(msg, e);
            } finally {
                HeartBeatBeaconDAOFactory.closeConnection();
            }
        } else {
            String msg = "Heart Beat Configuration Disabled. Server Context Information Not available.";
            throw new HeartBeatManagementException(msg);
        }
        return serverCtxInfo;
    }

    @Override
    public String updateServerContext(ServerContext ctx) throws HeartBeatManagementException {
        String uuid = null;
        if(HeartBeatBeaconConfig.getInstance().isEnabled()) {
            try {
                HeartBeatBeaconDAOFactory.beginTransaction();
                uuid = heartBeatDAO.retrieveExistingServerCtx(ctx);
                if (uuid == null) {
                    uuid = heartBeatDAO.recordServerCtx(ctx);
                    HeartBeatBeaconDAOFactory.commitTransaction();
                }
            } catch (HeartBeatDAOException e) {
                String msg = "Error Occured while retrieving server context.";
                throw new HeartBeatManagementException(msg, e);
            } catch (TransactionManagementException e) {
                HeartBeatBeaconDAOFactory.rollbackTransaction();
                String msg = "Error occurred while updating server context. Issue in opening a connection to the underlying data source";
                throw new HeartBeatManagementException(msg, e);
            } finally {
                HeartBeatBeaconDAOFactory.closeConnection();
            }
        } else {
            String msg = "Heart Beat Configuration Disabled. Updating Server Context Failed.";
            throw new HeartBeatManagementException(msg);
        }
        return uuid;
    }


    @Override
    public boolean recordHeartBeat(HeartBeatEvent event) throws HeartBeatManagementException {
        boolean operationSuccess = false;
        if (HeartBeatBeaconConfig.getInstance().isEnabled()) {
            try {
                HeartBeatBeaconDAOFactory.beginTransaction();
                if(heartBeatDAO.checkUUIDValidity(event.getServerUUID())){
                    operationSuccess = heartBeatDAO.recordHeatBeat(event);
                    HeartBeatBeaconDAOFactory.commitTransaction();
                } else {
                    String msg = "Server UUID Does not exist, heartbeat not recorded.";
                    throw new HeartBeatManagementException(msg);
                }
            } catch (HeartBeatDAOException e) {
                String msg = "Error occurred while recording heart beat.";
                throw new HeartBeatManagementException(msg, e);
            } catch (TransactionManagementException e) {
                HeartBeatBeaconDAOFactory.rollbackTransaction();
                String msg = "Error occurred performing heart beat record transaction. " +
                             "Transaction rolled back.";
                throw new HeartBeatManagementException(msg, e);
            } finally {
                HeartBeatBeaconDAOFactory.closeConnection();
            }
        } else {
            String msg = "Heart Beat Configuration Disabled. Recording Heart Beat Failed.";
            throw new HeartBeatManagementException(msg);
        }
        return operationSuccess;
    }

}

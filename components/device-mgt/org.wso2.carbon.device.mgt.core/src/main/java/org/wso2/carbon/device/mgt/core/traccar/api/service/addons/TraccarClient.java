/*
 * Copyright (C) 2018 - 2022 Entgra (Pvt) Ltd, Inc - All Rights Reserved.
 *
 * Unauthorised copying/redistribution of this file, via any medium is strictly prohibited.
 *
 * Licensed under the Entgra Commercial License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://entgra.io/licenses/entgra-commercial/1.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.core.traccar.api.service.addons;

import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.device.mgt.common.TrackerDeviceInfo;
import org.wso2.carbon.device.mgt.common.TrackerGroupInfo;
import org.wso2.carbon.device.mgt.common.exceptions.TransactionManagementException;
import org.wso2.carbon.device.mgt.common.TrackerAlreadyExistException;
import org.wso2.carbon.device.mgt.core.dao.TrackerManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.TrackerDAO;
import org.wso2.carbon.device.mgt.core.dao.TrackerManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.traccar.common.TraccarHandlerConstants;
import org.wso2.carbon.device.mgt.core.traccar.common.beans.TraccarDevice;
import org.wso2.carbon.device.mgt.core.traccar.common.beans.TraccarGroups;
import org.wso2.carbon.device.mgt.core.traccar.common.beans.TraccarPosition;
import org.wso2.carbon.device.mgt.core.traccar.common.config.TraccarConfigurationException;
import org.wso2.carbon.device.mgt.core.traccar.common.config.TraccarGateway;
import org.wso2.carbon.device.mgt.core.traccar.core.config.TraccarConfigurationManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TraccarClient implements org.wso2.carbon.device.mgt.core.traccar.api.service.TraccarClient {
    private static final Log log = LogFactory.getLog(TraccarClient.class);
    private static final int THREAD_POOL_SIZE = 50;
    private final OkHttpClient client;
    private final ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    final TraccarGateway traccarGateway = getTraccarGateway();
    final String endpoint = traccarGateway.getPropertyByName(TraccarHandlerConstants.TraccarConfig.ENDPOINT).getValue();
    final String authorization = traccarGateway.getPropertyByName(TraccarHandlerConstants.TraccarConfig.AUTHORIZATION).getValue();
    final String authorizationKey = traccarGateway.getPropertyByName(TraccarHandlerConstants.TraccarConfig.AUTHORIZATION_KEY).getValue();
    final String defaultPort = traccarGateway.getPropertyByName(TraccarHandlerConstants.TraccarConfig.DEFAULT_PORT).getValue();
    final String locationUpdatePort = traccarGateway.getPropertyByName(TraccarHandlerConstants.TraccarConfig.LOCATION_UPDATE_PORT).getValue();

    private final TrackerDAO trackerDAO;

    public TraccarClient() {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(45, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(50,30,TimeUnit.SECONDS))
                .build();
        this.trackerDAO = TrackerManagementDAOFactory.getTrackerDAO();
    }

    private class TrackerExecutor implements Runnable {
        final int deviceId;
        final int groupId;
        final int tenantId;
        final JSONObject payload;
        final String context;
        final String publisherUrl;
        private final String method;
        private final String type;

        private TrackerExecutor(int id, int tenantId, String publisherUrl, String context, JSONObject payload,
                                String method, String type) {
            this.deviceId = id;
            this.groupId = id;
            this.tenantId = tenantId;
            this.payload = payload;
            this.context = context;
            this.publisherUrl = publisherUrl;
            this.method = method;
            this.type = type;
        }

        public void run() {
            RequestBody requestBody;
            Request.Builder builder = new Request.Builder();
            Request request;
            Response response;

            if(method==TraccarHandlerConstants.Methods.POST){
                requestBody = RequestBody.create(payload.toString(), MediaType.parse("application/json; charset=utf-8"));
                builder = builder.post(requestBody);
            }else if(method==TraccarHandlerConstants.Methods.PUT){
                requestBody = RequestBody.create(payload.toString(), MediaType.parse("application/json; charset=utf-8"));
                builder = builder.put(requestBody);
            }else if(method==TraccarHandlerConstants.Methods.DELETE){
                builder = builder.delete();
            }

            request = builder.url(publisherUrl + context).addHeader(authorization, authorizationKey).build();

            try {
                response = client.newCall(request).execute();
                if(method==TraccarHandlerConstants.Methods.POST){
                    String result = response.body().string();
                    JSONObject obj = new JSONObject(result);
                    if (obj.has("id")){
                        int traccarId = obj.getInt("id");
                        try {
                            TrackerManagementDAOFactory.beginTransaction();
                            if(type==TraccarHandlerConstants.Types.DEVICE){
                                trackerDAO.addTrackerDevice(traccarId, deviceId, tenantId);
                                TrackerDeviceInfo res = trackerDAO.getTrackerDevice(deviceId, tenantId);
                                if(res.getStatus()==0){
                                    trackerDAO.updateTrackerDeviceIdANDStatus(res.getTraccarDeviceId(), deviceId, tenantId, 1);
                                }
                            }else if(type==TraccarHandlerConstants.Types.GROUP){
                                trackerDAO.addTrackerGroup(traccarId, groupId, tenantId);
                                TrackerGroupInfo res = trackerDAO.getTrackerGroup(groupId, tenantId);
                                if(res.getStatus()==0){
                                    trackerDAO.updateTrackerGroupIdANDStatus(res.getTraccarGroupId(), groupId, tenantId, 1);
                                }
                            }
                            TrackerManagementDAOFactory.commitTransaction();
                        } catch (JSONException e) {
                            TrackerManagementDAOFactory.rollbackTransaction();
                            String msg = "Error occurred on JSON object .";
                            log.error(msg, e);
                        } catch (TransactionManagementException e) {
                            TrackerManagementDAOFactory.rollbackTransaction();
                            String msg = "Error occurred establishing the DB connection .";
                            log.error(msg, e);
                        } catch (TrackerManagementDAOException e) {
                            TrackerManagementDAOFactory.rollbackTransaction();
                            String msg = null;
                            if(type==TraccarHandlerConstants.Types.DEVICE){
                                msg = "Error occurred while mapping with deviceId .";
                            }else if(type==TraccarHandlerConstants.Types.GROUP){
                                msg = "Error occurred while mapping with groupId .";
                            }
                            log.error(msg, e);
                        } finally {
                            TrackerManagementDAOFactory.closeConnection();
                        }
                    }
                    response.close();
                }
                if (log.isDebugEnabled()) {
                    log.debug("Successfully the request is proceed and communicated with Traccar");
                }
            } catch (IOException e) {
                log.error("Couldnt connect to traccar.", e);
            }
        }
    }

    /**
     * Add Traccar Device operation.
     * @param deviceInfo  with DeviceName UniqueId, Status, Disabled LastUpdate, PositionId, GroupId
     *                    Model, Contact, Category, fenceIds
     * @throws TraccarConfigurationException Failed while add Traccar Device the operation
     */
    public void addDevice(TraccarDevice deviceInfo, int tenantId) throws TraccarConfigurationException, TrackerAlreadyExistException {
        TrackerDeviceInfo res = null;
        try {
            TrackerManagementDAOFactory.openConnection();
            res = trackerDAO.getTrackerDevice(deviceInfo.getId(), tenantId);
            if(res!=null){
                String msg = "The device already exit";
                log.error(msg);
                throw new TrackerAlreadyExistException(msg);
            }
        } catch (TrackerManagementDAOException e) {
            String msg = "Error occurred while mapping with deviceId .";
            log.error(msg, e);
            throw new TraccarConfigurationException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred establishing the DB connection .";
            log.error(msg, e);
            throw new TraccarConfigurationException(msg, e);
        } finally {
            TrackerManagementDAOFactory.closeConnection();
        }

        JSONObject payload = payload(deviceInfo);
        String context = defaultPort+"/api/devices";
        Runnable trackerExecutor = new TrackerExecutor(deviceInfo.getId(), tenantId, endpoint, context, payload,
                TraccarHandlerConstants.Methods.POST, TraccarHandlerConstants.Types.DEVICE);
        executor.execute(trackerExecutor);
    }

    /**
     * update Traccar Device operation.
     * @param deviceInfo  with DeviceName UniqueId, Status, Disabled LastUpdate, PositionId, GroupId
     *                    Model, Contact, Category, fenceIds
     * @throws TraccarConfigurationException Failed while add Traccar Device the operation
     */
    public void updateDevice(TraccarDevice deviceInfo, int tenantId) throws TraccarConfigurationException, TrackerAlreadyExistException {
        TrackerDeviceInfo res = null;
        try {
            TrackerManagementDAOFactory.openConnection();
            res = trackerDAO.getTrackerDevice(deviceInfo.getId(), tenantId);
        } catch (TrackerManagementDAOException e) {
            String msg = "Error occurred while mapping with deviceId .";
            log.error(msg, e);
            throw new TraccarConfigurationException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred establishing the DB connection .";
            log.error(msg, e);
            throw new TraccarConfigurationException(msg, e);
        } finally {
            TrackerManagementDAOFactory.closeConnection();
        }

        if ((res==null) || (res.getTraccarDeviceId()==0)){
            try {
                TraccarDevice device = deviceInfo;
                String lastUpdatedTime = String.valueOf((new Date().getTime()));
                device.setLastUpdate(lastUpdatedTime);
                addDevice(deviceInfo, tenantId);
            } catch (TraccarConfigurationException e) {
                String msg = "Error occurred while mapping with groupId";
                log.error(msg, e);
                throw new TraccarConfigurationException(msg, e);
            } catch (TrackerAlreadyExistException e) {
                String msg = "The group already exist";
                log.error(msg, e);
                throw new TrackerAlreadyExistException(msg, e);
            }
        }else if (res!=null && (res.getTraccarDeviceId()!=0 && res.getStatus()==0)){
            //update the traccarGroupId and status
            try {
                TrackerManagementDAOFactory.beginTransaction();
                trackerDAO.updateTrackerDeviceIdANDStatus(res.getTraccarDeviceId(), deviceInfo.getId(), tenantId, 1);
                TrackerManagementDAOFactory.commitTransaction();
            } catch (TransactionManagementException e) {
                String msg = "Error occurred establishing the DB connection .";
                log.error(msg, e);
            } catch (TrackerManagementDAOException e) {
                String msg="Could not add the traccar group";
                log.error(msg, e);
            } finally{
                TrackerManagementDAOFactory.closeConnection();
            }
        }else{
            JSONObject payload = payload(deviceInfo);
            String context = defaultPort+"/api/devices";
            Runnable trackerExecutor = new TrackerExecutor(deviceInfo.getId(), tenantId, endpoint, context, payload,
                    TraccarHandlerConstants.Methods.PUT, TraccarHandlerConstants.Types.DEVICE);
            executor.execute(trackerExecutor);
        }
    }

    private JSONObject payload(TraccarDevice deviceInfo){
        JSONObject payload = new JSONObject();
        payload.put("name", deviceInfo.getDeviceName());
        payload.put("uniqueId", deviceInfo.getUniqueId());
        payload.put("status", deviceInfo.getStatus());
        payload.put("disabled", deviceInfo.getDisabled());
        payload.put("lastUpdate", deviceInfo.getLastUpdate());
        payload.put("positionId", deviceInfo.getPositionId());
        payload.put("groupId", deviceInfo.getGroupId());
        payload.put("phone", deviceInfo.getPhone());
        payload.put("model", deviceInfo.getModel());
        payload.put("contact", deviceInfo.getContact());
        payload.put("category", deviceInfo.getCategory());
        List<String> geoFenceIds = new ArrayList<>();
        payload.put("geofenceIds", geoFenceIds);
        payload.put("attributes", new JSONObject());
        return payload;
    }

    /**
     * Add Device GPS Location operation.
     * @param deviceInfo  with DeviceIdentifier, Timestamp, Lat, Lon, Bearing, Speed, ignition
     */
    public void updateLocation(TraccarDevice device, TraccarPosition deviceInfo, int tenantId) throws TraccarConfigurationException, TrackerAlreadyExistException {
        TrackerDeviceInfo res = null;
        try {
            TrackerManagementDAOFactory.openConnection();
            res = trackerDAO.getTrackerDevice(device.getId(), tenantId);
        } catch (SQLException e) {
            String msg = "Error occurred establishing the DB connection .";
            log.error(msg, e);
        } catch (TrackerManagementDAOException e) {
            String msg="Could not update the traccar group";
            log.error(msg, e);
        } finally{
            TrackerManagementDAOFactory.closeConnection();
        }

        if (res == null){
            try {
                addDevice(device, tenantId);
            } catch (TraccarConfigurationException e) {
                String msg = "Error occurred while mapping with groupId";
                log.error(msg, e);
                throw new TraccarConfigurationException(msg, e);
            } catch (TrackerAlreadyExistException e) {
                String msg = "The device already exist";
                log.error(msg, e);
                throw new TrackerAlreadyExistException(msg, e);
            }
        }else{
            String context = locationUpdatePort+"/?id="+deviceInfo.getDeviceIdentifier()+"&timestamp="+deviceInfo.getTimestamp()+
                    "&lat="+deviceInfo.getLat()+"&lon="+deviceInfo.getLon()+"&bearing="+deviceInfo.getBearing()+
                    "&speed="+deviceInfo.getSpeed()+"&ignition=true";
            Runnable trackerExecutor = new TrackerExecutor(0, 0, endpoint, context, null,
                    TraccarHandlerConstants.Methods.GET, TraccarHandlerConstants.Types.DEVICE);
            executor.execute(trackerExecutor);
            log.info("Device GPS location added on traccar");
        }

    }

    /**
     * Dis-enroll a Device operation.
     * @param deviceId  identified via deviceIdentifier
     * @throws TraccarConfigurationException Failed while dis-enroll a Traccar Device operation
     */
    public void disEndrollDevice(int deviceId, int tenantId) throws TraccarConfigurationException {
        TrackerDeviceInfo  res = null;
        JSONObject obj = null;
        try {
            TrackerManagementDAOFactory.beginTransaction();
            res = trackerDAO.getTrackerDevice(deviceId, tenantId);
            if(res!=null){
                obj = new JSONObject(res);
                if(obj!=null){
                    trackerDAO.removeTrackerDevice(deviceId, tenantId);
                    TrackerManagementDAOFactory.commitTransaction();
                }
            }
        } catch (TransactionManagementException e) {
            TrackerManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred establishing the DB connection";
            log.error(msg, e);
        } catch (TrackerManagementDAOException e) {
            TrackerManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while mapping with deviceId";
            log.error(msg, e);
        } finally {
            TrackerManagementDAOFactory.closeConnection();
        }

        String context = defaultPort+"/api/devices/"+obj.getInt("traccarDeviceId");
        Runnable trackerExecutor = new TrackerExecutor(obj.getInt("traccarDeviceId"), tenantId, endpoint, context, null,
                TraccarHandlerConstants.Methods.DELETE, TraccarHandlerConstants.Types.DEVICE);
        executor.execute(trackerExecutor);
    }

    /**
     * Add Traccar Device operation.
     * @param groupInfo  with groupName
     * @throws TraccarConfigurationException Failed while add Traccar Device the operation
     */
    public void addGroup(TraccarGroups groupInfo, int groupId, int tenantId) throws TraccarConfigurationException, TrackerAlreadyExistException {
        TrackerGroupInfo res = null;
        try {
            TrackerManagementDAOFactory.openConnection();
            res = trackerDAO.getTrackerGroup(groupId, tenantId);
            if (res!=null){
                String msg = "The group already exit";
                log.error(msg);
                throw new TrackerAlreadyExistException(msg);
            }
        } catch (TrackerManagementDAOException e) {
            String msg = "Error occurred while mapping with deviceId .";
            log.error(msg, e);
            throw new TraccarConfigurationException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred establishing the DB connection .";
            log.error(msg, e);
            throw new TraccarConfigurationException(msg, e);
        } finally {
            TrackerManagementDAOFactory.closeConnection();
        }

        if (res==null){
            JSONObject payload = new JSONObject();
            payload.put("name", groupInfo.getName());
            payload.put("attributes", new JSONObject());

            String context = defaultPort+"/api/groups";
            Runnable trackerExecutor = new TrackerExecutor(groupId, tenantId, endpoint, context, payload,
                    TraccarHandlerConstants.Methods.POST, TraccarHandlerConstants.Types.GROUP);
            executor.execute(trackerExecutor);
        }
    }

    /**
     * update Traccar Group operation.
     * @param groupInfo  with groupName
     * @throws TraccarConfigurationException Failed while add Traccar Device the operation
     */
    public void updateGroup(TraccarGroups groupInfo, int groupId, int tenantId) throws TraccarConfigurationException, TrackerAlreadyExistException {
        TrackerGroupInfo res = null;
        try {
            TrackerManagementDAOFactory.openConnection();
            res = trackerDAO.getTrackerGroup(groupId, tenantId);
        } catch (SQLException e) {
            String msg = "Error occurred establishing the DB connection .";
            log.error(msg, e);
        } catch (TrackerManagementDAOException e) {
            String msg="Could not find traccar group details";
            log.error(msg, e);
        } finally{
            TrackerManagementDAOFactory.closeConnection();
        }

        if ((res==null) || (res.getTraccarGroupId()==0)){
            //add a new traccar group
            try {
                addGroup(groupInfo, groupId, tenantId);
            } catch (TraccarConfigurationException e) {
                String msg = "Error occurred while mapping with groupId";
                log.error(msg, e);
                throw new TraccarConfigurationException(msg, e);
            } catch (TrackerAlreadyExistException e) {
                String msg = "The group already exist";
                log.error(msg, e);
                throw new TrackerAlreadyExistException(msg, e);
            }
        }else if (res!=null && (res.getTraccarGroupId()!=0 && res.getStatus()==0)){
            //update the traccargroupId and status
            try {
                TrackerManagementDAOFactory.beginTransaction();
                trackerDAO.updateTrackerGroupIdANDStatus(res.getTraccarGroupId(), groupId, tenantId, 1);
                TrackerManagementDAOFactory.commitTransaction();
            } catch (TransactionManagementException e) {
                String msg = "Error occurred establishing the DB connection .";
                log.error(msg, e);
            } catch (TrackerManagementDAOException e) {
                String msg="Could not add the traccar group";
                log.error(msg, e);
            } finally{
                TrackerManagementDAOFactory.closeConnection();
            }
        }else{
            JSONObject obj = new JSONObject(res);
            JSONObject payload = new JSONObject();
            payload.put("id", obj.getInt("traccarGroupId"));
            payload.put("name", groupInfo.getName());
            payload.put("attributes", new JSONObject());

            String context = defaultPort+"/api/groups/"+obj.getInt("traccarGroupId");
            Runnable trackerExecutor = new TrackerExecutor(groupId, tenantId, endpoint, context, payload,
                    TraccarHandlerConstants.Methods.PUT, TraccarHandlerConstants.Types.GROUP);
            executor.execute(trackerExecutor);
        }
    }

    /**
     * Add Traccar Device operation.
     * @param groupId
     * @throws TraccarConfigurationException Failed while add Traccar Device the operation
     */
    public void deleteGroup(int groupId, int tenantId) throws TraccarConfigurationException {
        TrackerGroupInfo res = null;
        JSONObject obj = null;
        try {
            TrackerManagementDAOFactory.beginTransaction();
            res = trackerDAO.getTrackerGroup(groupId, tenantId);
            if(res!=null){
                obj = new JSONObject(res);
                if(obj!=null){
                    trackerDAO.removeTrackerGroup(obj.getInt("id"));
                    TrackerManagementDAOFactory.commitTransaction();
                }
            }
        } catch (TransactionManagementException e) {
            TrackerManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred establishing the DB connection";
            log.error(msg, e);
        } catch (TrackerManagementDAOException e) {
            TrackerManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while mapping with groupId";
            log.error(msg, e);
        } finally {
            TrackerManagementDAOFactory.closeConnection();
        }

        if(obj!=null){
            String context = defaultPort+"/api/groups/"+obj.getInt("traccarGroupId");
            Runnable trackerExecutor = new TrackerExecutor(obj.getInt("traccarGroupId"), tenantId, endpoint, context,
                    null, TraccarHandlerConstants.Methods.DELETE, TraccarHandlerConstants.Types.GROUP);
            executor.execute(trackerExecutor);
        }
    }

    private TraccarGateway getTraccarGateway(){
        return TraccarConfigurationManager.getInstance().getTraccarConfig().getTraccarGateway(
                TraccarHandlerConstants.TraccarConfig.GATEWAY_NAME);
    }
}

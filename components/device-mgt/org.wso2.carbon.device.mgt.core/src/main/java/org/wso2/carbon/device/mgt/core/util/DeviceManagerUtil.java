/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.w3c.dom.Document;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.caching.impl.CacheImpl;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.analytics.data.publisher.service.EventsPublisherService;
import org.wso2.carbon.device.mgt.common.AppRegistrationCredentials;
import org.wso2.carbon.device.mgt.common.ApplicationRegistration;
import org.wso2.carbon.device.mgt.common.ApplicationRegistrationException;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.GroupPaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.exceptions.TransactionManagementException;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.common.notification.mgt.NotificationManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.common.type.mgt.DeviceTypeMetaDefinition;
import org.wso2.carbon.device.mgt.core.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.core.cache.DeviceCacheKey;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.DeviceManagementConfig;
import org.wso2.carbon.device.mgt.core.config.datasource.DataSourceConfig;
import org.wso2.carbon.device.mgt.core.config.datasource.JNDILookupDefinition;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.DeviceTypeDAO;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.operation.mgt.util.DeviceIDHolder;
import org.wso2.carbon.identity.jwt.client.extension.JWTClient;
import org.wso2.carbon.identity.jwt.client.extension.dto.AccessTokenInfo;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientException;
import org.wso2.carbon.identity.jwt.client.extension.service.JWTClientManagerService;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.NetworkUtils;

import javax.cache.Cache;
import javax.cache.CacheConfiguration;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.sql.DataSource;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public final class DeviceManagerUtil {

    private static final Log log = LogFactory.getLog(DeviceManagerUtil.class);

    private  static boolean isDeviceCacheInitialized = false;

    public static Document convertToDocument(File file) throws DeviceManagementException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            return docBuilder.parse(file);
        } catch (Exception e) {
            throw new DeviceManagementException("Error occurred while parsing file, while converting " +
                    "to a org.w3c.dom.Document", e);
        }
    }

    /**
     * Resolve data source from the data source definition.
     *
     * @param config data source configuration
     * @return data source resolved from the data source definition
     */
    public static DataSource resolveDataSource(DataSourceConfig config) {
        DataSource dataSource = null;
        if (config == null) {
            throw new RuntimeException("Device Management Repository data source configuration is null and thus, " +
                    "is not initialized");
        }
        JNDILookupDefinition jndiConfig = config.getJndiLookupDefinition();
        if (jndiConfig != null) {
            if (log.isDebugEnabled()) {
                log.debug("Initializing Device Management Repository data source using the JNDI Lookup Definition");
            }
            List<JNDILookupDefinition.JNDIProperty> jndiPropertyList =
                    jndiConfig.getJndiProperties();
            if (jndiPropertyList != null) {
                Hashtable<Object, Object> jndiProperties = new Hashtable<Object, Object>();
                for (JNDILookupDefinition.JNDIProperty prop : jndiPropertyList) {
                    jndiProperties.put(prop.getName(), prop.getValue());
                }
                dataSource = DeviceManagementDAOUtil.lookupDataSource(jndiConfig.getJndiName(), jndiProperties);
            } else {
                dataSource = DeviceManagementDAOUtil.lookupDataSource(jndiConfig.getJndiName(), null);
            }
        }
        return dataSource;
    }

    /**
     * Adds a new device type to the database if it does not exists.
     *
     * @param typeName device type
     * @param tenantId provider tenant Id
     * @param isSharedWithAllTenants is this device type shared with all tenants.
     * @return status of the operation
     */
    public static boolean registerDeviceType(String typeName, int tenantId, boolean isSharedWithAllTenants
            , DeviceTypeMetaDefinition deviceTypeDefinition)
            throws DeviceManagementException {
        boolean status;
        try {
            DeviceManagementDAOFactory.beginTransaction();
            DeviceTypeDAO deviceTypeDAO = DeviceManagementDAOFactory.getDeviceTypeDAO();
            DeviceType deviceType = deviceTypeDAO.getDeviceType(typeName, tenantId);
            if (deviceType == null) {
                deviceType = new DeviceType();
                deviceType.setName(typeName);
                deviceType.setDeviceTypeMetaDefinition(deviceTypeDefinition);
                deviceTypeDAO.addDeviceType(deviceType, tenantId, isSharedWithAllTenants);
            } else {
                if (deviceTypeDefinition != null) {
                    deviceType.setDeviceTypeMetaDefinition(deviceTypeDefinition);
                    deviceTypeDAO.updateDeviceType(deviceType, tenantId);
                }
            }
            DeviceManagementDAOFactory.commitTransaction();
            status = true;
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new DeviceManagementException("Error occurred while registering the device type '"
                                                        + typeName + "'", e);
        } catch (TransactionManagementException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new DeviceManagementException("SQL occurred while registering the device type '"
                                                        + typeName + "'", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return status;
    }

    /**
     * Get the DeviceType information from Database.
     *
     * @param typeName device type
     * @param tenantId provider tenant Id
     * @return DeviceType which contains info about the device-type.
     */
    public static DeviceType getDeviceType(String typeName, int tenantId) throws DeviceManagementException {
        DeviceType deviceType = null;
        try {
            DeviceManagementDAOFactory.openConnection();
            DeviceTypeDAO deviceTypeDAO = DeviceManagementDAOFactory.getDeviceTypeDAO();
            deviceType = deviceTypeDAO.getDeviceType(typeName, tenantId);
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while fetching the device type '"
                    + typeName + "'", e);
        } catch (SQLException e) {
            throw new DeviceManagementException("SQL Error occurred while fetching the device type '"
                    + typeName + "'", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return deviceType;
    }

    /**
     * Un-registers an existing device type from the device management metadata repository.
     *
     * @param typeName device type
     * @return status of the operation
     */
    public static boolean unregisterDeviceType(String typeName, int tenantId) throws DeviceManagementException {
        try {
            DeviceManagementDAOFactory.beginTransaction();
            DeviceTypeDAO deviceTypeDAO = DeviceManagementDAOFactory.getDeviceTypeDAO();
            DeviceType deviceType = deviceTypeDAO.getDeviceType(typeName, tenantId);
            if (deviceType != null) {
                deviceTypeDAO.removeDeviceType(typeName, tenantId);
            }
            DeviceManagementDAOFactory.commitTransaction();
            return true;
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new DeviceManagementException("Error occurred while registering the device type '" +
                                                        typeName + "'", e);
        } catch (TransactionManagementException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new DeviceManagementException("SQL occurred while registering the device type '" +
                                                        typeName + "'", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    public static Map<String, String> convertDevicePropertiesToMap(List<Device.Property> properties) {
        Map<String, String> propertiesMap = new HashMap<String, String>();
        for (Device.Property prop : properties) {
            propertiesMap.put(prop.getName(), prop.getValue());
        }
        return propertiesMap;
    }

    public static List<DeviceIdentifier> convertDevices(List<Device> devices) {

        List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
        for (Device device : devices) {
            DeviceIdentifier identifier = new DeviceIdentifier();
            identifier.setId(device.getDeviceIdentifier());
            identifier.setType(device.getType());
            deviceIdentifiers.add(identifier);
        }
        return deviceIdentifiers;
    }

    public static List<DeviceIdentifier> getValidDeviceIdentifiers(List<Device> devices) {
        List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
        for (Device device : devices) {
            if (device.getEnrolmentInfo() != null) {
                switch (device.getEnrolmentInfo().getStatus()) {
                    case BLOCKED:
                    case REMOVED:
                        break;
                    case SUSPENDED:
                        break;
                    default:
                        DeviceIdentifier identifier = new DeviceIdentifier();
                        identifier.setId(device.getDeviceIdentifier());
                        identifier.setType(device.getType());
                        deviceIdentifiers.add(identifier);
                }
            }
        }
        return deviceIdentifiers;
    }


    public static String getServerBaseHttpsUrl() {
        String hostName = "localhost";
        try {
            hostName = NetworkUtils.getMgtHostName();
        } catch (Exception ignored) {
        }
        String mgtConsoleTransport = CarbonUtils.getManagementTransport();
        ConfigurationContextService configContextService =
                DeviceManagementDataHolder.getInstance().getConfigurationContextService();
        int port = CarbonUtils.getTransportPort(configContextService, mgtConsoleTransport);
        int httpsProxyPort =
                CarbonUtils.getTransportProxyPort(configContextService.getServerConfigContext(),
                        mgtConsoleTransport);
        if (httpsProxyPort > 0) {
            port = httpsProxyPort;
        }
        return "https://" + hostName + ":" + port;
    }

    public static String getServerBaseHttpUrl() {
        String hostName = "localhost";
        try {
            hostName = NetworkUtils.getMgtHostName();
        } catch (Exception ignored) {
        }
        ConfigurationContextService configContextService =
                DeviceManagementDataHolder.getInstance().getConfigurationContextService();
        int port = CarbonUtils.getTransportPort(configContextService, "http");
        int httpProxyPort =
                CarbonUtils.getTransportProxyPort(configContextService.getServerConfigContext(),
                        "http");
        if (httpProxyPort > 0) {
            port = httpProxyPort;
        }
        return "http://" + hostName + ":" + port;
    }

    /**
     * returns the tenant Id of the specific tenant Domain
     *
     * @param tenantDomain
     * @return
     * @throws DeviceManagementException
     */
    public static int getTenantId(String tenantDomain) throws DeviceManagementException {
        try {
            if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                return MultitenantConstants.SUPER_TENANT_ID;
            }
            TenantManager tenantManager = DeviceManagementDataHolder.getInstance().getTenantManager();
            int tenantId = tenantManager.getTenantId(tenantDomain);
            if (tenantId == -1) {
                throw new DeviceManagementException("invalid tenant Domain :" + tenantDomain);
            }
            return tenantId;
        } catch (UserStoreException e) {
            throw new DeviceManagementException("invalid tenant Domain :" + tenantDomain);
        }
    }

    public static int validateActivityListPageSize(int limit) throws OperationManagementException {
        if (limit == 0) {
            DeviceManagementConfig deviceManagementConfig = DeviceConfigurationManager.getInstance().
                    getDeviceManagementConfig();
            if (deviceManagementConfig != null) {
                return deviceManagementConfig.getPaginationConfiguration().getActivityListPageSize();
            } else {
                throw new OperationManagementException("Device-Mgt configuration has not initialized. Please check the " +
                                                    "cdm-config.xml file.");
            }
        }
        return limit;
    }

    public static PaginationRequest validateOperationListPageSize(PaginationRequest paginationRequest) throws
                                                                                          OperationManagementException {
        if (paginationRequest.getRowCount() == 0) {
            DeviceManagementConfig deviceManagementConfig = DeviceConfigurationManager.getInstance().
                    getDeviceManagementConfig();
            if (deviceManagementConfig != null) {
                paginationRequest.setRowCount(deviceManagementConfig.getPaginationConfiguration().
                        getOperationListPageSize());
            } else {
                throw new OperationManagementException("Device-Mgt configuration has not initialized. Please check the " +
                                                    "cdm-config.xml file.");
            }
        }
        return paginationRequest;
    }

    public static PaginationRequest validateNotificationListPageSize(PaginationRequest paginationRequest) throws
                                                                                       NotificationManagementException {
        if (paginationRequest.getRowCount() == 0) {
            DeviceManagementConfig deviceManagementConfig = DeviceConfigurationManager.getInstance().
                    getDeviceManagementConfig();
            if (deviceManagementConfig != null) {
                paginationRequest.setRowCount(deviceManagementConfig.getPaginationConfiguration().
                        getNotificationListPageSize());
            } else {
                throw new NotificationManagementException("Device-Mgt configuration has not initialized. Please check the " +
                          "cdm-config.xml file.");
            }
        }
        return paginationRequest;
    }

    public static PaginationRequest validateDeviceListPageSize(PaginationRequest paginationRequest) throws
                                                                                             DeviceManagementException {
        if (paginationRequest.getRowCount() == 0) {
            DeviceManagementConfig deviceManagementConfig = DeviceConfigurationManager.getInstance().
                    getDeviceManagementConfig();
            if (deviceManagementConfig != null) {
                paginationRequest.setRowCount(deviceManagementConfig.getPaginationConfiguration().
                        getDeviceListPageSize());
            } else {
                throw new DeviceManagementException("Device-Mgt configuration has not initialized. Please check the " +
                                                    "cdm-config.xml file.");
            }
        }
        return paginationRequest;
    }

    public static GroupPaginationRequest validateGroupListPageSize(GroupPaginationRequest paginationRequest) throws
                                                                                                    GroupManagementException {
        if (paginationRequest.getRowCount() == 0) {
            DeviceManagementConfig deviceManagementConfig = DeviceConfigurationManager.getInstance()
                    .getDeviceManagementConfig();
            if (deviceManagementConfig != null) {
                paginationRequest.setRowCount(deviceManagementConfig.getPaginationConfiguration()
                                                      .getDeviceListPageSize());
            } else {
                throw new GroupManagementException("Device-Mgt configuration has not initialized. Please check the " +
                                                   "cdm-config.xml file.");
            }
        }
        return paginationRequest;
    }

    public static int validateDeviceListPageSize(int limit) throws DeviceManagementException {
        if (limit == 0) {
            DeviceManagementConfig deviceManagementConfig = DeviceConfigurationManager.getInstance().
                    getDeviceManagementConfig();
            if (deviceManagementConfig != null) {
                return deviceManagementConfig.getPaginationConfiguration().getDeviceListPageSize();
            } else {
                throw new DeviceManagementException("Device-Mgt configuration has not initialized. Please check the " +
                                                    "cdm-config.xml file.");
            }
        }
        return limit;
    }

    public static boolean isPublishLocationResponseEnabled() throws DeviceManagementException {
        DeviceManagementConfig deviceManagementConfig = DeviceConfigurationManager.getInstance().
                getDeviceManagementConfig();
        if (deviceManagementConfig != null) {
            return deviceManagementConfig.getOperationAnalyticsConfiguration().isPublishLocationResponseEnabled();
        } else {
            throw new DeviceManagementException("Device-Mgt configuration has not initialized. Please check the " +
                                                        "cdm-config.xml file.");
        }
    }

    public static boolean isPublishDeviceInfoResponseEnabled() throws DeviceManagementException {
        DeviceManagementConfig deviceManagementConfig = DeviceConfigurationManager.getInstance().
                getDeviceManagementConfig();
        if (deviceManagementConfig != null) {
            return deviceManagementConfig.getOperationAnalyticsConfiguration().isPublishDeviceInfoResponseEnabled();
        } else {
            throw new DeviceManagementException("Device-Mgt configuration has not initialized. Please check the " +
                                                "cdm-config.xml file.");
        }
    }

    public static boolean isPublishOperationResponseEnabled() throws DeviceManagementException {
        DeviceManagementConfig deviceManagementConfig = DeviceConfigurationManager.getInstance().
                getDeviceManagementConfig();
        if (deviceManagementConfig != null) {
            return deviceManagementConfig.getOperationAnalyticsConfiguration()
                    .getOperationResponseConfigurations().isEnabled();
        } else {
            throw new DeviceManagementException("Device-Mgt configuration has not initialized. Please check the " +
                                                "cdm-config.xml file.");
        }
    }

    public static List<String> getEnabledOperationsForResponsePublish() throws DeviceManagementException {
        DeviceManagementConfig deviceManagementConfig = DeviceConfigurationManager.getInstance().
                getDeviceManagementConfig();
        if (deviceManagementConfig != null) {
            return deviceManagementConfig.getOperationAnalyticsConfiguration()
                    .getOperationResponseConfigurations().getOperations();
        } else {
            throw new DeviceManagementException("Device-Mgt configuration has not initialized. Please check the " +
                                                "cdm-config.xml file.");
        }
    }

    public static DeviceIDHolder validateDeviceIdentifiers(List<DeviceIdentifier> deviceIDs) {
        List<DeviceIdentifier> errorDeviceIdList = new ArrayList<>();
        List<DeviceIdentifier> validDeviceIDList = new ArrayList<>();
        for (DeviceIdentifier deviceIdentifier : deviceIDs) {
            String deviceID = deviceIdentifier.getId();
            if (deviceID == null || deviceID.isEmpty()) {
                log.warn("When adding operation for devices, found a device identifiers which doesn't have defined "
                        + "the identity of the device, with the request. Hence ignoring the device identifier.");
                continue;
            }
            try {
                if (isValidDeviceIdentifier(deviceIdentifier)) {
                    validDeviceIDList.add(deviceIdentifier);
                } else {
                    errorDeviceIdList.add(deviceIdentifier);
                }
            } catch (DeviceManagementException e) {
                errorDeviceIdList.add(deviceIdentifier);
            }
        }
        DeviceIDHolder deviceIDHolder = new DeviceIDHolder();
        deviceIDHolder.setValidDeviceIDList(validDeviceIDList);
        deviceIDHolder.setErrorDeviceIdList(errorDeviceIdList);

        return deviceIDHolder;
    }

    public static boolean isValidDeviceIdentifier(DeviceIdentifier deviceIdentifier) throws DeviceManagementException {
        Device device = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().getDevice(deviceIdentifier,
                false);
        if (device == null || device.getDeviceIdentifier() == null ||
                device.getDeviceIdentifier().isEmpty() || device.getEnrolmentInfo() == null) {
            return false;
        } else
            return !EnrolmentInfo.Status.REMOVED.equals(device.getEnrolmentInfo().getStatus());
    }

    public static boolean isDeviceExists(DeviceIdentifier deviceIdentifier) throws DeviceManagementException {
        Device device = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().getDevice(deviceIdentifier,
                false);
        return !(device == null || device.getDeviceIdentifier() == null ||
                device.getDeviceIdentifier().isEmpty() || device.getEnrolmentInfo() == null);
    }

    private static CacheManager getCacheManager() {
        return Caching.getCacheManagerFactory().getCacheManager(DeviceManagementConstants.DM_CACHE_MANAGER);
    }

    public static EventsPublisherService getEventPublisherService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        EventsPublisherService eventsPublisherService =
                (EventsPublisherService) ctx.getOSGiService(EventsPublisherService.class, null);
        if (eventsPublisherService == null) {
            String msg = "Event Publisher service has not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return eventsPublisherService;
    }

    public static void initializeDeviceCache() {
        DeviceManagementConfig config = DeviceConfigurationManager.getInstance().getDeviceManagementConfig();
        int deviceCacheExpiry = config.getDeviceCacheConfiguration().getExpiryTime();
        long deviceCacheCapacity = config.getDeviceCacheConfiguration().getCapacity();
        CacheManager manager = getCacheManager();
        if (config.getDeviceCacheConfiguration().isEnabled()) {
            if(!isDeviceCacheInitialized) {
                isDeviceCacheInitialized = true;
                if (manager != null) {
                    if (deviceCacheExpiry > 0) {
                        manager.<DeviceCacheKey, Device>createCacheBuilder(DeviceManagementConstants.DEVICE_CACHE).
                                setExpiry(CacheConfiguration.ExpiryType.MODIFIED, new CacheConfiguration.Duration(TimeUnit.SECONDS,
                                        deviceCacheExpiry)).setExpiry(CacheConfiguration.ExpiryType.ACCESSED, new CacheConfiguration.
                                Duration(TimeUnit.SECONDS, deviceCacheExpiry)).setStoreByValue(true).build();
                        if(deviceCacheCapacity > 0 ) {
                            ((CacheImpl)(manager.<DeviceCacheKey, Device>getCache(DeviceManagementConstants.DEVICE_CACHE))).
                                    setCapacity(deviceCacheCapacity);
                        }
                    } else {
                        manager.<DeviceCacheKey, Device>getCache(DeviceManagementConstants.DEVICE_CACHE);
                    }
                } else {
                    if (deviceCacheExpiry > 0) {
                        Caching.getCacheManager().
                                <DeviceCacheKey, Device>createCacheBuilder(DeviceManagementConstants.DEVICE_CACHE).
                                setExpiry(CacheConfiguration.ExpiryType.MODIFIED, new CacheConfiguration.Duration(TimeUnit.SECONDS,
                                        deviceCacheExpiry)).setExpiry(CacheConfiguration.ExpiryType.ACCESSED, new CacheConfiguration.
                                Duration(TimeUnit.SECONDS, deviceCacheExpiry)).setStoreByValue(true).build();
                        ((CacheImpl)(manager.<DeviceCacheKey, Device>getCache(DeviceManagementConstants.DEVICE_CACHE))).
                                setCapacity(deviceCacheCapacity);
                    } else {
                        Caching.getCacheManager().<DeviceCacheKey, Device>getCache(DeviceManagementConstants.DEVICE_CACHE);
                    }
                }
            }
        }
    }

    public static Cache<DeviceCacheKey, Device> getDeviceCache() {
        DeviceManagementConfig config = DeviceConfigurationManager.getInstance().getDeviceManagementConfig();
        CacheManager manager = getCacheManager();
        Cache<DeviceCacheKey, Device> deviceCache = null;
        if (config.getDeviceCacheConfiguration().isEnabled()) {
            if(!isDeviceCacheInitialized) {
                initializeDeviceCache();
            }
            if (manager != null) {
                deviceCache = manager.<DeviceCacheKey, Device>getCache(DeviceManagementConstants.DEVICE_CACHE);
            } else {
                deviceCache =  Caching.getCacheManager(DeviceManagementConstants.DM_CACHE_MANAGER).
                        <DeviceCacheKey, Device>getCache(DeviceManagementConstants.DEVICE_CACHE);
            }
        }
        return deviceCache;
    }

    /**
     * Create an app and get app registration token from the application registration endpoint
     *
     * @return AppRegistrationToken object which contains access and refresh tokens
     * @throws ApplicationRegistrationException when application fails to connect with the app registration
     *                                          endpoint
     */
    @SuppressWarnings("PackageAccessibility")
    public static AppRegistrationCredentials getApplicationRegistrationCredentials(String host, String port,
                                                                            String username, String password)
            throws ApplicationRegistrationException {
        if (host == null || port == null) {
            String msg = "Required gatewayHost or gatewayPort system property is null";
            log.error(msg);
            throw new ApplicationRegistrationException(msg);
        }
        String internalServerAddr = "https://".concat(host).concat(":").concat(port);
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost apiEndpoint = new HttpPost(
                    internalServerAddr + DeviceManagementConstants.ConfigurationManagement
                            .APPLICATION_REGISTRATION_API_ENDPOINT);

            apiEndpoint.setHeader(HTTP.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
            apiEndpoint.setHeader(DeviceManagementConstants.ConfigurationManagement.AUTHORIZATION_HEADER,
                    DeviceManagementConstants.ConfigurationManagement.BASIC_AUTH.concat(" ")
                            .concat(getBase64EncodedCredentials(username + ":" + password)));
            apiEndpoint.setEntity(constructApplicationRegistrationPayload());
            HttpResponse response = client.execute(apiEndpoint);
            if (response != null) {
                log.info("Obtained client credentials: " + response.getStatusLine().getStatusCode());
                BufferedReader rd = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                return new ObjectMapper().readValue(result.toString(), AppRegistrationCredentials.class);
            } else {
                String msg = "Response is 'NUll' for the Application Registration API call.";
                log.error(msg);
                throw new ApplicationRegistrationException(msg);
            }
        } catch (IOException e) {
            throw new ApplicationRegistrationException(
                    "Error occurred when invoking API. API endpoint: "
                    + internalServerAddr + DeviceManagementConstants.ConfigurationManagement
                            .APPLICATION_REGISTRATION_API_ENDPOINT, e);
        }
    }

    /**
     * Use default admin credentials and encode them in Base64
     *
     * @return Base64 encoded client credentials
     */
    private static String getBase64EncodedCredentials(String credentials) {
        return Base64.getEncoder().encodeToString(credentials.getBytes());
    }

    /**
     * Create a JSON payload for application registration
     *
     * @return Generated JSON payload
     */
    @SuppressWarnings("PackageAccessibility")
    private static StringEntity constructApplicationRegistrationPayload() {
        ApplicationRegistration applicationRegistration = new ApplicationRegistration();
        applicationRegistration.setApplicationName("MyApp");
        applicationRegistration.setAllowedToAllDomains(false);
        List<String> tags = new ArrayList<>();
        tags.add("device_management");
        applicationRegistration.setTags(tags);
        applicationRegistration.setValidityPeriod(3600);
        Gson gson = new Gson();
        String payload = gson.toJson(applicationRegistration);
        return new StringEntity(payload, ContentType.APPLICATION_JSON);
    }

    /**
     * Retrieves access token for a given device
     * @param scopes scopes for token
     * @param clientId clientId
     * @param clientSecret clientSecret
     * @param deviceOwner owner of the device that is going to generate token
     * @return @{@link AccessTokenInfo} wrapped object of retrieved access token and refresh token
     * @throws JWTClientException if an error occurs when the jwt client creation or token retrieval
     */
    public static AccessTokenInfo getAccessTokenForDeviceOwner(String scopes, String clientId,
                                                               String clientSecret, String deviceOwner)
            throws JWTClientException {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        JWTClientManagerService jwtClientManagerService = (JWTClientManagerService) ctx
                .getOSGiService(JWTClientManagerService.class, null);
        JWTClient jwtClient = jwtClientManagerService.getJWTClient();
        return jwtClient.getAccessToken(clientId, clientSecret, deviceOwner, scopes);
    }
}

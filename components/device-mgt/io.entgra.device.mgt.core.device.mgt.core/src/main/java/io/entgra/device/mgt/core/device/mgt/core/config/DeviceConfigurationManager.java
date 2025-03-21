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

package io.entgra.device.mgt.core.device.mgt.core.config;

import io.entgra.device.mgt.core.device.mgt.common.DeviceManagementConstants;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.core.util.DeviceManagerUtil;
import org.w3c.dom.Document;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * Class responsible for the cdm configuration initialization.
 */
public class DeviceConfigurationManager {

    private DeviceManagementConfig currentDeviceConfig;
    private static DeviceConfigurationManager deviceConfigManager;

    private static final String DEVICE_MGT_CONFIG_PATH =
            CarbonUtils.getCarbonConfigDirPath() + File.separator +
                    DeviceManagementConstants.DataSourceProperties.DEVICE_CONFIG_XML_NAME;
    private static final String DEVICE_MGT_CONFIG_SCHEMA_PATH = "resources/config/schema/device-mgt-config-schema.xsd";

    public static DeviceConfigurationManager getInstance() {
        if (deviceConfigManager == null) {
            synchronized (DeviceConfigurationManager.class) {
                if (deviceConfigManager == null) {
                    deviceConfigManager = new DeviceConfigurationManager();
                }
            }
        }
        return deviceConfigManager;
    }

    public synchronized void initConfig(String configLocation) throws DeviceManagementException {
        try {
            File deviceMgtConfig = new File(configLocation);
            Document doc = DeviceManagerUtil.convertToDocument(deviceMgtConfig);

            /* Un-marshaling Device Management configuration */
            JAXBContext cdmContext = JAXBContext.newInstance(DeviceManagementConfig.class);
            Unmarshaller unmarshaller = cdmContext.createUnmarshaller();
            //unmarshaller.setSchema(getSchema());
            this.currentDeviceConfig = (DeviceManagementConfig) unmarshaller.unmarshal(doc);
        } catch (JAXBException e) {
            throw new DeviceManagementException("Error occurred while initializing Data Source config", e);
        }
    }

    public void initConfig() throws DeviceManagementException {
        this.initConfig(DEVICE_MGT_CONFIG_PATH);
    }

    public DeviceManagementConfig getDeviceManagementConfig() {
        return currentDeviceConfig;
    }

}

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
package io.entgra.device.mgt.core.device.mgt.core.operation;

import io.entgra.device.mgt.core.device.mgt.common.Device;
import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.entgra.device.mgt.core.device.mgt.common.PaginationRequest;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.Operation;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.OperationManagementException;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.OperationManager;
import io.entgra.device.mgt.core.device.mgt.common.spi.DeviceManagementService;
import io.entgra.device.mgt.core.device.mgt.core.TestDeviceManagementService;
import io.entgra.device.mgt.core.device.mgt.core.common.BaseDeviceManagementTest;
import io.entgra.device.mgt.core.device.mgt.core.common.TestDataHolder;
import io.entgra.device.mgt.core.device.mgt.core.internal.DeviceManagementDataHolder;
import io.entgra.device.mgt.core.device.mgt.core.mock.MockDataSource;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.OperationManagerImpl;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;
import org.powermock.api.mockito.PowerMockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Negative test cases for {@link OperationManagerImpl}
 * regarding the database connectivity.
 */
public class OperationManagementNegativeDBOperationTest extends BaseDeviceManagementTest {

    private static final String DEVICE_TYPE = "NEGDB_OP_TEST_TYPE";
    private static final String DEVICE_ID_PREFIX = "NEGDB_OP-TEST-DEVICE-ID-";
    private static final int NO_OF_DEVICES = 5;
    private static final String ADMIN_USER = "admin";
    private static final String OWNSERSHIP = "BYOD";

    private List<DeviceIdentifier> deviceIds = new ArrayList<>();
    private OperationManager operationMgtService;
    private MockDataSource dataSource;


    @BeforeClass
    @Override
    public void init() throws Exception {
        DataSource datasource = this.getDataSource(this.
                readDataSourceConfig(getDatasourceLocation() + "-mock" + DATASOURCE_EXT));
        OperationManagementDAOFactory.init(datasource);
        for (int i = 0; i < NO_OF_DEVICES; i++) {
            deviceIds.add(new DeviceIdentifier(DEVICE_ID_PREFIX + i, DEVICE_TYPE));
        }
        List<Device> devices = TestDataHolder.generateDummyDeviceData(this.deviceIds);
        DeviceManagementProviderService deviceMgtService = DeviceManagementDataHolder.getInstance().
                getDeviceManagementProvider();
        deviceMgtService.registerDeviceType(new TestDeviceManagementService(DEVICE_TYPE,
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME));
        for (Device device : devices) {
            deviceMgtService.enrollDevice(device);
        }
        List<Device> returnedDevices = deviceMgtService.getAllDevices(DEVICE_TYPE);
        for (Device device : returnedDevices) {
            if (!device.getDeviceIdentifier().startsWith(DEVICE_ID_PREFIX)) {
                throw new Exception("Incorrect device with ID - " + device.getDeviceIdentifier() + " returned!");
            }
        }
        DeviceManagementService deviceManagementService
                = new TestDeviceManagementService(DEVICE_TYPE, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        this.operationMgtService = PowerMockito.spy(new OperationManagerImpl(DEVICE_TYPE, deviceManagementService));
        PowerMockito.when(this.operationMgtService, "getNotificationStrategy")
                .thenReturn(new TestNotificationStrategy());
        this.setMockDataSource();
    }

    @Test(description = "Get operation by device id", expectedExceptions = OperationManagementException.class)
    public void getOperations() throws OperationManagementException {
        this.dataSource.setThrowException(true);
        try {
            this.operationMgtService.getOperations(this.deviceIds.get(0));
        } finally {
            this.dataSource.reset();
        }
    }

    @Test(description = "Get operations with paginated request",
            expectedExceptions = OperationManagementException.class)
    public void getOperationsPaginatedRequest() throws OperationManagementException {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID, true);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(ADMIN_USER);
        PaginationRequest request = new PaginationRequest(1, 2);
        request.setDeviceType(DEVICE_TYPE);
        request.setOwner(ADMIN_USER);
        request.setOwnership(OWNSERSHIP);
        this.dataSource.setThrowException(true);
        try {
            this.operationMgtService.getOperations(this.deviceIds.get(0), request);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
            this.dataSource.reset();
        }
    }

    @Test(description = "Get pending operations", expectedExceptions = OperationManagementException.class)
    public void getPendingOperations() throws OperationManagementException {
        this.dataSource.setThrowException(true);
        try {
            this.operationMgtService.getPendingOperations(this.deviceIds.get(0));
        } finally {
            this.dataSource.reset();
        }
    }

    @Test(description = "Get next pending operations", expectedExceptions = OperationManagementException.class)
    public void getNextPendingOperations() throws OperationManagementException {
        this.dataSource.setThrowException(true);
        try {
            this.operationMgtService.getNextPendingOperation(this.deviceIds.get(0));
        } finally {
            this.dataSource.reset();
        }
    }

    @Test(description = "Get operation by device and operation id",
            expectedExceptions = OperationManagementException.class)
    public void getOperationByDeviceAndOperationId() throws OperationManagementException {
        this.dataSource.setThrowException(true);
        try {
            this.operationMgtService.getOperationByDeviceAndOperationId(this.deviceIds.get(0), 1);
        } finally {
            this.dataSource.reset();
        }
    }

    @Test(description = "Get operation by device and status",
            expectedExceptions = OperationManagementException.class)
    public void getOperationByDeviceAndStatus() throws OperationManagementException, DeviceManagementException {
        this.dataSource.setThrowException(true);
        try {
            this.operationMgtService.getOperationsByDeviceAndStatus(this.deviceIds.get(0), Operation.Status.PENDING);
        } finally {
            this.dataSource.reset();
        }
    }

    @Test(description = "Get operation by operation id",
            expectedExceptions = OperationManagementException.class)
    public void getOperationByOperationId() throws OperationManagementException, DeviceManagementException {
        this.dataSource.setThrowException(true);
        try {
            this.operationMgtService.getOperation(1);
        } finally {
            this.dataSource.reset();
        }
    }

    @Test(description = "Get operation by activity id",
            expectedExceptions = OperationManagementException.class)
    public void getOperationByActivityId() throws OperationManagementException, DeviceManagementException {
        this.dataSource.setThrowException(true);
        try {
            this.operationMgtService.getOperationByActivityId("ACTIVITY_1");
        } finally {
            this.dataSource.reset();
        }
    }

    @Test(description = "Get operation by activity id and device id",
            expectedExceptions = OperationManagementException.class)
    public void getOperationByActivityAndDeviceID() throws OperationManagementException, DeviceManagementException {
        this.dataSource.setThrowException(true);
        try {
            this.operationMgtService.getOperationByActivityIdAndDevice("ACTIVITY_1", deviceIds.get(0));
        } finally {
            this.dataSource.reset();
        }
    }

    @Test(description = "Get activities updated after some timestamp",
            expectedExceptions = OperationManagementException.class)
    public void getActivitiesUpdatedAfter() throws OperationManagementException, DeviceManagementException {
        this.dataSource.setThrowException(true);
        try {
            this.operationMgtService.getActivitiesUpdatedAfter(System.currentTimeMillis() / 1000, 10, 0);
        } finally {
            this.dataSource.reset();
        }
    }

    @Test(description = "Get activities count updated after some timestamp",
            expectedExceptions = OperationManagementException.class)
    public void getActivityCountUpdatedAfter() throws OperationManagementException, DeviceManagementException {
        this.dataSource.setThrowException(true);
        try {
            this.operationMgtService.getActivityCountUpdatedAfter(System.currentTimeMillis() / 1000);
        } finally {
            this.dataSource.reset();
        }
    }

    private void setMockDataSource() throws NoSuchFieldException, IllegalAccessException {
        Field datasource = OperationManagementDAOFactory.class.getDeclaredField("dataSource");
        datasource.setAccessible(true);
        this.dataSource = new MockDataSource(null);
        datasource.set(datasource, this.dataSource);
    }

    @AfterClass
    public void resetDatabase() throws Exception {
        OperationManagementDAOFactory.init(this.getDataSource(this.
                readDataSourceConfig(getDatasourceLocation() + DATASOURCE_EXT)));
    }
}

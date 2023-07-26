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

package io.entgra.device.mgt.core.device.mgt.core.dao.impl;

import io.entgra.device.mgt.core.device.mgt.common.Device;
import io.entgra.device.mgt.core.device.mgt.common.GroupPaginationRequest;
import io.entgra.device.mgt.core.device.mgt.common.PaginationRequest;
import io.entgra.device.mgt.core.device.mgt.common.group.mgt.DeviceGroup;
import io.entgra.device.mgt.core.device.mgt.core.dao.GroupDAO;
import io.entgra.device.mgt.core.device.mgt.core.dao.GroupManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.dao.GroupManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.dao.util.DeviceManagementDAOUtil;
import io.entgra.device.mgt.core.device.mgt.core.dao.util.GroupManagementDAOUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * This class represents implementation of GroupDAO
 */
public abstract class AbstractGroupDAOImpl implements GroupDAO {

    private static final Log log = LogFactory.getLog(AbstractGroupDAOImpl.class);

    @Override
    public List<DeviceGroup> getGroups(GroupPaginationRequest request, int tenantId)
            throws GroupManagementDAOException {
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT ID, DESCRIPTION, GROUP_NAME, OWNER, STATUS, PARENT_PATH FROM DM_GROUP "
                    + "WHERE TENANT_ID = ?";
            if (StringUtils.isNotBlank(request.getGroupName())) {
                sql += " AND GROUP_NAME LIKE ?";
            }
            if (StringUtils.isNotBlank(request.getOwner())) {
                sql += " AND OWNER LIKE ?";
            }
            if (StringUtils.isNotBlank(request.getStatus())) {
                sql += " AND STATUS = ?";
            }
            if (StringUtils.isNotBlank(request.getParentPath())) {
                sql += " AND PARENT_PATH LIKE ?";
            }
            if (request.getRowCount() != 0) {
                sql += " LIMIT ? OFFSET ?";
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int paramIndex = 1;
                stmt.setInt(paramIndex++, tenantId);
                if (StringUtils.isNotBlank(request.getGroupName())) {
                    stmt.setString(paramIndex++, request.getGroupName() + "%");
                }
                if (StringUtils.isNotBlank(request.getOwner())) {
                    stmt.setString(paramIndex++, request.getOwner() + "%");
                }
                if (StringUtils.isNotBlank(request.getStatus())) {
                    stmt.setString(paramIndex++, request.getStatus().toUpperCase());
                }
                if (StringUtils.isNotBlank(request.getParentPath())) {
                    stmt.setString(paramIndex++, request.getParentPath());
                }
                if (request.getRowCount() != 0) {
                    stmt.setInt(paramIndex++, request.getRowCount());
                    stmt.setInt(paramIndex, request.getStartIndex());
                }
                List<DeviceGroup> deviceGroupList = new ArrayList<>();
                try (ResultSet resultSet = stmt.executeQuery()) {
                    while (resultSet.next()) {
                        deviceGroupList.add(GroupManagementDAOUtil.loadGroup(resultSet));
                    }
                }
                return deviceGroupList;
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving groups in tenant: " + tenantId;
            log.error(msg);
            throw new GroupManagementDAOException(msg, e);
        }
    }

    @Override
    public List<DeviceGroup> getGroups(GroupPaginationRequest request, List<Integer> deviceGroupIds,
            int tenantId) throws GroupManagementDAOException {
        int deviceGroupIdsCount = deviceGroupIds.size();
        if (deviceGroupIdsCount == 0) {
            return new ArrayList<>();
        }

        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT ID, DESCRIPTION, GROUP_NAME, OWNER, STATUS, PARENT_PATH FROM DM_GROUP WHERE TENANT_ID = ?";
            if (StringUtils.isNotBlank(request.getGroupName())) {
                sql += " AND GROUP_NAME LIKE ?";
            }
            if (StringUtils.isNotBlank(request.getOwner())) {
                sql += " AND OWNER LIKE ?";
            }
            if (StringUtils.isNotBlank(request.getParentPath())) {
                sql += " AND PARENT_PATH LIKE ?";
            }
            sql += " AND ID IN (";
            for (int i = 0; i < deviceGroupIdsCount; i++) {
                sql += (deviceGroupIdsCount - 1 != i) ? "?," : "?";
            }
            sql += ")";
            if (request.getRowCount() != 0) {
                sql += " LIMIT ? OFFSET ?";
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int paramIndex = 1;
                stmt.setInt(paramIndex++, tenantId);
                if (StringUtils.isNotBlank(request.getGroupName())) {
                    stmt.setString(paramIndex++, request.getGroupName() + "%");
                }
                if (StringUtils.isNotBlank(request.getOwner())) {
                    stmt.setString(paramIndex++, request.getOwner() + "%");
                }
                if (StringUtils.isNotBlank(request.getParentPath())) {
                    stmt.setString(paramIndex++, request.getParentPath());
                }
                for (Integer deviceGroupId : deviceGroupIds) {
                    stmt.setInt(paramIndex++, deviceGroupId);
                }
                if (request.getRowCount() != 0) {
                    stmt.setInt(paramIndex++, request.getRowCount());
                    stmt.setInt(paramIndex, request.getStartIndex());
                }
                List<DeviceGroup> deviceGroupList = new ArrayList<>();
                try (ResultSet resultSet = stmt.executeQuery()) {
                    while (resultSet.next()) {
                        deviceGroupList.add(GroupManagementDAOUtil.loadGroup(resultSet));
                    }
                }
                return deviceGroupList;
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving groups of groups IDs " + deviceGroupIds.toString()
                    +  " in tenant: " + tenantId;
            log.error(msg);
            throw new GroupManagementDAOException(msg, e);
        }
    }

    @Override
    public int addGroup(DeviceGroup deviceGroup, int tenantId) throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs;
        int groupId = -1;
        boolean hasStatus = false;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql;
            if (deviceGroup.getStatus() == null || deviceGroup.getStatus().isEmpty()) {
                sql = "INSERT INTO DM_GROUP(DESCRIPTION, GROUP_NAME, OWNER, TENANT_ID, PARENT_PATH) "
                        + "VALUES (?, ?, ?, ?, ?)";
            } else {
                sql = "INSERT INTO DM_GROUP(DESCRIPTION, GROUP_NAME, OWNER, TENANT_ID, PARENT_PATH, STATUS) "
                        + "VALUES (?, ?, ?, ?, ?, ?)";
                hasStatus = true;
            }
            stmt = conn.prepareStatement(sql, new String[]{"ID"});
            stmt.setString(1, deviceGroup.getDescription());
            stmt.setString(2, deviceGroup.getName());
            stmt.setString(3, deviceGroup.getOwner());
            stmt.setInt(4, tenantId);
            stmt.setString(5, deviceGroup.getParentPath());
            if (hasStatus) {
                stmt.setString(6, deviceGroup.getStatus());
            }

            stmt.executeUpdate();
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                groupId = rs.getInt(1);
            }
            return groupId;
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while adding deviceGroup '" +
                    deviceGroup.getName() + "'", e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    public boolean addGroupProperties(DeviceGroup deviceGroup, int groupId, int tenantId)
            throws GroupManagementDAOException {
        boolean status;
        PreparedStatement stmt = null;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            stmt = conn.prepareStatement(
                    "INSERT INTO GROUP_PROPERTIES(GROUP_ID, PROPERTY_NAME, " +
                            "PROPERTY_VALUE, TENANT_ID) VALUES (?, ?, ?, ?)");
            for (Map.Entry<String, String> entry : deviceGroup.getGroupProperties().entrySet()) {
                stmt.setInt(1, groupId);
                stmt.setString(2, entry.getKey());
                stmt.setString(3, entry.getValue());
                stmt.setInt(4, tenantId);
                stmt.addBatch();
            }
            stmt.executeBatch();
            status = true;
        } catch (SQLException e) {
            String msg = "Error occurred while adding properties for group '" +
                    deviceGroup.getName() + "' values : " + deviceGroup.getGroupProperties();
            throw new GroupManagementDAOException(msg, e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, null);
        }
        return status;
    }

    public boolean updateGroupProperties(DeviceGroup deviceGroup, int groupId, int tenantId)
            throws GroupManagementDAOException {
        boolean status;
        PreparedStatement stmt = null;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            stmt = conn.prepareStatement(
                    "UPDATE GROUP_PROPERTIES SET PROPERTY_VALUE = ? WHERE GROUP_ID = ? AND " +
                            "TENANT_ID = ? AND PROPERTY_NAME = ?");
            for (Map.Entry<String, String> entry : deviceGroup.getGroupProperties().entrySet()) {
                stmt.setString(1, entry.getValue());
                stmt.setInt(2, groupId);
                stmt.setInt(3, tenantId);
                stmt.setString(4, entry.getKey());
                stmt.addBatch();
            }
            stmt.executeBatch();
            status = true;
        } catch (SQLException e) {
            String msg = "Error occurred while adding properties for group '" +
                    deviceGroup.getName() + "' values : " + deviceGroup.getGroupProperties();
            throw new GroupManagementDAOException(msg, e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, null);
        }
        return status;
    }

    @Override
    public void updateGroup(DeviceGroup deviceGroup, int groupId, int tenantId)
            throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        boolean hasStatus = false;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql =
                    "UPDATE DM_GROUP SET DESCRIPTION = ?, GROUP_NAME = ?, OWNER = ?, PARENT_PATH = ? WHERE ID = ? "
                            + "AND TENANT_ID = ?";

            if (deviceGroup.getStatus() != null && !deviceGroup.getStatus().isEmpty()) {
                sql = "UPDATE DM_GROUP SET DESCRIPTION = ?, GROUP_NAME = ?, OWNER = ?, PARENT_PATH = ?, STATUS = ? "
                        + "WHERE ID = ? AND TENANT_ID = ?";
                hasStatus = true;
            }
            stmt = conn.prepareStatement(sql);
            int paramIndex = 1;
            stmt.setString(paramIndex++, deviceGroup.getDescription());
            stmt.setString(paramIndex++, deviceGroup.getName());
            stmt.setString(paramIndex++, deviceGroup.getOwner());
            stmt.setString(paramIndex++, deviceGroup.getParentPath());
            if (hasStatus) {
                stmt.setString(paramIndex++, deviceGroup.getStatus());
            }
            stmt.setInt(paramIndex++, groupId);
            stmt.setInt(paramIndex, tenantId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while updating deviceGroup '" +
                    deviceGroup.getName() + "'", e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void updateGroups(List<DeviceGroup> deviceGroups, int tenantId) throws GroupManagementDAOException {
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "UPDATE DM_GROUP SET DESCRIPTION = ?, GROUP_NAME = ?, OWNER = ?, STATUS = ?, "
                    + "PARENT_PATH = ? WHERE ID = ? AND TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)){
                for (DeviceGroup deviceGroup : deviceGroups) {
                    stmt.setString(1, deviceGroup.getDescription());
                    stmt.setString(2, deviceGroup.getName());
                    stmt.setString(3, deviceGroup.getOwner());
                    stmt.setString(4, deviceGroup.getStatus());
                    stmt.setString(5, deviceGroup.getParentPath());
                    stmt.setInt(6, deviceGroup.getGroupId());
                    stmt.setInt(7, tenantId);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while updating groups as batch";
            log.error(msg);
            throw new GroupManagementDAOException(msg, e);
        }
    }

    @Override
    public void deleteGroup(int groupId, int tenantId) throws GroupManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = GroupManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_ROLE_GROUP_MAP WHERE GROUP_ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, groupId);
            stmt.setInt(2, tenantId);
            stmt.executeUpdate();
            sql = "DELETE FROM DM_DEVICE_GROUP_MAP WHERE GROUP_ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, groupId);
            stmt.setInt(2, tenantId);
            stmt.executeUpdate();
            sql = "DELETE FROM DM_DEVICE_GROUP_POLICY WHERE DEVICE_GROUP_ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, groupId);
            stmt.setInt(2, tenantId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while removing mappings for group.'", e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, null);
        }

        try {
            conn = GroupManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_GROUP WHERE ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, groupId);
            stmt.setInt(2, tenantId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while deleting group.'", e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void deleteGroupsMapping(List<Integer> groupIds, int tenantId) throws GroupManagementDAOException {
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_ROLE_GROUP_MAP WHERE GROUP_ID = ? AND TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (int groupId : groupIds) {
                    stmt.setInt(1, groupId);
                    stmt.setInt(2, tenantId);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
            sql = "DELETE FROM DM_DEVICE_GROUP_MAP WHERE GROUP_ID = ? AND TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (int groupId : groupIds) {
                    stmt.setInt(1, groupId);
                    stmt.setInt(2, tenantId);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
            sql = "DELETE FROM DM_DEVICE_GROUP_POLICY WHERE DEVICE_GROUP_ID = ? AND TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (int groupId : groupIds) {
                    stmt.setInt(1, groupId);
                    stmt.setInt(2, tenantId);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while removing mappings of groups as batches";
            log.error(msg);
            throw new GroupManagementDAOException(msg, e);
        }
    }

    @Override
    public void deleteGroups(List<Integer> groupIds, int tenantId) throws GroupManagementDAOException {
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_GROUP WHERE ID = ? AND TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (int groupId : groupIds) {
                    stmt.setInt(1, groupId);
                    stmt.setInt(2, tenantId);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting groups as batches";
            log.error(msg);
            throw new GroupManagementDAOException(msg, e);
        }
    }

    public void deleteAllGroupProperties(int groupId, int tenantId)
            throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            stmt = conn.prepareStatement(
                    "DELETE FROM GROUP_PROPERTIES WHERE GROUP_ID = ? AND TENANT_ID = ?");
            stmt.setInt(1, groupId);
            stmt.setInt(2, tenantId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            String msg = "Error occurred while deleting group ID : " + groupId;
            throw new GroupManagementDAOException(msg, e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    public void deleteAllGroupsProperties(List<Integer> groupIds, int tenantId) throws GroupManagementDAOException {
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "DELETE FROM GROUP_PROPERTIES WHERE GROUP_ID = ? AND TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (Integer groupId : groupIds) {
                    stmt.setInt(1, groupId);
                    stmt.setInt(2, tenantId);
                    stmt.addBatch();
                }
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting properties of groups as batches";
            log.error(msg);
            throw new GroupManagementDAOException(msg, e);
        }
    }

    public Map<String, String> getAllGroupProperties(int groupId, int tenantId)
            throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        Map<String, String> properties = new HashMap<String, String>();
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT PROPERTY_NAME, PROPERTY_VALUE FROM GROUP_PROPERTIES WHERE GROUP_ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, groupId);
            stmt.setInt(2, tenantId);
            resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                properties.put(resultSet.getString("PROPERTY_NAME"),
                        resultSet.getString("PROPERTY_VALUE"));
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting group ID : " + groupId;
            throw new GroupManagementDAOException(msg, e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, null);
        }
        return properties;
    }

    @Override
    public DeviceGroup getGroup(int groupId, int tenantId) throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT ID, DESCRIPTION, GROUP_NAME, OWNER, STATUS, PARENT_PATH FROM DM_GROUP WHERE ID = ? "
                    + "AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, groupId);
            stmt.setInt(2, tenantId);
            resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return GroupManagementDAOUtil.loadGroup(resultSet);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while obtaining information of Device Group '" +
                    groupId + "'", e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
    }

    @Override
    public List<DeviceGroup> getChildrenGroups(String parentPath, int tenantId) throws GroupManagementDAOException {
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT ID, DESCRIPTION, GROUP_NAME, OWNER, STATUS, PARENT_PATH FROM DM_GROUP "
                    + "WHERE PARENT_PATH LIKE ? AND TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, parentPath + "%");
                stmt.setInt(2, tenantId);
                List<DeviceGroup> deviceGroupList = new ArrayList<>();
                try (ResultSet resultSet = stmt.executeQuery()) {
                    while (resultSet.next()) {
                        deviceGroupList.add(GroupManagementDAOUtil.loadGroup(resultSet));
                    }
                }
                return deviceGroupList;
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving children group having parent path '" + parentPath
                    + "' in tenant: " + tenantId;
            log.error(msg);
            throw new GroupManagementDAOException(msg, e);
        }
    }

    @Override
    public List<DeviceGroup> getRootGroups(int tenantId) throws GroupManagementDAOException {
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT ID, DESCRIPTION, GROUP_NAME, OWNER, STATUS, PARENT_PATH FROM DM_GROUP "
                    + "WHERE PARENT_PATH LIKE ? AND TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, "/");
                stmt.setInt(2, tenantId);
                List<DeviceGroup> deviceGroupList = new ArrayList<>();
                try (ResultSet resultSet = stmt.executeQuery()) {
                    deviceGroupList = new ArrayList<>();
                    while (resultSet.next()) {
                        deviceGroupList.add(GroupManagementDAOUtil.loadGroup(resultSet));
                    }
                }
                return deviceGroupList;
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving root groups in tenant: " + tenantId;
            log.error(msg);
            throw new GroupManagementDAOException(msg, e);
        }
    }

    @Override
    public List<DeviceGroup> getGroups(int deviceId, int tenantId) throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<DeviceGroup> deviceGroupBuilders = new ArrayList<>();
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT G.ID, G.GROUP_NAME, G.DESCRIPTION, G.OWNER, G.STATUS, G.PARENT_PATH FROM DM_GROUP G " +
                    "INNER JOIN DM_DEVICE_GROUP_MAP GM ON G.ID = GM.GROUP_ID " +
                    "WHERE GM.DEVICE_ID = ? AND GM.TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.setInt(2, tenantId);
            resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                deviceGroupBuilders.add(GroupManagementDAOUtil.loadGroup(resultSet));
            }
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while obtaining information of Device Groups ", e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return deviceGroupBuilders;
    }

    @Override
    public List<DeviceGroup> getGroups(int tenantId) throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<DeviceGroup> deviceGroupList = null;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT ID, DESCRIPTION, GROUP_NAME, OWNER, STATUS, PARENT_PATH FROM DM_GROUP "
                    + "WHERE TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            resultSet = stmt.executeQuery();
            deviceGroupList = new ArrayList<>();
            while (resultSet.next()) {
                deviceGroupList.add(GroupManagementDAOUtil.loadGroup(resultSet));
            }
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while listing all groups in tenant: " + tenantId, e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return deviceGroupList;
    }

    @Override
    public int getGroupCount(int tenantId, String status) throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        boolean statusAvailable = false;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT COUNT(ID) AS GROUP_COUNT FROM DM_GROUP WHERE TENANT_ID = ?";
            if (!StringUtils.isEmpty(status)) {
                sql += " AND STATUS = ?";
                statusAvailable = true;
            }
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            if (statusAvailable) {
                stmt.setString(2, status);
            }
            resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("GROUP_COUNT");
            } else {
                return 0;
            }
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while getting group count'", e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
    }

    @Override
    public int getGroupCount(GroupPaginationRequest request, int tenantId)
            throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;

        String groupName = request.getGroupName();
        boolean hasGroupName = false;
        String owner = request.getOwner();
        boolean hasOwner = false;
        boolean hasStatus = false;

        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT COUNT(ID) AS GROUP_COUNT FROM DM_GROUP WHERE TENANT_ID = ?";
            if (groupName != null && !groupName.isEmpty()) {
                sql += " AND GROUP_NAME LIKE ?";
                hasGroupName = true;
            }
            if (owner != null && !owner.isEmpty()) {
                sql += " AND OWNER LIKE ?";
                hasOwner = true;
            }
            if (!StringUtils.isEmpty(request.getStatus())) {
                sql += " AND STATUS = ?";
                hasStatus = true;
            }
            if (StringUtils.isNotBlank(request.getParentPath())) {
                sql += " AND PARENT_PATH = ?";
            }

            int paramIndex = 1;
            stmt = conn.prepareStatement(sql);
            stmt.setInt(paramIndex++, tenantId);
            if (hasGroupName) {
                stmt.setString(paramIndex++, groupName + "%");
            }
            if (hasOwner) {
                stmt.setString(paramIndex++, owner + "%");
            }
            if (hasStatus) {
                stmt.setString(paramIndex++, request.getStatus());
            }
            if (StringUtils.isNotBlank(request.getParentPath())) {
                stmt.setString(paramIndex, request.getParentPath());
            }
            resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("GROUP_COUNT");
            } else {
                return 0;
            }
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while listing all groups in tenant: " + tenantId, e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
    }

    @Override
    public DeviceGroup getGroup(String groupName, int tenantId) throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql =
                    "SELECT ID, DESCRIPTION, GROUP_NAME, OWNER, STATUS, PARENT_PATH FROM DM_GROUP "
                            + "WHERE LOWER(GROUP_NAME) = LOWER(?) AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, groupName);
            stmt.setInt(2, tenantId);
            resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return GroupManagementDAOUtil.loadGroup(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while group Id listing by group name.'", e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
    }

    @Override
    public void addDevice(int groupId, int deviceId, int tenantId)
            throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "INSERT INTO DM_DEVICE_GROUP_MAP(DEVICE_ID, GROUP_ID, TENANT_ID) VALUES (?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.setInt(2, groupId);
            stmt.setInt(3, tenantId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while adding device to Group.", e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void removeDevice(int groupId, int deviceId, int tenantId)
            throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_DEVICE_GROUP_MAP WHERE DEVICE_ID = ? AND GROUP_ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.setInt(2, groupId);
            stmt.setInt(3, tenantId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while removing device from Group.", e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public boolean isDeviceMappedToGroup(int groupId, int deviceId, int tenantId)
            throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT ID FROM DM_DEVICE_GROUP_MAP WHERE GROUP_ID = ? AND DEVICE_ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, groupId);
            stmt.setInt(2, deviceId);
            stmt.setInt(3, tenantId);
            resultSet = stmt.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while checking device mapping with group.", e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
    }

    @Override
    public int getDeviceCount(int groupId, int tenantId) throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql =
                    "SELECT COUNT(ID) AS DEVICE_COUNT FROM DM_DEVICE_GROUP_MAP WHERE GROUP_ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, groupId);
            stmt.setInt(2, tenantId);
            resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("DEVICE_COUNT");
            } else {
                return 0;
            }
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while getting device count from the group.", e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
    }

    @Override
    public List<String> getRoles(int groupId, int tenantId) throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<String> userRoles;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT ROLE FROM DM_ROLE_GROUP_MAP WHERE GROUP_ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, groupId);
            stmt.setInt(2, tenantId);
            resultSet = stmt.executeQuery();
            userRoles = new ArrayList<>();
            while (resultSet.next()) {
                userRoles.add(resultSet.getString("ROLE"));
            }
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while listing all groups in tenant: " + tenantId, e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return userRoles;
    }

    @Override
    public void addRole(int groupId, String role, int tenantId) throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "INSERT INTO DM_ROLE_GROUP_MAP(GROUP_ID, ROLE, TENANT_ID) VALUES (?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, groupId);
            stmt.setString(2, role);
            stmt.setInt(3, tenantId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while adding new user role to Group.", e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void removeRole(int groupId, String role, int tenantId) throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_ROLE_GROUP_MAP WHERE GROUP_ID = ? AND ROLE = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, groupId);
            stmt.setString(2, role);
            stmt.setInt(3, tenantId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while removing device from Group.", e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public List<DeviceGroup> getGroups(String[] roles, int tenantId) throws GroupManagementDAOException {
        int rolesCount = roles.length;
        if (rolesCount == 0) {
            return new ArrayList<>();
        }
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<DeviceGroup> deviceGroupList = null;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT ID, DESCRIPTION, GROUP_NAME, OWNER, STATUS, PARENT_PATH FROM DM_GROUP g, " +
                    "(SELECT GROUP_ID FROM DM_ROLE_GROUP_MAP WHERE ROLE IN (";

            int index = 0;
            while (index++ < rolesCount - 1) {
                sql += "?,";
            }
            sql += "?)) gr WHERE g.ID = gr.GROUP_ID AND TENANT_ID = ? GROUP BY g.ID";

            stmt = conn.prepareStatement(sql);
            index = 0;
            while (index++ < rolesCount) {
                stmt.setString(index, roles[index - 1]);
            }
            stmt.setInt(index, tenantId);
            resultSet = stmt.executeQuery();
            deviceGroupList = new ArrayList<>();
            while (resultSet.next()) {
                deviceGroupList.add(GroupManagementDAOUtil.loadGroup(resultSet));
            }
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while listing all groups in tenant: " + tenantId, e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return deviceGroupList;
    }

    @Override
    public List<Integer> getGroupIds(String[] roles, int tenantId) throws GroupManagementDAOException {
        if (roles.length == 0) {
            return new ArrayList<>();
        }
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<Integer> deviceGroupIdList = null;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT ID FROM DM_GROUP g, (SELECT GROUP_ID FROM DM_ROLE_GROUP_MAP WHERE ROLE IN (";

            int rolesCount = roles.length;
            for (int i = 0; i < rolesCount; i++) {
                sql += (rolesCount - 1 != i) ? "?," : "?";
            }
            sql += ")) gr WHERE g.ID = gr.GROUP_ID AND TENANT_ID = ? GROUP BY g.ID";

            stmt = conn.prepareStatement(sql);
            int index = 0;
            while (index++ < rolesCount) {
                stmt.setString(index, roles[index - 1]);
            }
            stmt.setInt(index, tenantId);
            resultSet = stmt.executeQuery();
            deviceGroupIdList = new ArrayList<>();
            while (resultSet.next()) {
                deviceGroupIdList.add(resultSet.getInt("ID"));
            }
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while listing all groups in tenant: " + tenantId, e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return deviceGroupIdList;
    }

    @Override
    public int getGroupsCount(String[] roles, int tenantId, String parentPath) throws GroupManagementDAOException {
        int rolesCount = roles.length;
        if (rolesCount == 0) {
            return 0;
        }
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT COUNT(ID) AS GROUP_COUNT FROM DM_GROUP g, " +
                    "(SELECT GROUP_ID FROM DM_ROLE_GROUP_MAP WHERE ROLE IN (";
            for (int i = 0; i < rolesCount; i++) {
                sql += (rolesCount - 1 != i) ? "?," : "?";
            }
            sql += ")) gr WHERE g.ID = gr.GROUP_ID AND TENANT_ID = ? ";
            if (StringUtils.isNotBlank(parentPath)) {
                sql += " AND g.PARENT_PATH = ? ";
            }
            sql +=  "GROUP BY g.ID";
            stmt = conn.prepareStatement(sql);
            int index = 0;
            while (index++ < rolesCount) {
                stmt.setString(index, roles[index - 1]);
            }
            stmt.setInt(index++, tenantId);
            if (StringUtils.isNotBlank(parentPath)) {
                stmt.setString(index, parentPath);
            }
            resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("GROUP_COUNT");
            } else {
                return 0;
            }
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while getting permitted groups count.", e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
    }

    @Override
    public List<DeviceGroup> getOwnGroups(String username, int tenantId) throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<DeviceGroup> deviceGroupList = null;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT ID, DESCRIPTION, GROUP_NAME, OWNER, STATUS, PARENT_PATH FROM DM_GROUP "
                    + "WHERE OWNER = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setInt(2, tenantId);
            resultSet = stmt.executeQuery();
            deviceGroupList = new ArrayList<>();
            while (resultSet.next()) {
                deviceGroupList.add(GroupManagementDAOUtil.loadGroup(resultSet));
            }
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while getting own groups of user '"
                    + username + "'", e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return deviceGroupList;
    }

    @Override
    public List<Integer> getOwnGroupIds(String username, int tenantId) throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<Integer> deviceGroupIdList = null;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT ID FROM DM_GROUP WHERE OWNER = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setInt(2, tenantId);
            resultSet = stmt.executeQuery();
            deviceGroupIdList = new ArrayList<>();
            while (resultSet.next()) {
                deviceGroupIdList.add(resultSet.getInt("ID"));
            }
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while getting own groups of user '"
                    + username + "'", e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return deviceGroupIdList;
    }

    @Override
    public int getOwnGroupsCount(String username, int tenantId, String parentPath) throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT COUNT(ID) AS GROUP_COUNT FROM DM_GROUP WHERE OWNER = ? AND TENANT_ID = ?";
            if (StringUtils.isNotBlank(parentPath)) {
                sql += " AND PARENT_PATH = ?";
            }
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setInt(2, tenantId);
            if (StringUtils.isNotBlank(parentPath)) {
                stmt.setString(3, parentPath);
            }
            resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("GROUP_COUNT");
            } else {
                return 0;
            }
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while getting own groups count of user '"
                    + username + "'", e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
    }

    @Override
    public List<Device> getAllDevicesOfGroup(String groupName, List<String> deviceStatuses, int tenantId)
            throws GroupManagementDAOException {
        List<Device> devices = new ArrayList<>();
        try {
            if (deviceStatuses.isEmpty()) {
                return devices;
            }
            Connection conn = GroupManagementDAOFactory.getConnection();
            StringJoiner joiner = new StringJoiner(",","SELECT "
                    + "d1.DEVICE_ID, "
                    + "d1.DESCRIPTION, "
                    + "d1.NAME AS DEVICE_NAME, "
                    + "d1.DEVICE_TYPE, "
                    + "d1.DEVICE_IDENTIFICATION, "
                    + "e.OWNER, "
                    + "e.OWNERSHIP, "
                    + "e.STATUS, "
                    + "e.IS_TRANSFERRED, "
                    + "e.DATE_OF_LAST_UPDATE, "
                    + "e.DATE_OF_ENROLMENT, "
                    + "e.ID AS ENROLMENT_ID "
                    + "FROM "
                    + "DM_ENROLMENT e, "
                    + "(SELECT gd.DEVICE_ID, gd.DESCRIPTION, gd.NAME, gd.DEVICE_IDENTIFICATION, t.NAME AS DEVICE_TYPE "
                    + "FROM "
                    + "(SELECT d.ID AS DEVICE_ID, d.DESCRIPTION, d.NAME, d.DEVICE_IDENTIFICATION, d.DEVICE_TYPE_ID "
                    + "FROM DM_DEVICE d, "
                    + "(SELECT dgm.DEVICE_ID "
                    + "FROM DM_DEVICE_GROUP_MAP dgm "
                    + "WHERE dgm.GROUP_ID = (SELECT ID FROM DM_GROUP WHERE GROUP_NAME = ? AND TENANT_ID = ?)) dgm1 "
                    + "WHERE d.ID = dgm1.DEVICE_ID AND d.TENANT_ID = ?) gd, DM_DEVICE_TYPE t "
                    + "WHERE gd.DEVICE_TYPE_ID = t.ID) d1 "
                    + "WHERE d1.DEVICE_ID = e.DEVICE_ID AND TENANT_ID = ? AND e.STATUS IN (",
                    ")");

            deviceStatuses.stream().map(ignored -> "?").forEach(joiner::add);
            String query = joiner.toString();

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                int index = 1;
                stmt.setString(index++, groupName);
                stmt.setInt(index++, tenantId);
                stmt.setInt(index++, tenantId);
                stmt.setInt(index++, tenantId);
                for (String deviceId : deviceStatuses) {
                    stmt.setObject(index++, deviceId);
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Device device = DeviceManagementDAOUtil.loadDevice(rs);
                        devices.add(device);
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while fetching the list of devices belongs to '" + groupName + "'";
            log.error(msg, e);
            throw new GroupManagementDAOException(msg, e);
        }
        return devices;
    }


        @Override
    public List<Device> getAllDevicesOfGroup(String groupName, int tenantId) throws GroupManagementDAOException {
        Connection conn;
        List<Device> devices;
        try {
            conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT "
                    + "d1.DEVICE_ID, "
                    + "d1.DESCRIPTION, "
                    + "d1.NAME AS DEVICE_NAME, "
                    + "d1.DEVICE_TYPE, "
                    + "d1.DEVICE_IDENTIFICATION, "
                    + "e.OWNER, "
                    + "e.OWNERSHIP, "
                    + "e.STATUS, "
                    + "e.IS_TRANSFERRED, "
                    + "e.DATE_OF_LAST_UPDATE, "
                    + "e.DATE_OF_ENROLMENT, "
                    + "e.ID AS ENROLMENT_ID "
                    + "FROM "
                    + "DM_ENROLMENT e, "
                    + "(SELECT gd.DEVICE_ID, gd.DESCRIPTION, gd.NAME, gd.DEVICE_IDENTIFICATION, t.NAME AS DEVICE_TYPE "
                    + "FROM "
                    + "(SELECT d.ID AS DEVICE_ID, d.DESCRIPTION, d.NAME, d.DEVICE_IDENTIFICATION, d.DEVICE_TYPE_ID "
                    + "FROM DM_DEVICE d, "
                    + "(SELECT dgm.DEVICE_ID "
                    + "FROM DM_DEVICE_GROUP_MAP dgm "
                    + "WHERE dgm.GROUP_ID = (SELECT ID FROM DM_GROUP WHERE GROUP_NAME = ? )) dgm1 "
                    + "WHERE d.ID = dgm1.DEVICE_ID AND d.TENANT_ID = ?) gd, DM_DEVICE_TYPE t "
                    + "WHERE gd.DEVICE_TYPE_ID = t.ID) d1 "
                    + "WHERE d1.DEVICE_ID = e.DEVICE_ID AND TENANT_ID = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, groupName);
                stmt.setInt(2, tenantId);
                stmt.setInt(3, tenantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    devices = new ArrayList<>();
                    while (rs.next()) {
                        Device device = DeviceManagementDAOUtil.loadDevice(rs);
                        devices.add(device);
                    }
                }
            }
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while retrieving information of all registered devices"
                    + " which belongs to the given group name.", e);
        }
        return devices;
    }

    @Override
    public List<Device> getGroupUnassignedDevices(PaginationRequest paginationRequest,
                                                  List<String> groupNames)
            throws GroupManagementDAOException {
        List<Device> groupUnassignedDeviceList;
        try {
            Connection connection = GroupManagementDAOFactory.getConnection();
            StringJoiner sql = new StringJoiner(",",
                    "SELECT DEVICE.ID AS DEVICE_ID, " +
                            "DEVICE.NAME AS DEVICE_NAME, " +
                            "DEVICE_TYPE.NAME AS DEVICE_TYPE, " +
                            "DEVICE.DESCRIPTION, " +
                            "DEVICE.DEVICE_IDENTIFICATION, " +
                            "ENROLMENT.ID AS ENROLMENT_ID, " +
                            "ENROLMENT.OWNER, " +
                            "ENROLMENT.OWNERSHIP, " +
                            "ENROLMENT.DATE_OF_ENROLMENT, " +
                            "ENROLMENT.DATE_OF_LAST_UPDATE, " +
                            "ENROLMENT.STATUS, " +
                            "ENROLMENT.IS_TRANSFERRED " +
                            "FROM DM_DEVICE AS DEVICE, DM_DEVICE_TYPE AS DEVICE_TYPE, DM_ENROLMENT " +
                            "AS ENROLMENT " +
                            "WHERE DEVICE_TYPE.NAME = ? AND DEVICE.ID " +
                            "NOT IN " +
                            "(SELECT DEVICE_ID " +
                            "FROM DM_DEVICE_GROUP_MAP " +
                            "WHERE GROUP_ID IN (SELECT ID FROM DM_GROUP WHERE GROUP_NAME NOT IN (",
                    ")) GROUP BY DEVICE_ID)");

            groupNames.stream().map(e -> "?").forEach(sql::add);
            try (PreparedStatement stmt = connection.prepareStatement(String.valueOf(sql))) {
                int index = 1;
                stmt.setString(index++, paginationRequest.getDeviceType());
                for (String groupName : groupNames) {
                    stmt.setString(index++, groupName);
                }
                try (ResultSet resultSet = stmt.executeQuery()) {
                    groupUnassignedDeviceList = new ArrayList<>();
                    while (resultSet.next()) {
                        groupUnassignedDeviceList.add(DeviceManagementDAOUtil.loadDevice(resultSet));
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving information of group unassigned devices";
            log.error(msg, e);
            throw new GroupManagementDAOException(msg, e);
        }
        return groupUnassignedDeviceList;
    }
}

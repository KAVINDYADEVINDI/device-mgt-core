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

package org.wso2.carbon.device.mgt.core.traccar.common.beans;

import org.wso2.carbon.device.mgt.common.DeviceIdentifier;

public class TraccarDevice {
    private String deviceIdentifier;
    private String deviceName;
    private String uniqueId;
    private String status;
    private String disabled;
    private String lastUpdate;
    private String positionId;
    private String groupId;
    private String phone;
    private String model;
    private String contact;
    private String category;
    
    public TraccarDevice(String deviceIdentifier){
        this.deviceIdentifier =deviceIdentifier;
    }

    public TraccarDevice(String deviceName, String uniqueId, String status, String disabled, String lastUpdate,
                         String positionId, String groupId, String phone, String model, String contact,
                         String category){
        this.deviceName =deviceName;
        this.uniqueId=uniqueId;
        this.status=status;
        this.disabled =disabled;
        this.lastUpdate =lastUpdate;
        this.positionId =positionId;
        this.groupId =groupId;
        this.phone =phone;
        this.model =model;
        this.contact =contact;
        this.category =category;
    }

    public TraccarDevice(){ }

    public String getDeviceIdentifier() {return deviceIdentifier;}

    public void setDeviceIdentifier(String deviceIdentifier) {this.deviceIdentifier = deviceIdentifier;}

    public String getDeviceName() {return deviceName;}

    public void setDeviceName(String deviceName) {this.deviceName = deviceName;}

    public String getUniqueId() {return uniqueId;}

    public void setUniqueId(String uniqueId) {this.uniqueId = uniqueId;}

    public String getStatus() {return status;}

    public void setStatus(String status) {this.status = status;}

    public String getDisabled() {return disabled;}

    public void setDisabled(String disabled) {this.disabled = disabled;}

    public String getLastUpdate() {return lastUpdate;}

    public void setLastUpdate(String lastUpdate) {this.lastUpdate = lastUpdate;}

    public String getPhone() {return phone;}

    public void setPhone(String phone) {this.phone = phone;}

    public String getModel() {return model;}

    public void setModel(String model) {this.model = model;}

    public String getContact() {return contact;}

    public void setContact(String contact) {this.contact = contact;}

    public String getCategory() {return category;}

    public void setCategory(String category) {this.category = category;}

    public String getPositionId() {return positionId;}

    public void setPositionId(String positionId) {this.positionId = positionId;}

    public String getGroupId() {return groupId;}

    public void setGroupId(String groupId) {this.groupId = groupId;}

}

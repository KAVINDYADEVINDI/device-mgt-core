package org.wso2.carbon.device.application.mgt.common.response;/* Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

import io.swagger.annotations.ApiModelProperty;

import java.util.List;

public class Application {
    @ApiModelProperty(name = "name",
            value = "Name of the application",
            required = true)
    private String name;

    @ApiModelProperty(name = "description",
            value = "Description of the application",
            required = true)
    private String description;

    @ApiModelProperty(name = "appCategory",
            value = "CategoryEntity of the application",
            required = true,
            example = "Educational, Gaming, Travel, Entertainment etc")
    private String appCategory;

    @ApiModelProperty(name = "type",
            value = "Type of the application",
            required = true,
            example = "ENTERPRISE, PUBLIC, WEB, WEB_CLIP etc")
    private String type;

    @ApiModelProperty(name = "subType",
            value = "Subscription type of the application",
            required = true,
            example = "PAID, FREE")
    private String subType;

    @ApiModelProperty(name = "paymentCurrency",
            value = "Payment currency of the application",
            required = true,
            example = "$")
    private String paymentCurrency;

    @ApiModelProperty(name = "tags",
            value = "List of application tags")
    private List<String> tags;

    @ApiModelProperty(name = "unrestrictedRoles",
            value = "List of roles that users should have to access the application")
    private List<String> unrestrictedRoles;

    @ApiModelProperty(name = "deviceType",
            value = "Related device type of the application",
            required = true,
            example = "IoS, Android, Arduino, RaspberryPi etc")
    private String deviceType;


    @ApiModelProperty(name = "applicationReleases",
            value = "List of application releases",
            required = true)
    private List<ApplicationRelease> applicationReleases;


    public String getName() {
        return name;
    }

    public void setName(String name) { this.name = name; }

    public String getAppCategory() {
        return appCategory;
    }

    public void setAppCategory(String appCategory) {
        this.appCategory = appCategory;
    }

    public List<String> getTags() { return tags; }

    public void setTags(List<String> tags) { this.tags = tags; }

    public String getType() { return type; }

    public void setType(String type) { this.type = type; }

    public String getSubType() { return subType; }

    public void setSubType(String subType) { this.subType = subType; }

    public String getPaymentCurrency() { return paymentCurrency; }

    public void setPaymentCurrency(String paymentCurrency) { this.paymentCurrency = paymentCurrency; }

    public List<ApplicationRelease> getApplicationReleases() { return applicationReleases; }

    public void setApplicationReleases(List<ApplicationRelease> applicationReleases) {
        this.applicationReleases = applicationReleases; }

    public List<String> getUnrestrictedRoles() { return unrestrictedRoles; }

    public void setUnrestrictedRoles(List<String> unrestrictedRoles) { this.unrestrictedRoles = unrestrictedRoles; }

    public String getDeviceType() { return deviceType; }

    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }
}

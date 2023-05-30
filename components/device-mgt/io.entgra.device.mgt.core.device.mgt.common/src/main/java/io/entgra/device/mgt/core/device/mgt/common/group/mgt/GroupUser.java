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
package io.entgra.device.mgt.core.device.mgt.common.group.mgt;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

/**
 * This class holds Device Group user name and assigned group roles of user. Exposed to external access
 */
@ApiModel(value = "GroupUser", description = "This class carries all information related to a user of a managed device group.")
public class GroupUser implements Serializable {

    private static final long serialVersionUID = 1998131711L;

    @ApiModelProperty(name = "username", value = "Username of the user.", required = true)
    private String username;

    @ApiModelProperty(name = "roles", value = "List of roles assigned to the user.")
    private List<String> groupRoles;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getGroupRoles() {
        return groupRoles;
    }

    public void setGroupRoles(List<String> groupRoles) {
        this.groupRoles = groupRoles;
    }

}

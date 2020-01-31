/*
 * Copyright (c) 2020, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (pvt) Ltd. licenses this file to you under the Apache License,
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

package org.wso2.carbon.device.mgt.extensions.device.type.template.policy.mgt;

import org.wso2.carbon.device.mgt.common.ui.policy.mgt.Policy;
import org.wso2.carbon.device.mgt.common.ui.policy.mgt.PolicyConfigurationManager;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationBasedPolicyManager implements PolicyConfigurationManager {
    private List<Policy> policies = new ArrayList<>();

    public ConfigurationBasedPolicyManager(List<org.wso2.carbon.device.mgt.extensions.device.type.template.config.Policy> policies){
        policies.forEach(policy -> {
            Policy policyConfiguration = new Policy();
            policyConfiguration.setName(policy.getName());
            if(policy.getPanels() != null){
                List<Policy.DataPanels> panel = new ArrayList<>();
                policy.getPanels().parallelStream().forEach(panelData -> {
                    Policy.DataPanels panelDataEntry = new Policy.DataPanels();
                    panelDataEntry.setPanel(panelData);
                    panel.add(panelDataEntry);
                });
                policyConfiguration.setPanels(panel);
            }
            this.policies.add(policyConfiguration);
        });
    }

    @Override
    public List<Policy> getPolicies() {
        return policies;
    }
}
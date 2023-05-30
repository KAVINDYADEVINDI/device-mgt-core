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

function onRequest(context) {
    var utility = require("/app/modules/utility.js").utility;
    var deviceType = context.uriParams.deviceType;
    var deviceName = request.getParameter("deviceName");
    var deviceId = request.getParameter("deviceId");
	var unitName = utility.getTenantedDeviceUnitName(deviceType, "analytics-view");
	if (!unitName) {
		unitName = "cdmf.unit.default.device.type.analytics-view";
	}
    return {
        "deviceAnalyticsViewUnitName": unitName,
        "deviceType": deviceType,
        "deviceName": deviceName,
        "deviceId": deviceId
    };
}

/* Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package org.wso2.carbon.device.application.mgt.core.impl;

import com.dd.plist.NSDictionary;
import net.dongliu.apk.parser.bean.ApkMeta;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.common.ApplicationInstaller;
import org.wso2.carbon.device.application.mgt.common.dto.ApplicationReleaseDTO;
import org.wso2.carbon.device.application.mgt.common.DeviceTypes;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationStorageManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.ResourceManagementException;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationStorageManager;
import org.wso2.carbon.device.application.mgt.core.exception.ParsingException;
import org.wso2.carbon.device.application.mgt.core.util.ArtifactsParser;
import org.wso2.carbon.device.application.mgt.core.util.StorageManagementUtil;

import java.io.*;
import java.util.List;

import static org.wso2.carbon.device.application.mgt.core.util.StorageManagementUtil.saveFile;

/**
 * This class contains the default concrete implementation of ApplicationStorage Management.
 */
public class ApplicationStorageManagerImpl implements ApplicationStorageManager {
    private static final Log log = LogFactory.getLog(ApplicationStorageManagerImpl.class);
    private String storagePath;
    private int screenShotMaxCount;

    /**
     * Create a new ApplicationStorageManager Instance
     *
     * @param storagePath        Storage Path to save the binary and image files.
     * @param screenShotMaxCount Maximum Screen-shots count
     */
    public ApplicationStorageManagerImpl(String storagePath, String screenShotMaxCount) {
        this.storagePath = storagePath;
        this.screenShotMaxCount = Integer.parseInt(screenShotMaxCount);
    }

    @Override
    public ApplicationReleaseDTO uploadImageArtifacts(ApplicationReleaseDTO applicationReleaseDTO,
            InputStream iconFileStream, InputStream bannerFileStream, List<InputStream> screenShotStreams)
            throws ResourceManagementException {
        String artifactDirectoryPath;
        String iconStoredLocation;
        String bannerStoredLocation;
        String scStoredLocation = null;

        try {
            artifactDirectoryPath = storagePath + applicationReleaseDTO.getAppHashValue();
            StorageManagementUtil.createArtifactDirectory(artifactDirectoryPath);

            if (iconFileStream != null) {
                iconStoredLocation = artifactDirectoryPath + File.separator + applicationReleaseDTO.getIconName();
                saveFile(iconFileStream, iconStoredLocation);
            }
            if (bannerFileStream != null) {
                bannerStoredLocation = artifactDirectoryPath + File.separator + applicationReleaseDTO.getBannerName();
                saveFile(bannerFileStream, bannerStoredLocation);
            }
            if (!screenShotStreams.isEmpty()) {
                if (screenShotStreams.size() > screenShotMaxCount) {
                    String msg = "Maximum limit for the screen-shot exceeds. You can't upload more than three "
                            + "screenshot for an application release";
                    log.error(msg);
                    throw new ApplicationStorageManagementException(msg);
                }
                int count = 1;
                for (InputStream screenshotStream : screenShotStreams) {
                    if (count == 1) {
                        scStoredLocation = artifactDirectoryPath + File.separator + applicationReleaseDTO.getScreenshotName1();
                    }
                    if (count == 2) {
                        scStoredLocation = artifactDirectoryPath + File.separator + applicationReleaseDTO.getScreenshotName2();
                    }
                    if (count == 3) {
                        scStoredLocation = artifactDirectoryPath + File.separator + applicationReleaseDTO.getScreenshotName3();
                    }
                    saveFile(screenshotStream, scStoredLocation);
                    count++;
                }
            }
            return applicationReleaseDTO;
        } catch (IOException e) {
            throw new ApplicationStorageManagementException("IO Exception while saving the screens hots for " +
                    "the application " + applicationReleaseDTO.getUuid(), e);
        }
    }

    @Override
    public ApplicationInstaller getAppInstallerData(InputStream binaryFile, String deviceType)
            throws ApplicationStorageManagementException {
        ApplicationInstaller applicationInstaller = new ApplicationInstaller();
        try {
            if (DeviceTypes.ANDROID.toString().equalsIgnoreCase(deviceType)) {
                ApkMeta apkMeta = ArtifactsParser.readAndroidManifestFile(binaryFile);
                applicationInstaller.setVersion(apkMeta.getVersionName());
                applicationInstaller.setPackageName(apkMeta.getPackageName());
            } else if (DeviceTypes.IOS.toString().equalsIgnoreCase(deviceType)) {
                NSDictionary plistInfo = ArtifactsParser.readiOSManifestFile(binaryFile);
                applicationInstaller
                        .setVersion(plistInfo.objectForKey(ArtifactsParser.IPA_BUNDLE_VERSION_KEY).toString());
                applicationInstaller
                        .setPackageName(plistInfo.objectForKey(ArtifactsParser.IPA_BUNDLE_IDENTIFIER_KEY).toString());
            } else {
                String msg = "Application Type doesn't match with supporting application types " + deviceType;
                log.error(msg);
                throw new ApplicationStorageManagementException(msg);
            }
        } catch (ParsingException e){
            String msg = "Application Type doesn't match with supporting application types " + deviceType;
            log.error(msg);
            throw new ApplicationStorageManagementException(msg);
        }
        return applicationInstaller;
    }

    @Override
    public ApplicationReleaseDTO uploadReleaseArtifact(ApplicationReleaseDTO applicationReleaseDTO,
            String deviceType, InputStream binaryFile) throws ResourceManagementException {
        try {
            String artifactDirectoryPath;
            String artifactPath;
            byte [] content = IOUtils.toByteArray(binaryFile);

            artifactDirectoryPath = storagePath + applicationReleaseDTO.getAppHashValue();
            StorageManagementUtil.createArtifactDirectory(artifactDirectoryPath);
            artifactPath = artifactDirectoryPath + File.separator + applicationReleaseDTO.getInstallerName();
            saveFile(new ByteArrayInputStream(content), artifactPath);
        } catch (IOException e) {
            String msg = "IO Exception while saving the release artifacts in the server for the application UUID "
                    + applicationReleaseDTO.getUuid();
            log.error(msg);
            throw new ApplicationStorageManagementException( msg, e);
        }
        return applicationReleaseDTO;
    }

    @Override
    public void copyImageArtifactsAndDeleteInstaller(String deletingAppHashValue,
            ApplicationReleaseDTO applicationReleaseDTO) throws ApplicationStorageManagementException {

        try {
            String appHashValue = applicationReleaseDTO.getAppHashValue();
            String bannerName = applicationReleaseDTO.getBannerName();
            String iconName = applicationReleaseDTO.getIconName();
            String screenshot1 = applicationReleaseDTO.getScreenshotName1();
            String screenshot2 = applicationReleaseDTO.getScreenshotName2();
            String screenshot3 = applicationReleaseDTO.getScreenshotName3();

            if (bannerName != null) {
                StorageManagementUtil.copy(storagePath + deletingAppHashValue + File.separator + bannerName,
                        storagePath + appHashValue + File.separator + bannerName);
            }
            if (iconName != null) {
                StorageManagementUtil.copy(storagePath + deletingAppHashValue + File.separator + iconName,
                        storagePath + appHashValue + File.separator + iconName);
            }
            if (screenshot1 != null) {
                StorageManagementUtil.copy(storagePath + deletingAppHashValue + File.separator + screenshot1,
                        storagePath + appHashValue + File.separator + screenshot1);
            }
            if (screenshot2 != null) {
                StorageManagementUtil.copy(storagePath + deletingAppHashValue + File.separator + screenshot2,
                        storagePath + appHashValue + File.separator + screenshot2);
            }
            if (screenshot3 != null) {
                StorageManagementUtil.copy(storagePath + deletingAppHashValue + File.separator + screenshot3,
                        storagePath + appHashValue + File.separator + screenshot3);
            }
            deleteAppReleaseArtifact( storagePath + deletingAppHashValue);
        } catch (IOException e) {
            String msg = "Application installer updating is failed because of I/O issue";
            log.error(msg);
            throw new ApplicationStorageManagementException(msg, e);
        }
    }



    @Override
    public void deleteAppReleaseArtifact(String appReleaseHashVal, String fileName) throws ApplicationStorageManagementException {
        String artifactPath = storagePath + appReleaseHashVal + File.separator + fileName;
        deleteAppReleaseArtifact(artifactPath);
    }

    @Override
    public void deleteAllApplicationReleaseArtifacts(List<String> directoryPaths)
            throws ApplicationStorageManagementException {
        for (String directoryBasePath : directoryPaths) {
            deleteAppReleaseArtifact(storagePath + directoryBasePath);
        }
    }

    @Override
    public InputStream getFileSttream (String path) throws ApplicationStorageManagementException {
        String filePath = storagePath + path;
        try {
            return StorageManagementUtil.getInputStream(filePath);
        } catch (IOException e) {
            String msg = "Error occured when accessing the file in file path: " + filePath;
            throw new ApplicationStorageManagementException(msg, e);
        }
    }

    /***
     * This method is responsible to  delete artifact file which is located in the artifact path.
     *
     * @param artifactPath relative path of the artifact file
     * @throws ApplicationStorageManagementException when the file couldn't find in the given artifact path or if an
     * IO error occured while deleting the artifact.
     */
    private void deleteAppReleaseArtifact(String artifactPath) throws ApplicationStorageManagementException {
        File artifact = new File(artifactPath);
        if (artifact.exists()) {
            try {
                StorageManagementUtil.delete(artifact);
            } catch (IOException e) {
                throw new ApplicationStorageManagementException(
                        "Error occured while deleting application release artifacts", e);
            }
        } else {
            String msg = "Tried to delete application release, but it doesn't exist in the file system";
            log.error(msg);
            throw new ApplicationStorageManagementException(msg);
        }
    }
}

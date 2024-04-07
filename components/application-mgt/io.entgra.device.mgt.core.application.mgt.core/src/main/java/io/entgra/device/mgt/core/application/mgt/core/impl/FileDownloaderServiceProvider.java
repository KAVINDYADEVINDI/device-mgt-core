/*
 *  Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.entgra.device.mgt.core.application.mgt.core.impl;

import io.entgra.device.mgt.core.application.mgt.common.FileDescriptor;
import io.entgra.device.mgt.core.application.mgt.common.FileMetaEntry;
import io.entgra.device.mgt.core.application.mgt.common.TransferLink;
import io.entgra.device.mgt.core.application.mgt.common.exception.FileDownloaderServiceException;
import io.entgra.device.mgt.core.application.mgt.common.exception.FileTransferServiceException;
import io.entgra.device.mgt.core.application.mgt.common.services.FileDownloaderService;
import io.entgra.device.mgt.core.application.mgt.common.services.FileTransferService;
import io.entgra.device.mgt.core.application.mgt.core.internal.DataHolder;
import io.entgra.device.mgt.core.application.mgt.core.util.Constants;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class FileDownloaderServiceProvider {
    private static final Log log = LogFactory.getLog(FileDownloaderServiceProvider.class);
    private static final FileTransferService fileTransferService = DataHolder.getInstance().getFileTransferService();
    private static final LocalFileDownloaderService localFileDownloaderService = new LocalFileDownloaderService();
    private static final RemoteFileDownloaderService remoteFileDownloaderService = new RemoteFileDownloaderService();
    public static FileDownloaderService getFileDownloaderService(URL downloadUrl) throws FileDownloaderServiceException {
        try {
            if (fileTransferService.isExistsOnLocal(downloadUrl)) {
                return localFileDownloaderService;
            }
            return remoteFileDownloaderService;
        } catch (FileTransferServiceException e) {
            String msg = "Error encountered while acquiring file downloader service";
            log.error(msg, e);
            throw new FileDownloaderServiceException(msg, e);
        }
    }

    /**
     * Class holing the implementation of the local file downloading service
     */
    private static class LocalFileDownloaderService implements FileDownloaderService {
        @Override
        public FileDescriptor download(URL downloadUrl) throws FileDownloaderServiceException {
            try {
                return fileTransferService.resolve(downloadUrl);
            } catch (FileTransferServiceException e) {
                String msg = "Error encountered while downloading file pointing by " + downloadUrl;
                log.error(msg, e);
                throw new FileDownloaderServiceException(msg, e);
            }
        }
    }

    /**
     * Class holing the implementation of the remote file downloading service
     */
    private static class RemoteFileDownloaderService implements FileDownloaderService {
        private static final OkHttpClient okhttpClient =
                new OkHttpClient.Builder().connectTimeout(500, TimeUnit.MILLISECONDS).build();
        @Override
        public FileDescriptor download(URL downloadUrl) throws FileDownloaderServiceException {
            FileMetaEntry fileMetaEntry = getFileMetaEntry(downloadUrl);
            try {
                TransferLink transferLink = fileTransferService.generateUploadLink(fileMetaEntry);
                FileDescriptor fileDescriptor = fileTransferService.resolve(new URL(transferLink.getDirectTransferLink()
                        + "/" + fileMetaEntry.getFileName() + "." + fileMetaEntry.getExtension()));
                FileUtils.copyURLToFile(downloadUrl, new File(fileDescriptor.getAbsolutePath()),
                        15000, 3600000);
                return fileDescriptor;
            } catch (FileTransferServiceException | IOException e) {
                String msg = "Error encountered while downloading file";
                log.error(msg, e);
                throw new FileDownloaderServiceException(msg, e);
            }
        }

        /**
         * Generate the {@link FileMetaEntry} from the remote file
         * @param downloadUrl Remote file URL
         * @return {@link FileMetaEntry}
         * @throws FileDownloaderServiceException Throws when error encountered while generating {@link FileMetaEntry}
         */
        private FileMetaEntry getFileMetaEntry(URL downloadUrl) throws FileDownloaderServiceException {
            Request request = new Request.Builder().url(downloadUrl).head().build();
            try (Response response = okhttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new FileDownloaderServiceException("Unexpected response code received for the remote url " + downloadUrl);
                }
                String contentDisposition = response.header("Content-Disposition");
                String contentType = response.header("Content-Type");
                String[] fileNameSegments = extractFileNameSegmentsFromUrl(downloadUrl);

                // if the url parsing failed to resolve the file name segments
                // falling to remote file name segment resolving
                if (fileNameSegments == null) {
                    fileNameSegments = getFileNameSegments(contentDisposition, contentType);
                }

                FileMetaEntry fileMetaEntry = new FileMetaEntry();
                fileMetaEntry.setSize(Long.parseLong(Objects.requireNonNull(response.header("Content-Length"))));
                fileMetaEntry.setFileName(fileNameSegments[0]);
                fileMetaEntry.setExtension(fileNameSegments[1]);
                return fileMetaEntry;
            } catch (IOException e) {
                throw new FileDownloaderServiceException("IO error occurred while constructing file name for the remote url " + downloadUrl);
            }
        }

        /**
         * Extracting file name segments by parsing the URL
         * @param url Remote URL to extract file name segments
         * @return Array containing file name segments or null when failed to extract
         */
        public static String[] extractFileNameSegmentsFromUrl(URL url) {
            if (url == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Null received as the remote URL");
                }
                return null;
            }

            String []urlSegments = url.toString().split("/");
            if (urlSegments.length < 1) {
                if (log.isDebugEnabled()) {
                    log.debug("Cannot determine the file name for the remote file");
                }
                return null;
            }

            String fullQualifiedName = urlSegments[urlSegments.length - 1];
            String []fileNameSegments = fullQualifiedName.split("\\.(?=[^.]+$)");
            if (fileNameSegments.length != 2) {
                if (log.isDebugEnabled()) {
                    log.debug("Error encountered when constructing file name");
                }
                return null;
            }
            return fileNameSegments;
        }

        /**
         * Extract file name segments(filename & extensions) from content disposition header and content type header
         * @param contentDisposition Content disposition header value
         * @param contentType Content type header value
         * @return Array of name segments
         * @throws FileDownloaderServiceException Throws when error occurred while extracting name segments
         */
        private static String[] getFileNameSegments(String contentDisposition, String contentType) throws FileDownloaderServiceException {
            if (contentDisposition == null && contentType == null) {
                throw new FileDownloaderServiceException("Cannot determine the file name for the remote file");
            }

            if (contentDisposition == null) {
                String extension;
                if (contentType.equals(Constants.MIME_TYPE_VND_ANDROID_PACKAGE_ARCHIVE)) {
                    extension = Constants.EXTENSION_APK;
                } else if (contentType.equals(Constants.MIME_TYPE_OCTET_STREAM)) {
                    extension = Constants.EXTENSION_IPA;
                } else if (contentType.equals(Constants.MIME_TYPE_VND_APPX)) {
                    extension = Constants.EXTENSION_APPX;
                } else if (contentType.equals(Constants.MIME_TYPE_X_MS_INSTALLER)
                        || contentType.equals(Constants.MIME_TYPE_VND_MS_WINDOWS_MSI)) {
                    extension = Constants.EXTENSION_MSI;
                } else {
                    String []contentTypeSegments = contentType.split("/");
                    if (contentTypeSegments.length != 2) {
                        throw new FileDownloaderServiceException("Encountered wrong content type header value");
                    }
                    extension = contentTypeSegments[contentTypeSegments.length - 1];
                }
                return new String[]{ UUID.randomUUID().toString(), extension};
            }

            String []contentDispositionSegments = contentDisposition.split("=");
            if (contentDispositionSegments.length != 2) {
                throw new FileDownloaderServiceException("Error encountered when constructing file name");
            }
            String fullQualifiedName = contentDispositionSegments[contentDispositionSegments.length - 1].replace("\"", "");
            String []fileNameSegments = fullQualifiedName.split("\\.(?=[^.]+$)");
            if (fileNameSegments.length != 2) {
                throw new FileDownloaderServiceException("Error encountered when constructing file name");
            }
            return fileNameSegments;
        }
    }
}

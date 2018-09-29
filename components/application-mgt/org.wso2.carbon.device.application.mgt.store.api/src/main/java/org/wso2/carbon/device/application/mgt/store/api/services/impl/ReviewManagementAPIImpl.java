/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.application.mgt.store.api.services.impl;

import io.swagger.annotations.ApiParam;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.common.Application;
import org.wso2.carbon.device.application.mgt.common.PaginationResult;
import org.wso2.carbon.device.application.mgt.common.Rating;
import org.wso2.carbon.device.application.mgt.common.Review;
import org.wso2.carbon.device.application.mgt.common.exception.RequestValidatingException;
import org.wso2.carbon.device.application.mgt.common.exception.ReviewDoesNotExistException;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationManager;
import org.wso2.carbon.device.application.mgt.common.services.ReviewManager;
import org.wso2.carbon.device.application.mgt.core.exception.NotFoundException;
import org.wso2.carbon.device.application.mgt.store.api.APIUtil;
import org.wso2.carbon.device.application.mgt.store.api.services.ReviewManagementAPI;
import org.wso2.carbon.device.application.mgt.common.PaginationRequest;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.ReviewManagementException;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.PUT;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import javax.ws.rs.core.Response;

/**
 * Review Management related jax-rs APIs.
 */
@Path("/review")
public class ReviewManagementAPIImpl implements ReviewManagementAPI {

    private static Log log = LogFactory.getLog(ReviewManagementAPIImpl.class);

    @Override
    @GET
    @Path("/{uuid}")
    public Response getAllReviews(
            @PathParam("uuid") String uuid,
            @DefaultValue("0") @QueryParam("offset") int offSet,
            @DefaultValue("20") @QueryParam("limit") int limit) {
        ReviewManager reviewManager = APIUtil.getReviewManager();
        PaginationRequest request = new PaginationRequest(offSet, limit);
        try {
            PaginationResult paginationResult = reviewManager.getAllReviews(request, uuid);
            return Response.status(Response.Status.OK).entity(paginationResult).build();
        } catch (ReviewManagementException e) {
            String msg = "Error occurred while retrieving reviews for application UUID: " + uuid;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    @POST
    @Consumes("application/json")
    @Path("/{uuid}")
    public Response addReview(
            @ApiParam Review review,
            @PathParam("uuid") String uuid) {
        ReviewManager reviewManager = APIUtil.getReviewManager();
        try {
            boolean isReviewCreated = reviewManager.addReview(review, uuid);
            if (isReviewCreated) {
                return Response.status(Response.Status.CREATED).entity(review).build();
            } else {
                String msg = "Given review is not valid. Please check the review payload.";
                log.error(msg);
                return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
            }
        } catch (NotFoundException e) {
            String msg = "Couldn't find an application release for UUID: " + uuid;
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (ReviewManagementException e) {
            String msg = "Error occurred while creating the review";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (RequestValidatingException e) {
            String msg =
                    "Error occurred while adding for application release. UUID of the application release: " + uuid;
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occured while accessing application release for UUID: " + uuid;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    @PUT
    @Consumes("application/json")
    @Path("/{uuid}/{reviewId}")
    public Response updateReview(
            @ApiParam Review review,
            @PathParam("uuid") String uuid,
            @PathParam("reviewId") int reviewId) {
        ReviewManager reviewManager = APIUtil.getReviewManager();
        try {
            if (reviewManager.updateReview(review, reviewId, uuid, null)) {
                return Response.status(Response.Status.OK).entity(review).build();
            } else {
                String msg = "Review updating failed. Please contact the administrator";
                log.error(msg);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
            }
        } catch (ReviewManagementException e) {
            String msg = "Error occurred while retrieving comments.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (RequestValidatingException e) {
            String msg = "Error occurred while updating review. Review id: " + reviewId;
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }
    }

    @Override
    @DELETE
    @Path("/{uuid}/{reviewId}")
    public Response deleteReview(
            @PathParam("uuid") String uuid,
            @PathParam("reviewId") int reviewId) {

        ReviewManager reviewManager = APIUtil.getReviewManager();
        try {
            if (reviewManager.deleteReview(uuid, reviewId)) {
                return Response.status(Response.Status.OK).entity("Review is deleted successfully.").build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Review deleting is failed.")
                        .build();
            }
        } catch (ReviewManagementException e) {
            String msg = "Error occurred while deleting the comment.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (ReviewDoesNotExistException e) {
            String msg = "Couldn't find a review for review-id: " + reviewId + " to delete.";
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        }
    }

    @Override
    @GET
    @Path("/{uuid}/rating")
    public Response getRating(
            @PathParam("uuid") String uuid) {
        ReviewManager reviewManager = APIUtil.getReviewManager();
        Rating rating;
        try {
            rating = reviewManager.getRating(uuid);
        } catch (ReviewManagementException e) {
            log.error("Review Management Exception occurs", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.status(Response.Status.OK).entity(rating).build();
    }

}
/*
 *  Copyright (c) 2022 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *
 */

package org.eclipse.dataspaceconnector.registration.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import org.eclipse.dataspaceconnector.registration.authority.model.Participant;
import org.eclipse.dataspaceconnector.registration.model.ParticipantDto;

import java.util.List;
import java.util.Objects;

import static org.eclipse.dataspaceconnector.registration.auth.DidJwtAuthenticationFilter.CALLER_DID_HEADER;


/**
 * Registration Service API controller to manage dataspace participants.
 */
@Tag(name = "Registry")
@Produces({"application/json"})
@Consumes({"application/json"})
@Path("/registry")
public class RegistrationApiController {

    /**
     * An IDS URL (this will be removed in https://github.com/agera-edc/MinimumViableDataspace/issues/174)
     */
    private static final String TEMPORARY_IDS_URL_HEADER = "IdsUrl";

    private final RegistrationService service;

    /**
     * Constructs an instance of {@link RegistrationApiController}
     *
     * @param service service handling the registration service logic.
     */
    public RegistrationApiController(RegistrationService service) {
        this.service = service;
    }

    @GET
    @Path("/participant")
    @Operation(description = "Get a participant by caller DID.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Dataspace participant.",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ParticipantDto.class)
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Dataspace participant not found."
            )
    })
    public ParticipantDto getParticipant(@Context HttpHeaders headers) {
        var issuer = Objects.requireNonNull(headers.getHeaderString(CALLER_DID_HEADER));

        return service.findByDid(issuer);
    }

    @Path("/participants")
    @GET
    @Operation(description = "Gets all dataspace participants.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Dataspace participants.",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = ParticipantDto.class))
                            )
                    }
            )
    })
    public List<ParticipantDto> listParticipants() {
        return service.listParticipants();
    }

    @Path("/participant")
    @Operation(description = "Asynchronously request to add a dataspace participant.")
    @ApiResponse(responseCode = "204", description = "No content")
    @POST
    public Response addParticipant(
            @HeaderParam(TEMPORARY_IDS_URL_HEADER) String idsUrl,
            @Context HttpHeaders headers) {
        var issuer = Objects.requireNonNull(headers.getHeaderString(CALLER_DID_HEADER));

        var result = service.addParticipant(issuer, idsUrl);
        return result.succeeded() ? Response.ok().build() : Response.status(Response.Status.BAD_REQUEST).entity(result.getFailureDetail()).build();
    }
}

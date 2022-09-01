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

package org.eclipse.dataspaceconnector.registration.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.dataspaceconnector.registration.cli.RegistrationServiceCli;
import org.eclipse.dataspaceconnector.registration.client.models.ParticipantDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpStatusCode;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.dataspaceconnector.junit.testfixtures.TestUtils.getFreePort;
import static org.eclipse.dataspaceconnector.registration.client.RegistrationServiceTestUtils.DATASPACE_DID_WEB2;
import static org.eclipse.dataspaceconnector.registration.client.RegistrationServiceTestUtils.addEnrollmentCredential;
import static org.eclipse.dataspaceconnector.registration.client.RegistrationServiceTestUtils.createDid;
import static org.eclipse.dataspaceconnector.registration.client.RegistrationServiceTestUtils.didDocument;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.stop.Stop.stopQuietly;

@IntegrationTest
class RegistrationApiCommandLineClientTest {

    static final ObjectMapper MAPPER = new ObjectMapper();
    static Path privateKeyFile;

    int apiPort;
    String did;
    ClientAndServer httpSourceClientAndServer;

    @BeforeEach
    void setUpClass() throws Exception {
        privateKeyFile = Files.createTempFile("test", ".pem");
        privateKeyFile.toFile().deleteOnExit();
        Files.writeString(privateKeyFile, TestKeyData.PRIVATE_KEY_P256);
        apiPort = getFreePort();
        did = createDid(apiPort);
        httpSourceClientAndServer = startClientAndServer(apiPort);
        httpSourceClientAndServer.when(request().withPath("/.well-known/did.json"))
                .respond(response()
                        .withBody(didDocument(did))
                        .withStatusCode(HttpStatusCode.OK_200.code()));
    }

    @AfterEach
    void tearDown() {
        stopQuietly(httpSourceClientAndServer);
    }

    @Test
    void listParticipants() throws Exception {

        assertThat(listParticipantCmd(did)).noneSatisfy(p -> assertThat(p.getDid()).isEqualTo(did));

        addEnrollmentCredential(did);

        addParticipantCmd(did);

        Thread.sleep(20000);

        assertThat(listParticipantCmd(did)).anySatisfy(p -> assertThat(p.getDid()).isEqualTo(did));
    }

    @Test
    void getParticipant() throws Exception {

        addParticipantCmd(did);

        var result = getParticipantCmd(did);

        assertThat(result.getDid()).isEqualTo(did);
        assertThat(result.getStatus()).isNotNull();
    }

    @Test
    void getParticipant_notFound() {
        CommandLine cmd = RegistrationServiceCli.getCommandLine();
        var writer = new StringWriter();
        cmd.setOut(new PrintWriter(writer));

        var statusCmdExitCode = cmd.execute(
                "-c", did,
                "-d", DATASPACE_DID_WEB2,
                "-k", privateKeyFile.toString(),
                "--http-scheme",
                "participants", "get");

        assertThat(statusCmdExitCode).isEqualTo(1);
        var output = writer.toString();
        assertThat(output).isEmpty();
    }

    private String executeCmd(List<String> cmdArgs) {
        CommandLine cmd = RegistrationServiceCli.getCommandLine();
        var writer = new StringWriter();
        cmd.setOut(new PrintWriter(writer));

        var cmdExitCode = cmd.execute(cmdArgs.toArray(new String[0]));
        var output = writer.toString();

        assertThat(cmdExitCode).isEqualTo(0);

        return output;
    }

    private List<String> commonCmdParams(String didWeb) {

        return List.of(
                "-c", didWeb,
                "-d", DATASPACE_DID_WEB2,
                "-k", privateKeyFile.toString(),
                "--http-scheme",
                "participants"
        );
    }

    private ParticipantDto getParticipantCmd(String didWeb) throws JsonProcessingException {
        var getParticipantArgs = new ArrayList<>(commonCmdParams(didWeb));
        getParticipantArgs.add("get");
        var output = executeCmd(getParticipantArgs);

        return MAPPER.readValue(output, ParticipantDto.class);
    }

    private void addParticipantCmd(String clientDidWeb) {
        var getParticipantArgs = new ArrayList<>(commonCmdParams(clientDidWeb));
        getParticipantArgs.add("add");
        executeCmd(getParticipantArgs);
    }

    private List<ParticipantDto> listParticipantCmd(String didWeb) throws JsonProcessingException {
        var getParticipantArgs = new ArrayList<>(commonCmdParams(didWeb));
        getParticipantArgs.add("list");
        var output = executeCmd(getParticipantArgs);

        return MAPPER.readValue(output, new TypeReference<>() {
        });
    }
}

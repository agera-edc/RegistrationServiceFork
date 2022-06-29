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

import com.github.javafaker.Faker;
import com.nimbusds.jose.jwk.ECKey;
import org.eclipse.dataspaceconnector.iam.did.crypto.key.EcPrivateKeyWrapper;
import org.eclipse.dataspaceconnector.registration.client.api.RegistryApi;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.dataspaceconnector.registration.client.TestUtils.DID_WEB;
import static org.eclipse.dataspaceconnector.registration.client.TestUtils.PRIVATE_KEY_FILE;

@IntegrationTest
public class RegistrationApiClientTest {
    static final String API_URL = "http://localhost:8182/authority";

    static final Faker FAKER = new Faker();
    String participant = FAKER.internet().url();

    @Test
    void listParticipants() throws Exception {
        var privateKey = Path.of(PRIVATE_KEY_FILE);
        var ecKey = (ECKey) ECKey.parseFromPEMEncodedObjects(Files.readString(privateKey));
        var privateKeyWrapper = new EcPrivateKeyWrapper(ecKey);

        var apiClient = ApiClientFactory.createApiClient(API_URL, DID_WEB, privateKeyWrapper);
        var api = new RegistryApi(apiClient);

        assertThat(api.listParticipants())
                .noneSatisfy(p -> participant.equals(p.getUrl()));

        api.addParticipant(participant);

        assertThat(api.listParticipants())
                .anySatisfy(p -> participant.equals(p.getUrl()));
    }
}

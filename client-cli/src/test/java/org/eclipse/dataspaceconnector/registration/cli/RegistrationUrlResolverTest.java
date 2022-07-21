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

package org.eclipse.dataspaceconnector.registration.cli;

import com.github.javafaker.Faker;
import org.eclipse.dataspaceconnector.iam.did.spi.document.DidDocument;
import org.eclipse.dataspaceconnector.iam.did.spi.document.Service;
import org.eclipse.dataspaceconnector.iam.did.spi.resolution.DidResolver;
import org.eclipse.dataspaceconnector.iam.did.web.resolution.WebDidResolver;
import org.eclipse.dataspaceconnector.spi.result.Result;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RegistrationUrlResolverTest {

    private static final String REGISTRATION_URL_TYPE = "RegistrationUrl";
    private static final Faker FAKER = new Faker();

    DidResolver didResolver = mock(WebDidResolver.class);
    RegistrationUrlResolver urlResolver = new RegistrationUrlResolver(didResolver);

    String did = "did:web:" + FAKER.internet().domainName();
    String apiUrl = "http://registrationUrl/api";

    @Test
    void resolveUrl_success() {

        DidDocument didDocument = didDocument(List.of(new Service("some-id", REGISTRATION_URL_TYPE, apiUrl), new Service("some-id", "some-other-type", apiUrl)));
        when(didResolver.resolve(did)).thenReturn(Result.success(didDocument));

        Optional<String> resultApiUrl = urlResolver.resolveUrl(did);

        assertThat(resultApiUrl).isNotEmpty();
        assertThat(resultApiUrl.get()).isEqualTo(apiUrl);

    }

    @Test
    void resolveUrl_noRegistrationUrlType() {

        DidDocument didDocument = didDocument(List.of(new Service("some-id", "some-other-type", apiUrl)));
        when(didResolver.resolve(did)).thenReturn(Result.success(didDocument));

        Optional<String> resultApiUrl = urlResolver.resolveUrl(did);

        assertThat(resultApiUrl).isEmpty();
    }

    @Test
    void resolveUrl_noRegistrationUrl() {

        DidDocument didDocument = didDocument(List.of());
        when(didResolver.resolve(did)).thenReturn(Result.success(didDocument));

        Optional<String> resultApiUrl = urlResolver.resolveUrl(did);

        assertThat(resultApiUrl).isEmpty();

    }

    @Test
    void resolveUrl_failureToGetDid() {

        when(didResolver.resolve(did)).thenReturn(Result.failure("Failure"));

        assertThatThrownBy(() -> urlResolver.resolveUrl(did)).isInstanceOf(CliException.class);
    }

    private DidDocument didDocument(List<Service> services) {
        return DidDocument.Builder.newInstance().service(services).build();
    }

}
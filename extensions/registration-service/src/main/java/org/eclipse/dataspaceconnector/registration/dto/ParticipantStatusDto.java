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

package org.eclipse.dataspaceconnector.registration.dto;

/**
 * Participant onboarding status. Only expose a handful of statuses to avoid leaking internal model state.
 */
public enum ParticipantStatusDto {
    AUTHORIZING, // verifying participants credentials
    AUTHORIZED, // participant is fully onboarded
    DENIED, // participant onboarding request denied
}

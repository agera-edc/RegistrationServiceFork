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

plugins {
    `java-library`
    id("application")
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

val edcVersion: String by project
val edcGroup: String by project
val identityHubVersion: String by project
val identityHubGroup: String by project

dependencies {
    implementation(project(":extensions:registration-service"))
    implementation(project(":extensions:participant-verifier"))
    implementation(project(":extensions:registration-policy-gaiax-member"))
    implementation("${edcGroup}:identity-did-web:${edcVersion}")
    implementation("${edcGroup}:identity-did-core:${edcVersion}")
    implementation("${edcGroup}:core:${edcVersion}")
    implementation("${edcGroup}:observability-api:${edcVersion}")
    implementation("${edcGroup}:core-micrometer:${edcVersion}")
    runtimeOnly("${edcGroup}:jetty-micrometer:${edcVersion}")
    runtimeOnly("${edcGroup}:jersey-micrometer:${edcVersion}")
    implementation("${edcGroup}:filesystem-configuration:${edcVersion}")
    implementation("${identityHubGroup}:identity-hub-credentials-verifier:${identityHubVersion}")

    // JDK Logger
    implementation("${edcGroup}:jdk-logger-monitor:${edcVersion}")

    // To use FileSystem vault e.g. -DuseFsVault="true".Only for non-production usages.
    val useFsVault: Boolean = System.getProperty("useFsVault", "false").toBoolean()
    if (useFsVault) {
        implementation("${edcGroup}:filesystem-vault:${edcVersion}")
    } else {
        implementation("${edcGroup}:azure-vault:${edcVersion}")
    }
}

application {
    mainClass.set("org.eclipse.dataspaceconnector.boot.system.runtime.BaseRuntime")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    mergeServiceFiles()
    archiveFileName.set("app.jar")
}

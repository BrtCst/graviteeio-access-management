/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.am.gateway.handler.scim.model;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public enum ScimType {

    INVALID_FILTER("invalidFilter"),
    TOO_MANY("tooMany"),
    UNIQUENESS("uniqueness"),
    MUTABILITY("mutability"),
    INVALID_SYNTAX("invalidSyntax"),
    INVALID_PATH("invalidPath"),
    NO_TARGET("noTarget"),
    INVALID_VALUE("invalidValue"),
    INVALID_VERS("invalidVers"),
    SENSITIVE("sensitive");

    private final String value;

    ScimType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}

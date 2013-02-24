/**
 *  Copyright 2012 Rackspace
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.rackspacecloud.client.service_registry.exceptions;

public class ValidationException extends APIException {
    private final Integer code;
    private final String type;
    private final String details;
    private final String txnId;

    public ValidationException(Integer code, String type, String message, String details, String txnId) {
        super(message);
        this.code = code;
        this.type = type;
        this.details = details;
        this.txnId = txnId;
    }

    public Integer getCode() {
        return code;
    }

    public String getType() {
        return type;
    }

    public String getDetails() {
        return details;
    }

    public String getTxnId() {
        return txnId;
    }

    @Override
    public String toString() {
        return String.format("[ValidationException code=%s, type=%s, " +
                             "details=\"%s\" txnId=%s]", this.code, this.type,
                             this.details, this.txnId);
    }
}

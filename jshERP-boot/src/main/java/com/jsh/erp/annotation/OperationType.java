package com.jsh.erp.annotation;

import com.jsh.erp.constants.BusinessConstants;

public enum OperationType {
    ADD(BusinessConstants.LOG_OPERATION_TYPE_ADD),
    BATCH_ADD(BusinessConstants.LOG_OPERATION_TYPE_BATCH_ADD),
    EDIT(BusinessConstants.LOG_OPERATION_TYPE_EDIT),
    DELETE(BusinessConstants.LOG_OPERATION_TYPE_DELETE),
    LOGIN(BusinessConstants.LOG_OPERATION_TYPE_LOGIN),
    IMPORT(BusinessConstants.LOG_OPERATION_TYPE_IMPORT),
    ENABLED(BusinessConstants.LOG_OPERATION_TYPE_ENABLED);

    private final String value;

    OperationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
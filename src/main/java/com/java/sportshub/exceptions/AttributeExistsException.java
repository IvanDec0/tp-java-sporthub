package com.java.sportshub.exceptions;

public class AttributeExistsException extends RuntimeException {

    public AttributeExistsException(String message) {
        super(message);
    }

    public AttributeExistsException(String resourceName, String attributeName, String attributeValue) {
        super(String.format("%s already exists with the %s: '%s'", resourceName, attributeName, attributeValue));
    }
}
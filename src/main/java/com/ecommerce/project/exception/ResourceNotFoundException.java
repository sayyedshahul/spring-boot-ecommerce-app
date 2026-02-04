package com.ecommerce.project.exception;

public class ResourceNotFoundException extends RuntimeException{
    private String resourceName;
    private String field;
    private long fieldId;

    public ResourceNotFoundException(String resourceName, String field, long fieldId){
        super(String.format("%s not found with %s: %d", resourceName, field, fieldId));
        this.resourceName = resourceName;
        this.fieldId = fieldId;
        this.field = field;
    }
}

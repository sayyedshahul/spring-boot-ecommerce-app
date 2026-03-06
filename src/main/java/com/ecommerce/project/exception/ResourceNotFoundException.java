package com.ecommerce.project.exception;

public class ResourceNotFoundException extends RuntimeException{
    private String resourceName;
    private String field;
    private Long fieldId;
    private String fieldValue;

    public ResourceNotFoundException(String resourceName, String field, long fieldId){
        super(String.format("%s not found with %s: %d", resourceName, field, fieldId));
        this.resourceName = resourceName;
        this.fieldId = fieldId;
        this.field = field;
    }

    public ResourceNotFoundException(String resourceName, String field, String fieldValue){
        super(String.format("%s not found with %s: %s", resourceName, field, fieldValue));
        this.resourceName = resourceName;
        this.fieldValue = fieldValue;
        this.field = field;
    }
}

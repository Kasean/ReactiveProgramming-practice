package org.kasean.exceptions;

public class SportAlreadyExistsException extends RuntimeException {
    public SportAlreadyExistsException(String name) {
        super("Sport with name '" + name + "' already exists");
    }
}

/*
 *
 */
package com.bodastage.boda_bulkcmparser;

/**
 *
 * @author info@bodastage.com
 */
public final class ParserStates {
    
    /**
     * Managed Object parameters extraction stage.
     */
    public static final int EXTRACTING_PARAMETERS = 1;
    
    /**
     * Parameter value extraction stage
     */
    public static final int EXTRACTING_VALUES = 2;
    
    /**
     * Parsing completed
     */
    public static final int EXTRACTING_DONE = 3;
}

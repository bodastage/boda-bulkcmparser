/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.com.bodastage.boda_bulkcmparser.bulkcmxml;

import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @version 1.0.0
 * @author Bodastage<info@bodastage.com>
 */
class VsDataSomeMO {
    private String parameter1 = "value1";
    private String parameter2 = "value2";
    private String parameter3 = "value3";

    public String getParameter1() {
        return parameter1;
    }

    @XmlElement(name = "parameter1", namespace = "http://BodastageSpecificAttributes#BodastageSpecificAttributes")
    public void setParameter1(String parameter1) {
        this.parameter1 = parameter1;
    }

    public String getParameter2() {
        return parameter2;
    }

    @XmlElement(name = "parameter2", namespace = "http://BodastageSpecificAttributes#BodastageSpecificAttributes")
    public void setParameter2(String parameter2) {
        this.parameter2 = parameter2;
    }

    public String getParameter3() {
        return parameter3;
    }

    @XmlElement(name = "parameter3", namespace = "http://BodastageSpecificAttributes#BodastageSpecificAttributes")
    public void setParameter3(String parameter3) {
        this.parameter3 = parameter3;
    }
    
}

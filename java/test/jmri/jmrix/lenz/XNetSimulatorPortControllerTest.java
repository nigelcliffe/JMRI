package jmri.jmrix.lenz;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the XNetSimulatorPortController class
 * <p>
 *
 * @author      Paul Bender Copyright (C) 2016
 */
public class XNetSimulatorPortControllerTest extends jmri.jmrix.AbstractSerialPortControllerTest {

    @Override
    @Before
    public void setUp(){
       apps.tests.Log4JFixture.setUp();
       JUnitUtil.resetInstanceManager();
       XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());
       XNetSystemConnectionMemo memo = new XNetSystemConnectionMemo(tc);
       apc = new XNetSimulatorPortController(){
            @Override
            public boolean status(){
              return true;
            }
            @Override
            public void configure(){
            }
            @Override
            public java.io.DataInputStream getInputStream(){
                return null;
            }
            @Override
            public java.io.DataOutputStream getOutputStream(){
                return null;
            }

            /**
             * Get an array of valid baud rates; used to display valid options.
             */
            public String[] validBaudRates(){
               String[] retval = {"9600"};
               return retval;
            }
            /**
             * Open a specified port. The appname argument is to be provided to the
             * underlying OS during startup so that it can show on status displays, etc
             */
            @Override
            public String openPort(String portName, String appName){
               return "";
            }

            @Override
            public void setOutputBufferEmpty(boolean s){
            }
            
            @Override
            public boolean okToSend(){
                  return true;
            }

       };
    }

    @Override
    @After
    public void tearDown(){
       JUnitUtil.resetInstanceManager();
       apps.tests.Log4JFixture.tearDown();
    }

}

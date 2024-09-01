package com.example.ssh;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SshSessionFactoryTest {

    private SshSessionFactory factory;
    private Session mockSession;

    @BeforeEach
    public void setUp() throws Exception {
        // Mock JSch and Session
        JSch mockJSch = mock(JSch.class);
        mockSession = mock(Session.class);

        List<String> hosts = Arrays.asList("host1", "host2");
        factory = new SshSessionFactory(hosts, 22, "username", "password", mockJSch);



        // Inject mock JSch instance
        Mockito.doReturn(mockSession).when(mockJSch).getSession(anyString(), anyString(), anyInt());
    }

    //@Test
    public void testMakeObject_Success() throws Exception {
        // Arrange
        when(mockSession.isConnected()).thenReturn(true);
        when(factory.makeObject()).thenReturn(new DefaultPooledObject<>(mockSession));

        // Act
        PooledObject<Session> pooledObject = factory.makeObject();

        // Assert
        assertNotNull(pooledObject);
        assertTrue(pooledObject.getObject().isConnected());
        verify(mockSession).connect();
    }

    @Test
    public void testMakeObject_Failure() throws Exception {
        // Arrange
        doThrow(new JSchException("Connection failed")).when(mockSession).connect();

        // Act
        assertThrows(Exception.class, () -> {
            factory.makeObject();
        });
    }

    @Test
    public void testDestroyObject() throws Exception {
        // Arrange
        when(mockSession.isConnected()).thenReturn(true);
        PooledObject<Session> pooledObject = new DefaultPooledObject<>(mockSession);

        // Act
        factory.destroyObject(pooledObject);

        // Assert
        verify(mockSession).disconnect();
    }

    @Test
    public void testValidateObject() throws Exception {
        // Arrange
        when(mockSession.isConnected()).thenReturn(true);
        PooledObject<Session> pooledObject = new DefaultPooledObject<>(mockSession);

        // Act
        boolean isValid = factory.validateObject(pooledObject);

        // Assert
        assertTrue(isValid);
    }

    @Test
    public void testValidateObject_Invalid() throws Exception {
        // Arrange
        when(mockSession.isConnected()).thenReturn(false);
        PooledObject<Session> pooledObject = new DefaultPooledObject<>(mockSession);

        // Act
        boolean isValid = factory.validateObject(pooledObject);

        // Assert
        assertFalse(isValid);
    }
}

package com.example.ssh.pool;

import com.example.ssh.config.SshClientConfigProperties;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SshClientFactoryTest {

    @Mock
    private Session mockSession;

    @Mock
    private JSch mockJSch;

    private SshClientFactory factory;

    @BeforeEach
    public void setUp() throws Exception {
        factory = new SshClientFactory(
                List.of(new SshClientConfigProperties(
                        "Server",
                        "host1",
                        22,
                        "user",
                        "password",
                        "",
                        null)),
                mockJSch);
    }

    @Test
    public void testMakeObjectOnSuccess() throws Exception {
        // Arrange
        // Inject mock JSch instance
        Mockito.doReturn(mockSession).when(mockJSch).getSession(anyString(), anyString(), anyInt());
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
    public void testMakeObjectOnFailure() throws Exception {
        // Inject mock JSch instance
        Mockito.doReturn(mockSession).when(mockJSch).getSession(anyString(), anyString(), anyInt());

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
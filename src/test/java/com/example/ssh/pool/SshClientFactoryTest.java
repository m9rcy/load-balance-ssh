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
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;

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

    @Mock
    private RetryTemplate mockTemplate;

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
                mockTemplate,
                mockJSch);
    }

    @Test
    public void testMakeObjectOnSuccess() throws Exception {

        when(mockTemplate.execute(any())).thenAnswer(invocation -> {
            RetryCallback<Session, Exception> retryCallback = invocation.getArgument(0);
            return retryCallback.doWithRetry(null);
        });

        // Arrange
        // Inject mock JSch instance
        Mockito.doReturn(mockSession).when(mockJSch).getSession(anyString(), anyString(), anyInt());
        when(mockSession.isConnected()).thenReturn(true);

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


    @Test
    public void testRoundRobinSelection() {

        List<SshClientConfigProperties> sshClientProps = List.of(
                new SshClientConfigProperties("Server1", "host1", 22, "user1", "password1", "", null),
                new SshClientConfigProperties("Server2", "host2", 22, "user2", "password2", "", null),
                new SshClientConfigProperties("Server3", "host3", 22, "user3", "password3", "", null)
        );

        factory = new SshClientFactory(sshClientProps);

        // Check that the first call to getSshClient() returns the first configuration
        SshClientConfigProperties client1 = factory.getSshClient();
        assertEquals(sshClientProps.get(0), client1, "Expected first client configuration");

        // Check that the second call returns the second configuration
        SshClientConfigProperties client2 = factory.getSshClient();
        assertEquals(sshClientProps.get(1), client2, "Expected second client configuration");

        // Check that the third call returns the third configuration
        SshClientConfigProperties client3 = factory.getSshClient();
        assertEquals(sshClientProps.get(2), client3, "Expected third client configuration");

        // Check that the fourth call wraps around and returns the first configuration again
        SshClientConfigProperties client4 = factory.getSshClient();
        assertEquals(sshClientProps.get(0), client4, "Expected round-robin to wrap back to first client configuration");

        // Check that the fifth call returns the second configuration again
        SshClientConfigProperties client5 = factory.getSshClient();
        assertEquals(sshClientProps.get(1), client5, "Expected round-robin to return second client configuration again");
    }



}
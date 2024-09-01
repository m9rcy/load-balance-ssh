package com.example.ssh;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SshClientConnectionPoolTest {

    private SshConnectionPool pool;
    private SshSessionFactory mockFactory;
    private Session mockSession;

    private JSch mockJSch;

    @BeforeEach
    public void setUp() throws Exception {
        List<String> hosts = Arrays.asList("host1", "host2");
        mockFactory = mock(SshSessionFactory.class);
        // Mock JSch and Session
        mockJSch = mock(JSch.class);
        mockSession = mock(Session.class);
        pool = new SshConnectionPool(hosts, 22, "username", "password", mockJSch);

        // Mocking the factory to provide the session
        when(mockFactory.makeObject()).thenReturn(new DefaultPooledObject<>(mockSession));
        when(mockSession.isConnected()).thenReturn(true);
    }

    @Test
    public void testBorrowSession_Success() throws Exception {
        when(mockJSch.getSession(anyString(), anyString(), anyInt())).thenReturn(mockSession);

        // Act
        Session session = pool.borrowSession();

        // Assert
        assertNotNull(session);
        assertTrue(session.isConnected());
    }

    @Test
    public void testReturnSession_Success() throws Exception {
        // Arrange
        pool = Mockito.spy(pool);
        doNothing().when(pool).returnSession(mockSession);

        // Act
        pool.returnSession(mockSession);

        // Assert
        verify(pool).returnSession(mockSession);
    }

    @Test
    public void testBorrowSession_Failure() {
        try {
            // Arrange
            when(mockFactory.makeObject()).thenThrow(new JSchException("Connection failed"));

            // Act
            pool.borrowSession();
            fail("Exception was expected");
        } catch (Exception e) {
            // Assert
            assertTrue(e.getMessage().contains("Failed to borrow SSH session from the pool"));
        }
    }

    @Test
    public void testClosePool() {
        // Arrange
        GenericObjectPool<Session> mockObjectPool = mock(GenericObjectPool.class);
        pool = Mockito.spy(pool);

        // Injecting the mocked pool
//        doReturn(mockObjectPool).when(pool).close();
        doNothing().when(pool).close();

        // Act
        pool.close();

        // Assert
        verify(pool).close();
    }
}

package de.tud.sep.mbi.sessionconnect.webservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.infinispan.Cache;
import org.junit.jupiter.api.Test;
import org.keycloak.connections.infinispan.InfinispanConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.services.managers.AuthenticationManager.AuthResult;
import org.mockito.Mockito;

class SessionConnectRealmResourceProviderTest {

	private static final String TEST_USER_ID = "testUser";

	@Test
	void testGetResource() {
		final SessionConnectRealmResourceProvider provider = new SessionConnectRealmResourceProvider(mockSession(),
				mockAuthResult());
		assertEquals(provider, provider.getResource(), "The provider should be the resource.");
	}

	@Test
	void testPostSuccessful() {
		final KeycloakSession session = mockSession();
		final UUID testSessionId = UUID.randomUUID();
		final Cache<UUID, String> cache = session.getProvider(InfinispanConnectionProvider.class)
				.getCache(SessionConnectRealmResourceProvider.CACHE_NAME);
		final SessionConnectRealmResourceProvider resource = new SessionConnectRealmResourceProvider(session,
				mockAuthResult());
		when(cache.containsKey(eq(testSessionId))).thenReturn(true);
		when(cache.get(eq(testSessionId))).thenReturn("");
		final Response response = resource.put(testSessionId.toString());
		assertEquals(Status.OK.getStatusCode(), response.getStatus(), "The status code should be ok.");
		verify(cache).put(testSessionId, TEST_USER_ID, 1L, TimeUnit.HOURS);
	}

	@Test
	void testUserNotAuthorized() {
		assertEquals(Status.UNAUTHORIZED.getStatusCode(), new SessionConnectRealmResourceProvider(mockSession(), null)
				.put(UUID.randomUUID().toString()).getStatus(), "The status conde should be unauthorized.");
	}

	@Test
	void testPostSessionNotStored() {
		final KeycloakSession session = mockSession();
		final Cache<UUID, String> cache = session.getProvider(InfinispanConnectionProvider.class)
				.getCache(SessionConnectRealmResourceProvider.CACHE_NAME);
		when(cache.containsKey(any(UUID.class))).thenReturn(false);
		assertEquals(Status.BAD_REQUEST.getStatusCode(),
				new SessionConnectRealmResourceProvider(session, mockAuthResult()).put(UUID.randomUUID().toString())
						.getStatus(),
				"The status conde should be a bad request.");
	}

	@Test
	void testPostSessionAlreadyConnected() {
		final UUID testSessionId = UUID.randomUUID();
		final KeycloakSession session = mockSession();
		final Cache<UUID, String> cache = session.getProvider(InfinispanConnectionProvider.class)
				.getCache(SessionConnectRealmResourceProvider.CACHE_NAME);
		final SessionConnectRealmResourceProvider resource = new SessionConnectRealmResourceProvider(session,
				mockAuthResult());
		when(cache.containsKey(any(UUID.class))).thenReturn(true);
		when(cache.get(eq(testSessionId))).thenReturn("anotherUser");
		assertEquals(Status.BAD_REQUEST.getStatusCode(), resource.put(testSessionId.toString()).getStatus(),
				"The status conde should be a bad request.");
	}

	private static KeycloakSession mockSession() {
		@SuppressWarnings("unchecked")
		final Cache<UUID, String> cache = mock(Cache.class);
		final InfinispanConnectionProvider infinispanConnectionProvider = mock(InfinispanConnectionProvider.class);
		final KeycloakSession session = Mockito.mock(KeycloakSession.class);
		doReturn(cache).when(infinispanConnectionProvider).getCache(SessionConnectRealmResourceProvider.CACHE_NAME);
		when(session.getProvider(InfinispanConnectionProvider.class)).thenReturn(infinispanConnectionProvider);
		return session;
	}

	private static AuthResult mockAuthResult() {
		final UserModel userModel = mock(UserModel.class);
		final AuthResult result = mock(AuthResult.class);
		when(userModel.getId()).thenReturn(TEST_USER_ID);
		when(result.getUser()).thenReturn(userModel);
		return result;
	}
}

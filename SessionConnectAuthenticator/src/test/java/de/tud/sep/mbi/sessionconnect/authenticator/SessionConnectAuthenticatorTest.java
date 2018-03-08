package de.tud.sep.mbi.sessionconnect.authenticator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;

import org.infinispan.Cache;
import org.jboss.resteasy.specimpl.UriInfoImpl;
import org.jboss.resteasy.spi.HttpRequest;
import org.junit.jupiter.api.Test;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.connections.infinispan.InfinispanConnectionProvider;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class SessionConnectAuthenticatorTest {

	private static final UUID TEST_UUID = UUID.fromString("f62474dc-06bc-40e3-8231-b2ecaef72f7b");
	private static final String TEST_USER_ID = "ImplausibleValueForAUserId";
	private static final String TEST_BASE_URI = "www.keycloak.com/base";
	private static final String TEST_REALM_NAME = "testRealm";
	private static final int NR_TRIES_FOR_UUID = 2;

	@Test
	void testAuthenticate() {
		final AuthenticationFlowContext context = mockActionContext();
		final Cache<UUID, String> cache = context.getSession().getProvider(InfinispanConnectionProvider.class)
				.getCache(SessionConnectAuthenticator.CACHE_NAME);
		final ArgumentCaptor<UUID> sessionIdCaptor = ArgumentCaptor.forClass(UUID.class);

		context.getUriInfo().getQueryParameters().add(SessionConnectAuthenticator.USE_SESSIONCONNECT_QUERY_PARAMETER,
				null);

		new SessionConnectAuthenticator().authenticate(context);

		verify(cache, times(NR_TRIES_FOR_UUID)).containsKey(any(UUID.class));
		verify(cache).put(sessionIdCaptor.capture(), eq(""), eq(1L), eq(TimeUnit.HOURS));
		verify(context.form()).setAttribute(eq(SessionConnectAuthenticator.SESSION_ID_ATTRIBUTE),
				eq(sessionIdCaptor.getValue().toString()));
		verify(context).forceChallenge(any(Response.class));
	}

	@Test
	void testAuthenticateSkip() {
		final AuthenticationFlowContext context = mockActionContext();

		new SessionConnectAuthenticator().authenticate(context);

		verify(context).challenge(any(Response.class));
		verify(context, never()).success();
	}

	@Test
	void testActionSuccess() {
		final AuthenticationFlowContext context = mockActionContext();
		final ArgumentCaptor<UserModel> userCaptor = ArgumentCaptor.forClass(UserModel.class);

		new SessionConnectAuthenticator().action(context);

		verify(context).setUser(userCaptor.capture());
		assertEquals(TEST_USER_ID, userCaptor.getValue().getId(), "The test user should become logged in.");
		verify(context).success();
		verify(context.getSession().getProvider(InfinispanConnectionProvider.class)
				.getCache(SessionConnectAuthenticator.CACHE_NAME)).remove(TEST_UUID);
		verify(context, never()).attempted();
	}

	@Test
	void testActionFailureSessionNotAuthorized() {
		final AuthenticationFlowContext context = mockActionContext();
		when(context.getSession().getProvider(InfinispanConnectionProvider.class)
				.getCache(SessionConnectAuthenticator.CACHE_NAME).get(eq(TEST_UUID))).thenReturn("");

		new SessionConnectAuthenticator().action(context);

		verifyFailureChallenge(context, AuthenticationFlowError.INVALID_CREDENTIALS);
		verify(context.form()).setAttribute(eq(SessionConnectAuthenticator.SESSION_ID_ATTRIBUTE),
				eq(TEST_UUID.toString()));
	}

	@Test
	void testActionNoSessionIdInSession() {
		final AuthenticationFlowContext context = mockActionContext();
		when(context.getHttpRequest().getDecodedFormParameters()).thenReturn(new MultivaluedHashMap<>());

		new SessionConnectAuthenticator().action(context);

		verifyFailureChallenge(context, AuthenticationFlowError.INVALID_CLIENT_SESSION);
	}

	@Test
	void testActionFailureInvalidUUID() {
		final AuthenticationFlowContext context = mockActionContext();
		final MultivaluedMap<String, String> formParameters = new MultivaluedHashMap<>();
		final String invalidSessionId = "abc";
		formParameters.putSingle(SessionConnectAuthenticator.SESSION_ID_ATTRIBUTE, invalidSessionId);
		when(context.getHttpRequest().getDecodedFormParameters()).thenReturn(formParameters);

		new SessionConnectAuthenticator().action(context);

		verifyFailureChallenge(context, AuthenticationFlowError.INVALID_CLIENT_SESSION);
		verify(context.form()).setAttribute(eq(SessionConnectAuthenticator.SESSION_ID_ATTRIBUTE), eq(invalidSessionId));
	}

	@Test
	void testRequiresUser() {
		assertFalse(new SessionConnectAuthenticator().requiresUser(), "This authenticator shuld not require a user.");
	}

	@Test
	void testConfiguredFor() {
		assertTrue(new SessionConnectAuthenticator().configuredFor(mock(KeycloakSession.class), mock(RealmModel.class),
				mock(UserModel.class)), "This authenticator should be configured for every user.");
	}

	private static AuthenticationFlowContext mockActionContext() {
		final AuthenticationFlowContext context = Mockito.mock(AuthenticationFlowContext.class);
		final KeycloakSession session = mock(KeycloakSession.class);
		final InfinispanConnectionProvider infinispanConnectionProvider = mock(InfinispanConnectionProvider.class);
		@SuppressWarnings("unchecked")
		final Cache<UUID, String> cache = mock(Cache.class);
		final UserProvider userProvider = mock(UserProvider.class);
		final UserModel user = mock(UserModel.class);
		final RealmModel realmModel = mock(RealmModel.class);
		final LoginFormsProvider form = mock(LoginFormsProvider.class);
		final HttpRequest httpRequest = mock(HttpRequest.class);
		final MultivaluedMap<String, String> formParameters = new MultivaluedHashMap<>();
		UriInfoImpl uriInfo = null;
		try {
			uriInfo = new UriInfoImpl(new URI("http://" + TEST_BASE_URI), new URI(TEST_BASE_URI), "", "",
					new ArrayList<PathSegment>());
		} catch (final URISyntaxException e) {
			fail(e);
		}

		when(context.getSession()).thenReturn(session);
		when(context.getRealm()).thenReturn(realmModel);
		when(context.form()).thenReturn(form);
		when(context.getHttpRequest()).thenReturn(httpRequest);
		when(context.getUriInfo()).thenReturn(uriInfo);

		when(session.users()).thenReturn(userProvider);
		when(session.getProvider(InfinispanConnectionProvider.class)).thenReturn(infinispanConnectionProvider);

		doReturn(cache).when(infinispanConnectionProvider).getCache(SessionConnectAuthenticator.CACHE_NAME);
		when(cache.get(TEST_UUID)).thenReturn(TEST_USER_ID);
		when(cache.containsKey(any(UUID.class))).thenAnswer(new Answer<Boolean>() {
			int i = 0;

			@Override
			public Boolean answer(InvocationOnMock invocation) {
				return ++i < NR_TRIES_FOR_UUID;
			}
		});
		when(userProvider.getUserById(any(String.class), any(RealmModel.class))).thenReturn(user);
		when(user.getId()).thenReturn(TEST_USER_ID);

		when(realmModel.getName()).thenReturn(TEST_REALM_NAME);

		when(form.setError(any(String.class))).thenReturn(form);
		when(form.setAttribute(any(String.class), any(Object.class))).thenReturn(form);
		when(form.createForm(any(String.class))).thenReturn(mock(Response.class));

		when(httpRequest.getDecodedFormParameters()).thenReturn(formParameters);

		formParameters.putSingle(SessionConnectAuthenticator.SESSION_ID_ATTRIBUTE, TEST_UUID.toString());
		formParameters.putSingle(SessionConnectAuthenticator.USE_SESSIONCONNECT_QUERY_PARAMETER, "");

		return context;
	}

	private static void verifyFailureChallenge(AuthenticationFlowContext context, AuthenticationFlowError error) {
		final LoginFormsProvider form = context.form();
		verify(context).failureChallenge(eq(error), any(Response.class));
		verify(form).setError(any(String.class));
		verify(form.setError("")).createForm(SessionConnectAuthenticator.SESSION_CONNECT_FORM);
		verify(context, never()).success();
	}

}

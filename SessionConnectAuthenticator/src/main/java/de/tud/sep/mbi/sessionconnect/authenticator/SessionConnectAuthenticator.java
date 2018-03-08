package de.tud.sep.mbi.sessionconnect.authenticator;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Response;

import org.infinispan.Cache;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.connections.infinispan.InfinispanConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

/**
 * The Authenticator module of SessionConnect. Authenticates unregistered user A
 * as user B, if user B authorizes A on another device.
 *
 * @author Mario Trageser, Paul Mohr
 */
public class SessionConnectAuthenticator implements Authenticator {

	/**
	 * The name of the session to user cache.
	 */
	protected static final String CACHE_NAME = "SESSION_TO_USER_ID";

	/**
	 * The name of the ftl file for the form.
	 */
	protected static final String SESSION_CONNECT_FORM = "sessionconnect-form.ftl";

	/**
	 * The name of the session id attribute.
	 */
	protected static final String SESSION_ID_ATTRIBUTE = "session_id";

	/**
	 * If this query parameter is present in the authentication URL, this
	 * authenticator will be used, otherwise, it will be skipped.
	 */
	protected static final String USE_SESSIONCONNECT_QUERY_PARAMETER = "use_sessionconnect";

	/**
	 * The lifetime of entries in the SESSION_TO_USER_ID cache in hours.
	 */
	private static final int CACHE_LIFETIME = 1;

	@Override
	public void close() {
	}

	/**
	 * {@inheritDoc}
	 *
	 * If the query parameter USE_SESSIONCONNECT_QUERY_PARAMETER is not present in
	 * the authentication URL, this authenticator will be attempted. Otherwise, it
	 * generates a session id not contained in the SESSION_TO_USER cache, and maps
	 * it to an empty string. This session id will be set as a session attribute
	 * under the name session_id. Challenges the user with the form defined in
	 * sessionconnect-form.ftl. The session id will be passed to the form as an
	 * attribute with the name session_id.
	 */
	@Override
	public void authenticate(AuthenticationFlowContext context) {
		final Cache<UUID, String> cache = context.getSession().getProvider(InfinispanConnectionProvider.class)
				.getCache(CACHE_NAME);
		UUID sessionId;
		do
			sessionId = UUID.randomUUID();
		while (cache.containsKey(sessionId));
		// Save the session id to an empty string
		cache.put(sessionId, "", CACHE_LIFETIME, TimeUnit.HOURS);
		// Add a new parameter to the freemarker form's model
		context.form().setAttribute(SESSION_ID_ATTRIBUTE, sessionId.toString());
		// Build the freemarker form
		final Response customChallenge = context.form().createForm(SESSION_CONNECT_FORM);
		// Send out the form
		if (!context.getUriInfo().getQueryParameters().containsKey(USE_SESSIONCONNECT_QUERY_PARAMETER))
			context.challenge(customChallenge);
		else
			context.forceChallenge(customChallenge);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Checks, if the session id stored in the form parameter session_id is mapped
	 * to a user. If this is the case, the caller will be logged in as this user.
	 * Otherwise, an error message will be displayed.
	 */
	@Override
	public void action(AuthenticationFlowContext context) {
		final String sessionIdString = context.getHttpRequest().getDecodedFormParameters()
				.getFirst(SESSION_ID_ATTRIBUTE);
		final Cache<UUID, String> cache = context.getSession().getProvider(InfinispanConnectionProvider.class)
				.getCache(CACHE_NAME);
		UUID sessionId;
		if (sessionIdString == null)
			context.failureChallenge(AuthenticationFlowError.INVALID_CLIENT_SESSION, context.form()
					.setError("Form parameter \"session_id\" is not present.").createForm(SESSION_CONNECT_FORM));
		else
			try {
				sessionId = UUID.fromString(sessionIdString);
				final String userId = cache.get(sessionId);
				if (userId.isEmpty())
					context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS,
							context.form().setError("Session not authorized by any user")
									.setAttribute(SESSION_ID_ATTRIBUTE, sessionIdString)
									.createForm(SESSION_CONNECT_FORM));
				else {
					context.setUser(context.getSession().users().getUserById(userId, context.getRealm()));
					context.success();
					cache.remove(sessionId);
				}
			} catch (final IllegalArgumentException e) {
				context.failureChallenge(AuthenticationFlowError.INVALID_CLIENT_SESSION,
						context.form().setError("Form parameter \"session_id\" does not represent a UUID.")
								.setAttribute(SESSION_ID_ATTRIBUTE, sessionIdString).createForm(SESSION_CONNECT_FORM));
			}
	}

	/**
	 *
	 * {@inheritDoc}
	 *
	 * This authenticator does not require a user.
	 */
	@Override
	public boolean requiresUser() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 *
	 * A user is always configured for this authenticator.
	 */
	@Override
	public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
		return true;
	}

	/**
	 * {@inheritDoc}
	 *
	 * This authenticator does not have required actions.
	 */
	@Override
	public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
	}
}
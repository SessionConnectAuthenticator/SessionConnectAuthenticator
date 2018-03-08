package de.tud.sep.mbi.sessionconnect.webservice;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.infinispan.Cache;
import org.keycloak.connections.infinispan.InfinispanConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.services.managers.AuthenticationManager.AuthResult;
import org.keycloak.services.resource.RealmResourceProvider;

/**
 * Using this resource, a patient can connect a session of a third party user
 * with his own account.
 *
 * @author Mario Trageser
 */
public class SessionConnectRealmResourceProvider implements RealmResourceProvider {

	/**
	 * The name of the session to user cache.
	 */
	protected static final String CACHE_NAME = "SESSION_TO_USER_ID";

	/**
	 * The parameter for the session id.
	 */
	private static final String SESSION_ID_PARAMETER = "sessionId";

	/**
	 * The path of this resource, relative to the realm's path.
	 */
	private static final String PATH = "{" + SESSION_ID_PARAMETER + "}";

	/**
	 * The lifetime of entries in the SESSION_TO_USER_ID cache in hours.
	 */
	private static final long CACHE_LIFETIME = 1;

	/**
	 * The authentication result of the current session.
	 */
	private final AuthResult authResult;

	/**
	 * The cache, mapping session ids to user ids.
	 */
	private final Cache<UUID, String> cache;

	/**
	 * Creates a new SessionConnectRealmResourceProvider.
	 *
	 * @param session
	 *            The caller's keycloak session.
	 * @param authResult
	 *            The result of the client's authentication.
	 */
	SessionConnectRealmResourceProvider(KeycloakSession session, AuthResult authResult) {
		this.authResult = authResult;
		cache = session.getProvider(InfinispanConnectionProvider.class).getCache(CACHE_NAME);
	}

	@Override
	public void close() {
	}

	@Override
	public Object getResource() {
		return this;
	}

	/**
	 * Using this method, a patient can connect a session of a third party user with
	 * his own account. As a prerequisite, the third party user must have created a
	 * session, which is stored in the cache. Additionally, this session must not be
	 * connected to any user yet.
	 *
	 * @param userId
	 *            The id of the calling patient.
	 * @param sessionId
	 *            The id of the third party user's session.
	 * @return 200, if everything worked fine. 400, if the session id was not stored
	 *         in the cache, 401, if the user is not logged in, or already was
	 *         associated with some user.
	 */
	// TODO This should be a POST. For debugging via the browser, it needs to be
	// GET.
	@PUT
	@Path(PATH)
	public Response put(@PathParam(SESSION_ID_PARAMETER) String sessionId) {
		final UUID uuid = UUID.fromString(sessionId);
		if (authResult == null)
			return Response.status(Status.UNAUTHORIZED).build();
		final UserModel user = authResult.getUser();
		if (!cache.containsKey(uuid) || !cache.get(uuid).isEmpty())
			return Response.status(Status.BAD_REQUEST).build();
		cache.put(uuid, user.getId(), CACHE_LIFETIME, TimeUnit.HOURS);
		return Response.ok().build();
	}

	/**
	 * This method may only be used for debug purposes! Do not compile this method
	 * into the build! Retrieves the user id, mapped by a session id.
	 *
	 * @param sessionId
	 *            The user id, the session id is mapped to.
	 * @return The user id, the session id is mapped to.
	 */
	@GET
	@Path("debug/{" + SESSION_ID_PARAMETER + "}")
	public Response get(@PathParam(SESSION_ID_PARAMETER) String sessionId) {
		return Response.ok(cache.get(UUID.fromString(sessionId))).build();
	}
}

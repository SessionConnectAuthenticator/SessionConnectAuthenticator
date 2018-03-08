package de.tud.sep.mbi.sessionconnect.webservice;

import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

/**
 * A factory for creating a session connect resource provider.
 *
 * @author Mario Trageser
 *
 */
public class SessionConnectRealmResourceProviderFactory implements RealmResourceProviderFactory {

	/**
	 * The id of this provider.
	 */
	private static final String PROVIDER_ID = "sessionconnect";

	/**
	 * The name of the realm, on which the session connect runs.
	 */
	private static final String SESSIONCONNECT_REALM = System.getenv("SESSIONCONNECT_REALM");

	@Override
	public RealmResourceProvider create(KeycloakSession session) {
		return new SessionConnectRealmResourceProvider(session, AuthenticationManager
				.authenticateIdentityCookie(session, session.realms().getRealmByName(SESSIONCONNECT_REALM), true));
	}

	@Override
	public void init(Scope config) {
	}

	@Override
	public void postInit(KeycloakSessionFactory factory) {
	}

	@Override
	public void close() {
	}

	@Override
	public String getId() {
		return PROVIDER_ID;
	}

}

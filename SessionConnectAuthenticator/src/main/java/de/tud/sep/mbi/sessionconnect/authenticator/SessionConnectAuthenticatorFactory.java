package de.tud.sep.mbi.sessionconnect.authenticator;

import java.util.ArrayList;
import java.util.List;

import org.keycloak.Config.Scope;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.ConfigurableAuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel.Requirement;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * A factory creating a sample authenticator.
 *
 * @author Mario Trageser
 */
public class SessionConnectAuthenticatorFactory implements AuthenticatorFactory, ConfigurableAuthenticatorFactory {

	/**
	 * The id of this provider.
	 */
	protected static final String PROVIDER_ID = "sessionconnect-authenticator";

	/**
	 * The displayed name of this authenticator.
	 */
	private static final String DISPLAY_TYPE = "Session Connect Authenticator";

	/**
	 * The singleton authenticator instance.
	 */
	private static final SessionConnectAuthenticator SINGLETON = new SessionConnectAuthenticator();

	/**
	 * The possible requirements for the authenticator.
	 */
	private static final Requirement[] REQUIREMENT_CHOICES = { Requirement.ALTERNATIVE, Requirement.OPTIONAL,
			Requirement.DISABLED };

	/**
	 * Returns a singleton instance.
	 */
	@Override
	public Authenticator create(KeycloakSession session) {
		return SINGLETON;
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

	@Override
	public String getDisplayType() {
		return DISPLAY_TYPE;
	}

	@Override
	public String getReferenceCategory() {
		return null;
	}

	/**
	 * This authenticator can be configured.
	 */
	@Override
	public boolean isConfigurable() {
		return true;
	}

	/**
	 * This authenticator supports every requirement.
	 */
	@Override
	public Requirement[] getRequirementChoices() {
		return REQUIREMENT_CHOICES;
	}

	/**
	 * This authenticator does not have required actions.
	 */
	@Override
	public boolean isUserSetupAllowed() {
		return false;
	}

	@Override
	public String getHelpText() {
		return "This authenticator offers a session id. Should an authenticated user authorize that session (on a seperate application), it allows authentication as that user.";
	}

	/**
	 * {@inheritDoc}
	 *
	 * Nothing in this authenticator can be configured.
	 */
	@Override
	public List<ProviderConfigProperty> getConfigProperties() {
		return new ArrayList<>();
	}

}

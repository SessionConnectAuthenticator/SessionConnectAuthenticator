package de.tud.sep.mbi.sessionconnect.authenticator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.AuthenticationExecutionModel.Requirement;
import org.keycloak.provider.ProviderConfigProperty;

class SessionConnectAuthenticatorFactoryTest {

	@Test
	void testCreate() {
		final SessionConnectAuthenticatorFactory factory = new SessionConnectAuthenticatorFactory();
		final Authenticator authenticator = factory.create(null);
		assertNotNull(authenticator, "The created authenticator should not be null.");
		assertSame(authenticator, factory.create(null), "The create method should return a singleton.");
	}

	@Test
	void testId() {
		assertEquals(SessionConnectAuthenticatorFactory.PROVIDER_ID, new SessionConnectAuthenticatorFactory().getId(),
				"This authenticator's id should be sessionconnect-authenticator");
	}

	@Test
	void testReferenceCategory() {
		assertNull(new SessionConnectAuthenticatorFactory().getReferenceCategory(),
				"This authenticator should not have a reference category.");
	}

	@Test
	void testConfigurable() {
		assertTrue(new SessionConnectAuthenticatorFactory().isConfigurable(),
				"This authenticator should be configurable.");
	}

	@Test
	void testRequirementChoices() {
		final List<Requirement> requirementChoices = Arrays
				.asList(new SessionConnectAuthenticatorFactory().getRequirementChoices());
		assertFalse(requirementChoices.contains(Requirement.REQUIRED), "The required choice should not be supported.");
		assertEquals(Requirement.values().length - 1, requirementChoices.size(),
				"This authenticator should support all remaining requirement choices.");
	}

	@Test
	void testUserSetupAllowed() {
		assertFalse(new SessionConnectAuthenticatorFactory().isUserSetupAllowed(),
				"This authenticator should not require a user setup.");
	}

	@Test
	void testConfigProperties() {
		final List<ProviderConfigProperty> configProperties = new SessionConnectAuthenticatorFactory()
				.getConfigProperties();
		assertTrue(configProperties.isEmpty(), "There should be nothing to configure.");
	}

}

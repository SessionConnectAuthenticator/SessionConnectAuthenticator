# Create Authenticator jar
- Must contain: \META-INF\services\org.keycloak.authentication.AuthenticatorFactory (text file containing qualified name of all authenticator factories)
- Must contain: All authenticators and their factories

# Build and Run the Keycloak Server in Docker
- Start Docker Quickstart Terminal.
- Go to the SPI directory and call "./gradlew build".
- Copy your Authenticator jar to "Keycloak\module\Authenticator.jar".
- Copy the Webservice.jar into the same directory
- Copy "Authenticator\ftl\*.ftl" to "Keycloak\ftl".
- Go to the Keycloak directory.
- Call "docker build -t registry.eu-de.bluemix.net/tud_mbi/keycloak:<VERSION> .".
- Call "docker images" to identify the image's number.
- Call "docker run -p 8080:8080 <IMAGE_ID>".
- You can access keycloak at "<DOCKER_IP>:8080".

# Build and Deploy the Keycloak Server
To build and deploy the Keycloak server, go to the Keycloak directory and call deploy

#Set up the Session Connect  Authenticator
- Open "<KEYCLOAK_URL>/auth".
- Click on "Administration Console". Alternatively, you can usa any name.
- Log in with username "admin" and password "admin".
- On the left, click on "Authentication".
- In the "Flow" tab, click on "New".
- Enter "Session Connect Authenticator" as the alias and click on "Save".
- Click on "Add execution".
- Select "Session Connect Authenticator" as the provider and click on "Save".
- Set the "Requirement" to "REQUIRED". Alternatively, you can use any Requirement.
- Go to the "Bindings" tab.
- Select "Session Connect Authenticator" as the "Browser Flow" and click on "Save". Alternatively, you can use any flow.
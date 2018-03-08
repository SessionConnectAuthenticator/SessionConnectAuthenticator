SET VERSION=0.13
SET AUTHENTICATOR_VERSION=0.1-SNAPSHOT
SET WEBSERICE_VERSION=0.1-SNAPSHOT

SET REGION=eu-de
SET ARTIFACT_ID=Keycloak
SET DEPLOYMENT_ID=keycloak
SET SERVICE_ID=%DEPLOYMENT_ID%-service
SET IMAGE_NAME=registry.%REGION%.bluemix.net/tud_mbi/%DEPLOYMENT_ID%:%VERSION%
SET BUILD=./gradlew build
SET BUILD_DIR=build\libs
SET AUTHENTICATOR=SessionConnectAuthenticator
SET WEBSERVICE=SessionConnectWebservice

cd ..\%AUTHENTICATOR%
call %BUILD%
cd ..\%WEBSERVICE%
call %BUILD%
cd ..\%ARTIFACT_ID%
mkdir module
mkdir ftl
copy ..\%AUTHENTICATOR%\%BUILD_DIR%\SessionConnectAuthenticator-%AUTHENTICATOR_VERSION%.jar module\Authenticator.jar
copy ..\%WEBSERVICE%\%BUILD_DIR%\SessionConnectWebservice-%WEBSERICE_VERSION%.jar module\Webservice.jar
copy ..\%AUTHENTICATOR%\ftl ftl
docker build -t %IMAGE_NAME% .
bx login --sso
bx target -r %REGION% -o tud_mbi -s dev
docker push %IMAGE_NAME%
kubectl delete deployment,service %DEPLOYMENT_ID% %SERVICE_ID% --ignore-not-found=true
kubectl apply -f deployment.yml
PAUSE
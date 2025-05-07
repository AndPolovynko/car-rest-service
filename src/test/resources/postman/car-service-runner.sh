set -eu pipefail

echo "Attempting to connect to keycloak"
until $(nc -zv keycloak 8180); do
    printf '.'
    sleep 10
done
echo "Connected to keycloak!"

exec java -jar carservice-0.0.1-SNAPSHOT.jar


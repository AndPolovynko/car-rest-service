set -eu pipefail

echo "Attempting to connect to car-service"
until $(nc -zv car-service 8080); do
    printf '.'
    sleep 5
done
echo "Connected to car-service!"

newman run /resources/postman-collection.json

exit 0


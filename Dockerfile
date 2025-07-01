FROM mongo:7.0

# Set environment variable to allow initialization scripts
ENV MONGO_INITDB_ROOT_USERNAME=admin
ENV MONGO_INITDB_ROOT_PASSWORD=adminpass
ENV MONGO_INITDB_DATABASE=mydatabase

# Copy initialization script
COPY init-mongo.js /docker-entrypoint-initdb.d/
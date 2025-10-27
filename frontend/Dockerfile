# build stage
FROM node:18-alpine as build
WORKDIR /app

# Build arguments for environment variables
ARG REACT_APP_KEYCLOAK_AUTHORITY
ARG REACT_APP_KEYCLOAK_CLIENT_ID
ARG REACT_APP_KEYCLOAK_REDIRECT_URI

# Set as environment variables for npm build
ENV REACT_APP_KEYCLOAK_AUTHORITY=$REACT_APP_KEYCLOAK_AUTHORITY
ENV REACT_APP_KEYCLOAK_CLIENT_ID=$REACT_APP_KEYCLOAK_CLIENT_ID
ENV REACT_APP_KEYCLOAK_REDIRECT_URI=$REACT_APP_KEYCLOAK_REDIRECT_URI

COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# production stage
FROM nginx:1.23-alpine
COPY --from=build /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
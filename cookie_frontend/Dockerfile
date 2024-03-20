FROM node:lts-alpine3.18 AS build
WORKDIR /usr/local/app

COPY ./ /usr/local/app/

RUN npm install
RUN npm run build

#SECOND STAGE
FROM nginx:alpine3.18

COPY /nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /usr/local/app/dist/cookie_frontend /usr/share/nginx/html

EXPOSE 80

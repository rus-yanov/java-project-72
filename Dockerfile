FROM gradle:7.4.2-jdk17

WORKDIR /app

COPY /app .

RUN APP_ENV=production ./build/install/app/bin/app

CMD ./build/install/app/bin/app

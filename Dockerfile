FROM gradle:7.4.2-jdk17

WORKDIR /app

COPY /app .

RUN make start-dist

CMD ./build/install/app/bin/app

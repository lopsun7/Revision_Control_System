FROM openjdk:8
COPY ./src /tmp
WORKDIR /tmp
#RUN javac -d ./build $(find src -name "*.java")
RUN javac Main.java
ENTRYPOINT ["java","Main"]

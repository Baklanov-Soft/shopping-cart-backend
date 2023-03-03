FROM eclipse-temurin:17.0.6_10-jre-jammy

WORKDIR /opt/app

#COPY ./application.conf ./target/scala-2.13/shopping-cart-backend-assembly-0.1.0-SNAPSHOT.jar ./
COPY ./target/scala-2.13/shopping-cart-backend-assembly-0.1.0-SNAPSHOT.jar ./

ENTRYPOINT ["java", "-cp", "shopping-cart-backend-assembly-0.1.0-SNAPSHOT.jar", "org.baklanovsoft.shoppingcart.Main"]

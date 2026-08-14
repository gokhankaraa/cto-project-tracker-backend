# --- 1. Asama: build ---
# Uygulamayi Docker'in icinde derleyip jar uretir; makinede Java/Maven kurulu olmasi gerekmez.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Once bagimlilik tanimlarini kopyala (katman onbellegi: kod degisince bagimliliklar yeniden inmez)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -q -B dependency:go-offline

# Kaynaklari kopyala ve jar uret (testler build sirasinda calisir)
COPY src/ src/
RUN ./mvnw -q -B clean package

# --- 2. Asama: runtime ---
# Sadece JRE iceren kucuk imaj; yalnizca uretilen jar calisir.
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]


## simple consulta mysql en la terminal 
    
### cmd mysql :  
 
```
mysql -u root -pclavemanuel
    
```

### eliminar base de datos mysql :  
```
DROP DATABASE nombre_database;
```

### cmd mysql :  
    
```
SHOW DATABASES;
EXIT;
    
```
## verificar que el servidor mysql esta activo 
    
```
sudo systemctl status mysql       
```
## ejecutar mysql .sql

```
mysql -u root -pclavemanuel < /ruta/de/tu/archivo/creardatabase.sql
    
```
## actualizar dependencias spring boot 
    
```
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```
Una vez que guardes los cambios en tu pom.xml, debes avisarle a Maven que descargue las dependencias e intente compilar de nuevo.
Ejecuta el siguiente comando en tu terminal dentro de la carpeta raíz de tu proyecto:
    
```
./mvnw clean compile
```
    
#### actualizar credenciales mysql en spring boot 
    
 configurar las credenciales de tu base de datos en el archivo de propiedades de Spring Boot.
 Busca en tu proyecto el archivo application.properties. Generalmente se encuentra en la ruta:
src/main/resources/application.properties

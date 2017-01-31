SET watchpath=%~dp0
mvn package && filewatcher 'template.xlsx' 'java -jar target/minipage.jar %watchpath%template.xlsx'
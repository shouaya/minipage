SET watchpath=%~dp0
filewatcher '**/*.js' 'template.xlsx' 'java -jar %watchpath%../target/minipage.jar %watchpath%template.xlsx'
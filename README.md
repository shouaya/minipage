![](https://s3-ap-northeast-1.amazonaws.com/ms17222/step/Original_without_effects_204x75.png)

A Human Like Page Design Tool
=========================

how to package: 
> mvn package

how to run:
> create execute.bat

    rd /s /q "out"
    del "log.txt"
    mkdir "out"
    java -jar template.xlsx chromedriver.exe "Apple iPhone 6" >> log.txt

> run execute.bat

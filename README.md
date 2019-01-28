# Retail Group News

Версия 1.1.0

**Корпоративная лента новостей**

## Технологии

При создании этого приложения мы использовали следующие технологии:
#### Java 8 SE 

https://www.oracle.com/technetwork/java/javase/overview/index.html

#### JavaFX 2.2

Приложение построено на платформе JavaFX, более детально о платформе смотрите на официальнном сайте Oracle (https://www.oracle.com/technetwork/java/javase/overview/javafx-overview-2158620.html) 

#### Jersey 

Jersey - фреймворк для работы с RESTful веб сервисами. Он предоставляет собственный API, который расширяет инструментарий JAX-RS дополнительными функциями и утилитами для дальнейшего упрощения обслуживания RESTful и разработки клиентов.

(https://jersey.github.io/)

#### Socket.IO for Java

Полнофункциональная клиентская библиотека [Socket.IO](https://socket.io/) для Java, совместимая с Socket.IO v1.0 и более поздними версиями.

Source code: (https://github.com/socketio/socket.io-client-java)

#### JSoup (https://jsoup.org/)
Jsoup - это Java-библиотека с открытым исходным кодом, предназначенная для анализа, извлечения и управления данными, хранящимися в документах HTML.


### Сборка приложения

Чтобы создать установщик для платформы Windows, нужно также установить Wix Toolset (http://wixtoolset.org/) версию не ниже 3.11. Чтобы установить, перейдите по [ссылке] (https://github.com/wixtoolset/wix3/releases/tag/wix3111rtm) и скачайте файл wix311.exe или wix311.exe.zip, установите програму на свой компьютер.
Далее Вам понадобятся конфигурационный файл, который уже есть в каталоге deploy/package/windows c расширеннием \*.wxs, он уже содержит настройки которые нужны для сборки нашего проекта. Еще Вам будет нужно установить систему сборки Java проектов [Maven](https://maven.apache.org/), так как Wix Toolset создает только Windows инсталлятор, но само JavaFX приложение собирает javafx-maven-plugin. Далее запускает сборку приложения выполнив в командной строке ``jfx:build-jar`` и ``jfx:build-native``, если проект успешно построился, забираем наш установщик retail-group-news-(version).exe из папки target/jfx/native. Сборку проекта нужно делать на Windows, потому что мы создаем нативное приложение.

> Impltech company   
> www.impltech.com  
> 2018

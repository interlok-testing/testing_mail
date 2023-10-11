# Mail Testing

[![license](https://img.shields.io/github/license/interlok-testing/testing_mail.svg)](https://github.com/interlok-testing/testing_mail/blob/develop/LICENSE)
[![Actions Status](https://github.com/interlok-testing/testing_mail/actions/workflows/gradle-build.yml/badge.svg)](https://github.com/interlok-testing/testing_mail/actions/workflows/gradle-build.yml)

Project tests interlok-mail features

## What it does

This project is very simple and contains two channels with one workflow each.

The first workflow has a polling trigger that produces a message every 30 seconds and publish it to a Mail box.

The second workflow is checking new message every 30 seconds on the mail box and copy the message on the file system.

```mermaid
graph LR
  subgraph To Mail
    direction LR
    PT(Polling Trigger) --> SC1(Service Collection)
    SC1 --> MP(Mail Producer)
  end
  MP --> MS[Mail Server]
  MS --> MC("Mail Consumer")
  subgraph Mail To FS
    direction LR
    MC --> SC2(Service Collection)
    SC2 --> FP(FS Producer)
  end
  FP --> FS(FS)
```

## Getting started

Before starting Interlok you need to create a Kafka docker container with

* `docker-compose up`

Then start Interlok

* `./gradlew clean build`
* `(cd ./build/distribution && java -jar lib/interlok-boot.jar)`

The config is using a variables.properties to configure the mail host, credentials and the file system directory.

```
emailHost=gmail.com
fromEmail=interlok.testing+from@${emailHost}
fromPassword=SomePassword
toEmail=interlok.testing+to@${emailHost}
toPassword=SomePassword
smtpPort=587
smtpUrl=smtp://smtp.${emailHost}:${smtpPort}
imapPort=993
imapUrl=imap://imap.${emailHost}:${imapPort}/INBOX
fsDir=file://localhost/./messages/in
```

For simplicity I chose to use Gmail but any mail provider supporting **SMTP** and **IMAP** or **POP3** can be used.

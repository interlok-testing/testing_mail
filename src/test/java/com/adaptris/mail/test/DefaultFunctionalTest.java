package com.adaptris.mail.test;

import com.adaptris.core.fs.FsHelper;
import com.adaptris.testing.SingleAdapterFunctionalTest;
import net.markwalder.vtestmail.pop3.Pop3Server;
import net.markwalder.vtestmail.smtp.SmtpServer;
import net.markwalder.vtestmail.store.Mailbox;
import net.markwalder.vtestmail.store.MailboxStore;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.util.Properties;
import java.util.stream.Stream;

public class DefaultFunctionalTest extends SingleAdapterFunctionalTest {
    MailboxStore mailboxStore;
    SmtpServer smtpServer;
    Pop3Server pop3Server;
    String emailHost;
    String fromEmail;
    String fromPassword;
    String toEmail;
    String toPassword;
    int sendFrequencySeconds = 3;
    String fsDir;
    Path fsDirPath;
    Mailbox mailboxTo;
    Mailbox mailboxFrom;

    @BeforeAll
    @Override
    public void setup() throws Exception {
        MailboxStore store = new MailboxStore();
        smtpServer = new SmtpServer(store);
        smtpServer.setUseSSL(false);
        smtpServer.addAuthType("LOGIN");
        smtpServer.setAuthenticationRequired(true);
        smtpServer.start();
        pop3Server = new Pop3Server(store);
        pop3Server.setUseSSL(false);
        pop3Server.setAuthenticationRequired(true);
        pop3Server.addAuthType("LOGIN");
        pop3Server.start();
        super.setup();
    }

    @AfterAll
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        try {
            smtpServer.stop();
        } catch (Exception ignored) {

        }
        try {
            pop3Server.stop();
        } catch (Exception ignored) {

        }
    }

    @Override
    protected void customiseVariablesIfExists(Properties props) {
        props.put("emailHost", "localhost");
        props.put("smtpPort", String.valueOf(smtpServer.getPort()));
        props.put("imapPort", String.valueOf(pop3Server.getPort()));
        props.put("sendFrequencySeconds", String.valueOf(sendFrequencySeconds));
        props.put("receiveFrequencySeconds", String.valueOf(1));
        props.put("sslEnabled", String.valueOf(false));
        props.put("mailBoxProtocol", "pop3");
        emailHost = props.getProperty("emailHost");

        props.put("toEmail", String.format("interlok.testing+to@%s", emailHost));
        props.put("fromEmail", String.format("interlok.testing+from@%s", emailHost));


        fromEmail = props.getProperty("fromEmail");
        fromPassword = props.getProperty("fromPassword");
        toEmail = props.getProperty("toEmail");
        toPassword = props.getProperty("toPassword");
        fsDir = props.getProperty("fsDir");
        try {
            fsDirPath = Paths.get(FsHelper.toFile(fsDir).toURI());
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
        try {
            Files.deleteIfExists(fsDirPath);
        } catch (Exception ignore) {}

        mailboxTo = pop3Server.getStore().createMailbox(toEmail, toPassword, toEmail);
        mailboxFrom = smtpServer.getStore().createMailbox(fromEmail, fromPassword, fromEmail);
    }

    @Test
    public void test() throws Exception {
        Thread.sleep(sendFrequencySeconds * 1000L);
        waitForFileEvent(fsDirPath, 1000L, StandardWatchEventKinds.ENTRY_CREATE);
        try (Stream<Path> listed = Files.list(fsDirPath)) {
            Assertions.assertTrue(listed.findAny().isPresent());
        }
    }
}

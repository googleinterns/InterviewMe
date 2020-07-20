// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.data;

import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailUtility {
  public static void sendEmail(
      String host,
      String port,
      final String userName,
      final String password,
      String toAddress,
      String subject,
      String message)
      throws AddressException, MessagingException {

    // sets SMTP server properties
    Properties properties = new Properties();
    properties.put("mail.smtp.host", host);
    properties.put("mail.smtp.port", port);
    properties.put("mail.smtp.auth", "true");
    properties.put("mail.smtp.starttls.enable", "true");

    // creates a new session with an authenticator
    Authenticator auth =
        new Authenticator() {
          public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(userName, password);
          }
        };

    Session session = Session.getInstance(properties, auth);
    session.setDebug(true);

    // creates a new e-mail message
    Message msg = new MimeMessage(session);

    msg.setFrom(new InternetAddress(userName));
    InternetAddress[] toAddresses = {new InternetAddress(toAddress)};
    msg.setRecipients(Message.RecipientType.TO, toAddresses);
    msg.setSubject(subject);
    msg.setSentDate(new Date());
    msg.setText(message);

    // sends the e-mail
    Transport.send(msg);
  }
}

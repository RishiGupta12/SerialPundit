## Email home automation

This application example demonstrates how to read unread emails from gmail account, parse them to 
check if they contain command to be executed by server application which will communicate with serial port.

This application can be used to control serial devices for surveillance use cases.

#### Running this application

Click on 'Refresh ports' button to fetch serial ports present in system. Select the port, configure it and click 
on Connect button. Enter email id, authentication token and click on Login button. Send a mail to this email id 
with subject field set to 'CMD1'.

The application will automatically read this unread email from given account and will check if subject is set to 
"CMD1" or "CMD2". If it founds CMD1/2 it will send this string to serial port.
   
#### What this application does and how it does
- Create Java swing JFrame and setup GUI components and show them.
- When user supplies serial port and configuration, open serial port and configure it.
- When user supplies email id and authentication token, connect to google server and login into account.
- Create worker thread that will read inbox for unread emails.
- If there is an unread email, check if it's subject line is CMD1/2. If yes, send this line to serial port.
- When user presses close button of UI window, close serial port, close connection to server and exit application.

#### Prerequisites
- The javamail jar is needed to build this application. Get it from here :
  http://www.oracle.com/technetwork/java/javamail/index-138643.html
- python script from terminal to generate authentication token using these ID and secret.
  https://raw.githubusercontent.com/google/gmail-oauth2-tools/master/python/oauth2.py

#### How to generate OAuth 2.0 token for gmail
- Create project from google developer console.
  https://console.developers.google.com/project
  
- Once project is created, click on Enable and Manage API option under Use google API. New page will open.

- In this new page select credentials in left most frame. Click on Create credentials button and select OAuth client ID.

- Configure consent screen by given product name and clicking on save button. Select Application type as other.

  This step will give OAuth 2.0 client ID and client secret from google developer console.

- Run python script from terminal to generate authentication token using these ID and secret.
  ```
  ./oauth2.py --user=XXX@gmail.com --client_id=YYY --client_secret=ZZZ --generate_oauth2_token
  ```
- Terminal will generate a URL and ask to verify this token. Copy this link and paste in web browser to verify. 
  Once verified it will generate OAuth 2.0 token.

#### How to read gmail in Java using IMAP protocol and OAuth 2.0 authentication
For non-Gmail clients, Gmail supports the standard IMAP and SMTP protocols. The Gmail IMAP and SMTP servers 
support authorization via the standard OAuth 2.0 protocol and use the standard Simple Authentication and Security 
Layer (SASL), via the native IMAP AUTHENTICATE and SMTP AUTH commands.

##### 1. Email client
Under the hood our application is actually an email client who uses IMAP protocol to exchange data packets with 
Google server. The javax.mail.* package provides all the required methods to send/receive mail to/from mail server.

##### 2. SASL client
The SASL is a framework for authentication and data security in Internet protocols. It decouples authentication mechanisms from 
application protocols, in theory allowing any authentication mechanism supported by SASL to be used in any application protocol 
that uses SASL. A SASL mechanism implements a series of challenges and responses. IMAP supports SASL.

Our application creates a SASL client to carry out authentication part with Google server to login into e-mail account.

##### 3. Security provider

SASL is a method for adding authentication support to connection-based protocols. To use this specification, IMAP includes 
a command for identifying and authenticating a user to a server. The command has a required argument identifying a SASL
mechanism. 

The SASL mechanisms are named by strings. A SASL mechanism specify the contents and semantics of the authentication 
data.

Our application creates a provider which let the application authenticate with Google server via SASL and OAuth 2.0.

The Java platform includes a number of providers that implement a core set of security services. It also allows for 
additional custom (vendor specific) providers to be installed. This enables developers to extend the platform with new 
security mechanisms.

Security services are implemented in providers, which are plugged into the Java platform via a standard interface that 
makes it easy for applications to obtain security services without having to know anything about their implementations. 
This allows developers to focus on how to integrate security into their applications, rather than on how to actually 
implement complex security mechanisms.
  
Providers are inter-operable across applications. Specifically, an application is not bound to a specific provider, and a 
provider is not bound to a specific application.

The Java SASL API defines classes and interfaces for applications that use SASL mechanisms. It is defined to be mechanism-neutral; 
an application that uses the API need not be hardwired into using any particular SASL mechanism. Applications can select the mechanism 
to use based on desired security features. The API supports both client and server applications. The javax.security.sasl.Sasl class 
is used to create SaslClient and SaslServer objects.

SASL mechanism implementations are supplied in provider packages. Each provider may support one or more SASL mechanisms and is 
registered and invoked via the standard provider architecture.

##### 4. OAuth 2.0
OAuth is an open standard for authorization, commonly used as a way for Internet users to log into third party websites 
using their accounts without exposing their password. Generally, OAuth provides to clients a 'secure delegated access' to 
server resources on behalf of a resource owner. It specifies a process for resource owners to authorize third-party access 
to their server resources without sharing their credentials.

Our application send OAuth authentication token to Google server with the help of SASL.

In the context of the Java runtime environment, authentication is the process of identifying the user of an executing Java 
program. The Java platform provides API support and provider implementations for a number of standard secure communication protocols.
	  
#### Going further
- Full home automation can be done by connecting wireless modules at serial port.
- Commands can be sent from a particular email id only to further enhance control on serial device.
- Refresh tokens can be used to persist connection to server.


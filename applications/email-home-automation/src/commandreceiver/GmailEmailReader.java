/*
 * This file is part of SerialPundit.
 * 
 * Copyright (C) 2014-2016, Rishi Gupta. All rights reserved.
 *
 * The SerialPundit is DUAL LICENSED. It is made available under the terms of the GNU Affero 
 * General Public License (AGPL) v3.0 for non-commercial use and under the terms of a commercial 
 * license for commercial use of this software. 
 * 
 * The SerialPundit is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package commandreceiver;

import java.io.IOException;
import java.io.InputStream;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.search.FlagTerm;

import com.sun.mail.imap.IMAPSSLStore;
import com.sun.mail.imap.IMAPStore;

public final class GmailEmailReader implements Runnable {

    private IMAPStore imapStore;
    private Folder folder;
    private Message msg[];
    private boolean exitReading;
    private ProgramStatusPanel pstatusPaneli;
    private PortSettingsPanel psPaneli;
    private String commandToExecute;

    public GmailEmailReader(ProgramStatusPanel pstatusPanel, PortSettingsPanel psPanel) {
        pstatusPaneli = pstatusPanel;
        psPaneli = psPanel;
    }

    @Override
    public void run() {

        // Continue reading until stopped explicitly
        while(exitReading == false) {

            try {
                folder = imapStore.getFolder("INBOX");

                // write is needed to mark message as read.
                folder.open(Folder.READ_WRITE);

                // get unread mails
                msg = folder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));

                for(int x=0; x < msg.length; x++) {
                    chkAndExecuteCommand(msg[x]);
                }

                folder.close(true);
                folder = null;
            } catch (MessagingException e) {
                pstatusPaneli.setExtraInfo(e.getMessage());
            } catch (IOException e) {
                pstatusPaneli.setExtraInfo(e.getMessage());
            }

            // sleep for 2 second.
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
            }
        }

        // cleanup.
        if(folder != null) {
            try {
                folder.close(true);
            } catch (MessagingException e) {
                pstatusPaneli.setExtraInfo(e.getMessage());
            }
        }
        folder = null;
    }

    public void setImapStore(IMAPSSLStore store) {
        imapStore = store;
    }

    public void close() throws MessagingException {
        exitReading = true;
        Thread.currentThread().interrupt();
    }

    private void extractContentAndExecuteCommand(Object obj) throws MessagingException, IOException {

        if (obj instanceof String) {
            String content = (String) obj;
            if(content.trim().equals("CMD1")) {
                commandToExecute = "CMD1";
            }else if(content.trim().equals("CMD2")) {
                commandToExecute = "CMD2";
            }else {
                // ignore.
            }
        }else if (obj instanceof Multipart) {
            Multipart mp = (Multipart)obj;
            int count = mp.getCount();
            for (int x=0; x < count; x++) {
                extractContentAndExecuteCommand(mp.getBodyPart(x).getContent());
            }
        }else if (obj instanceof InputStream) {
            // ignore.
            // InputStream is = (InputStream)obj;
            //	int c;
            //	while ((c = is.read()) != -1)
            //		System.out.write(c);
        }else {
            // ignore.
        }
    }

    private void chkAndExecuteCommand(Message msg) throws MessagingException, IOException {

        commandToExecute = null;
        String subject = msg.getSubject();

        if(subject != null) {
            if(subject.trim().equals("CMD1")) {
                extractContentAndExecuteCommand(msg.getContent());
                if((commandToExecute != null) && commandToExecute.equals("CMD1")) {
                    psPaneli.executeCommand("CMD1");
                    // explicitly mark message as read
                    msg.setFlag(Flags.Flag.SEEN, true);
                }
            }
            else if(subject.trim().equals("CMD2")) {
                extractContentAndExecuteCommand(msg.getContent());
                if((commandToExecute != null) && commandToExecute.equals("CMD2")) {
                    psPaneli.executeCommand("CMD2");
                    msg.setFlag(Flags.Flag.SEEN, true);
                }
            }
            else {
                // if this is not the command email message, mark it as unread. Note here is a catch.
                // Because we are reading periodically marking email as unread will be marked as read
                // in next loop. So a dedicated email ID with control of serial port only must be used.
                msg.setFlag(Flags.Flag.SEEN, false);
            }
        }
    }
}

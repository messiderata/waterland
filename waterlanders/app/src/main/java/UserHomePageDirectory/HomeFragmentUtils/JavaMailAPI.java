package UserHomePageDirectory.HomeFragmentUtils;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.waterlanders.R;

import java.io.File;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

@SuppressLint("StaticFieldLeak")
public class JavaMailAPI extends AsyncTask<Void,Void,Void>  {

    //Add those line in dependencies
    //implementation files('libs/activation.jar')
    //implementation files('libs/additionnal.jar')
    //implementation files('libs/mail.jar')

    //Need INTERNET permission

    //Variables
    private Context mContext;
    private Session mSession;

    private String mEmail;
    private String mSubject;
    private File mMessage;

    private ProgressDialog mProgressDialog;

    //Constructor
    public JavaMailAPI(Context mContext, String mEmail, String mSubject, File mMessage) {
        this.mContext = mContext;
        this.mEmail = mEmail;
        this.mSubject = mSubject;
        this.mMessage = mMessage;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //Show progress dialog while sending email
        mProgressDialog = ProgressDialog.show(mContext,"Generating Invoice", "Please wait...",false,false);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        //Dismiss progress dialog when message successfully send
        mProgressDialog.dismiss();

        //Show success toast
        Toast.makeText(mContext,"Invoice Sent to your email",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected Void doInBackground(Void... params) {
        //Creating properties
        Properties props = new Properties();

        //Configuring properties for gmail
        //If you are not using gmail you may need to change the values
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        //Creating a new session
        mSession = Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
                    //Authenticating the password
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(mContext.getString(R.string.sEmail), mContext.getString(R.string.sPass));
                    }
                });

        try {
            //Creating MimeMessage object
            MimeMessage mm = new MimeMessage(mSession);

            //Setting sender address
            mm.setFrom(new InternetAddress("alywaterland@gmail.com"));
            mm.addRecipient(Message.RecipientType.TO, new InternetAddress(mEmail));
            mm.setSubject(mSubject);

            //Adding pdf file message body
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            Multipart multipart = new MimeMultipart();
            javax.activation.DataSource source = new FileDataSource(mMessage);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(mMessage.getName());
            multipart.addBodyPart(messageBodyPart);
            // Attach the pdf
            mm.setContent(multipart);

            // Send the PDF to the User's Email
            Transport.send(mm);

        } catch (MessagingException e) {
            e.printStackTrace();
            Log.e("invoiceEmail", "Error: "+e);
        }
        return null;
    }
}
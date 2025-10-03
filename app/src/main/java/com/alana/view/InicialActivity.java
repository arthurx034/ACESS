package com.alana.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.alana.R;

public class InicialActivity extends AppCompatActivity {

    LinearLayout btn_login, btn_cadastro;
    ImageButton btn_insta, btn_email, btn_whats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);

        btn_login = findViewById(R.id.btn_login);
        btn_cadastro = findViewById(R.id.btn_cadastro);
        btn_insta = findViewById(R.id.btn_insta);
        btn_email = findViewById(R.id.btn_email);
        btn_whats = findViewById(R.id.btn_whats);

        btn_login.setOnClickListener(v -> {
            startActivity(new Intent(InicialActivity.this, LoginActivity.class));
        });

        btn_cadastro.setOnClickListener(v -> {
            startActivity(new Intent(InicialActivity.this, RegisterActivity.class));
        });

        btn_insta.setOnClickListener(v -> {
            Uri uri = Uri.parse("http://instagram.com/_u/arthurx_034");
            Intent instaIntent = new Intent(Intent.ACTION_VIEW, uri);
            instaIntent.setPackage("com.instagram.android");
            try {
                startActivity(instaIntent);
            } catch (android.content.ActivityNotFoundException e) {
                // Instagram app not installed, open in browser
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.instagram.com/arthurx_034/"));
                startActivity(browserIntent);
            }
        });

        btn_email.setOnClickListener(v -> {
            Intent gmailIntent = new Intent(Intent.ACTION_SENDTO);
            gmailIntent.setData(Uri.parse("mailto:arthuribeirorodrigues@gmail.com"));
            gmailIntent.putExtra(Intent.EXTRA_SUBJECT, "Contato pelo Aplicativo");
            gmailIntent.putExtra(Intent.EXTRA_TEXT,
                    "Olá,\n\nEstou entrando em contato através do aplicativo. Gostaria de tirar algumas dúvidas e obter mais informações.\n\nObrigado!");
            // Try to open Gmail app directly
            gmailIntent.setPackage("com.google.android.gm");

            try {
                startActivity(gmailIntent);
            } catch (android.content.ActivityNotFoundException e) {
                // Gmail not installed — show chooser for any email client
                Intent chooserIntent = Intent.createChooser(
                        new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))
                                .putExtra(Intent.EXTRA_EMAIL, new String[]{"arthuribeirorodrigues@gmail.com"})
                                .putExtra(Intent.EXTRA_SUBJECT, "Subject Here")
                                .putExtra(Intent.EXTRA_TEXT, "Body of the email here"),
                        "Send email via");
                startActivity(chooserIntent);
            }
        });

        btn_whats.setOnClickListener(v -> {
            String phone = "5534999695432"; // international format: country code + number (here: 55 + 34999695432)
            String message = "Olá! Estou entrando em contato através do aplicativo. Gostaria de mais informações sobre como usar o app.";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("whatsapp://send?phone=" + phone + "&text=" + Uri.encode(message)));
            intent.setPackage("com.whatsapp");
            try {
                startActivity(intent);
            } catch (android.content.ActivityNotFoundException ex) {
                // WhatsApp not installed — open Play Store
                Intent playStore = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=com.whatsapp"));
                startActivity(playStore);
            }
        });
    }
}

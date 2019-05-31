package com.appify.umcaapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.appify.umcaapp.model.Member;
import com.appify.umcaapp.utils.Utilities;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

public class MemberProfileActivity extends AppCompatActivity {
    private TextView nameTv, areaofMinistryTv, phoneNoTv, emailTv, birthdayTv;
    private ImageView imageView;
    private ScrollView view;

    private Member member;

    private final int REQUEST_CALL = 23;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_profile);

        member = (Member) getIntent().getSerializableExtra("member");

        view = findViewById(R.id.layout);

        nameTv = findViewById(R.id.name_tv);
        areaofMinistryTv = findViewById(R.id.area_of_ministry_tv);
        phoneNoTv = findViewById(R.id.phone_no_tv);
        emailTv = findViewById(R.id.email_tv);
        birthdayTv = findViewById(R.id.birthday_tv);
        imageView = findViewById(R.id.image_view);

        nameTv.setText(member.getName());
        areaofMinistryTv.setText(member.getAreaOfMinistry());

        if (member.getPhoneNo()!= null) {
            phoneNoTv.setText(member.getPhoneNo());
            phoneNoTv.setPaintFlags(phoneNoTv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        } else {
            phoneNoTv.setText("");
        }
        phoneNoTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (member.getPhoneNo() != null && member.getPhoneNo().length() > 1) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+member.getPhoneNo()));
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    } else {

                        Snackbar.make(view, "Unable to dial number", Snackbar.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(MemberProfileActivity.this, "Can't make phone call. Member's phone number not available", Toast.LENGTH_LONG).show();
                }
            }
        });

        if (member.getEmail() != null) {
            emailTv.setText(member.getEmail());
            emailTv.setPaintFlags(emailTv.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
        } else {
            emailTv.setText("");
        }
        emailTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (member.getEmail()!= null && member.getEmail().length()>1) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                    intent.putExtra(Intent.EXTRA_EMAIL, member.getEmail());
                    intent.putExtra(Intent.EXTRA_SUBJECT, "");
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                } else {
                    Utilities.makeSnackBar(view, "Member's phone number unavailable");
                }
            }
        });

        if (member.getBirthday()!= null) {
            birthdayTv.setText(member.getBirthday().toString());
        } else {
            birthdayTv.setText("");
        }

        Glide.with(this).load(member.getImageUrl())
                .apply(RequestOptions.circleCropTransform())
                .into(imageView);
    }
}


package com.example.caotri.mapapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.share.model.ShareContent;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.LikeView;
import com.facebook.share.widget.ShareButton;
import com.facebook.share.widget.ShareDialog;

import java.util.Arrays;

public class DetailLocation extends AppCompatActivity{

    InfoLocation infoLocation;
    private LikeView likeView;
    private ShareButton shareButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        infoLocation = getIntent().getParcelableExtra("details");
        setTitle(infoLocation.getTitle());
        setContentView(R.layout.activity_detail_location);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_map);
        Init();
        toolbar.setTitle(infoLocation.getTitle());
    }

    public void FabClick(View v) {
        Intent intent = new Intent(this, MapsActivity.class);
        setResult(2,intent);
        finish();
    }

    public void Init() {
        TextView title = (TextView) findViewById(R.id.Title);
        title.setText(infoLocation.getTitle());
        TextView address = (TextView) findViewById(R.id.Address);
        address.setText(infoLocation.getAddress());
        TextView faddress = (TextView) findViewById(R.id.FormatAddress);
        faddress.setText(infoLocation.getFormat_address());
        TextView cost = (TextView) findViewById(R.id.Cost);
        cost.setText(infoLocation.getCost());
        TextView areas = (TextView) findViewById(R.id.Areas);
        String area = infoLocation.getAreas().substring(0,infoLocation.getAreas().length() - 1);
        areas.setText(Html.fromHtml(area + "<sup>2</sup>"));
        TextView detail = (TextView) findViewById(R.id.Detail);
        detail.setText(infoLocation.getDetail());
        TextView route = (TextView) findViewById(R.id.route);
        route.setText("Khoảng cách");
        TextView route_detail = (TextView) findViewById(R.id.route_detail);
        route_detail.setText("UnKnown");
        if(infoLocation.getRoute() != null) {
            route_detail.setText(infoLocation.getRoute());
        }
        likeView = (LikeView) findViewById(R.id.like_view);
        LikeView();
        shareButton = (ShareButton)findViewById(R.id.fb_share_button);
        ShareView();
    }
    private void LikeView() {
        likeView.setObjectIdAndType(
                infoLocation.getUrl(),
                LikeView.ObjectType.PAGE);
    }
    private void ShareView(){
        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentUrl(Uri.parse(infoLocation.getUrl()))
                .build();
        shareButton.setShareContent(content);
    }


}

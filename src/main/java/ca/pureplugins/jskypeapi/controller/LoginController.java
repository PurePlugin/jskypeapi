package ca.pureplugins.jskypeapi.controller;

import ca.pureplugins.jskypeapi.util.SimpleCookieJar;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class LoginController {
    private final HttpUrl url;
    private final OkHttpClient client;

    private String pie;
    private String etm;

    private String skypetoken;
    private Instant expireTime;

    private final String username;
    private final String password;

    public LoginController(String username, String password) {
        this.username = username;
        this.password = password;

        url = HttpUrl.parse("https://login.skype.com/login").newBuilder()
                .setQueryParameter("client_id", "578134")
                .setQueryParameter("redirect_uri", "https://web.skype.com").build();

        client = new OkHttpClient.Builder()
                .cookieJar(new SimpleCookieJar())
                .build();

        expireTime = Instant.now();
    }

    private void updateToken() throws IOException {
        if (refreshToken()) {
            return;
        }

        Request request = new Request.Builder()
                .url(url)
                .post(new FormBody.Builder()
                        .add("username", username)
                        .add("password", password)
                        .add("pie", pie)
                        .add("etm", etm)
                        .add("timezone_field", "+00|00")
                        .add("js_time", Long.toString(Instant.now().getEpochSecond()))
                        .build())
                .build();

        Response response = client.newCall(request).execute();
        Document document = Jsoup.parse(response.body().string());

        setToken(document);
    }

    private boolean refreshToken() throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        Response response = client.newCall(request).execute();
        Document document = Jsoup.parse(response.body().string());

        Elements tokenElement = document.getElementsByAttributeValue("name", "skypetoken");
        if (!tokenElement.isEmpty()) {
            setToken(document);
            return true;
        }

        pie = document.getElementById("pie").val();
        etm = document.getElementById("etm").val();
        return false;
    }

    private void setToken(Document document) {
        skypetoken = document.getElementsByAttributeValue("name", "skypetoken").val();
        expireTime = Instant.now().plusSeconds(Long.parseLong(document.getElementsByAttributeValue("name", "expires_in").val()));
    }

    private boolean isAboutToExpire() {
        return Instant.now().until(expireTime, ChronoUnit.SECONDS) < 60;
    }

    public String getToken() throws IOException {
        if (isAboutToExpire()) {
            updateToken();
        }

        return skypetoken;
    }
}

package ru.o2genum.howtosay.api;

import com.android.volley.RequestQueue;
import com.android.volley.Response;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import ru.o2genum.howtosay.api.dto.forvo.Pronunciation;
import ru.o2genum.howtosay.api.dto.forvo.WordPronunciations;

/**
 * Created by o2genum on 06/07/15.
 */
public class ForvoApi {
    private RequestQueue mRequestQueue;
    private String mApiKey;
    private String mHost;

    public ForvoApi(RequestQueue requestQueue, String host, String apiKey) {
        mRequestQueue = requestQueue;
        mApiKey = apiKey;
        mHost = host;
    }

    public void getWordPronunciations(String word, Response.Listener<WordPronunciations> responseListener,
                                  Response.ErrorListener errorListener) {
        UrlBulder urlBulder = new UrlBulder();
        urlBulder.appendArgument("action", "word-pronunciations");
        urlBulder.appendArgument("order", "rate-desc");
        urlBulder.appendArgument("word", word);

        GsonRequest gsonRequest = new GsonRequest<WordPronunciations>(urlBulder.toString(), WordPronunciations.class, null, responseListener, errorListener);
        mRequestQueue.add(gsonRequest);
    }

    private class UrlBulder {
        StringBuilder sb;

        public UrlBulder() {
            String base =
                    "http://" + mHost + "/";
            sb = new StringBuilder(base);
            appendArgument("format", "json");
            appendArgument("key", mApiKey);
        }

        public void appendArgument(String key, String value) {
            try {
                sb.append(key + "/" + URLEncoder.encode(value, "UTF-8").replaceAll("\\+", "%20") + "/");
            } catch (UnsupportedEncodingException ex) {}
        }

        public String toString() {
            return sb.toString();
        }
    }
}

/*******************************************************************************
 * Copyright (c) 2017 Istio Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.ruipages.book.reviews.rest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
public class LibertyRestEndpoint {

    private final static Boolean ratings_enabled =
            Boolean.valueOf(System.getenv("ENABLE_RATINGS"));
    private final static String star_color =
            System.getenv("STAR_COLOR") == null ?
                    "black" :
                    System.getenv("STAR_COLOR");
    private final static String services_domain =
            System.getenv("SERVICES_DOMAIN") == null ?
                    "" :
                    ("." + System.getenv("SERVICES_DOMAIN"));
    private final static String ratings_service =
            "http://ratings" + services_domain + ":9080/ratings";

    @Resource
    private RestTemplateBuilder restTemplateBuilder;

    static {
        System.out.println("!!! INFO !!");
        System.out.println("ratings_enabled: " + ratings_enabled);
        System.out.println("star_color: " + star_color);
        System.out.println("services_domain: " + services_domain);
        System.out.println("ratings_service: " + ratings_service);
    }

    private String getJsonResponse(String productId, int starsReviewer1,
                                   int starsReviewer2) {

        String result = "{";
        result += "\"id\": \"" + productId + "\",";
        result += "\"reviews\": [";

        // reviewer 1:
        result += "{";
        result += "  \"reviewer\": \"Reviewer1\",";
        result +=
                "  \"text\": \"An extremely entertaining play by Shakespeare. The slapstick humour is refreshing!\"";
        if (ratings_enabled) {
            if (starsReviewer1 != -1) {
                result += ", \"rating\": {\"stars\": " + starsReviewer1
                        + ", \"color\": \"" + star_color + "\"}";
            } else {
                result +=
                        ", \"rating\": {\"error\": \"Ratings service is currently unavailable\"}";
            }
        }
        result += "},";

        // reviewer 2:
        result += "{";
        result += "  \"reviewer\": \"Reviewer2\",";
        result +=
                "  \"text\": \"Absolutely fun and entertaining. The play lacks thematic depth when compared to other plays by Shakespeare.\"";
        if (ratings_enabled) {
            if (starsReviewer2 != -1) {
                result += ", \"rating\": {\"stars\": " + starsReviewer2
                        + ", \"color\": \"" + star_color + "\"}";
            } else {
                result +=
                        ", \"rating\": {\"error\": \"Ratings service is currently unavailable\"}";
            }
        }
        result += "}";

        result += "]";
        result += "}";

        return result;
    }

    public RestTemplate getRestTemplate(int ct, int rt) {
        return restTemplateBuilder
                .setConnectTimeout(ct)
                .setReadTimeout(rt)
                .build();
    }

    private JSONObject getRatings(String productId, String user, String xreq, String xtraceid, String xspanid,
                                  String xparentspanid, String xsampled, String xflags, String xotspan) {

        String url = ratings_service + "/" + productId;
        Map<String, String> param = new HashMap<>();

        int timeout = star_color.equals("black") ? 10000 : 2500;
        RestTemplate restTemplate = getRestTemplate(timeout, timeout);


        HttpHeaders requestHeaders = new HttpHeaders();

        if (StringUtils.isNotBlank(xreq)) {
            requestHeaders.add("x-request-id", xreq);
        }
        if (StringUtils.isNotBlank(xtraceid)) {
            requestHeaders.add("x-b3-traceid", xtraceid);
        }
        if (StringUtils.isNotBlank(xspanid)) {
            requestHeaders.add("x-b3-spanid", xspanid);
        }
        if (StringUtils.isNotBlank(xparentspanid)) {
            requestHeaders.add("x-b3-parentspanid", xparentspanid);
        }
        if (StringUtils.isNotBlank(xsampled)) {
            requestHeaders.add("x-b3-sampled", xsampled);
        }
        if (StringUtils.isNotBlank(xflags)) {
            requestHeaders.add("x-b3-flags", xflags);
        }
        if (StringUtils.isNotBlank(xotspan)) {
            requestHeaders.add("x-ot-span-context", xotspan);
        }
        if (StringUtils.isNotBlank(user)) {
            requestHeaders.add("end-user", user);
        }

        try {
            HttpEntity<String> requestEntity = new HttpEntity<>(null, requestHeaders);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class, param);
            HttpStatus statusCode = response.getStatusCode();

            if (statusCode == HttpStatus.OK) {
                String sttr = response.getBody();
                System.out.println(sttr);
                return JSON.parseObject(sttr);
            } else {
                System.out.println("Error: unable to contact " + ratings_service + " got status of " + statusCode);
                return null;
            }
        }catch (Exception e){
            System.out.println("Error: unable to contact " + ratings_service);
            return null;
        }

    }


    @GetMapping("/health")
    public JSONObject health() {
        JSONObject jo = new JSONObject();
        jo.put("status", "Reviews is healthy");
        return jo;
    }

    @GetMapping("/reviews/{productId}")
    public String bookReviewsById(@PathVariable("productId") int productId,
                                  @RequestHeader(value = "end-user", defaultValue = "") String user,
                                  @RequestHeader(value = "x-request-id", defaultValue = "") String xreq,
                                  @RequestHeader(value = "x-b3-traceid", defaultValue = "") String xtraceid,
                                  @RequestHeader(value = "x-b3-spanid", defaultValue = "") String xspanid,
                                  @RequestHeader(value = "x-b3-parentspanid", defaultValue = "") String xparentspanid,
                                  @RequestHeader(value = "x-b3-sampled", defaultValue = "") String xsampled,
                                  @RequestHeader(value = "x-b3-flags", defaultValue = "") String xflags,
                                  @RequestHeader(value = "x-ot-span-context", defaultValue = "") String xotspan) {
        int starsReviewer1 = -1;
        int starsReviewer2 = -1;

        if (ratings_enabled) {
            JSONObject ratingsResponse = getRatings(Integer.toString(productId), user, xreq, xtraceid, xspanid, xparentspanid, xsampled, xflags, xotspan);
            if (ratingsResponse != null) {
                if (ratingsResponse.containsKey("ratings")) {
                    JSONObject ratings = ratingsResponse.getJSONObject("ratings");
                    if (ratings.containsKey("Reviewer1")) {
                        starsReviewer1 = ratings.getInteger("Reviewer1");
                    }
                    if (ratings.containsKey("Reviewer2")) {
                        starsReviewer2 = ratings.getInteger("Reviewer2");
                    }
                }
            }
        }

        return getJsonResponse(Integer.toString(productId), starsReviewer1, starsReviewer2);
    }

}

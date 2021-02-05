package com.jukshio.jwccgateapplib.Networking;

/*
 * Vishwam Corp CONFIDENTIAL

 * Vishwam Corp 2018
 * All Rights Reserved.

 * NOTICE:  All information contained herein is, and remains
 * the property of Vishwam Corp. The intellectual and technical concepts contained
 * herein are proprietary to Vishwam Corp
 * and are protected by trade secret or copyright law of U.S.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Vishwam Corp
 */

import org.json.JSONObject;

/**
 * This interface provides callbacks on face match api call.
 * **/

public interface APICompletionCallback {

    void onResponse(String responseCode, VishwamError error, JSONObject jsonObjectApiResponse, JSONObject headers);
}
//public interface APICompletionCallback {
//
//    void onResponse(VishwamError error, Response response);
//}
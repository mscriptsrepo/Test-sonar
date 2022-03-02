/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mscripts.externalrequesthandler.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import org.apache.log4j.Logger;

/**
 *
 * @author rhiresheddi
 */
public class HTTPRequestPoster {

    private static final Logger logger = Logger.getLogger(HTTPRequestPoster.class);
    public static String doPost(String requestToken, String url_str, String post_str) throws Exception {
        StringBuffer response = null;
        String ret_response = "";
        BufferedReader in = null;
        
        try {
            URL url = new URL(url_str);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            
            conn.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(post_str);
            wr.flush();
            wr.close();
                        
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
            out.write(post_str);
            out.flush();
            out.close();

            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            response = new StringBuffer();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
            }
            
            ret_response = response.toString();
            in.close();
        } catch (IOException ex) {
            logger.fatal("request id: " + requestToken + "exception :",ex);
            throw ex;
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return ret_response;
    }
}

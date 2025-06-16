package com.uservoice.uservoicesdk.cache;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.content.Context;
import android.util.Log;

public class IOUtils {

    public static void writeStringAsFile(Context context, String Data, String fileName) throws IOException {
        context = context.getApplicationContext();

        FileWriter out = new FileWriter(new File(context.getCacheDir(), fileName));
        out.write(Data);
        out.close();

    }

    public static String readStringFromFile(Context context, String fileName) throws IOException {

        String ret = "";

        InputStream inputStream = new FileInputStream(new File(context.getCacheDir(), fileName));

        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String receiveString = "";
            StringBuilder stringBuilder = new StringBuilder();

            while ((receiveString = bufferedReader.readLine()) != null) {
                stringBuilder.append(receiveString);
            }

            inputStream.close();
            ret = stringBuilder.toString();
        }

        return ret;
    }

}

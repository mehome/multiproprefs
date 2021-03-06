/*
 * ACDD Project
 * file BundleParser.java  is  part of ACCD
 * The MIT License (MIT)  Copyright (c) 2015 Bunny Blue,achellies.
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *
 */
package org.acdd.android.initializer;

import android.content.Context;

import org.acdd.android.compat.ICrashReporter;
import org.acdd.bundleInfo.BundleInfoList;
import org.acdd.bundleInfo.BundleInfoList.BundleInfo;

import org.acdd.framework.ACDD;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * BundleParser parser bundle list info from json
 *
 * @author BunnyBlue
 */
public class BundleParser {
    private static final String SUFFIX_BUNDLE_FILE = "bundle-info.json"; // 所有 bundle-info.json 的统一后缀

    public static void parser(Context context) {
        ArrayList<BundleInfoList.BundleInfo> bundleInfos = new ArrayList<BundleInfoList.BundleInfo>();
        ArrayList<String> bundleInfoFileList = getAllBundleJson(context);
        if (null == bundleInfoFileList || 0 == bundleInfoFileList.size())
            return;

        for (int i = 0; i < bundleInfoFileList.size(); i++)
            parser(context, bundleInfoFileList.get(i), bundleInfos);

        BundleInfoList.getInstance().init(bundleInfos);
    }

    private static void parser(Context context, String fileName, ArrayList<BundleInfoList.BundleInfo> bundleInfos) {
        if (null == context || null == bundleInfos) return;

        InputStream is;
        try {
            is = context.getAssets().open(fileName);
            int size = is.available();

            // Read the entire asset into a local byte buffer.
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            JSONArray jsonArray = new JSONArray(new String(buffer));
            for (int index = 0; index < jsonArray.length(); index++) {
                JSONObject tmp = jsonArray.optJSONObject(index);
                BundleInfo bundleInfo = new BundleInfo();

                bundleInfo.bundleName = tmp.optString("pkgName");
                bundleInfo.hasSO = tmp.optBoolean("hasSO");

                ArrayList<String> components = new ArrayList<String>();

                JSONArray activities = tmp.optJSONArray("activities");
                for (int j = 0; j < activities.length(); j++) {
                    components.add(activities.getString(j));
                }

                JSONArray receivers = tmp.optJSONArray("receivers");
                for (int j = 0; j < receivers.length(); j++) {
                    components.add(receivers.getString(j));
                }

                JSONArray services = tmp.optJSONArray("services");
                for (int j = 0; j < services.length(); j++) {
                    components.add(services.getString(j));
                }

                JSONArray contentProviders = tmp.optJSONArray("contentProviders");
                for (int j = 0; j < contentProviders.length(); j++) {
                    components.add(contentProviders.getString(j));
                }

                JSONArray dependencys = tmp.optJSONArray("dependency");
                for (int j = 0; j < dependencys.length(); j++) {
                    bundleInfo.DependentBundles.add(dependencys.getString(j));
                }

                bundleInfo.Components = components;
                bundleInfos.add(bundleInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ACDD.getInstance().reportCrash(ICrashReporter.ACDD_PARSE_BUNDLE_INFO_ERROR, e);
        }
    }

    private static ArrayList<String> getAllBundleJson(Context context) {
        if (null == context)
            return null;

        String[] allAssetsFiles = null;
        try {
            allAssetsFiles = context.getAssets().list("");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (null == allAssetsFiles) return null;

        ArrayList<String> bundleJsonList = new ArrayList<String>();
        for (String assetsFile : allAssetsFiles) {
            if (null != assetsFile
                    && assetsFile.contains(SUFFIX_BUNDLE_FILE))
                bundleJsonList.add(assetsFile);
        }

        return bundleJsonList;
    }
}

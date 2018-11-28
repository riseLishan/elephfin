package com.fx.elephfin.result;

import android.text.TextUtils;
import android.util.Log;

import java.util.Map;

/**
 * Created by 莫仁周 on 2017/2/16 0016.
 */

public class PayResult {
        private String resultStatus;
        private String result;
        private String memo;

        public PayResult(Map<String, String> rawResult) {
            if (rawResult == null) {
                return;
            }

            for (String key : rawResult.keySet()) {
                if (TextUtils.equals(key, "resultStatus")) {
                    resultStatus = rawResult.get(key);
                    Log.e("--main--","resultStatus"+resultStatus);
                } else if (TextUtils.equals(key, "result")) {
                    result = rawResult.get(key);
                    Log.e("--main--","result"+result);
                } else if (TextUtils.equals(key, "memo")) {
                    memo = rawResult.get(key);
                    Log.e("--main--","memo"+memo);
                }
            }
        }

        @Override
        public String toString() {
            return "resultStatus={" + resultStatus + "};memo={" + memo
                    + "};result={" + result + "}";
        }

        /**
         * @return the resultStatus
         */
        public String getResultStatus() {
            return resultStatus;
        }

        /**
         * @return the memo
         */
        public String getMemo() {
            return memo;
        }

        /**
         * @return the result
         */
        public String getResult() {
            return result;
        }
    }


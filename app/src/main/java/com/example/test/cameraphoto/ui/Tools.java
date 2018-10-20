package com.example.test.cameraphoto.ui;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Edison on 2016/12/19.
 */
public class Tools {

    public static boolean isMobile(String mobiles) {
        if (isNullOrEmpty(mobiles)) {
            return false;
        }
        Pattern p = Pattern
                .compile("^1\\d{10}$");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }

    public static boolean isEmail(String strEmail) {
        if (isNullOrEmpty(strEmail)) {
            return false;
        }
        String strPattern = "^[a-zA-Z0-9_.-]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*\\.[a-zA-Z0-9]{2,6}$";
        Pattern p = Pattern.compile(strPattern);
        Matcher m = p.matcher(strEmail);
        return m.matches();
    }

    /**
     * 获取价格，本系 统返回的价格都是以分为单位，所以要除100
     *
     * @param price
     * @return
     */
    public static String getPrice(Long price) {
        Double realPrice = price == null ? 0 : price.doubleValue() / 100.0f;
        if (realPrice.intValue() - realPrice == 0) {
            return String.valueOf(realPrice.intValue());
        } else {
            return String.valueOf(realPrice);
        }
    }

    public static Double getPriceD(Long price) {
        Double realPrice = price == null ? 0 : price.doubleValue() / 100.0f;
        return realPrice;
    }

    public static boolean isNumeric(String value) {
        if (isNullOrEmpty(value)) {
            return false;
        }
        String strPattern = "[0-9]*";
        Pattern p = Pattern.compile(strPattern);
        Matcher m = p.matcher(value);
        return m.matches();
    }

    public static boolean isLocalPath(String path) {
        return !TextUtils.isEmpty(path) && path.startsWith("/");
    }

    public static <T> T checkNotNull(T reference) {
        if (reference == null) {
            throw new NullPointerException();
        } else {
            return reference;
        }
    }

    public static <T> T checkNotNull(T reference, @Nullable Object errorMessage) {
        if (reference == null) {
            throw new NullPointerException(String.valueOf(errorMessage));
        } else {
            return reference;
        }
    }

    public static boolean isListNullOrEmpty(@Nullable List list) {
        return list == null || list.isEmpty();
    }

    public static boolean isNullOrEmpty(@Nullable String string) {
        return string == null || string.length() == 0;
    }

    public static boolean isNullOrEmpty(@Nullable CharSequence string) {
        return string == null || string.length() == 0;
    }

    public static boolean equal(@Nullable Object a, @Nullable Object b) {
        return a == b || a != null && a.equals(b);
    }

    public static int hashCode(@Nullable Object... objects) {
        return Arrays.hashCode(objects);
    }

    /**
     * Parcelable对象深拷贝
     */
    public static <T extends Parcelable> T copy(@Nullable Parcelable input) {
        Parcel parcel = null;
        if (input == null) {
            return null;
        }
        try {
            parcel = Parcel.obtain();
            parcel.writeParcelable(input, 0);

            parcel.setDataPosition(0);
            return parcel.readParcelable(input.getClass().getClassLoader());
        } finally {
            if (parcel != null) {
                parcel.recycle();
            }
        }
    }

    /**
     * 根据用户名的不同长度，来进行替换 ，达到保密效果
     *
     * @param userName 用户名
     * @return 替换后的用户名
     */
    public static String userNameReplaceWithStar(String userName) {
        String userNameAfterReplaced;

        if (userName == null) {
            userName = "";
        }

        int nameLength = userName.length();

        if (nameLength <= 1) {
            userNameAfterReplaced = "*";
        } else if (nameLength == 2) {
            userNameAfterReplaced = replaceAction(userName, "(?<=\\d{0})\\d(?=\\d{1})");
        } else if (nameLength <= 6) {
            userNameAfterReplaced = replaceAction(userName, "(?<=\\d{1})\\d(?=\\d{1})");
        } else if (nameLength == 7) {
            userNameAfterReplaced = replaceAction(userName, "(?<=\\d{1})\\d(?=\\d{2})");
        } else if (nameLength == 8) {
            userNameAfterReplaced = replaceAction(userName, "(?<=\\d{2})\\d(?=\\d{2})");
        } else if (nameLength == 9) {
            userNameAfterReplaced = replaceAction(userName, "(?<=\\d{2})\\d(?=\\d{3})");
        } else if (nameLength == 10) {
            userNameAfterReplaced = replaceAction(userName, "(?<=\\d{3})\\d(?=\\d{3})");
        } else {
            userNameAfterReplaced = replaceAction(userName, "(?<=\\d{3})\\d(?=\\d{4})");
        }

        return userNameAfterReplaced;
    }

    /**
     * 实际替换动作
     *
     * @param username username
     * @param regular  正则
     * @return
     */
    private static String replaceAction(String username, String regular) {
        return username.replaceAll(regular, "*");
    }

    /**
     * 身份证号替换，保留前四位和后四位
     * <p>
     * 如果身份证号为空 或者 null ,返回null ；否则，返回替换后的字符串；
     *
     * @param idCard 身份证号
     * @return 带星号的字符串
     */
    public static String idCardReplaceWithStar(String idCard) {

        if (idCard.isEmpty() || idCard == null) {
            return null;
        } else {
            return replaceAction(idCard, "(?<=\\d{4})\\d(?=\\d{4})");
        }
    }

    /**
     * 手机号码替换，保留前三位和后四位
     * <p>
     * 如果身份证号为空 或者 null ,返回null ；否则，返回替换后的字符串；
     *
     * @param mobile 身份证号
     * @return 带星号的字符串
     */
    public static String mobileReplaceWithStar(String mobile) {

        if (mobile.isEmpty() || mobile == null) {
            return null;
        } else {
            return replaceAction(mobile, "(?<=\\d{3})\\d(?=\\d{4})");
        }
    }

}

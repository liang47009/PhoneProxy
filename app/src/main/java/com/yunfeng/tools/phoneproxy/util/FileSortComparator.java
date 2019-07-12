package com.yunfeng.tools.phoneproxy.util;

import com.yunfeng.tools.phoneproxy.view.custom.FtpFileItem;

import java.util.Comparator;

public class FileSortComparator implements Comparator<FtpFileItem> {
    class Int {
        public int i;
    }

    public int findDigitEnd(char[] arrChar, Int at) {
        int k = at.i;
        char c = arrChar[k];
        boolean bFirstZero = (c == '0');
        while (k < arrChar.length) {
            c = arrChar[k];
            //first non-digit which is a high chance.
            if (c > '9' || c < '0') {
                break;
            } else if (bFirstZero && c == '0') {
                at.i++;
            }
            k++;
        }
        return k;
    }

    @Override
    public int compare(FtpFileItem ftpFileItem, FtpFileItem ftpFileItem1) {
        if (ftpFileItem != null || ftpFileItem1 != null) {

            String name = ftpFileItem == null ? "" : ftpFileItem.name;
            String name1 = ftpFileItem1 == null ? "" : ftpFileItem1.name;

            char[] a = name.toCharArray();
            char[] b = name1.toCharArray();

            Int aNonzeroIndex = new Int();
            Int bNonzeroIndex = new Int();
            int aIndex = 0, bIndex = 0,
                    aComparedUnitTailIndex, bComparedUnitTailIndex;

//              Pattern pattern = Pattern.compile("D*(d+)D*");
//              Matcher matcher1 = pattern.matcher(a);
//              Matcher matcher2 = pattern.matcher(b);
//              if(matcher1.find() && matcher2.find()) {
//                  String s1 = matcher1.group(1);
//                  String s2 = matcher2.group(1);
//              }

            while (aIndex < a.length && bIndex < b.length) {
                //aIndex <
                aNonzeroIndex.i = aIndex;
                bNonzeroIndex.i = bIndex;
                aComparedUnitTailIndex = findDigitEnd(a, aNonzeroIndex);
                bComparedUnitTailIndex = findDigitEnd(b, bNonzeroIndex);
                //compare by number
                if (aComparedUnitTailIndex > aIndex && bComparedUnitTailIndex > bIndex) {
                    int aDigitIndex = aNonzeroIndex.i;
                    int bDigitIndex = bNonzeroIndex.i;
                    int aDigit = aComparedUnitTailIndex - aDigitIndex;
                    int bDigit = bComparedUnitTailIndex - bDigitIndex;
                    //compare by digit
                    if (aDigit != bDigit)
                        return aDigit - bDigit;
                    //the number of their digit is same.
                    while (aDigitIndex < aComparedUnitTailIndex) {
                        if (a[aDigitIndex] != b[bDigitIndex])
                            return a[aDigitIndex] - b[bDigitIndex];
                        aDigitIndex++;
                        bDigitIndex++;
                    }
                    //if they are equal compared by number, compare the number of '0' when start with "0"
                    //ps note: paNonZero and pbNonZero can be added the above loop "while", but it is changed meanwhile.
                    //so, the following comparsion is ok.
                    aDigit = aNonzeroIndex.i - aIndex;
                    bDigit = bNonzeroIndex.i - bIndex;
                    if (aDigit != bDigit)
                        return aDigit - bDigit;
                    aIndex = aComparedUnitTailIndex;
                    bIndex = bComparedUnitTailIndex;
                } else {
                    if (a[aIndex] != b[bIndex])
                        return a[aIndex] - b[bIndex];
                    aIndex++;
                    bIndex++;
                }

            }
            return a.length - b.length;
        }
        return -1;
    }
}

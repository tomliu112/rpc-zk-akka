package myrpc.utils;

/**
 * ClassName: StringUtil <br/>
 * Function: 有关字符串处理的工具类
 * 
 * @author Zhang Xu
 */
public class StringUtil {

    /**
     * 空字符串
     */
    public static final String EMPTY = "";

    /**
     * 逗号
     */
    public static final char COMMA = ',';

    /**
     * 冒号
     */
    public static final String COLON = ":";

    /**
     * 检查字符串是否为<code>null</code>或空字符串<code>""</code>。
     * 
     * <pre>
     * StringUtil.isEmpty(null)      = true
     * StringUtil.isEmpty(&quot;&quot;)        = true
     * StringUtil.isEmpty(&quot; &quot;)       = false
     * StringUtil.isEmpty(&quot;bob&quot;)     = false
     * StringUtil.isEmpty(&quot;  bob  &quot;) = false
     * </pre>
     * 
     * @param str
     *            要检查的字符串
     * @return 如果为空, 则返回<code>true</code>
     */
    public static boolean isEmpty(CharSequence str) {
        return ((str == null) || (str.length() == 0));
    }

    /**
     * 检查字符串是否不是<code>null</code>和空字符串<code>""</code>。
     * 
     * <pre>
     * StringUtil.isEmpty(null)      = false
     * StringUtil.isEmpty(&quot;&quot;)        = false
     * StringUtil.isEmpty(&quot; &quot;)       = true
     * StringUtil.isEmpty(&quot;bob&quot;)     = true
     * StringUtil.isEmpty(&quot;  bob  &quot;) = true
     * </pre>
     * 
     * @param str
     *            要检查的字符串
     * @return 如果不为空, 则返回<code>true</code>
     */
    public static boolean isNotEmpty(String str) {
        return ((str != null) && (str.length() > 0));
    }

    /**
     * 将数组中的元素连接成一个字符串。
     * 
     * <pre>
     * StringUtil.join(null, *)               = null
     * StringUtil.join([], *)                 = &quot;&quot;
     * StringUtil.join([null], *)             = &quot;&quot;
     * StringUtil.join([&quot;a&quot;, &quot;b&quot;, &quot;c&quot;], ';')  = &quot;a;b;c&quot;
     * StringUtil.join([&quot;a&quot;, &quot;b&quot;, &quot;c&quot;], null) = &quot;abc&quot;
     * StringUtil.join([null, &quot;&quot;, &quot;a&quot;], ';')  = &quot;;;a&quot;
     * </pre>
     * 
     * @param array
     *            要连接的数组
     * @param separator
     *            分隔符
     * @return 连接后的字符串，如果原数组为<code>null</code>，则返回<code>null</code>
     */
    public static <T> String join(T[] array, char separator) {
        if (array == null) {
            return null;
        }

        int arraySize = array.length;
        int bufSize = (arraySize == 0) ? 0 : ((((array[0] == null) ? 16 : array[0].toString()
                .length()) + 1) * arraySize);
        StringBuilder buf = new StringBuilder(bufSize);

        for (int i = 0; i < arraySize; i++) {
            if (i > 0) {
                buf.append(separator);
            }

            if (array[i] != null) {
                buf.append(array[i]);
            }
        }

        return buf.toString();
    }

    /**
     * 针对数组，转为字符串
     * 
     * @param array 数组
     * @return 数组拼接后的字符串
     */
    public static <T> String toString4Array(T[] array) {
        return join(array, ',');
    }



}

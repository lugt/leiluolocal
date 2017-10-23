package earth.server.user;

/**
 * Created by Frapo on 2017/1/22.
 */
public class BasicDao {

    /**
     * 用户唯一ID，通过ID生成器生成， UNIQUE，INDEX
     */
    public String Etid = "xxxxxxxxxxxxxxxxxxxxxxx";

    /**
     * 显示名称
     */
    public String DisplayName = "测试用户";

    /**
     * 文字密码(保存模式 SHA512（Plain+ETID+EX）)
     */
    public String Password = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";

    /**
     * ExtendV 高级认证代码
     */
    public String ExtendV = "1";

    public long Cell = 9998611100001111L;

    public String Email = "example@example.com";

}

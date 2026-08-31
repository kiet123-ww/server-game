package nro.noti;

import lombok.Getter;
import lombok.Setter;
import nro.consts.Cmd;
import nro.server.io.Message;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * @author outcast c-cute hột me 😳
 */
@Setter
@Getter
public class Notification {

    private int id;

    private String title;

    private String content;

}

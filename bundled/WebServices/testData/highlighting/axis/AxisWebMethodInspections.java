package axis;

import javax.jws.WebMethod;
import javax.jws.WebService;

@WebService
public class AxisWebMethodInspections {
    @WebMethod(exclude = true)
    public Boolean getBoolean() {
        return Boolean.TRUE;
    }

    @WebMethod
    public Boolean <error>getBoolean2</error>() {
        return Boolean.TRUE;
    }
}
